if [[ "$DEPLOY_MODE" == "service" ]]; then
  systemctl list-unit-files | grep -q "^${SERVICE_NAME}.service" \
    || fail "Service not found: ${SERVICE_NAME}"
fi