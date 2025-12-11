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

    @GetMapping
    public String home(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            Model model) {
        
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

        // Songs - dengan filter opsional
        var songs = songsService.getAllSongs(authUser.getId(), search);
        model.addAttribute("songs", songs);

        // Statistik
        Integer totalSongs = songs.size();
        Integer totalDuration = songsService.getTotalDuration(authUser.getId());
        Integer totalDurationMinutes = totalDuration / 60;
        
        model.addAttribute("totalSongs", totalSongs);
        model.addAttribute("totalDuration", totalDuration);
        model.addAttribute("totalDurationMinutes", totalDurationMinutes);

        // Chart data
        var chartDataGenre = songsService.getChartDataByGenre(authUser.getId());
        var chartDataArtist = songsService.getChartDataByArtist(authUser.getId());
        model.addAttribute("chartDataGenre", chartDataGenre);
        model.addAttribute("chartDataArtist", chartDataArtist);

        // Song Form
        model.addAttribute("songForm", new SongForm());

        // Search params untuk form filter
        model.addAttribute("searchParam", search != null ? search : "");
        model.addAttribute("genreParam", genre != null ? genre : "");
        model.addAttribute("artistParam", artist != null ? artist : "");

        return ConstUtil.TEMPLATE_PAGES_HOME;
    }
}