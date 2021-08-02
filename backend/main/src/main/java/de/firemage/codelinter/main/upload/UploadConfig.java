package de.firemage.codelinter.main.upload;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("upload")
public class UploadConfig {
    @Getter
    @Setter
    private String location = "upload";
}
