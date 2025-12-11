package org.delcom.app.views;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.delcom.app.dto.CoverSongForm;
import org.delcom.app.dto.SongForm;
import org.delcom.app.entities.Songs;
import org.delcom.app.entities.User;
import org.delcom.app.services.FileStorageService;
import org.delcom.app.services.SongsService;
import org.delcom.app.utils.ConstUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/songs")
public class SongView {

    private final SongsService songsService;
    private final FileStorageService fileStorageService;

    public SongView(SongsService songsService, FileStorageService fileStorageService) {
        this.songsService = songsService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/add")
    public String postAddSong(@Valid @ModelAttribute("songForm") SongForm songForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        // Validasi form
        if (songForm.getTitle() == null || songForm.getTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul lagu tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addSongModalOpen", true);
            return "redirect:/";
        }

        if (songForm.getArtist() == null || songForm.getArtist().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama artist tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addSongModalOpen", true);
            return "redirect:/";
        }

        if (songForm.getGenre() == null || songForm.getGenre().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Genre tidak boleh kosong");
            redirectAttributes.addFlashAttribute("addSongModalOpen", true);
            return "redirect:/";
        }

        if (!songForm.isValidDuration()) {
            redirectAttributes.addFlashAttribute("error", "Durasi tidak valid (1-7200 detik)");
            redirectAttributes.addFlashAttribute("addSongModalOpen", true);
            return "redirect:/";
        }

        if (!songForm.isValidReleaseYear()) {
            redirectAttributes.addFlashAttribute("error", "Tahun rilis tidak valid");
            redirectAttributes.addFlashAttribute("addSongModalOpen", true);
            return "redirect:/";
        }

        // Simpan lagu
        var entity = songsService.createSong(
                authUser.getId(),
                songForm.getTitle(),
                songForm.getArtist(),
                songForm.getAlbum(),
                songForm.getGenre(),
                songForm.getDuration(),
                songForm.getReleaseYear());

        if (entity == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal menambahkan lagu");
            redirectAttributes.addFlashAttribute("addSongModalOpen", true);
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Lagu berhasil ditambahkan.");
        return "redirect:/";
    }

    @PostMapping("/edit")
    public String postEditSong(@Valid @ModelAttribute("songForm") SongForm songForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {
        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;

        // Validasi form
        if (songForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID lagu tidak valid");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            return "redirect:/";
        }

        if (songForm.getTitle() == null || songForm.getTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul lagu tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            redirectAttributes.addFlashAttribute("editSongModalId", songForm.getId());
            return "redirect:/";
        }

        if (songForm.getArtist() == null || songForm.getArtist().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama artist tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            redirectAttributes.addFlashAttribute("editSongModalId", songForm.getId());
            return "redirect:/";
        }

        if (songForm.getGenre() == null || songForm.getGenre().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Genre tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            redirectAttributes.addFlashAttribute("editSongModalId", songForm.getId());
            return "redirect:/";
        }

        if (!songForm.isValidDuration()) {
            redirectAttributes.addFlashAttribute("error", "Durasi tidak valid (1-7200 detik)");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            redirectAttributes.addFlashAttribute("editSongModalId", songForm.getId());
            return "redirect:/";
        }

        if (!songForm.isValidReleaseYear()) {
            redirectAttributes.addFlashAttribute("error", "Tahun rilis tidak valid");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            redirectAttributes.addFlashAttribute("editSongModalId", songForm.getId());
            return "redirect:/";
        }

        // Update lagu
        var updated = songsService.updateSong(
                authUser.getId(),
                songForm.getId(),
                songForm.getTitle(),
                songForm.getArtist(),
                songForm.getAlbum(),
                songForm.getGenre(),
                songForm.getDuration(),
                songForm.getReleaseYear());
        if (updated == null) {
            redirectAttributes.addFlashAttribute("error", "Gagal memperbarui lagu");
            redirectAttributes.addFlashAttribute("editSongModalOpen", true);
            redirectAttributes.addFlashAttribute("editSongModalId", songForm.getId());
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Lagu berhasil diperbarui.");
        return "redirect:/";
    }

    @PostMapping("/delete")
    public String postDeleteSong(@Valid @ModelAttribute("songForm") SongForm songForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }

        User authUser = (User) principal;

        // Validasi form
        if (songForm.getId() == null) {
            redirectAttributes.addFlashAttribute("error", "ID lagu tidak valid");
            redirectAttributes.addFlashAttribute("deleteSongModalOpen", true);
            return "redirect:/";
        }

        if (songForm.getConfirmTitle() == null || songForm.getConfirmTitle().isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi judul tidak boleh kosong");
            redirectAttributes.addFlashAttribute("deleteSongModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteSongModalId", songForm.getId());
            return "redirect:/";
        }

        // Periksa apakah lagu tersedia
        Songs existingSong = songsService.getSongById(authUser.getId(), songForm.getId());
        if (existingSong == null) {
            redirectAttributes.addFlashAttribute("error", "Lagu tidak ditemukan");
            redirectAttributes.addFlashAttribute("deleteSongModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteSongModalId", songForm.getId());
            return "redirect:/";
        }

        if (!existingSong.getTitle().equals(songForm.getConfirmTitle())) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi judul tidak sesuai");
            redirectAttributes.addFlashAttribute("deleteSongModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteSongModalId", songForm.getId());
            return "redirect:/";
        }

        // Hapus lagu
        boolean deleted = songsService.deleteSong(
                authUser.getId(),
                songForm.getId());
        if (!deleted) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghapus lagu");
            redirectAttributes.addFlashAttribute("deleteSongModalOpen", true);
            redirectAttributes.addFlashAttribute("deleteSongModalId", songForm.getId());
            return "redirect:/";
        }

        // Redirect dengan pesan sukses
        redirectAttributes.addFlashAttribute("success", "Lagu berhasil dihapus.");
        return "redirect:/";
    }

    @GetMapping("/{songId}")
    public String getDetailSong(@PathVariable UUID songId, Model model) {
        // Autentikasi user
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

        // Ambil lagu
        Songs song = songsService.getSongById(authUser.getId(), songId);
        if (song == null) {
            return "redirect:/";
        }
        model.addAttribute("song", song);

        // Cover Song Form
        CoverSongForm coverSongForm = new CoverSongForm();
        coverSongForm.setId(songId);
        model.addAttribute("coverSongForm", coverSongForm);

        return ConstUtil.TEMPLATE_PAGES_SONGS_DETAIL;
    }

    @PostMapping("/edit-cover")
    public String postEditCoverSong(@Valid @ModelAttribute("coverSongForm") CoverSongForm coverSongForm,
            RedirectAttributes redirectAttributes,
            HttpSession session,
            Model model) {

        // Autentikasi user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/auth/logout";
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            return "redirect:/auth/logout";
        }
        User authUser = (User) principal;

        if (coverSongForm.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "File cover tidak boleh kosong");
            redirectAttributes.addFlashAttribute("editCoverSongModalOpen", true);
            return "redirect:/songs/" + coverSongForm.getId();
        }

