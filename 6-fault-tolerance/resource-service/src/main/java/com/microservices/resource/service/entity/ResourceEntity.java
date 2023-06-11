package com.microservices.resource.service.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "resources")
public class ResourceEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column(nullable = false)
    private Long storageId;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long size;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(Long storageId) {
        this.storageId = storageId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ResourceEntity that = (ResourceEntity) obj;
        return Objects.equals(id, that.id)
            && Objects.equals(storageId, that.storageId)
            && Objects.equals(key, that.key)
            && Objects.equals(name, that.name)
            && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storageId, key, name, size);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ResourceEntity.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("storageId=" + storageId)
            .add("key='" + key + "'")
            .add("name='" + name + "'")
            .add("size=" + size)
            .toString();
    }
}
