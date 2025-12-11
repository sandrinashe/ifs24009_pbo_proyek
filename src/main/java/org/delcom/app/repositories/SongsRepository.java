package org.delcom.app.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.Songs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SongsRepository extends JpaRepository<Songs, UUID> {
    
    // Mencari lagu berdasarkan keyword (title, artist, album, genre)
    @Query("SELECT s FROM Songs s WHERE (LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.artist) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.album) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(s.genre) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND s.userId = :userId ORDER BY s.createdAt DESC")
    List<Songs> findByKeyword(UUID userId, String keyword);

    // Mendapatkan semua lagu berdasarkan userId
    @Query("SELECT s FROM Songs s WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    List<Songs> findAllByUserId(UUID userId);

    // Mendapatkan lagu berdasarkan id dan userId
    @Query("SELECT s FROM Songs s WHERE s.id = :id AND s.userId = :userId")
    Optional<Songs> findByUserIdAndId(UUID userId, UUID id);

    // Mendapatkan lagu berdasarkan genre
    @Query("SELECT s FROM Songs s WHERE s.userId = :userId AND LOWER(s.genre) = LOWER(:genre) ORDER BY s.createdAt DESC")
    List<Songs> findByUserIdAndGenre(UUID userId, String genre);

    // Mendapatkan lagu berdasarkan artist
    @Query("SELECT s FROM Songs s WHERE s.userId = :userId AND LOWER(s.artist) LIKE LOWER(CONCAT('%', :artist, '%')) ORDER BY s.createdAt DESC")
    List<Songs> findByUserIdAndArtist(UUID userId, String artist);

    // Query untuk chart data - jumlah lagu per genre
    @Query("SELECT s.genre, COUNT(s) FROM Songs s WHERE s.userId = :userId GROUP BY s.genre ORDER BY COUNT(s) DESC")
    List<Object[]> countSongsByGenre(UUID userId);

    // Query untuk chart data - jumlah lagu per artist
    @Query("SELECT s.artist, COUNT(s) FROM Songs s WHERE s.userId = :userId GROUP BY s.artist ORDER BY COUNT(s) DESC")
    List<Object[]> countSongsByArtist(UUID userId);
}