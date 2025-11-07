| Scenario | Current Behavior | After Change | Impact |
|-----------|------------------|--------------|---------|
| `Accept: application/json` | ✅ 200 OK | ✅ 200 OK | None |
| `Accept: text/plain` | ✅ 200 OK | ❌ 406 Not Acceptable | Must use JSON |
| No `Accept` header | Defaults to `text/plain` | Defaults to `application/json` | Verify consumer parsing logic |