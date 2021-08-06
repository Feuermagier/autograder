package de.firemage.codelinter.main.upload;

import de.firemage.codelinter.linter.file.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadedFile store(MultipartFile file) throws ClientUploadException, InternalUploadException;
    void delete(UploadedFile file) throws InternalUploadException;
}
