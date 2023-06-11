package com.microservices.storage.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class StorageServiceApplicationTest {

    @Test
    void contextLoads() {
    }
}
