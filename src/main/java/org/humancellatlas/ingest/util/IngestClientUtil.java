package org.humancellatlas.ingest.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.client.Traverson;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Optional;
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
        Traverson.TraversalBuilder traversal = traverseOn(resourcesUri)
                .follow("self");

        PagedResources<JsonNode> entityPage = traversal.toObject(jsonNodePageType );

        return collectPages(entityPage, traversal);
    }

    /**
     * give a page of HAL resources, collects all resources from subsequent pages and returns
     * a collection of JSON objects
     *
     * @param pagedResources
     * @return
     */
    public static Collection<JsonNode> collectPages(PagedResources<JsonNode> pagedResources, Traverson.TraversalBuilder traversalSoFar) {
        Collection<JsonNode> entites = getJsonsFromPage(pagedResources);

        if(! Optional.ofNullable(pagedResources.getNextLink()).isPresent()) {
            return entites;
        } else {
            traversalSoFar = traversalSoFar.follow("next");
            PagedResources<JsonNode> nextPage = traversalSoFar.toObject(jsonNodePageType );
            entites.addAll(collectPages(nextPage, traversalSoFar));
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

    private static JsonNode nodeAsTree(JsonNode node) {
        try {
            return mapper.readTree(node.asText());
        } catch (IOException e) {
            log.trace("Failed to read an embedded json string into a JsonNode", e);
            throw new RuntimeException(e);
        }
    }
}
