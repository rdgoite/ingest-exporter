package org.humancellatlas.ingest;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.humancellatlas.ingest.client.IngestClient;
import org.humancellatlas.ingest.client.impl.IngestClientImpl;
import org.humancellatlas.ingest.model.EntityType;
import org.humancellatlas.ingest.model.SubmissionEnvelopeReference;
import org.humancellatlas.ingest.testutil.MockConfigurationService;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;


import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;

import static org.junit.Assert.*;

/**
 * Created by rolando on 23/02/2018.
 */
public class IngestClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8088);

    public IngestClientTest() {

    }

    @Test
    public void testGetAllEntitiesForSubmissionEnvelope() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String mockIngestApiUri = "http://localhost:8088";
        String mockEnvelopeId = "mockenvelopeid";
        String mockEnvelopeUri = mockIngestApiUri + "/envelopes/" + mockEnvelopeId;
        String mockEntityType = EntityType.PROCESSES.toString().toLowerCase();
        String mockEntitiesUri = mockIngestApiUri + "/" + mockEntityType;
        SubmissionEnvelopeReference mockEnvelopeReference = new SubmissionEnvelopeReference(
                mockEnvelopeId,
                UUID.randomUUID(),
                "/envelopes/" + mockEnvelopeId);

        // set up a stub for a mock submission envelope resource
        class MockEnvelope {
            @JsonProperty("_links") Map<String, Object> _links;

            MockEnvelope() {
                _links = new HashMap<String, Object>() {{
                    put(mockEntityType, new HashMap<String, Object>() {{
                        put("href", mockEntitiesUri);
                    }});
                    put("self", new HashMap<String, Object>() {{
                        put("href", mockEnvelopeUri);
                    }});
                }};
            }
        }

        MockEnvelope mockEnvelope = new MockEnvelope();

        stubFor(
                get(urlEqualTo("/envelopes/" + mockEnvelopeId))
                        .withHeader("Accept", equalTo("application/hal+json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/hal+json")
                                .withBody(new ObjectMapper().writeValueAsString(mockEnvelope))));

        // set up stubs for three small pages, each with 4 embedded resources
        class MockEntity {
            @JsonProperty("content")
            Map<String, Object> content;
            @JsonProperty("uuid") Map<String, Object> uuid;

            MockEntity() {
                content = new HashMap<String, Object>() {{
                    put("mockfield", "blah blah");
                }};
                uuid = new HashMap<String, Object>() {{
                    put("uuid", UUID.randomUUID().toString());
                }};
            }
        }

        class Page {
            @JsonProperty("_embedded") Map<String, Object> _embedded;
            @JsonProperty("_links") Map<String, Object> _links;

            Page(int pageNum, boolean last) throws Exception {
                _embedded = new HashMap<String, Object>() {{
                    put(mockEntityType, Arrays.asList(
                            mapper.writeValueAsString(new MockEntity()),
                            mapper.writeValueAsString(new MockEntity()),
                            mapper.writeValueAsString(new MockEntity()),
                            mapper.writeValueAsString(new MockEntity())
                    ));
                }};

                _links = new HashMap<String, Object>() {{
                    put("first", new HashMap<String, Object>() {{
                        put("href", mockEntitiesUri + "?page=0&size=4" );
                    }});
                    put("self", new HashMap<String, Object>() {{
                        put("href", mockEntitiesUri + "{?page,size,sort}");
                        put("templated", true);
                    }});
                }};

                if(!last) {
                    _links.put("next", new HashMap<String, Object>() {{
                        put("href", mockEntitiesUri + String.format("?page=%s&size=4", pageNum) );
                    }});
                }
            }
        }


        Page pageOne = new Page(1, false);
        Page pageTwo = new Page(2, false);
        Page pageThree = new Page(3, true);

        stubFor(
                get(urlEqualTo("/" + mockEntityType))
                        .withHeader("Accept", equalTo("application/hal+json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/hal+json")
                                .withBody(new ObjectMapper().writeValueAsString(pageOne))));


        stubFor(
                get(urlEqualTo("/" + mockEntityType +  "?page=1&size=4"))
                        .withHeader("Accept", equalTo("application/hal+json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/hal+json")
                                .withBody(new ObjectMapper().writeValueAsString(pageTwo))));

        stubFor(
                get(urlEqualTo("/" + mockEntityType +  "?page=2&size=4"))
                        .withHeader("Accept", equalTo("application/hal+json"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/hal+json")
                                .withBody(new ObjectMapper().writeValueAsString(pageThree))));

        IngestClient ingestClient = new IngestClientImpl(MockConfigurationService.create());
        Collection<JsonNode> entityResources = ingestClient.getAllEntitiesForSubmissionEnvelope(mockEnvelopeReference, EntityType.PROCESSES);
        assertTrue(entityResources.size() == 12);
    }
}
