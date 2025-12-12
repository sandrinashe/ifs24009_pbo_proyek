package org.delcom.app.views;

import org.delcom.app.dto.SongForm;
import org.delcom.app.entities.User;
import org.delcom.app.services.SongsService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeView {

    private final SongsService songsService;

    public HomeView(SongsService songsService) {
        this.songsService = songsService;
    }

    // ... (Method home yang sudah ada biarkan saja) ...
    @GetMapping
    public String home(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            Model model) {
        
        // ... (Kode autentikasi & logic home Anda yang lama) ...
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // Songs
        var songs = songsService.getAllSongs(authUser.getId(), search);
        model.addAttribute("songs", songs);

        // Statistik & Chart Logic (Sesuai kode Anda sebelumnya)
        Integer totalSongs = songs.size();
        Integer totalDuration = songsService.getTotalDuration(authUser.getId());
        Integer totalDurationMinutes = totalDuration != null ? totalDuration / 60 : 0; // Null safety check
        
        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalDuration", totalDuration);
        model.addAttribute("totalDurationMinutes", totalDurationMinutes);

        var chartDataGenre = songsService.getChartDataByGenre(authUser.getId());
        var chartDataArtist = songsService.getChartDataByArtist(authUser.getId()); // Pastikan logic sorting ada di Service
        model.addAttribute("chartDataGenre", chartDataGenre);
        model.addAttribute("chartDataArtist", chartDataArtist);

        model.addAttribute("songForm", new SongForm());
        model.addAttribute("searchParam", search != null ? search : "");

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }

    // ==========================================
    // TAMBAHKAN KODE MY LIBRARY DI BAWAH INI
    // ==========================================
    
    // Tambahkan ini di dalam HomeView.java
    @GetMapping("/library")
    public String library(
            @RequestParam(required = false) String search,
            Model model) {

        // 1. Cek Autentikasi
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;
        model.addAttribute("auth", authUser);

        // 2. Ambil Data Lagu
        var songs = songsService.getAllSongs(authUser.getId(), search);
        model.addAttribute("songs", songs);
        model.addAttribute("searchParam", search != null ? search : "");

        // 3. Return ke template library
        return "pages/library"; 
    }
}