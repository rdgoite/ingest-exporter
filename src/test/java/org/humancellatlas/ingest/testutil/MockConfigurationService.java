package org.humancellatlas.ingest.testutil;

import org.apache.http.client.utils.URIBuilder;
import org.humancellatlas.ingest.config.ConfigurationService;

/**
 * Created by rolando on 23/02/2018.
 */
public class MockConfigurationService {

    public static final String INGEST_API_HOST = "localhost";
    public static final int INGEST_API_PORT = 8088;

    public static ConfigurationService create() throws Exception {

        ConfigurationService configurationService = new ConfigurationService();
        configurationService.INGEST_API_URI = new URIBuilder()
                .setHost(INGEST_API_HOST)
                .setScheme("http")
                .setPort(INGEST_API_PORT)
                .setPath("").build();

        return configurationService;
    }
}
