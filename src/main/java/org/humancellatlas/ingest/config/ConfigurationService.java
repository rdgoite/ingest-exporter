package org.humancellatlas.ingest.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by rolando on 22/02/2018.
 */
@Service("configuration")
@Data
public class ConfigurationService implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${INGEST_HOST:'api.ingest.dev.data.humancellatlas.org'}")
    private String ingestHostString;
    @Value("${INGEST_PORT:80}")
    private String ingestPortString;
    @Value("${INGEST_API_ROOT:'http'}")
    private String ingestScheme;
    @Value("${INGEST_API_ROOT:'/'}")
    private String ingestApiPath;

    @Getter(AccessLevel.NONE) public URI INGEST_API_URI;

    @Value("${blueboxHost:'dss.dev.data.humancellatlas.org'}")
    private String blueboxHost;
    @Value("${BLUEBOX_PORT:80}")
    private String blueboxPortString;
    @Value("${BLUEBOX_SCHEMA:'http'}")
    private String blueboxScheme;
    @Value("${blueboxApiPath:'/'}")
    private String blueboxApiPath;

    @Getter(AccessLevel.NONE) private URI BLUEBOX_API_URI;


    public void init() {
        try {
            this.INGEST_API_URI = new URIBuilder()
                    .setHost(ingestHostString)
                    .setScheme(ingestScheme)
                    .setPort(Integer.parseInt(ingestPortString))
                    .setPath(ingestApiPath).build();
        } catch (URISyntaxException e) {
            getLog().trace("Failed to construct a URI for the ingest core API", e);
        }

        try {
            this.BLUEBOX_API_URI = new URIBuilder()
                    .setHost(blueboxHost)
                    .setScheme(blueboxScheme)
                    .setPort(Integer.parseInt(blueboxPortString))
                    .setPath(blueboxApiPath)
                    .build();

        } catch (URISyntaxException e) {
            getLog().trace("Failed to construct a URI for the ingest core API", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.init();
    }
}
