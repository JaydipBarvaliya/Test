package com.td.esig.api.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class CheckMandatoryProperty {

    private final ConfigurationProperties config;

    @Value("${configuration.property.mandatory:}")
    private String mandateProp;

    private List<String> mandatoryPropList = Collections.emptyList();

    public CheckMandatoryProperty(ConfigurationProperties config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(mandateProp)) {
            mandatoryPropList = Stream.of(mandateProp.split(":"))
                                      .map(String::trim)
                                      .filter(s -> !s.isEmpty())
                                      .collect(Collectors.toList());
        } else {
            log.warn("No mandatory properties defined under 'configuration.property.mandatory'");
        }
        log.debug("Initialized mandatory property list: {}", mandatoryPropList);
    }

    /**
     * Checks a mandatory property.
     *
     * @param lob the LOB value
     * @param propToCheck property name to check
     * @return property value if valid
     * @throws SharedServiceLayerException if the value is null
     */
    public String checkMandatoryProp(String lob, String propToCheck) throws SharedServiceLayerException {
        String propValue = config.getConfigProperty(lob, propToCheck);

        if (isMandatory(propToCheck) && !StringUtils.hasText(propValue)) {
            String msg = String.format("Mandatory Config Property '%s' cannot be NULL for LOB: %s", propToCheck, lob);
            log.error(msg);
            throw new SharedServiceLayerException(new Status("500", Severity.Error), msg);
        }

        return propValue;
    }

    /**
     * Checks if a property is mandatory.
     *
     * @param propertyToCheck property name
     * @return true if mandatory, false otherwise
     */
    boolean isMandatory(String propertyToCheck) {
        boolean result = mandatoryPropList.contains(propertyToCheck);
        if (result) {
            log.debug("'{}' is a mandatory property", propertyToCheck);
        }
        return result;
    }
}