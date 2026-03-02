Perfect callout ✅. If you’re not passing secondary token at all, then we should not even mention “Primary + Secondary” as our behavior. We’ll state it as: BatchDoc supports it, but DGVLM intentionally uses Primary-only.

Here’s the updated Confluence-ready, cleanly sectioned version you can paste:

⸻

Cascading Scopes — Why it’s not feasible for DGVLM (especially Rescheduler/Cron)

1) Definitions (what we mean by “Primary Token”)
	•	Primary Token = the OAuth/JWT token received from LOB → DGVLM.
	•	This token represents the LOB/user context and is the only token we forward downstream.
	•	DGVLM does not send any Secondary Token to BatchDoc (even though BatchDoc may support it).

⸻

2) How token propagation works in the real-time (Ingest) flow

Flow: LOB → DGVLM → BatchDoc (+ DGVLA / Kafka-related actions)

In the ingest (sync) call:
	•	LOB sends a Primary Token to DGVLM.
	•	DGVLM forwards the same Primary Token when calling:
	•	BatchDoc API
	•	DGVLA API (if applicable in the use-case)
	•	The Primary Token already contains all required scopes needed to:
	•	Call BatchDoc
	•	Call DGVLA
	•	Perform downstream operations such as pulling/processing events from Kafka (as per the authorization design for these flows)

✅ Result: Ingest flow can support “cascading” behavior because the LOB token is available in-request and includes required scopes.

⸻

3) Why cascading scopes breaks for Rescheduler / Cron retry processing

Key constraint: The cron/rescheduler runs without an active LOB request context.

So when the rescheduler executes:
	•	There is no LOB Primary Token available (because no LOB call is happening at that time).
	•	We cannot recreate the LOB token (it is user/session/context-driven).
	•	We cannot store/persist the LOB token for later use (security + compliance + token expiration).

❌ Therefore, cascading scopes is not feasible in async retry because cascading assumes:
	•	“Take the LOB Primary Token and propagate it downstream”
	•	But in cron/rescheduler, that token does not exist.

⸻

4) What we do instead (Rescheduler auth strategy)

For rescheduler processing:
	•	DGVLM generates a service token by calling PingFed (service-to-service authentication).
	•	That generated token is treated as the Primary Token for downstream calls (BatchDoc/DGVLA) in this background execution context.
	•	No Secondary Token is passed to BatchDoc in this design.

⸻

5) Summary decision

✅ Cascading scope is applicable only for synchronous ingest requests where the LOB Primary Token exists and already contains all required scopes.
❌ Cascading scope is not possible for cron/rescheduler retry processing because the LOB Primary Token is unavailable and cannot be stored.

⸻

If you want, I can also add a mini flow diagram (ASCII / Confluence-friendly) showing:
	•	Ingest: LOB token exists → forward it
	•	Rescheduler: no LOB token → PingFed token → forward it

Just say “add diagram” and I’ll paste it.