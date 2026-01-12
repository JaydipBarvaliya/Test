package com.td.dgvlm.api.filenet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "filenet.batchdoc")
public record FileNetBatchDocProperties(
        String baseUrl,
        String extractPath,
        int timeoutSeconds
) {}





@EnableConfigurationProperties(FileNetBatchDocProperties.class)




package com.td.dgvlm.api.filenet.client;

import com.td.dgvlm.api.filenet.dto.BatchDocRequest;
import com.td.dgvlm.api.filenet.dto.BatchDocResponse;

public interface FileNetBatchDocClient {
    BatchDocResponse triggerExtract(
            BatchDocRequest request,
            String traceabilityId,
            String messageId,
            String bearerToken
    );
}




package com.td.dgvlm.api.filenet.client.impl;

import com.td.dgvlm.api.filenet.client.FileNetBatchDocClient;
import com.td.dgvlm.api.filenet.config.FileNetBatchDocProperties;
import com.td.dgvlm.api.filenet.dto.BatchDocRequest;
import com.td.dgvlm.api.filenet.dto.BatchDocResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientFileNetBatchDocClient implements FileNetBatchDocClient {

    private final WebClient webClient;
    private final FileNetBatchDocProperties props;

    public WebClientFileNetBatchDocClient(WebClient.Builder builder, FileNetBatchDocProperties props) {
        this.props = props;
        this.webClient = builder
                .baseUrl(props.baseUrl())
                .build();
    }

    @Override
    public BatchDocResponse triggerExtract(
            BatchDocRequest request,
            String traceabilityId,
            String messageId,
            String bearerToken
    ) {
        return webClient
                .post()
                .uri(props.extractPath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("TraceabilityId", traceabilityId)
                .header("MessageId", messageId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BatchDocResponse.class)
                .timeout(Duration.ofSeconds(props.timeoutSeconds()))
                .block();
    }
}





package com.td.dgvlm.api.filenet.client.impl;

import com.td.dgvlm.api.filenet.client.FileNetBatchDocClient;
import com.td.dgvlm.api.filenet.dto.BatchDocRequest;
import com.td.dgvlm.api.filenet.dto.BatchDocResponse;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class MockFileNetBatchDocClient implements FileNetBatchDocClient {

    @Override
    public BatchDocResponse triggerExtract(
            BatchDocRequest request,
            String traceabilityId,
            String messageId,
            String bearerToken
    ) {
        return new BatchDocResponse(UUID.randomUUID().toString());
    }
}






