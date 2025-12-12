package org.delcom.app.services;

import org.delcom.app.entities.Songs;
import org.delcom.app.repositories.SongsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongsServiceTest {

    @Mock
    private SongsRepository songsRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private SongsService songsService;

    private Songs mockSong;
    private UUID userId;
    private UUID songId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        songId = UUID.randomUUID();

        mockSong = new Songs();
        mockSong.setId(songId);
        mockSong.setUserId(userId);
        mockSong.setTitle("Test Song");
        mockSong.setArtist("Test Artist");
        mockSong.setGenre("Pop");
        mockSong.setDuration(200);
        mockSong.setReleaseYear(2023);
    }

    // ========================================================================
    // 1. Test Create Song
    // ========================================================================
    @Test
    void createSong_Success() {
        when(songsRepository.save(any(Songs.class))).thenReturn(mockSong);

        Songs result = songsService.createSong(userId, "Title", "Artist", "Album", "Pop", 180, 2022);

        assertNotNull(result);
        assertEquals(songId, result.getId());
        verify(songsRepository).save(any(Songs.class));
    }

    // ========================================================================
    // 2. Test Get All Songs (Fixing Yellow Coverage)
    // ========================================================================
    @Test
    void getAllSongs_SearchIsNull() {
        // Case: search == null
        when(songsRepository.findAllByUserId(userId)).thenReturn(List.of(mockSong));

        List<Songs> result = songsService.getAllSongs(userId, null);

        assertEquals(1, result.size());
        verify(songsRepository).findAllByUserId(userId);
        verify(songsRepository, never()).findByKeyword(any(), any());
    }

    @Test
    void getAllSongs_SearchIsNotEmpty() {
        // Case: search != null && !isEmpty
        String keyword = "Test";
        when(songsRepository.findByKeyword(userId, keyword)).thenReturn(List.of(mockSong));

        List<Songs> result = songsService.getAllSongs(userId, keyword);

        assertEquals(1, result.size());
        verify(songsRepository).findByKeyword(userId, keyword);
    }

    @Test
    void getAllSongs_SearchIsEmptyString() {
        // Case: search != null TAPI isEmpty == true ("")
        // Ini yang memperbaiki warna kuning pada logika &&
        when(songsRepository.findAllByUserId(userId)).thenReturn(List.of(mockSong));

        List<Songs> result = songsService.getAllSongs(userId, "");

        assertEquals(1, result.size());
        // Harus memanggil findAllByUserId, BUKAN findByKeyword
        verify(songsRepository).findAllByUserId(userId);
    }

    @Test
    void getAllSongs_SearchIsWhitespace() {
        // Case: search != null TAPI trim().isEmpty() == true ("   ")
        when(songsRepository.findAllByUserId(userId)).thenReturn(List.of(mockSong));

        List<Songs> result = songsService.getAllSongs(userId, "   ");

        assertEquals(1, result.size());
        verify(songsRepository).findAllByUserId(userId);
    }

    // ========================================================================
    // 3. Test Get Song By ID
    // ========================================================================
    @Test
    void getSongById_Found() {
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.of(mockSong));
        Songs result = songsService.getSongById(userId, songId);
        assertNotNull(result);
    }

    @Test
    void getSongById_NotFound() {
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.empty());
        Songs result = songsService.getSongById(userId, songId);
        assertNull(result);
    }

    // ========================================================================
    // 4. Test Update Song
    // ========================================================================
    @Test
    void updateSong_Success() {
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.of(mockSong));
        when(songsRepository.save(any(Songs.class))).thenReturn(mockSong);

        Songs result = songsService.updateSong(userId, songId, "New Title", "New Artist", "New Album", "Rock", 300, 2024);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
    }

    @Test
    void updateSong_NotFound() {
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.empty());
        Songs result = songsService.updateSong(userId, songId, "T", "A", "A", "G", 100, 2020);
        assertNull(result);
    }

    // ========================================================================
    // 5. Test Delete Song
    // ========================================================================
    @Test
    void deleteSong_Success_WithCover() {
        mockSong.setCover("cover.jpg");
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.of(mockSong));

        boolean result = songsService.deleteSong(userId, songId);

        assertTrue(result);
        verify(fileStorageService).deleteFile("cover.jpg");
        verify(songsRepository).deleteById(songId);
    }

    @Test
    void deleteSong_Success_NoCover() {
        mockSong.setCover(null);
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.of(mockSong));

        boolean result = songsService.deleteSong(userId, songId);

        assertTrue(result);
        verify(fileStorageService, never()).deleteFile(any());
        verify(songsRepository).deleteById(songId);
    }

    @Test
    void deleteSong_NotFound() {
        when(songsRepository.findByUserIdAndId(userId, songId)).thenReturn(Optional.empty());
        boolean result = songsService.deleteSong(userId, songId);
        assertFalse(result);
    }

    // ========================================================================
    // 6. Test Update Cover
    // ========================================================================
    @Test
    void updateCover_Success_ReplacesOldCover() {
        mockSong.setCover("old-cover.jpg");
        when(songsRepository.findById(songId)).thenReturn(Optional.of(mockSong));
        when(songsRepository.save(any(Songs.class))).thenReturn(mockSong);

        Songs result = songsService.updateCover(songId, "new-cover.jpg");

        assertNotNull(result);
        assertEquals("new-cover.jpg", result.getCover());
        verify(fileStorageService).deleteFile("old-cover.jpg");
    }

    @Test
    void updateCover_Success_NoOldCover() {
        // Ini memperbaiki coverage if (song.getCover() != null)
        mockSong.setCover(null); 
        when(songsRepository.findById(songId)).thenReturn(Optional.of(mockSong));
        when(songsRepository.save(any(Songs.class))).thenReturn(mockSong);

        Songs result = songsService.updateCover(songId, "new-cover.jpg");

        assertNotNull(result);
        assertEquals("new-cover.jpg", result.getCover());
        // Pastikan TIDAK mencoba menghapus file karena cover lama null
        verify(fileStorageService, never()).deleteFile(any()); 
    }

    @Test
    void updateCover_NotFound() {
        when(songsRepository.findById(songId)).thenReturn(Optional.empty());
        Songs result = songsService.updateCover(songId, "new.jpg");
        assertNull(result);
    }

    // ========================================================================
    // 7. Test Filters (Fixing Red Lines)
    // ========================================================================
    @Test
    void getSongsByGenre_Success() {
        when(songsRepository.findByUserIdAndGenre(userId, "Pop")).thenReturn(List.of(mockSong));

        List<Songs> result = songsService.getSongsByGenre(userId, "Pop");

        assertEquals(1, result.size());
        assertEquals("Pop", result.get(0).getGenre());
        verify(songsRepository).findByUserIdAndGenre(userId, "Pop");
    }

    @Test
    void getSongsByArtist_Success() {
        when(songsRepository.findByUserIdAndArtist(userId, "Test Artist")).thenReturn(List.of(mockSong));

        List<Songs> result = songsService.getSongsByArtist(userId, "Test Artist");

        assertEquals(1, result.size());
        assertEquals("Test Artist", result.get(0).getArtist());
        verify(songsRepository).findByUserIdAndArtist(userId, "Test Artist");
    }

    // ========================================================================
    // 8. Test Charts
    // ========================================================================
    @Test
    void getChartDataByGenre_Success() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"Pop", 10L});
        mockResults.add(new Object[]{"Rock", 5L});

        when(songsRepository.countSongsByGenre(userId)).thenReturn(mockResults);

        Map<String, Long> result = songsService.getChartDataByGenre(userId);

        assertEquals(2, result.size());
        assertEquals(10L, result.get("Pop"));
    }

    @Test
    void getChartDataByArtist_Success() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"Artist A", 2L});

        when(songsRepository.countSongsByArtist(userId)).thenReturn(mockResults);

        Map<String, Long> result = songsService.getChartDataByArtist(userId);

        assertEquals(1, result.size());
        assertEquals(2L, result.get("Artist A"));
    }
    
    @Test
    void getChartDataByGenre_Empty() {
        // Test loop coverage jika data kosong
        when(songsRepository.countSongsByGenre(userId)).thenReturn(Collections.emptyList());
        Map<String, Long> result = songsService.getChartDataByGenre(userId);
        assertTrue(result.isEmpty());
    }

    // ========================================================================
    // 9. Test Total Duration
    // ========================================================================
    @Test
    void getTotalDuration_Success() {
        Songs song1 = new Songs(); song1.setDuration(100);
        Songs song2 = new Songs(); song2.setDuration(200);
        
        when(songsRepository.findAllByUserId(userId)).thenReturn(List.of(song1, song2));

        Integer total = songsService.getTotalDuration(userId);

        assertEquals(300, total);
    }
    
    @Test
    void getTotalDuration_EmptyList() {
        // Test loop coverage jika user tidak punya lagu
        when(songsRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());
        Integer total = songsService.getTotalDuration(userId);
        assertEquals(0, total);
    }
}