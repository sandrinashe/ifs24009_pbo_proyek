package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.Songs;
import org.delcom.app.entities.User;
import org.delcom.app.services.SongsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongsControllerTest {

    @Mock
    private SongsService songsService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private SongsController songsController;

    private User mockUser;
    private Songs mockSong;
    private UUID userId;
    private UUID songId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        songId = UUID.randomUUID();

        // Setup User Dummy
        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("testuser");

        // Setup Song Dummy
        mockSong = new Songs();
        mockSong.setId(songId);
        mockSong.setTitle("Bohemian Rhapsody");
        mockSong.setArtist("Queen");
        mockSong.setAlbum("A Night at the Opera");
        mockSong.setGenre("Rock");
        mockSong.setDuration(354);
        mockSong.setReleaseYear(1975);

        // Inject AuthContext
        ReflectionTestUtils.setField(songsController, "authContext", authContext);
    }

    // ==========================================
    // 1. CREATE SONG (POST)
    // ==========================================

    @Test
    void createSong_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.createSong(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockSong);

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = songsController.createSong(mockSong);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    void createSong_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = songsController.createSong(mockSong);
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    // --- Validation Splits (Create) ---
    @Test
    void createSong_TitleNull() {
        mockSong.setTitle(null);
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_TitleEmpty() {
        mockSong.setTitle("");
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_ArtistNull() {
        mockSong.setTitle("Val"); mockSong.setArtist(null);
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_ArtistEmpty() {
        mockSong.setTitle("Val"); mockSong.setArtist("");
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_GenreNull() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre(null);
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_GenreEmpty() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre("");
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_DurationNull() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre("Val"); mockSong.setDuration(null);
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void createSong_DurationInvalid() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre("Val"); mockSong.setDuration(0);
        ResponseEntity<?> response = songsController.createSong(mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==========================================
    // 2. GET ALL SONGS (GET) - FIXED FOR FULL COVERAGE
    // ==========================================

    @Test
    void getAllSongs_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.getAllSongs(null, null, null);
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    // CASE 1: Genre Valid (Masuk IF pertama)
    @Test
    void getAllSongs_FilterByGenre() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getSongsByGenre(userId, "Rock")).thenReturn(List.of(mockSong));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            songsController.getAllSongs(null, "Rock", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songsService).getSongsByGenre(userId, "Rock");
    }

    // CASE 2: Artist Valid (Genre Null) -> (Masuk ELSE IF)
    @Test
    void getAllSongs_FilterByArtist_GenreNull() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getSongsByArtist(userId, "Queen")).thenReturn(List.of(mockSong));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            songsController.getAllSongs(null, null, "Queen");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songsService).getSongsByArtist(userId, "Queen");
    }

    // CASE 3: Genre EMPTY String ("") -> (Cek logika !genre.isEmpty())
    // Genre tidak null, tapi kosong, jadi harus LEWAT (False) di IF pertama, masuk ke cek Artist
    @Test
    void getAllSongs_GenreEmpty_ShouldCheckArtist() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getSongsByArtist(userId, "Queen")).thenReturn(List.of(mockSong));

        // Param genre dikirim "" (kosong), artist "Queen"
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            songsController.getAllSongs(null, "", "Queen");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Harus memanggil byArtist, membuktikan dia melewati cek genre karena kosong
        verify(songsService).getSongsByArtist(userId, "Queen");
    }

    // CASE 4: Artist EMPTY String ("") -> (Cek logika !artist.isEmpty())
    // Genre Null, Artist tidak null tapi kosong -> Harus LEWAT (False) di ELSE IF, masuk ke ELSE (Default/Search)
    @Test
    void getAllSongs_ArtistEmpty_ShouldGoToDefault() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        // Expect memanggil getAllSongs (search), BUKAN getSongsByArtist
        when(songsService.getAllSongs(eq(userId), any())).thenReturn(List.of(mockSong));

        // Param genre null, artist "" (kosong)
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            songsController.getAllSongs("someSearch", null, "   "); // spasi/kosong

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songsService).getAllSongs(userId, "someSearch");
    }

    // CASE 5: Semua Null -> (Masuk ELSE)
    @Test
    void getAllSongs_NoFilter() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getAllSongs(eq(userId), any())).thenReturn(List.of(mockSong));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
            songsController.getAllSongs(null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songsService).getAllSongs(userId, null);
    }

    // ==========================================
    // 3. GET SONG BY ID (GET)
    // ==========================================

    @Test
    void getSongById_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getSongById(userId, songId)).thenReturn(mockSong);

        ResponseEntity<ApiResponse<Map<String, Songs>>> response = songsController.getSongById(songId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getSongById_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getSongById(userId, songId)).thenReturn(null);

        ResponseEntity<ApiResponse<Map<String, Songs>>> response = songsController.getSongById(songId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getSongById_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.getSongById(songId);
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    // ==========================================
    // 4. UPDATE SONG (PUT)
    // ==========================================

    @Test
    void updateSong_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.updateSong(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(mockSong);

        ResponseEntity<ApiResponse<Songs>> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateSong_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.updateSong(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        ResponseEntity<ApiResponse<Songs>> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateSong_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    // --- Validation Splits (Update) ---
    @Test
    void updateSong_TitleNull() {
        mockSong.setTitle(null);
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_TitleEmpty() {
        mockSong.setTitle("");
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_ArtistNull() {
        mockSong.setTitle("Val"); mockSong.setArtist(null);
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_ArtistEmpty() {
        mockSong.setTitle("Val"); mockSong.setArtist("");
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_GenreNull() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre(null);
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_GenreEmpty() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre("");
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_DurationNull() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre("Val"); mockSong.setDuration(null);
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    @Test
    void updateSong_DurationInvalid() {
        mockSong.setTitle("Val"); mockSong.setArtist("Val"); mockSong.setGenre("Val"); mockSong.setDuration(0);
        ResponseEntity<?> response = songsController.updateSong(songId, mockSong);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ==========================================
    // 5. DELETE SONG (DELETE)
    // ==========================================

    @Test
    void deleteSong_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.deleteSong(userId, songId)).thenReturn(true);

        ResponseEntity<ApiResponse<String>> response = songsController.deleteSong(songId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteSong_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.deleteSong(userId, songId)).thenReturn(false);

        ResponseEntity<ApiResponse<String>> response = songsController.deleteSong(songId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteSong_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.deleteSong(songId);
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    // ==========================================
    // 6. STATISTICS & CHARTS
    // ==========================================

    @Test
    void getStatistics_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getAllSongs(eq(userId), any())).thenReturn(List.of(mockSong));
        when(songsService.getTotalDuration(userId)).thenReturn(100);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = songsController.getStatistics();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    void getStatistics_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.getStatistics();
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getChartByGenre_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getChartDataByGenre(userId)).thenReturn(Map.of("Rock", 1L));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = songsController.getChartByGenre();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getChartByGenre_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.getChartByGenre();
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getChartByArtist_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(mockUser);
        when(songsService.getChartDataByArtist(userId)).thenReturn(Map.of("Queen", 1L));

        ResponseEntity<ApiResponse<Map<String, Object>>> response = songsController.getChartByArtist();
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getChartByArtist_Unauthorized() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<?> response = songsController.getChartByArtist();
        assertTrue(response.getStatusCode() == HttpStatus.FORBIDDEN || response.getStatusCode() == HttpStatus.UNAUTHORIZED);
    }
}