        // Check if song exists
        Songs song = songsService.getSongById(authUser.getId(), coverSongForm.getId());
        if (song == null) {
            redirectAttributes.addFlashAttribute("error", "Lagu tidak ditemukan");
            redirectAttributes.addFlashAttribute("editCoverSongModalOpen", true);
            return "redirect:/";
        }

        // Validasi manual file type
        if (!coverSongForm.isValidImage()) {
            redirectAttributes.addFlashAttribute("error", "Format file tidak didukung. Gunakan JPG, PNG, GIF, atau WebP");
            redirectAttributes.addFlashAttribute("editCoverSongModalOpen", true);
            return "redirect:/songs/" + coverSongForm.getId();
        }

        // Validasi file size (max 5MB)
        if (!coverSongForm.isSizeValid(5 * 1024 * 1024)) {
            redirectAttributes.addFlashAttribute("error", "Ukuran file terlalu besar. Maksimal 5MB");
            redirectAttributes.addFlashAttribute("editCoverSongModalOpen", true);
            return "redirect:/songs/" + coverSongForm.getId();
        }

        try {
            // Simpan file
            String fileName = fileStorageService.storeFile(coverSongForm.getCoverFile(), coverSongForm.getId());

            // Update song dengan nama file cover
            songsService.updateCover(coverSongForm.getId(), fileName);

            redirectAttributes.addFlashAttribute("success", "Cover lagu berhasil diupload");
            return "redirect:/songs/" + coverSongForm.getId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengupload cover");
            redirectAttributes.addFlashAttribute("editCoverSongModalOpen", true);
            return "redirect:/songs/" + coverSongForm.getId();
        }

    }

    @GetMapping("/cover/{filename:.+}")
    @ResponseBody
    public Resource getCoverByFilename(@PathVariable String filename) {
        try {
            Path file = fileStorageService.loadFile(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}