package com.microservices.song.service.controller;

import com.microservices.song.service.dto.SongCreatedResponse;
import com.microservices.song.service.dto.SongDto;
import com.microservices.song.service.dto.SongsDeletedResponse;
import com.microservices.song.service.service.SongService;
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

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongCreatedResponse> createSong(@RequestBody @Valid SongDto songDto) {
        return ResponseEntity.ok(songService.createSong(songDto));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongDto> getSong(@PathVariable Long id) {
        return ResponseEntity.ok(songService.getSong(id));
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SongsDeletedResponse> deleteSongs(@RequestParam List<Long> ids) {
        var deleteResponseDto = songService.deleteSongs(ids);
        return ResponseEntity.ok(deleteResponseDto);
    }
}
