package com.microservices.song.service.controller;

import com.microservices.song.service.dto.SongCreatedResponse;
import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.dto.SongsDeletedResponse;
import com.microservices.song.service.service.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RequestMapping("/songs")
@RestController
public class SongController {

    private static final Logger logger = LoggerFactory.getLogger(SongController.class);

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongCreatedResponse> createSong(@RequestBody @Valid SongDto songDto) {
        logger.info("Create song: {}", songDto);
        return ResponseEntity.ok(songService.createSong(songDto));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongDto> getSong(@PathVariable Long id) {
        logger.info("Get song by id: {}", id);
        return ResponseEntity.ok(songService.getSong(id));
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongsDeletedResponse> deleteSongs(@RequestParam List<Long> ids) {
        logger.info("Delete songs by ids: {}", ids);
        var deleteResponseDto = songService.deleteSongs(ids);
        return ResponseEntity.ok(deleteResponseDto);
    }
}
