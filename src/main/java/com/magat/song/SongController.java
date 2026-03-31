package com.magat.song;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path="/magat/songs")
public class SongController {
    @Autowired
    private SongRepository songRepository;

    @PostMapping
    public ResponseEntity<Song> createSong(@RequestBody Song song) throws URISyntaxException {
        Song savedSong = songRepository.save(song);
        return ResponseEntity.ok()
                .location(new URI("/magat/songs/" + savedSong.getId()))
                .body(savedSong);
    }

    @GetMapping
    public @ResponseBody Iterable<Song> getAllSongs() {
        return songRepository.findAllByOrderByIdAsc();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getSongById(@PathVariable Long id) {
        Optional<Song> song = songRepository.findById(id);

        if (song.isPresent()) {
            return ResponseEntity.ok(song.get());
        }

        return ResponseEntity.status(404).body("No song found with ID: " + id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editSong(@PathVariable Long id, @RequestBody Song songDetails) {
        Optional<Song> targetSong = songRepository.findById(id);

        if (targetSong.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        Song songToUpdate = targetSong.get();

        // Updating all required fields
        songToUpdate.setTitle(songDetails.getTitle());
        songToUpdate.setArtist(songDetails.getArtist());
        songToUpdate.setAlbum(songDetails.getAlbum());
        songToUpdate.setGenre(songDetails.getGenre());
        songToUpdate.setUrl(songDetails.getUrl());

        Song updatedSong = songRepository.save(songToUpdate);

        return ResponseEntity.ok(updatedSong);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deleteSong(@PathVariable Long id) {
        Optional<Song> song = songRepository.findById(id);

        if (song.isPresent()) {
            songRepository.deleteById(id);
            return ResponseEntity.ok("Song with ID " + id + " deleted.");
        }

        return ResponseEntity.status(404).body("No Song found with ID: " + id);
    }

    @GetMapping("/search/{keyword}")
    @ResponseBody
    // 2. Changed @RequestParam to @PathVariable
    public ResponseEntity<List<Song>> searchSongs(@PathVariable String keyword) {
        List<Song> results = songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCaseOrderByIdAsc(
                keyword, keyword, keyword, keyword
        );

        return ResponseEntity.ok(results);
    }
}