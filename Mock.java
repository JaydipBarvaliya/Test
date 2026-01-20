private List<ConfigurationDto> toDtos(List<Configuration> entities) {
    if (CollectionUtils.isEmpty(entities)) {
        return List.of();
    }

    return entities.stream()
            .map(EntityToDTOMapper.INSTANCE::toDto)
            .toList();
}