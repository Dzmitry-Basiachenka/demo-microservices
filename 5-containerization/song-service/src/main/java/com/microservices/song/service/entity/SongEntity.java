package com.microservices.song.service.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "songs")
public class SongEntity {

    @Id
    @Column
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String artist;

    @Column
    private String album;

    @Column(nullable = false)
    private String length;

    @Column
    private String released;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getReleased() {
        return released;
    }

    public void setReleased(String released) {
        this.released = released;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SongEntity that = (SongEntity) obj;
        return Objects.equals(id, that.id)
            && Objects.equals(name, that.name)
            && Objects.equals(artist, that.artist)
            && Objects.equals(album, that.album)
            && Objects.equals(length, that.length)
            && Objects.equals(released, that.released);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, artist, album, length, released);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SongEntity.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("name='" + name + "'")
            .add("artist='" + artist + "'")
            .add("album='" + album + "'")
            .add("length='" + length + "'")
            .add("released=" + released)
            .toString();
    }
}
