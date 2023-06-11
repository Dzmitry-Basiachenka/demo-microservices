package com.microservices.resource.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class WireMockInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        var wireMockServer = new WireMockServer(
            new WireMockConfiguration()
                .dynamicPort()
                .extensions(new ResponseTemplateTransformer(false))
        );

        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        applicationContext
            .getBeanFactory()
            .registerSingleton("wireMockServer", wireMockServer);

        TestPropertyValues
            .of("com.microservices.service.api-gateway.url:http://localhost:" + wireMockServer.port())
            .applyTo(applicationContext);
    }
}
