package org.delcom.app.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Value("${app.upload.dir:./uploads}")
    protected String uploadDir;

    /**
     * Menyimpan file cover untuk song
     * @param file MultipartFile yang akan disimpan
     * @param songId UUID dari song
     * @return nama file yang tersimpan
     * @throws IOException jika terjadi error saat menyimpan file
     */
    public String storeFile(MultipartFile file, UUID songId) throws IOException {
        // Buat directory jika belum ada
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename dengan prefix 'song_cover_'
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = "song_cover_" + songId.toString() + fileExtension;

        // Simpan file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    /**
     * Menyimpan file dengan custom prefix
     * @param file MultipartFile yang akan disimpan
     * @param entityId UUID dari entity (song, playlist, dll)
     * @param prefix prefix untuk nama file (contoh: "song_cover", "playlist_cover")
     * @return nama file yang tersimpan
     * @throws IOException jika terjadi error saat menyimpan file
     */
    public String storeFileWithPrefix(MultipartFile file, UUID entityId, String prefix) throws IOException {
        // Buat directory jika belum ada
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename dengan custom prefix
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = prefix + "_" + entityId.toString() + fileExtension;

        // Simpan file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    /**
     * Menghapus file berdasarkan filename
     * @param filename nama file yang akan dihapus
     * @return true jika berhasil dihapus, false jika gagal
     */
    public boolean deleteFile(String filename) {
        try {
            if (filename == null || filename.isBlank()) {
                return false;
            }
            Path filePath = Paths.get(uploadDir).resolve(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + filename + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Memuat file berdasarkan filename
     * @param filename nama file yang akan dimuat
     * @return Path dari file
     */
    public Path loadFile(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    /**
     * Mengecek apakah file exists
     * @param filename nama file yang akan dicek
     * @return true jika file ada, false jika tidak ada
     */
    public boolean fileExists(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        return Files.exists(loadFile(filename));
    }

    /**
     * Mendapatkan ukuran file dalam bytes
     * @param filename nama file
     * @return ukuran file dalam bytes, -1 jika file tidak ada
     */
    public long getFileSize(String filename) {
        try {
            if (!fileExists(filename)) {
                return -1;
            }
            Path filePath = loadFile(filename);
            return Files.size(filePath);
        } catch (IOException e) {
            System.err.println("Error getting file size: " + filename + " - " + e.getMessage());
            return -1;
        }
    }

    /**
     * Mendapatkan content type dari file
     * @param filename nama file
     * @return content type string, null jika tidak dapat ditentukan
     */
    public String getContentType(String filename) {
        try {
            if (!fileExists(filename)) {
                return null;
            }
            Path filePath = loadFile(filename);
            return Files.probeContentType(filePath);
        } catch (IOException e) {
            System.err.println("Error getting content type: " + filename + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Validasi apakah file adalah gambar yang valid
     * @param file MultipartFile yang akan divalidasi
     * @return true jika valid, false jika tidak
     */
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }

    /**
     * Validasi ukuran file
     * @param file MultipartFile yang akan divalidasi
     * @param maxSize ukuran maksimal dalam bytes
     * @return true jika valid, false jika terlalu besar
     */
    public boolean isValidSize(MultipartFile file, long maxSize) {
        if (file == null) {
            return false;
        }
        return file.getSize() <= maxSize;
    }

    /**
     * Mendapatkan ekstensi file dari filename
     * @param filename nama file
     * @return ekstensi file (tanpa dot), null jika tidak ada ekstensi
     */
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return null;
        }
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1).toLowerCase() : null;
    }

    /**
     * Membersihkan semua file yang tidak digunakan (orphaned files)
     * CATATAN: Method ini harus dipanggil dengan hati-hati
     * @return jumlah file yang dihapus
     */
    public int cleanupOrphanedFiles() {
        int deletedCount = 0;
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                return 0;
            }
            
        } catch (Exception e) {
            System.err.println("Error cleaning up orphaned files: " + e.getMessage());
        }
        return deletedCount;
    }

    /**
     * Mendapatkan path upload directory
     * @return path upload directory
     */
    public String getUploadDir() {
        return uploadDir;
    }

    /**
     * Mengecek apakah upload directory exists dan dapat ditulis
     * @return true jika valid, false jika tidak
     */
    public boolean isUploadDirValid() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            return Files.isWritable(uploadPath);
        } catch (IOException e) {
            System.err.println("Error validating upload directory: " + e.getMessage());
            return false;
        }
    }
}