package org.delcom.app.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SongsTest {

    @Test
    @DisplayName("Should create instance with No-Args Constructor and Setters")
    void testNoArgsConstructorAndSetters() {
        // Arrange
        Songs song = new Songs();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        // Act
        song.setId(id);
        song.setUserId(userId);
        song.setTitle("Imagine");
        song.setArtist("John Lennon");
        song.setAlbum("Imagine Album");
        song.setGenre("Pop");
        song.setDuration(180);
        song.setReleaseYear(1971);
        song.setCover("image.jpg");

        // Assert
        assertEquals(id, song.getId());
        assertEquals(userId, song.getUserId());
        assertEquals("Imagine", song.getTitle());
        assertEquals("John Lennon", song.getArtist());
        assertEquals("Imagine Album", song.getAlbum());
        assertEquals("Pop", song.getGenre());
        assertEquals(180, song.getDuration());
        assertEquals(1971, song.getReleaseYear());
        assertEquals("image.jpg", song.getCover());
    }

    @Test
    @DisplayName("Should create instance with Parameterized Constructor")
    void testParameterizedConstructor() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String title = "Bohemian Rhapsody";
        String artist = "Queen";
        String album = "A Night at the Opera";
        String genre = "Rock";
        Integer duration = 354;
        Integer releaseYear = 1975;

        // Act
        Songs song = new Songs(userId, title, artist, album, genre, duration, releaseYear);

        // Assert
        assertEquals(userId, song.getUserId());
        assertEquals(title, song.getTitle());
        assertEquals(artist, song.getArtist());
        assertEquals(album, song.getAlbum());
        assertEquals(genre, song.getGenre());
        assertEquals(duration, song.getDuration());
        assertEquals(releaseYear, song.getReleaseYear());
    }

    @Test
    @DisplayName("Should handle PrePersist (onCreate)")
    void testOnCreate() {
        // Arrange
        Songs song = new Songs();

        // Act
        // Kita panggil method protected ini secara manual untuk memastikan logikanya benar
        // (Dalam aplikasi nyata, Hibernate yang akan memanggil ini)
        song.onCreate();

        // Assert
        assertNotNull(song.getCreatedAt(), "CreatedAt tidak boleh null setelah onCreate");
        assertNotNull(song.getUpdatedAt(), "UpdatedAt tidak boleh null setelah onCreate");
        
        // Pastikan waktunya barusan (selisih milidetik)
        assertTrue(song.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertEquals(song.getCreatedAt(), song.getUpdatedAt(), "Saat create, created dan updated harus sama");
    }

    @Test
    @DisplayName("Should handle PreUpdate (onUpdate)")
    void testOnUpdate() throws InterruptedException {
        // Arrange
        Songs song = new Songs();
        song.onCreate(); // Set waktu awal
        LocalDateTime createdAt = song.getCreatedAt();
        LocalDateTime firstUpdatedAt = song.getUpdatedAt();

        // Tunggu sebentar agar waktu berubah (10 milidetik)
        Thread.sleep(10);

        // Act
        song.onUpdate(); // Simulasi update

        // Assert
        assertEquals(createdAt, song.getCreatedAt(), "CreatedAt tidak boleh berubah saat update");
        assertTrue(song.getUpdatedAt().isAfter(firstUpdatedAt), "UpdatedAt harus lebih baru dari waktu sebelumnya");
    }
}