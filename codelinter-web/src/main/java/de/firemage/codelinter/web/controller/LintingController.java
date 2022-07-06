package de.firemage.codelinter.web.controller;

import de.firemage.codelinter.core.compiler.JavaVersion;
import de.firemage.codelinter.core.file.UploadedFile;
import de.firemage.codelinter.web.lint.LintingConfig;
import de.firemage.codelinter.web.lint.LintingService;
import de.firemage.codelinter.web.result.FileClientErrorResult;
import de.firemage.codelinter.web.result.InternalErrorResult;
import de.firemage.codelinter.web.result.LintingResult;
import de.firemage.codelinter.web.upload.ClientUploadException;
import de.firemage.codelinter.web.upload.InternalUploadException;
import de.firemage.codelinter.web.upload.UploadService;
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
        try {
            UploadedFile uploadedFile = this.uploadService.store(file);

            LintingResult result = this.lintingService.lint(uploadedFile, new LintingConfig(
                    true,
                    true,
                    true,
                    true,
                    true,
                    JavaVersion.JAVA_11));

            this.uploadService.delete(uploadedFile);
            return result;

        } catch (ClientUploadException e) {
            return new FileClientErrorResult(e.getMessage());
        } catch (InternalUploadException e) {
            e.printStackTrace();
            return new InternalErrorResult(e.getMessage());
        }
    }
}
