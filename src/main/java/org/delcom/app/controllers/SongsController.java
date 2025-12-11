package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Songs;
import org.delcom.app.entities.User;
import org.delcom.app.services.SongsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
public class SongsController {
    private final SongsService songsService;

    @Autowired
    protected AuthContext authContext;

    public SongsController(SongsService songsService) {
        this.songsService = songsService;
    }

    // Menambahkan lagu baru
    // -------------------------------
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, UUID>>> createSong(@RequestBody Songs reqSong) {

        // Validasi input
        if (reqSong.getTitle() == null || reqSong.getTitle().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data title tidak valid", null));
        } else if (reqSong.getArtist() == null || reqSong.getArtist().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data artist tidak valid", null));
        } else if (reqSong.getGenre() == null || reqSong.getGenre().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data genre tidak valid", null));
        } else if (reqSong.getDuration() == null || reqSong.getDuration() <= 0) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data duration tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Songs newSong = songsService.createSong(
            authUser.getId(), 
            reqSong.getTitle(), 
            reqSong.getArtist(),
            reqSong.getAlbum(),
            reqSong.getGenre(),
            reqSong.getDuration(),
            reqSong.getReleaseYear()
        );

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Lagu berhasil ditambahkan",
            Map.of("id", newSong.getId())
        ));
    }

    // Mendapatkan semua lagu dengan opsi pencarian
    // -------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllSongs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist) {
        
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<Songs> songs;
        
        // Filter berdasarkan parameter
        if (genre != null && !genre.trim().isEmpty()) {
            songs = songsService.getSongsByGenre(authUser.getId(), genre);
        } else if (artist != null && !artist.trim().isEmpty()) {
            songs = songsService.getSongsByArtist(authUser.getId(), artist);
        } else {
            songs = songsService.getAllSongs(authUser.getId(), search);
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Daftar lagu berhasil diambil",
            Map.of("songs", songs, "total", songs.size())
        ));
    }

    // Mendapatkan lagu berdasarkan ID
    // -------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Songs>>> getSongById(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Songs song = songsService.getSongById(authUser.getId(), id);
        if (song == null) {
            return ResponseEntity.status(404)
                .body(new ApiResponse<>("fail", "Data lagu tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Data lagu berhasil diambil",
            Map.of("song", song)
        ));
    }

    // Memperbarui lagu berdasarkan ID
    // -------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Songs>> updateSong(
            @PathVariable UUID id, 
            @RequestBody Songs reqSong) {

        // Validasi input
        if (reqSong.getTitle() == null || reqSong.getTitle().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data title tidak valid", null));
        } else if (reqSong.getArtist() == null || reqSong.getArtist().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data artist tidak valid", null));
        } else if (reqSong.getGenre() == null || reqSong.getGenre().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data genre tidak valid", null));
        } else if (reqSong.getDuration() == null || reqSong.getDuration() <= 0) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>("fail", "Data duration tidak valid", null));
        }

        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Songs updatedSong = songsService.updateSong(
            authUser.getId(), 
            id, 
            reqSong.getTitle(),
            reqSong.getArtist(),
            reqSong.getAlbum(),
            reqSong.getGenre(),
            reqSong.getDuration(),
            reqSong.getReleaseYear()
        );

        if (updatedSong == null) {
            return ResponseEntity.status(404)
                .body(new ApiResponse<>("fail", "Data lagu tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success", 
            "Data lagu berhasil diperbarui", 
            null
        ));
    }

    // Menghapus lagu berdasarkan ID
    // -------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSong(@PathVariable UUID id) {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        boolean status = songsService.deleteSong(authUser.getId(), id);
        if (!status) {
            return ResponseEntity.status(404)
                .body(new ApiResponse<>("fail", "Data lagu tidak ditemukan", null));
        }

        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Data lagu berhasil dihapus",
            null
        ));
    }

    // Mendapatkan chart data berdasarkan genre
    // -------------------------------
    @GetMapping("/charts/genre")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartByGenre() {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Map<String, Long> chartData = songsService.getChartDataByGenre(authUser.getId());
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Chart data berdasarkan genre berhasil diambil",
            Map.of("chartData", chartData)
        ));
    }

    // Mendapatkan chart data berdasarkan artist
    // -------------------------------
    @GetMapping("/charts/artist")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getChartByArtist() {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        Map<String, Long> chartData = songsService.getChartDataByArtist(authUser.getId());
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Chart data berdasarkan artist berhasil diambil",
            Map.of("chartData", chartData)
        ));
    }

    // Mendapatkan statistik lagu
    // -------------------------------
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        // Validasi autentikasi
        if (!authContext.isAuthenticated()) {
            return ResponseEntity.status(403)
                .body(new ApiResponse<>("fail", "User tidak terautentikasi", null));
        }
        User authUser = authContext.getAuthUser();

        List<Songs> allSongs = songsService.getAllSongs(authUser.getId(), null);
        Integer totalDuration = songsService.getTotalDuration(authUser.getId());
        
        return ResponseEntity.ok(new ApiResponse<>(
            "success",
            "Statistik lagu berhasil diambil",
            Map.of(
                "totalSongs", allSongs.size(),
                "totalDuration", totalDuration,
                "totalDurationInMinutes", totalDuration / 60
            )
        ));
    }
}