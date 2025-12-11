package org.delcom.app.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Songs;
import org.delcom.app.repositories.SongsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SongsService {
    private final SongsRepository songsRepository;
    private final FileStorageService fileStorageService;

    public SongsService(SongsRepository songsRepository, FileStorageService fileStorageService) {
        this.songsRepository = songsRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public Songs createSong(UUID userId, String title, String artist, String album, 
                           String genre, Integer duration, Integer releaseYear) {
        Songs song = new Songs(userId, title, artist, album, genre, duration, releaseYear);
        return songsRepository.save(song);
    }

    public List<Songs> getAllSongs(UUID userId, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return songsRepository.findByKeyword(userId, search);
        }
        return songsRepository.findAllByUserId(userId);
    }

    public Songs getSongById(UUID userId, UUID id) {
        return songsRepository.findByUserIdAndId(userId, id).orElse(null);
    }

    @Transactional
    public Songs updateSong(UUID userId, UUID id, String title, String artist, 
                           String album, String genre, Integer duration, Integer releaseYear) {
        Songs song = songsRepository.findByUserIdAndId(userId, id).orElse(null);
        if (song != null) {
            song.setTitle(title);
            song.setArtist(artist);
            song.setAlbum(album);
            song.setGenre(genre);
            song.setDuration(duration);
            song.setReleaseYear(releaseYear);
            return songsRepository.save(song);
        }
        return null;
    }

    @Transactional
    public boolean deleteSong(UUID userId, UUID id) {
        Songs song = songsRepository.findByUserIdAndId(userId, id).orElse(null);
        if (song == null) {
            return false;
        }

        // Hapus cover jika ada
        if (song.getCover() != null) {
            fileStorageService.deleteFile(song.getCover());
        }

        songsRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Songs updateCover(UUID songId, String coverFilename) {
        Optional<Songs> songOpt = songsRepository.findById(songId);
        if (songOpt.isPresent()) {
            Songs song = songOpt.get();

            // Hapus file cover lama jika ada
            if (song.getCover() != null) {
                fileStorageService.deleteFile(song.getCover());
            }

            song.setCover(coverFilename);
            return songsRepository.save(song);
        }
        return null;
    }

    // Service untuk filter berdasarkan genre
    public List<Songs> getSongsByGenre(UUID userId, String genre) {
        return songsRepository.findByUserIdAndGenre(userId, genre);
    }

    // Service untuk filter berdasarkan artist
    public List<Songs> getSongsByArtist(UUID userId, String artist) {
        return songsRepository.findByUserIdAndArtist(userId, artist);
    }

    // Service untuk chart data - jumlah lagu per genre
    public Map<String, Long> getChartDataByGenre(UUID userId) {
        List<Object[]> results = songsRepository.countSongsByGenre(userId);
        Map<String, Long> chartData = new HashMap<>();
        
        for (Object[] result : results) {
            String genre = (String) result[0];
            Long count = (Long) result[1];
            chartData.put(genre, count);
        }
        
        return chartData;
    }

    // Service untuk chart data - jumlah lagu per artist
    public Map<String, Long> getChartDataByArtist(UUID userId) {
        List<Object[]> results = songsRepository.countSongsByArtist(userId);
        Map<String, Long> chartData = new HashMap<>();
        
        for (Object[] result : results) {
            String artist = (String) result[0];
            Long count = (Long) result[1];
            chartData.put(artist, count);
        }
        
        return chartData;
    }

    // Service untuk mendapatkan total durasi semua lagu
    public Integer getTotalDuration(UUID userId) {
        List<Songs> songs = songsRepository.findAllByUserId(userId);
        return songs.stream()
                   .mapToInt(Songs::getDuration)
                   .sum();
    }
}