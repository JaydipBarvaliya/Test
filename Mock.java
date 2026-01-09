if [[ "$DEPLOY_MODE" == "service" ]]; then
  systemctl stop "$SERVICE_NAME"
fi

# always replace jar
deploy_jar

if [[ "$DEPLOY_MODE" == "service" ]]; then
  systemctl start "$SERVICE_NAME"
  run_health_check
else
  log "Batch job detected. Skipping service start and health check."
fi