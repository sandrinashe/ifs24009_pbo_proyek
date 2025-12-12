package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoverSongFormTest {

    private CoverSongForm coverSongForm;

    @Mock
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        coverSongForm = new CoverSongForm();
    }

    // --- Basic Getter/Setter ---

    @Test
    @DisplayName("Should set and get ID correctly")
    void testSetAndGetId() {
        UUID id = UUID.randomUUID();
        coverSongForm.setId(id);
        assertEquals(id, coverSongForm.getId());
    }

    @Test
    @DisplayName("Should get CoverFile correctly")
    void testGetCoverFile() {
        coverSongForm.setCoverFile(mockFile);
        assertEquals(mockFile, coverSongForm.getCoverFile());
    }

    // --- Test: isEmpty() ---

    @Test
    @DisplayName("isEmpty returns true when file is null")
    void testIsEmpty_WhenFileIsNull() {
        coverSongForm.setCoverFile(null);
        assertTrue(coverSongForm.isEmpty());
    }

    @Test
    @DisplayName("isEmpty returns true when file is empty")
    void testIsEmpty_WhenFileIsEmpty() {
        when(mockFile.isEmpty()).thenReturn(true);
        coverSongForm.setCoverFile(mockFile);
        assertTrue(coverSongForm.isEmpty());
    }

    @Test
    @DisplayName("isEmpty returns false when file has content")
    void testIsEmpty_WhenFileHasContent() {
        when(mockFile.isEmpty()).thenReturn(false);
        coverSongForm.setCoverFile(mockFile);
        assertFalse(coverSongForm.isEmpty());
    }

    // --- Test: getOriginalFilename() ---

    @Test
    @DisplayName("getOriginalFilename returns correct name")
    void testGetOriginalFilename() {
        when(mockFile.getOriginalFilename()).thenReturn("test-image.png");
        coverSongForm.setCoverFile(mockFile);
        
        assertEquals("test-image.png", coverSongForm.getOriginalFilename());
    }

    @Test
    @DisplayName("getOriginalFilename returns null when file is null")
    void testGetOriginalFilename_Null() {
        coverSongForm.setCoverFile(null);
        assertNull(coverSongForm.getOriginalFilename());
    }

    // --- Test: isValidImage() ---
    // Coverage Note: Kita harus test SEMUA tipe di kondisi OR (jpeg, png, gif, webp)

    @Test
    @DisplayName("isValidImage returns true for ALL valid content types")
    void testIsValidImage_SuccessCases() {
        coverSongForm.setCoverFile(mockFile);
        when(mockFile.isEmpty()).thenReturn(false);

        // 1. Check JPEG
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        assertTrue(coverSongForm.isValidImage(), "Should be valid for image/jpeg");

        // 2. Check PNG
        when(mockFile.getContentType()).thenReturn("image/png");
        assertTrue(coverSongForm.isValidImage(), "Should be valid for image/png");

        // 3. Check GIF (Tadi belum ada, makanya kuning)
        when(mockFile.getContentType()).thenReturn("image/gif");
        assertTrue(coverSongForm.isValidImage(), "Should be valid for image/gif");

        // 4. Check WEBP
        when(mockFile.getContentType()).thenReturn("image/webp");
        assertTrue(coverSongForm.isValidImage(), "Should be valid for image/webp");
    }

    @Test
    @DisplayName("isValidImage returns false for invalid content type")
    void testIsValidImage_InvalidType() {
        coverSongForm.setCoverFile(mockFile);
        when(mockFile.isEmpty()).thenReturn(false);
        
        // Test PDF
        when(mockFile.getContentType()).thenReturn("application/pdf");
        assertFalse(coverSongForm.isValidImage());
        
        // Test Null type (jika content type tidak terdeteksi)
        when(mockFile.getContentType()).thenReturn(null);
        assertFalse(coverSongForm.isValidImage());
    }

    @Test
    @DisplayName("isValidImage returns false when file is empty (Logic isEmpty)")
    void testIsValidImage_EmptyFile() {
        coverSongForm.setCoverFile(mockFile);
        when(mockFile.isEmpty()).thenReturn(true); // Ini memicu this.isEmpty() -> true
        
        assertFalse(coverSongForm.isValidImage());
    }

    @Test
    @DisplayName("isValidImage returns false when file is null")
    void testIsValidImage_NullFile() {
        coverSongForm.setCoverFile(null); // Ini memicu this.isEmpty() -> true (karena null)
        assertFalse(coverSongForm.isValidImage());
    }

    // --- Test: isSizeValid() ---

    @Test
    @DisplayName("isSizeValid checks file size correctly")
    void testIsSizeValid_Normal() {
        long maxSize = 1000L;
        coverSongForm.setCoverFile(mockFile);

        // Valid size (smaller)
        when(mockFile.getSize()).thenReturn(500L);
        assertTrue(coverSongForm.isSizeValid(maxSize));

        // Valid size (equal)
        when(mockFile.getSize()).thenReturn(1000L);
        assertTrue(coverSongForm.isSizeValid(maxSize));

        // Invalid size (larger)
        when(mockFile.getSize()).thenReturn(1001L);
        assertFalse(coverSongForm.isSizeValid(maxSize));
    }

    @Test
    @DisplayName("isSizeValid returns false (or handles safe) when coverFile is null")
    void testIsSizeValid_NullFile() {
        // Test case ini penting untuk menghilangkan kuning pada "coverFile != null"
        coverSongForm.setCoverFile(null);
        assertFalse(coverSongForm.isSizeValid(1000L));
    }

    // --- Test: getFileExtension() ---

    @Test
    @DisplayName("getFileExtension extracts extension correctly")
    void testGetFileExtension_Success() {
        coverSongForm.setCoverFile(mockFile);

        // Normal case
        when(mockFile.getOriginalFilename()).thenReturn("photo.jpg");
        assertEquals("jpg", coverSongForm.getFileExtension());

        // Uppercase case
        when(mockFile.getOriginalFilename()).thenReturn("PHOTO.PNG");
        assertEquals("png", coverSongForm.getFileExtension());

        // Multiple dots
        when(mockFile.getOriginalFilename()).thenReturn("archive.tar.gz");
        assertEquals("gz", coverSongForm.getFileExtension());
    }

    @Test
    @DisplayName("getFileExtension returns null for edge cases")
    void testGetFileExtension_NullsAndNoExt() {
        // 1. CoverFile is Null (Menghilangkan kuning di cek coverFile == null)
        coverSongForm.setCoverFile(null);
        assertNull(coverSongForm.getFileExtension());

        // 2. Filename is Null (Menghilangkan kuning di cek getOriginalFilename == null)
        coverSongForm.setCoverFile(mockFile);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        assertNull(coverSongForm.getFileExtension());

        // 3. No extension (index lastDot > 0 fail)
        when(mockFile.getOriginalFilename()).thenReturn("filename");
        assertNull(coverSongForm.getFileExtension());

        // 4. Dot at start (hidden file) -> considered no extension by logic > 0
        when(mockFile.getOriginalFilename()).thenReturn(".gitignore");
        assertNull(coverSongForm.getFileExtension());
    }

    // --- Test: isValidExtension() ---
    // Coverage Note: Harus test SEMUA ekstensi (jpg, jpeg, png, gif, webp)

    @Test
    @DisplayName("isValidExtension validates ALL allowed extensions")
    void testIsValidExtension_SuccessCases() {
        coverSongForm.setCoverFile(mockFile);

        // 1. jpg
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        assertTrue(coverSongForm.isValidExtension());

        // 2. jpeg
        when(mockFile.getOriginalFilename()).thenReturn("image.jpeg");
        assertTrue(coverSongForm.isValidExtension());

        // 3. png (Case Insensitive)
        when(mockFile.getOriginalFilename()).thenReturn("IMAGE.PNG"); 
        assertTrue(coverSongForm.isValidExtension());

        // 4. gif (Tadi belum ada)
        when(mockFile.getOriginalFilename()).thenReturn("animation.gif");
        assertTrue(coverSongForm.isValidExtension());

        // 5. webp (Tadi belum ada)
        when(mockFile.getOriginalFilename()).thenReturn("modern.webp");
        assertTrue(coverSongForm.isValidExtension());
    }

    @Test
    @DisplayName("isValidExtension returns false for invalid extensions")
    void testIsValidExtension_Invalid() {
        coverSongForm.setCoverFile(mockFile);

        when(mockFile.getOriginalFilename()).thenReturn("script.exe");
        assertFalse(coverSongForm.isValidExtension());

        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        assertFalse(coverSongForm.isValidExtension());
    }

    @Test
    @DisplayName("isValidExtension returns false when extension is null")
    void testIsValidExtension_NullExtension() {
        coverSongForm.setCoverFile(mockFile);
        
        // File tanpa ekstensi akan membuat getFileExtension() return null
        when(mockFile.getOriginalFilename()).thenReturn("README");
        
        // Ini mengetes bagian "extension != null"
        assertFalse(coverSongForm.isValidExtension());
    }
}