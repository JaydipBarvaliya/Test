else
  if ss -lnt "sport = :${HEALTH_VALUE}" | grep -q LISTEN; then
    log "Port ${HEALTH_VALUE} is listening"
    break
  fi
fi