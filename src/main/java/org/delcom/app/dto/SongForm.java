package org.delcom.app.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class SongForm {

    private UUID id;

    @NotBlank(message = "Judul lagu tidak boleh kosong")
    private String title;

    @NotBlank(message = "Nama artist tidak boleh kosong")
    private String artist;

    private String album;

    @NotBlank(message = "Genre tidak boleh kosong")
    private String genre;

    @NotNull(message = "Durasi tidak boleh kosong")
    @Min(value = 1, message = "Durasi minimal 1 detik")
    private Integer duration; // dalam detik

    private Integer releaseYear;

    private String confirmTitle; // untuk konfirmasi penghapusan

    // Constructor
    public SongForm() {
    }

    public SongForm(String title, String artist, String album, String genre, Integer duration, Integer releaseYear) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.duration = duration;
        this.releaseYear = releaseYear;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getConfirmTitle() {
        return confirmTitle;
    }

    public void setConfirmTitle(String confirmTitle) {
        this.confirmTitle = confirmTitle;
    }

    // Helper methods
    public String getFormattedDuration() {
        if (duration == null) {
            return "00:00";
        }
        int minutes = duration / 60;
        int seconds = duration % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isValidDuration() {
        return duration != null && duration > 0 && duration <= 7200; // maksimal 2 jam (7200 detik)
    }

    public boolean isValidReleaseYear() {
        if (releaseYear == null) {
            return true; // nullable
        }
        int currentYear = java.time.Year.now().getValue();
        return releaseYear >= 1900 && releaseYear <= currentYear;
    }

    // Validation helper
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
               artist != null && !artist.trim().isEmpty() &&
               genre != null && !genre.trim().isEmpty() &&
               isValidDuration() &&
               isValidReleaseYear();
    }

    @Override
    public String toString() {
        return "SongForm{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", genre='" + genre + '\'' +
                ", duration=" + duration +
                ", releaseYear=" + releaseYear +
                '}';
    }
}