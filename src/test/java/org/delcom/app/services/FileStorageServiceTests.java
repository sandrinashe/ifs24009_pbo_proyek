package org.delcom.app.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTests {

    private FileStorageService fileStorageService;
    private MultipartFile mockMultipartFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        fileStorageService = new FileStorageService();
        fileStorageService.uploadDir = tempDir.toString();
        mockMultipartFile = mock(MultipartFile.class);
    }

    // ========================================================================
    // 1. STORE FILE (Normal & Error Cases)
    // ========================================================================

    @Test
    @DisplayName("Store: Normal Case")
    void storeFile_Success() throws IOException {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("song.mp3");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);

        assertTrue(result.contains(id.toString()));
        assertTrue(result.endsWith(".mp3"));
    }

    @Test
    @DisplayName("Store: Creates Directory if Missing")
    void storeFile_CreatesDir() throws IOException {
        String subDir = tempDir.resolve("sub").toString();
        fileStorageService.uploadDir = subDir;
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("song.mp3");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        fileStorageService.storeFile(mockMultipartFile, id);
        
        assertTrue(Files.exists(Paths.get(subDir)));
    }

    @Test
    @DisplayName("Store: Filename Null")
    void storeFile_NullName() throws IOException {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);
        
        // Hasil tidak punya ekstensi (karena null)
        assertFalse(result.contains(".")); 
    }

    @Test
    @DisplayName("Store: No Extension")
    void storeFile_NoExtension() throws IOException {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("README");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFile(mockMultipartFile, id);
        
        assertFalse(result.contains(".")); 
    }

    // ========================================================================
    // 2. STORE WITH PREFIX (Targeting Line 67 Logic)
    // ========================================================================

    @Test
    @DisplayName("Store With Prefix: Success")
    void storeFileWithPrefix_Success() throws IOException {
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("img.jpg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFileWithPrefix(mockMultipartFile, id, "cover");
        
        assertTrue(result.startsWith("cover_"));
        assertTrue(result.endsWith(".jpg"));
    }
    
    @Test
    @DisplayName("Store With Prefix: Creates Directory")
    void storeFileWithPrefix_CreatesDir() throws IOException {
        String subDir = tempDir.resolve("prefixDir").toString();
        fileStorageService.uploadDir = subDir;
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("img.jpg");
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        fileStorageService.storeFileWithPrefix(mockMultipartFile, id, "cover");
        assertTrue(Files.exists(Paths.get(subDir)));
    }

    @Test
    @DisplayName("Store With Prefix: No Extension (Fix Yellow Line 67 - Condition 2)")
    void storeFileWithPrefix_NoExtension() throws IOException {
        // Menguji kondisi: Name != null TAPI contains(".") == false
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn("myfile"); // Tanpa titik
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFileWithPrefix(mockMultipartFile, id, "pref");

        assertTrue(result.startsWith("pref_"));
        assertFalse(result.contains(".")); 
    }

    @Test
    @DisplayName("Store With Prefix: Null Name (Fix Yellow Line 67 - Condition 1)")
    void storeFileWithPrefix_NullName() throws IOException {
        // Menguji kondisi: Name == null.
        UUID id = UUID.randomUUID();
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);
        when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));

        String result = fileStorageService.storeFileWithPrefix(mockMultipartFile, id, "pref");

        assertTrue(result.startsWith("pref_"));
        assertFalse(result.contains(".")); 
    }

    // ========================================================================
    // 3. DELETE FILE
    // ========================================================================

    @Test
    @DisplayName("Delete: Null")
    void deleteFile_Null() {
        assertFalse(fileStorageService.deleteFile(null));
    }

    @Test
    @DisplayName("Delete: Blank")
    void deleteFile_Blank() {
        assertFalse(fileStorageService.deleteFile(""));
    }

    @Test
    @DisplayName("Delete: Success")
    void deleteFile_Success() throws IOException {
        String name = "del.txt";
        Files.createFile(tempDir.resolve(name));
        assertTrue(fileStorageService.deleteFile(name));
    }

    @Test
    @DisplayName("Delete: Exception (Trigger Catch)")
    void deleteFile_Exception() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Mock deleteIfExists melempar IOException
            filesMock.when(() -> Files.deleteIfExists(any())).thenThrow(new IOException("Locked"));
            assertFalse(fileStorageService.deleteFile("file.txt"));
        }
    }

    // ========================================================================
    // 4. UTILS & BASIC VALIDATION
    // ========================================================================

    @Test
    void fileExists_And_Load() throws IOException {
        assertFalse(fileStorageService.fileExists(null));
        assertFalse(fileStorageService.fileExists(""));
        
        String name = "real.txt";
        Files.createFile(tempDir.resolve(name));
        assertTrue(fileStorageService.fileExists(name));
        assertNotNull(fileStorageService.loadFile(name));
    }

    @Test
    void getFileSize_Checks() throws IOException {
        assertEquals(-1, fileStorageService.getFileSize("ghost.txt"));

        String name = "size.txt";
        Files.writeString(tempDir.resolve(name), "123");
        assertEquals(3, fileStorageService.getFileSize(name));
    }

    @Test
    void getFileSize_Exception() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any())).thenReturn(true);
            filesMock.when(() -> Files.size(any())).thenThrow(new IOException("Err"));
            assertEquals(-1, fileStorageService.getFileSize("f"));
        }
    }

    @Test
    void getContentType_Checks() throws IOException {
        assertNull(fileStorageService.getContentType("ghost.txt"));
        
        String name = "img.png";
        Files.createFile(tempDir.resolve(name));
        fileStorageService.getContentType(name);
        
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(any())).thenReturn(true);
            filesMock.when(() -> Files.probeContentType(any())).thenThrow(new IOException("Err"));
            assertNull(fileStorageService.getContentType("f"));
        }
    }

    // ========================================================================
    // 5. VALID IMAGE LOGIC (Fixing Line 162-174)
    // ========================================================================

    @Test
    void isValidImage_NullOrEmpty() {
        // Line 162: file == null
        assertFalse(fileStorageService.isValidImage(null));

        // Line 162: file.isEmpty()
        when(mockMultipartFile.isEmpty()).thenReturn(true);
        assertFalse(fileStorageService.isValidImage(mockMultipartFile));
    }

    @Test
    void isValidImage_ContentTypes() {
        // Setup tidak kosong
        when(mockMultipartFile.isEmpty()).thenReturn(false);

        // Line 167: ContentType Null
        when(mockMultipartFile.getContentType()).thenReturn(null);
        assertFalse(fileStorageService.isValidImage(mockMultipartFile));

        // Line 171-174: Valid Types (Covering OR conditions)
        when(mockMultipartFile.getContentType()).thenReturn("image/jpeg");
        assertTrue(fileStorageService.isValidImage(mockMultipartFile));

        when(mockMultipartFile.getContentType()).thenReturn("image/png");
        assertTrue(fileStorageService.isValidImage(mockMultipartFile));

        when(mockMultipartFile.getContentType()).thenReturn("image/gif");
        assertTrue(fileStorageService.isValidImage(mockMultipartFile));

        when(mockMultipartFile.getContentType()).thenReturn("image/webp");
        assertTrue(fileStorageService.isValidImage(mockMultipartFile));

        // Invalid Type
        when(mockMultipartFile.getContentType()).thenReturn("text/plain");
        assertFalse(fileStorageService.isValidImage(mockMultipartFile));
    }

    // ========================================================================
    // 6. VALID SIZE & EXTENSION LOGIC (Fixing Line 184, 196, 200)
    // ========================================================================

    @Test
    void isValidSize_Checks() {
        // Line 184: file == null
        assertFalse(fileStorageService.isValidSize(null, 100));
        
        // Line 187: Check size
        when(mockMultipartFile.getSize()).thenReturn(100L);
        assertTrue(fileStorageService.isValidSize(mockMultipartFile, 200)); // 100 <= 200
        assertFalse(fileStorageService.isValidSize(mockMultipartFile, 50)); // 100 > 50
    }

    @Test
    void getFileExtension_Checks() {
        // Line 196: null
        assertNull(fileStorageService.getFileExtension(null));
        
        // Line 196: !contains(".")
        assertNull(fileStorageService.getFileExtension("file_without_dot")); 
        
        // Line 200: lastDot > 0 (Normal case)
        assertEquals("txt", fileStorageService.getFileExtension("file.txt"));
        
        // Line 200: lastDot == 0 (Hidden file case, e.g. .gitignore)
        // lastDot > 0 is FALSE, so returns null
        assertNull(fileStorageService.getFileExtension(".gitignore")); 
    }

    @Test
    void getUploadDir_Test() {
        assertEquals(tempDir.toString(), fileStorageService.getUploadDir());
    }

    // ========================================================================
    // 7. CLEANUP (Fix Line 217 Red)
    // ========================================================================
    @Test
    void cleanup_FolderMissing() {
        fileStorageService.uploadDir = tempDir.resolve("missing").toString();
        assertEquals(0, fileStorageService.cleanupOrphanedFiles());
    }
    
    @Test
    void cleanup_FolderExists() {
        assertEquals(0, fileStorageService.cleanupOrphanedFiles());
    }

    @Test
    @DisplayName("Cleanup: Exception (Fix Red Line 217)")
    void cleanup_Exception() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Kita paksa Files.exists melempar RuntimeException.
            // Ini akan mentrigger 'catch (Exception e)' di baris 217.
            filesMock.when(() -> Files.exists(any(Path.class))).thenThrow(new RuntimeException("Disk Error Force"));
            
            int count = fileStorageService.cleanupOrphanedFiles();
            assertEquals(0, count);
        }
    }

    // ========================================================================
    // 8. UPLOAD DIR VALID (Fix Line 241 Red)
    // ========================================================================
    @Test
    void isUploadDirValid_Success() {
        assertTrue(fileStorageService.isUploadDirValid());
    }

    @Test
    void isUploadDirValid_Creates() {
        fileStorageService.uploadDir = tempDir.resolve("new").toString();
        assertTrue(fileStorageService.isUploadDirValid());
        assertTrue(Files.exists(Paths.get(fileStorageService.uploadDir)));
    }

    @Test
    @DisplayName("UploadDirValid: Exception (Fix Red Line 241)")
    void isUploadDirValid_Exception() {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // SKENARIO AGAR MASUK CATCH (IOException):
            // 1. Files.exists() return FALSE -> Masuk ke blok IF creation.
            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            
            // 2. Files.createDirectories() melempar IOException.
            // Ini akan mentrigger catch(IOException e) yang ada di baris 241.
            filesMock.when(() -> Files.createDirectories(any(Path.class))).thenThrow(new IOException("Create Failed"));
            
            assertFalse(fileStorageService.isUploadDirValid());
        }
    }
}