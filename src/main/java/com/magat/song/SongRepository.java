package com.magat.song;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findAllByOrderByIdAsc();
    List<Song> findByTitleContaining(String title);
    List<Song> findByTitleContainingIgnoreCase(String title);
    List<Song> findByArtistContaining(String title);
    List<Song> findByArtistContainingIgnoreCase(String title);
    List<Song> findByAlbumContaining(String title);
    List<Song> findByAlbumContainingIgnoreCase(String title);
    List<Song> findByGenreContaining(String title);
    List<Song> findByGenreContainingIgnoreCase(String title);
    List<Song> findByTitleOrArtistOrAlbumOrGenre(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingOrArtistContainingOrAlbumOrGenre(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingOrArtistContainingOrAlbumContainingOrGenre(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingOrArtistContainingOrAlbumContainingOrGenreContaining(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumIgnoreCaseOrGenreIgnoreCase(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreIgnoreCase(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCase(String title, String artist, String album, String genre);
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCaseOrAlbumContainingIgnoreCaseOrGenreContainingIgnoreCaseOrderByIdAsc(String title, String artist, String album, String genre);
}