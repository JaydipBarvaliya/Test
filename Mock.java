@PostConstruct
public void init() {
    mandatoryPropList = List.of(mandateProp.split(":"));
}

public String checkMandatoryProp(String lob, String propToCheck)
        throws SharedServiceLayerException {
    String propValue = config.getConfigProperty(lob, propToCheck);

    if (isMandatory(propToCheck) && null == propValue) {
        throw new SharedServiceLayerException(
                new Status("500", Severity.Error),
                "Mandatory Config Property: " + propToCheck + " cannot be NULL for LOB: " + lob);
    }

    return propValue;
}

boolean isMandatory(String propertyToCheck) {
    return !mandatoryPropList.isEmpty() && mandatoryPropList.contains(propertyToCheck);
}