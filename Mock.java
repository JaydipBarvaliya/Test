package com.td.dgvlm.api.filenet.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BatchDocRequest(
        String primaryRepositoryId,
        List<ProcessBlock> process,
        List<KeyValuePair> extractOption
) {
    public record ProcessBlock(
            List<KeyValuePair> repositorySearchCriteria,
            List<KeyValuePair> documentSearchCriteria,
            List<KeyValuePair> option
    ) {}

    public record KeyValuePair(
            String keyName,
            Object keyValue
    ) {}
}