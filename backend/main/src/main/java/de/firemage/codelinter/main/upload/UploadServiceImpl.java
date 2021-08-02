package de.firemage.codelinter.main.upload;

import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.linter.file.ZipFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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
        Path destination = this.uploadDirectory.resolve(UUID.randomUUID() + ".zip");

        // Additional security check. Not really necessary, but better check that the file is definitely at the
        // right place.
        if (!destination.getParent().toAbsolutePath().equals(this.uploadDirectory.toAbsolutePath())) {
            throw new InternalUploadException("Cannot store the file outside the current directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InternalUploadException(e);
        }

        try {
            return new UploadedFile(destination.toFile());
        } catch (ZipFormatException e) {
            throw new ClientUploadException(e);
        } catch (IOException e) {
            throw new InternalUploadException(e);
        }
    }

    @Override
    public void delete(UploadedFile file) throws InternalUploadException {
        try {
            Files.delete(file.getFile().toPath());
        } catch (IOException e) {
            throw new InternalUploadException(e);
        }
    }
}
