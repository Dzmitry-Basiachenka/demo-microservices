package com.microservices.resource.processor;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class ResourceProcessorApplicationTest {

    @Test
    void contextLoads() {
    }
}
