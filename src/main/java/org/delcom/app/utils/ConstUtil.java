package org.delcom.app.utils;

public class ConstUtil {
    
    // Template paths for authentication pages
    public static final String TEMPLATE_PAGES_AUTH_LOGIN = "pages/auth/login";
    public static final String TEMPLATE_PAGES_AUTH_REGISTER = "pages/auth/register";
    
    // Template paths for main pages
    public static final String TEMPLATE_PAGES_HOME = "pages/home";
    
    // Template paths for songs pages
    public static final String TEMPLATE_PAGES_SONGS_DETAIL = "pages/songs/detail";
    public static final String TEMPLATE_PAGES_SONGS_LIST = "pages/songs/list";
    
    // API endpoints
    public static final String API_BASE_PATH = "/api";
    public static final String API_AUTH_PATH = API_BASE_PATH + "/auth";
    public static final String API_SONGS_PATH = API_BASE_PATH + "/songs";
    
    // File upload settings
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "gif", "webp"};
    
    // Music settings
    public static final int MIN_DURATION = 1; // 1 second
    public static final int MAX_DURATION = 7200; // 2 hours in seconds
    public static final int MIN_RELEASE_YEAR = 1900;
    
    // Pagination settings
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    
    // Message constants
    public static final String MSG_SUCCESS_ADD_SONG = "Lagu berhasil ditambahkan";
    public static final String MSG_SUCCESS_UPDATE_SONG = "Lagu berhasil diperbarui";
    public static final String MSG_SUCCESS_DELETE_SONG = "Lagu berhasil dihapus";
    public static final String MSG_SUCCESS_UPLOAD_COVER = "Cover lagu berhasil diupload";
    
    public static final String MSG_ERROR_SONG_NOT_FOUND = "Lagu tidak ditemukan";
    public static final String MSG_ERROR_INVALID_FILE = "Format file tidak valid";
    public static final String MSG_ERROR_FILE_TOO_LARGE = "Ukuran file terlalu besar";
    public static final String MSG_ERROR_UNAUTHORIZED = "User tidak terautentikasi";
    
    // Genre options (dapat disesuaikan)
    public static final String[] MUSIC_GENRES = {
        "Pop", "Rock", "Jazz", "Classical", "Hip Hop", 
        "R&B", "Country", "Electronic", "Reggae", "Blues",
        "Metal", "Folk", "Latin", "K-Pop", "Indie"
    };
}