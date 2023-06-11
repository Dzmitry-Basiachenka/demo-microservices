package com.microservices.song.service.controller;

import com.microservices.song.service.dto.SongCreatedResponse;
import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.dto.SongsDeletedResponse;
import com.microservices.song.service.entity.SongEntity;
import com.microservices.song.service.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SongControllerApplicationTest {

    private static final String URL_PATH = "/songs";

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private SongRepository songRepository;

    @BeforeEach
    void init() {
        songRepository.deleteAll();
    }

    @Test
    void shouldCreateSong() {
        var songDto = getSongDto();

        var responseEntity = testRestTemplate.postForEntity(URL_PATH, songDto, SongCreatedResponse.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        var songCreatedResponse = responseEntity.getBody();
        assertNotNull(songCreatedResponse);
        assertNotNull(songCreatedResponse.id());

        var foundSongEntity = songRepository.findById(songCreatedResponse.id());
        assertTrue(foundSongEntity.isPresent());

        var actualSongEntity = foundSongEntity.get();
        assertEquals(songCreatedResponse.id(), actualSongEntity.getId());
        assertEquals(songDto.id(), actualSongEntity.getId());
        assertEquals(songDto.name(), actualSongEntity.getName());
        assertEquals(songDto.artist(), actualSongEntity.getArtist());
        assertEquals(songDto.album(), actualSongEntity.getAlbum());
        assertEquals(songDto.length(), actualSongEntity.getLength());
        assertEquals(songDto.released(), actualSongEntity.getReleased());
    }

    @Test
    void shouldGetSong() {
        var savedSongEntity = songRepository.save(getSongEntity());

        var responseEntity = testRestTemplate.getForEntity(URL_PATH + "/" + savedSongEntity.getId(), SongDto.class);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        var songDto = responseEntity.getBody();
        assertNotNull(songDto);
        assertEquals(savedSongEntity.getId(), songDto.id());
        assertEquals(savedSongEntity.getName(), songDto.name());
        assertEquals(savedSongEntity.getArtist(), songDto.artist());
        assertEquals(savedSongEntity.getAlbum(), songDto.album());
        assertEquals(savedSongEntity.getLength(), songDto.length());
        assertEquals(savedSongEntity.getReleased(), songDto.released());
    }

    @Test
    void shouldDeleteSong() {
        var savedSongEntity = songRepository.save(getSongEntity());

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var responseEntity = testRestTemplate.exchange(
            UriComponentsBuilder.fromUriString(URL_PATH).queryParam("ids", savedSongEntity.getId()).build().toUri(),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            SongsDeletedResponse.class
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, responseEntity.getHeaders().getContentType());

        var songDeletedResponse = responseEntity.getBody();
        assertNotNull(songDeletedResponse);
        assertNotNull(songDeletedResponse.ids());
        assertEquals(1, songDeletedResponse.ids().size());

        var foundSongEntities = songRepository.findAllById(songDeletedResponse.ids());
        assertNotNull(foundSongEntities);
        assertTrue(foundSongEntities.isEmpty());
    }

    private SongDto getSongDto() {
        return new SongDto(
            1L,
            "Song",
            "John Doe",
            "Songs",
            "60",
            "2020"
        );
    }

    private SongEntity getSongEntity() {
        SongEntity songEntity = new SongEntity();
        songEntity.setId(1L);
        songEntity.setName("Song");
        songEntity.setArtist("John Doe");
        songEntity.setAlbum("Songs");
        songEntity.setLength("60");
        songEntity.setReleased("2020");
        return songEntity;
    }
}
