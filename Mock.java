@Cacheable(
    value = "token",
    key = "#apiName.name() + ':' + #lobId"
)