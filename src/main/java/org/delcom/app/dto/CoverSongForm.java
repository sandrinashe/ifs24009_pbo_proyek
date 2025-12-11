package org.delcom.app.dto;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotNull;

public class CoverSongForm {

    private UUID id;

    @NotNull(message = "Cover tidak boleh kosong")
    private MultipartFile coverFile;

    // Constructor
    public CoverSongForm() {
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MultipartFile getCoverFile() {
        return coverFile;
    }

    public void setCoverFile(MultipartFile coverFile) {
        this.coverFile = coverFile;
    }

    // Helper methods
    public boolean isEmpty() {
        return coverFile == null || coverFile.isEmpty();
    }

    public String getOriginalFilename() {
        return coverFile != null ? coverFile.getOriginalFilename() : null;
    }

    // Validation methods
    public boolean isValidImage() {
        if (this.isEmpty()) {
            return false;
        }

        String contentType = coverFile.getContentType();
        return contentType != null &&
                (contentType.equals("image/jpeg") ||
                        contentType.equals("image/png") ||
                        contentType.equals("image/gif") ||
                        contentType.equals("image/webp"));
    }

    public boolean isSizeValid(long maxSize) {
        return coverFile != null && coverFile.getSize() <= maxSize;
    }

    // Additional validation for song cover
    public String getFileExtension() {
        if (coverFile == null || coverFile.getOriginalFilename() == null) {
            return null;
        }
        String filename = coverFile.getOriginalFilename();
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : null;
    }

    public boolean isValidExtension() {
        String extension = getFileExtension();
        return extension != null && 
               (extension.equals("jpg") || 
                extension.equals("jpeg") || 
                extension.equals("png") || 
                extension.equals("gif") || 
                extension.equals("webp"));
    }
}