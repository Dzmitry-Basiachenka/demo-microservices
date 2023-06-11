package com.microservices.resource.service.cucumber;

import com.microservices.resource.service.AbstractIntegrationTest;
import com.microservices.resource.service.cucumber.config.TestConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestConfig.class)
@CucumberContextConfiguration
public class CucumberConfig extends AbstractIntegrationTest {
}
