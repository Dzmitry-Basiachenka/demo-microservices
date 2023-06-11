package com.microservices.resource.service.cucumber.definition;

import com.microservices.resource.service.cucumber.client.ResourceClient;
import com.microservices.resource.service.dto.ResourceUploadedResponse;
import com.microservices.resource.service.dto.ResourcesDeletedResponse;
import com.microservices.resource.service.entity.ResourceEntity;
import com.microservices.resource.service.repository.ResourceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.common.mapper.TypeRef;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.shaded.org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

    private static final String FILES_PATH = "/audio/";

    private final ResourceClient resourceClient;
    private final ResourceRepository resourceRepository;
    private final ObjectMapper objectMapper;

    private MockMvcResponse response;
    private ResourceUploadedResponse resourceUploadedResponse;
    private ResourcesDeletedResponse resourcesDeletedResponse;

    public StepDefinitions(ResourceClient resourceClient, ResourceRepository resourceRepository, ObjectMapper objectMapper) {
        this.resourceClient = resourceClient;
        this.resourceRepository = resourceRepository;
        this.objectMapper = objectMapper;
    }

    @When("user uploads file {string}")
    public void userUploadsFile(String file) {
        resourceUploadedResponse = uploadFile(file);
    }

    @And("resource uploaded response is")
    public void resourceUploadedResponseIs(String jsonResponse) throws JsonProcessingException {
        var expectedResponse = objectMapper.readValue(jsonResponse, new TypeReference<ResourceUploadedResponse>() {
        });
        assertThat(resourceUploadedResponse.id()).isEqualTo(expectedResponse.id());
    }

    @Then("the following resources are saved")
    public void theFollowingResourcesAreSaved(List<Resource> resources) {
        resources.forEach(resource -> {
                Optional<ResourceEntity> foundResource = resourceRepository.findById(resource.id());
                assertThat(foundResource).isPresent();

                ResourceEntity actualResource = foundResource.get();
                assertThat(actualResource.getId().equals(resource.id())).isTrue();
                assertThat(actualResource.getBucket().equals(resource.bucket())).isTrue();
                assertThat(actualResource.getKey()).isNotNull();
                assertThat(actualResource.getName().equals(resource.name())).isTrue();
                assertThat(actualResource.getSize().equals(resource.size())).isTrue();
            }
        );
    }

    @Given("the following resources uploaded")
    public void theFollowingResourcesUploaded(List<Resource> resources) {
        resources.forEach(resource -> {
                var resourceUploadedResponse = uploadFile(resource.key());
                assertThat(resourceUploadedResponse.id()).isEqualTo(resource.id());
            }
        );
    }

    @When("user downloads resource with id={long}")
    public void userDownloadsResourceWithId(long id) {
        response = resourceClient.downloadResource(id);
    }

    @When("user deletes resource with id={long}")
    public void userDeletesResourceWithId(long id) {
        response = resourceClient.deleteResource(id);

        resourcesDeletedResponse = response.as(new TypeRef<>() {
        });
        assertThat(resourcesDeletedResponse.ids().size()).isEqualTo(1);
        assertThat(resourcesDeletedResponse.ids().iterator().next()).isEqualTo(id);
    }

    @And("resources deleted response is")
    public void resourcesDeletedResponseIs(String jsonResponse) throws JsonProcessingException {
        var expectedResponse = objectMapper.readValue(jsonResponse, new TypeReference<ResourcesDeletedResponse>() {
        });
        assertThat(resourcesDeletedResponse.ids()).isEqualTo(expectedResponse.ids());
    }

    @Then("response code is {int}")
    public void responseCodeIs(int responseStatus) {
        assertThat(response.getStatusCode()).isEqualTo(responseStatus);
    }

    @And("response content type is {string}")
    public void responseContentTypeIs(String contentType) {
        assertThat(response.getContentType()).isEqualTo(contentType);
    }

    @And("response body has size {long}")
    public void responseBodyHasSize(long fileSize) {
        assertThat(response.asByteArray().length).isEqualTo(fileSize);
    }

    public ResourceUploadedResponse uploadFile(String file) {
        try (InputStream inputStream = new ClassPathResource(FILES_PATH + file).getInputStream()) {
            response = resourceClient.uploadResource(inputStream, FilenameUtils.getName(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.as(new TypeRef<>() {
        });
    }
}
