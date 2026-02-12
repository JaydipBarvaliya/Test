package com.td.dgvlm.api.service;

import com.td.dgvlm.api.repository.ClientAuthConfigurationRepository;
import com.td.dgvlm.api.dto.ClientAuthConfigurationDto;
import com.td.dgvlm.api.entity.ClientAppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAuthConfigurationServiceTest {

    @Mock
    private ClientAuthConfigurationRepository repository;

    @InjectMocks
    private ClientAuthConfigurationService service;

    private ClientAppConfiguration entity;

    @BeforeEach
    void setUp() {
        entity = new ClientAppConfiguration();
        entity.setId(1L);
        entity.setClientId("test-client");
        entity.setClientSecret("secret");
    }

    @Test
    void fetchAllData_shouldReturnMappedDtos_whenDataExists() {

        when(repository.findAll()).thenReturn(List.of(entity));

        List<ClientAuthConfigurationDto> result = service.fetchAllData();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-client", result.get(0).getClientId());
    }

    @Test
    void fetchAllData_shouldReturnEmptyList_whenRepositoryReturnsEmpty() {

        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<ClientAuthConfigurationDto> result = service.fetchAllData();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void fetchAllData_shouldReturnEmptyList_whenRepositoryReturnsNull() {

        when(repository.findAll()).thenReturn(null);

        List<ClientAuthConfigurationDto> result = service.fetchAllData();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}