package org.delcom.app.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SongFormTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==========================================
    // 1. TEST BASIC POJO (Getters, Setters, ToString)
    // ==========================================
    
    @Test
    @DisplayName("Test Full POJO - Getters, Setters, Constructor, ToString")
    void testPojoComplete() {
        // 1. Test All-Args Constructor (jika ada) atau Setter manual
        UUID id = UUID.randomUUID();
        SongForm form = new SongForm("Title", "Artist", "Album", "Genre", 120, 2020);
        
        // Set ID manual karena biasanya tidak ada di constructor form
        form.setId(id);
        form.setConfirmTitle("Delete Confirm");

        // 2. Test Getters
        assertEquals(id, form.getId());
        assertEquals("Title", form.getTitle());
        assertEquals("Artist", form.getArtist());
        assertEquals("Album", form.getAlbum());
        assertEquals("Genre", form.getGenre());
        assertEquals(120, form.getDuration());
        assertEquals(2020, form.getReleaseYear());
        assertEquals("Delete Confirm", form.getConfirmTitle());

        // 3. Test Setters (Update values)
        form.setTitle("New Title");
        form.setArtist("New Artist");
        form.setAlbum("New Album");
        form.setGenre("New Genre");
        form.setDuration(200);
        form.setReleaseYear(2021);

        assertEquals("New Title", form.getTitle());
        assertEquals("New Artist", form.getArtist());
        assertEquals("New Album", form.getAlbum());
        assertEquals("New Genre", form.getGenre());
        assertEquals(200, form.getDuration());
        assertEquals(2021, form.getReleaseYear());

        // 4. Test ToString (Ini sering merah jika tidak dipanggil)
        String stringResult = form.toString();
        assertNotNull(stringResult);
        assertTrue(stringResult.contains("id="));
        assertTrue(stringResult.contains("New Title"));
    }

    // ==========================================
    // 2. TEST LOGIC: getFormattedDuration()
    // ==========================================

    @Test
    @DisplayName("getFormattedDuration logic checks")
    void testGetFormattedDuration() {
        SongForm form = new SongForm();

        // Branch 1: duration == null
        form.setDuration(null);
        assertEquals("00:00", form.getFormattedDuration());

        // Calculation: < 1 min
        form.setDuration(45);
        assertEquals("00:45", form.getFormattedDuration());

        // Calculation: > 1 min
        form.setDuration(65); // 1 min 5 sec
        assertEquals("01:05", form.getFormattedDuration());
        
        // Calculation: Exact minute
        form.setDuration(120);
        assertEquals("02:00", form.getFormattedDuration());
    }

    // ==========================================
    // 3. TEST LOGIC: isValidDuration()
    // ==========================================

    @Test
    @DisplayName("isValidDuration branch logic")
    void testIsValidDuration() {
        SongForm form = new SongForm();

        // 1. Null -> False
        form.setDuration(null);
        assertFalse(form.isValidDuration(), "Duration null should be invalid");

        // 2. 0 -> False (Lower Bound)
        form.setDuration(0);
        assertFalse(form.isValidDuration(), "Duration 0 should be invalid");

        // 3. 7201 -> False (Upper Bound > 7200)
        form.setDuration(7201);
        assertFalse(form.isValidDuration(), "Duration > 7200 should be invalid");

        // 4. Valid cases
        form.setDuration(1);
        assertTrue(form.isValidDuration());
        
        form.setDuration(7200);
        assertTrue(form.isValidDuration());
    }

    // ==========================================
    // 4. TEST LOGIC: isValidReleaseYear()
    // ==========================================

    @Test
    @DisplayName("isValidReleaseYear branch logic")
    void testIsValidReleaseYear() {
        SongForm form = new SongForm();
        int currentYear = Year.now().getValue();

        // 1. Null -> True (Branch: if releaseYear == null return true)
        form.setReleaseYear(null);
        assertTrue(form.isValidReleaseYear(), "Release year null should be allowed");

        // 2. < 1900 -> False
        form.setReleaseYear(1899);
        assertFalse(form.isValidReleaseYear(), "Year < 1900 should be invalid");

        // 3. > Current Year -> False
        form.setReleaseYear(currentYear + 1);
        assertFalse(form.isValidReleaseYear(), "Future year should be invalid");

        // 4. Valid cases
        form.setReleaseYear(1900);
        assertTrue(form.isValidReleaseYear());
        
        form.setReleaseYear(currentYear);
        assertTrue(form.isValidReleaseYear());
    }

    // ==========================================
    // 5. TEST LOGIC: isValid() (COMPREHENSIVE)
    // ==========================================
    // Ini bagian terpenting untuk menghilangkan warna kuning di method isValid()
    // Kita harus menguji setiap kondisi '&&' secara terpisah.

    @Test
    @DisplayName("isValid returns True only when everything is perfect")
    void testIsValid_Success() {
        SongForm form = new SongForm("Title", "Artist", "Album", "Pop", 200, 2020);
        assertTrue(form.isValid());
    }

    @Test
    @DisplayName("isValid fails when Title is Invalid")
    void testIsValid_TitleFail() {
        SongForm form = new SongForm("Title", "Artist", "Album", "Pop", 200, 2020);
        
        // 1. Title Null
        form.setTitle(null);
        assertFalse(form.isValid(), "Should fail when title is null");

        // 2. Title Empty/Whitespace (Logic !title.trim().isEmpty())
        form.setTitle("   ");
        assertFalse(form.isValid(), "Should fail when title is whitespace");
    }

    @Test
    @DisplayName("isValid fails when Artist is Invalid")
    void testIsValid_ArtistFail() {
        SongForm form = new SongForm("Title", "Artist", "Album", "Pop", 200, 2020);
        
        // 1. Artist Null
        form.setArtist(null);
        assertFalse(form.isValid());

        // 2. Artist Empty
        form.setArtist("  ");
        assertFalse(form.isValid());
    }

    @Test
    @DisplayName("isValid fails when Genre is Invalid")
    void testIsValid_GenreFail() {
        SongForm form = new SongForm("Title", "Artist", "Album", "Pop", 200, 2020);

        // 1. Genre Null
        form.setGenre(null);
        assertFalse(form.isValid());

        // 2. Genre Empty
        form.setGenre("  ");
        assertFalse(form.isValid());
    }

    @Test
    @DisplayName("isValid fails when Duration is Invalid (Integration)")
    void testIsValid_DurationFail() {
        SongForm form = new SongForm("Title", "Artist", "Album", "Pop", 200, 2020);

        // Invalid duration Logic (misal null)
        form.setDuration(null);
        // isValid memanggil isValidDuration() -> return false -> isValid return false
        assertFalse(form.isValid());
    }

    @Test
    @DisplayName("isValid fails when ReleaseYear is Invalid (Integration)")
    void testIsValid_YearFail() {
        SongForm form = new SongForm("Title", "Artist", "Album", "Pop", 200, 2020);

        // Invalid year logic (misal 1800)
        form.setReleaseYear(1800);
        // isValid memanggil isValidReleaseYear() -> return false -> isValid return false
        assertFalse(form.isValid());
    }

    // ==========================================
    // 6. TEST ANNOTATIONS (Optional but Good)
    // ==========================================

    @Test
    @DisplayName("Jakarta Validation Annotations check")
    void testAnnotations() {
        SongForm form = new SongForm(); // Kosong
        Set<ConstraintViolation<SongForm>> violations = validator.validate(form);
        
        // Harusnya error karena @NotBlank, @NotNull
        assertFalse(violations.isEmpty());
    }
}