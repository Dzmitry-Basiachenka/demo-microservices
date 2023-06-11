package com.microservices.resource.processor.client.contract;

import com.microservices.resource.processor.dto.SongCreatedResponse;
import com.microservices.resource.processor.dto.SongDto;
import com.microservices.resource.processor.dto.SongsDeletedResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureStubRunner(
    stubsMode = StubRunnerProperties.StubsMode.LOCAL,
    ids = "com.microservices:song-service:+:stubs:8082")
@SpringBootTest
public class SongClientContractTest {

    private static final String URL = "http://localhost:8082/songs";

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void pingStub() {
        ResponseEntity<Void> response = restTemplate.getForEntity("http://localhost:8082/ping", Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldCreateSong() {
        var songDto = getSongDto();
        var id = songDto.id();

        var response = restTemplate.postForEntity(URL, songDto, SongCreatedResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
    }

    @Test
    void shouldGetSong() {
        var songDto = getSongDto();
        var id = songDto.id();

        var response = restTemplate.getForEntity(URL + "/" + id, SongDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(songDto);
    }

    @Test
    void shouldDeleteSong() {
        var id = 1L;
        var deleteResponseDto = new SongsDeletedResponse(List.of(id));

        var headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var response = restTemplate.exchange(
            UriComponentsBuilder.fromUriString(URL).queryParam("ids", id).build().toUri(),
            HttpMethod.DELETE,
            new HttpEntity<>(headers),
            SongsDeletedResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(deleteResponseDto);
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
}
