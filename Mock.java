Good 👍 this is where you need to be very precise so nobody misinterprets your design.

Right now your section title says:

“How token propagation works in the real-time (Ingest) flow”

But in reality, you are only propagating Primary Token, not Secondary.

So don’t leave ambiguity. Fix it cleanly like this:

⸻

2) How Token Propagation Works in the Real-Time (Ingest) Flow

Flow:

LOB → DGVLM → BatchDoc (+ DGVLA / Kafka-related actions)

In the ingest call:
	•	LOB sends a Primary Token (JWT) to DGVLM.
	•	DGVLM forwards the same Primary Token when calling:
	•	BatchDoc API
	•	DGVLA API (if applicable)
	•	The Primary Token contains all required scopes to:
	•	Call BatchDoc
	•	Call DGVLA
	•	Perform Kafka-related operations as per authorization design
	•	No Secondary Token is generated or passed in this flow.

Result:

Ingest flow supports cascading behavior because the LOB Primary Token is available in-request and already includes required scopes.

⸻

Now, if you want to be even sharper (and architect-level clean), you can add one small clarification line:

Although BatchDoc may support Primary + Secondary token model, DGVLM currently operates using Primary Token only.

That removes confusion permanently.

⸻

Your mistake earlier was subtle:
The section header implied both tokens are propagated.
But your implementation is strictly Primary-only propagation.

Fix that wording and your design becomes solid and defensible in architecture review 💪

If you want, I can also help you tighten the Rescheduler section so security reviewers cannot question it.