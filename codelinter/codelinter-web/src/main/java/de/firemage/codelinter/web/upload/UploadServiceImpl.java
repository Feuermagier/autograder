package de.firemage.codelinter.web.upload;

import de.firemage.codelinter.core.ZipUtil;
import de.firemage.codelinter.core.file.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

@Service
public class UploadServiceImpl implements UploadService {
    private final Path uploadDirectory;

    @Autowired
    public UploadServiceImpl(UploadConfig config) {
        this.uploadDirectory = Paths.get(config.getLocation());

        if (!this.uploadDirectory.toFile().exists()) {
            if (!this.uploadDirectory.toFile().mkdirs()) {
                throw new IllegalArgumentException("Failed to create the upload directory");
            }
        }

        if (!this.uploadDirectory.toFile().isDirectory() || !this.uploadDirectory.toFile().canWrite()) {
            throw new IllegalArgumentException("The upload directory is not a writable directory");
        }
    }

    @Override
    public UploadedFile store(MultipartFile file) throws ClientUploadException, InternalUploadException {
        String name = UUID.randomUUID().toString().replace("-", "_");
        Path destination = this.uploadDirectory.resolve(name + ".zip");

        // Additional security check. Not really necessary, but better check that the file is definitely at the
        // right place.
        if (!destination.getParent().toAbsolutePath().equals(this.uploadDirectory.toAbsolutePath())) {
            throw new InternalUploadException("Cannot store the file outside the current directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);

            try {
                new ZipFile(destination.toFile()).close();
            } catch (ZipException e) {
                throw new ClientUploadException(e);
            }

            File unzippedDestination = new File(destination.getParent().toFile(), name + "_unzipped");
            ZipUtil.unzip(destination.toFile(), unzippedDestination);

            Files.delete(destination);

            return new UploadedFile(unzippedDestination);
        } catch (IOException e) {
            throw new InternalUploadException(e);
        }
    }

    @Override
    public void delete(UploadedFile file) throws InternalUploadException {
        try {
            file.delete();
        } catch (IOException e) {
            throw new InternalUploadException(e);
        }
    }
}
