package org.humancellatlas.ingest.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by rolando on 23/02/2018.
 */
public class IngestClientUtil {
    private static final Logger log = LoggerFactory.getLogger(IngestClientUtil.class);

    private static ObjectMapper mapper = new ObjectMapper();
    private static ParameterizedTypeReference<PagedResources<JsonNode>> jsonNodePageType = new ParameterizedTypeReference<PagedResources<JsonNode>>() {};


    /**
     * Given a URI that returns a page of resources, returns all embedded resources from all pages as a collection of
     * JSONs
     *
     * @param resourcesUri
     * @return
     */
    public static Collection<JsonNode> getAllResources(String resourcesUri) {
        return getAllResourcesWithTemplateParams(resourcesUri, new LinkedMultiValueMap<>());
    }

    public static Collection<JsonNode> getAllResourcesWithTemplateParams(String resourcesUri, MultiValueMap<String, String> templateParams) {
        PagedResources<JsonNode> entityPage = restTemplate()
                .exchange(
                        uriWithQueryParams(resourcesUri, templateParams),
                        HttpMethod.GET,
                        null,
                        jsonNodePageType)
                .getBody();

        return collectPages(entityPage, templateParams);
    }


    /**
     * give a page of HAL resources, collects all resources from subsequent pages and returns
     * a collection of JSON objects
     *
     * @param pagedResources
     * @return
     */
    public static Collection<JsonNode> collectPages(PagedResources<JsonNode> pagedResources, MultiValueMap<String, String> templateParams) {
        Collection<JsonNode> entites = getJsonsFromPage(pagedResources);

        if(! Optional.ofNullable(pagedResources.getNextLink()).isPresent()) {
            return entites;
        } else {
            URI uri = UriComponentsBuilder
                    .fromHttpUrl(pagedResources.getNextLink().getHref())
                    .queryParams(templateParams)
                    .build().encode().toUri();

            PagedResources<JsonNode> nextPage = restTemplate()
                    .exchange(uri, HttpMethod.GET, null, jsonNodePageType).getBody();

            entites.addAll(collectPages(nextPage, templateParams));
            return entites;
        }
    }

    public static Collection<JsonNode> getJsonsFromPage(PagedResources<JsonNode> page) {
        return page
                .getContent()
                .stream()
                .map(IngestClientUtil::nodeAsTree)
                .collect(Collectors.toList());

    }

    public static Traverson traverseOn(String baseUri) {
        return new Traverson(URI.create(baseUri), MediaTypes.HAL_JSON);
    }

    public static RestTemplate restTemplate() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(MediaType.parseMediaTypes("application/hal+json"));
        converter.setObjectMapper(mapper);
        return new RestTemplate(Collections.singletonList(converter));
    }

    public static URI uriWithQueryParams(String uri, MultiValueMap<String, String> queryParams) {
        return UriComponentsBuilder
                .fromHttpUrl(uri)
                .queryParams(queryParams)
                .build().encode().toUri();
    }

    private static JsonNode nodeAsTree(JsonNode node) {
        try {
            return mapper.readTree(node.asText());
        } catch (IOException e) {
            log.trace("Failed to read an embedded json string into a JsonNode", e);
            throw new RuntimeException(e);
        }
    }
}
