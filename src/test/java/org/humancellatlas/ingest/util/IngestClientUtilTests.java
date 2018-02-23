package org.humancellatlas.ingest.util;

/**
 * Created by rolando on 23/02/2018.
 */
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;


public class IngestClientUtilTests {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8088);

    @Test
    public void testResourcePageCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String mockIngestApiUri = "http://localhost:8088";
        String mockEntityType = "mockentities";
        String mockEntitiesUri = mockIngestApiUri + "/" + mockEntityType;

        // set up stubs for three small pages, each with 4 embedded resources

        class MockEntity {
            @JsonProperty("content") Map<String, Object> content;
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

        Collection<JsonNode> jsons = IngestClientUtil.getAllResources(mockEntitiesUri);
        // assert we collected all resources from the 3 pages of 4
        assertTrue(jsons.size() == 12);
        // assert all UUIDs are different
        Set<String> uuids = new HashSet<>();

        jsons.forEach(json -> {
            String uuid = json.get("uuid").get("uuid").toString();
            assertTrue(! uuids.contains(uuid));
            uuids.add(uuid);
        });

    }

}
