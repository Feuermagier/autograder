package de.firemage.codelinter.main.controller;

import de.firemage.codelinter.linter.file.UploadedFile;
import de.firemage.codelinter.main.lint.LintingConfig;
import de.firemage.codelinter.main.lint.LintingService;
import de.firemage.codelinter.main.result.FileClientErrorResult;
import de.firemage.codelinter.main.result.InternalErrorResult;
import de.firemage.codelinter.main.result.LintingResult;
import de.firemage.codelinter.main.upload.ClientUploadException;
import de.firemage.codelinter.main.upload.InternalUploadException;
import de.firemage.codelinter.main.upload.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/lint")
public class LintingController {
    private final UploadService uploadService;
    private final LintingService lintingService;

    @Autowired
    public LintingController(UploadService uploadService, LintingService lintingService) {
        this.uploadService = uploadService;
        this.lintingService = lintingService;
    }

    @GetMapping("/")
    public String index() {
        return "Hi";
    }

    @PostMapping("/")
    @CrossOrigin("http://localhost:5000")
    public LintingResult handleFileUpload(@RequestParam("file") MultipartFile file) {
        UploadedFile uploadedFile;
        try {
            uploadedFile = this.uploadService.store(file);
        } catch (ClientUploadException e) {
            return new FileClientErrorResult(e.getMessage());
        } catch (InternalUploadException e) {
            return new InternalErrorResult(e.getMessage());
        }

        LintingResult result = this.lintingService.lint(uploadedFile, new LintingConfig(true, true, true, 11));

        this.uploadService.delete(uploadedFile);
        return result;
    }
}
