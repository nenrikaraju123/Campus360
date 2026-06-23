package com.campus360.platform.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileSystemStorageProvider implements StorageProvider {

    private final Path rootLocation;

    public LocalFileSystemStorageProvider(@Value("${campus360.storage.local.dir:./data/uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(InputStream content, String originalFileName, String contentType) {
        try {
            if (content == null) {
                throw new IllegalArgumentException("Failed to store empty file");
            }
            
            String extension = "";
            int extIndex = originalFileName.lastIndexOf(".");
            if (extIndex > 0) {
                extension = originalFileName.substring(extIndex);
            }
            
            String uniqueName = UUID.randomUUID().toString() + extension;
            Path destinationFile = this.rootLocation.resolve(Paths.get(uniqueName))
                    .normalize().toAbsolutePath();
            
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new SecurityException("Cannot store file outside current directory.");
            }
            
            Files.copy(content, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            
            return uniqueName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public InputStream retrieve(String physicalPath) {
        try {
            Path file = rootLocation.resolve(physicalPath).normalize();
            if (!file.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new SecurityException("Cannot retrieve file outside current directory.");
            }
            return Files.newInputStream(file);
        } catch (Exception e) {
            throw new RuntimeException("Could not read file: " + physicalPath, e);
        }
    }

    @Override
    public String getType() {
        return "LOCAL";
    }
}
