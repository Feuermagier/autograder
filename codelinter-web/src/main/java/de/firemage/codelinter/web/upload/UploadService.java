package de.firemage.codelinter.web.upload;

import de.firemage.codelinter.core.file.UploadedFile;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    UploadedFile store(MultipartFile file) throws ClientUploadException, InternalUploadException;
    void delete(UploadedFile file) throws InternalUploadException;
}
