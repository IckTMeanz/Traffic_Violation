package vn.icktmeanz.trafficViolation.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String imagePath = "D:/MyDrive/DATN/storage/image";
    private String folderPath = "D:/MyDrive/DATN/storage/folder";
    private String videoPath = "D:/MyDrive/DATN/storage/video";
}
