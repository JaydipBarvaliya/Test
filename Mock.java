tlog() {
  tail -f "$@" | sed \
    -e 's/ERROR/\o033[31m&\o033[0m/g' \
    -e 's/WARN/\o033[33m&\o033[0m/g' \
    -e 's/INFO/\o033[32m&\o033[0m/g' \
    -e 's/Exception/\o033[35m&\o033[0m/g'
}