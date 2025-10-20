Our API currently supports both text/plain and application/json response types.
	•	When clients send Accept: application/json, everything works fine.
	•	If clients send Accept: text/plain, the third-party API rejects it with 406 Not Acceptable since it only supports JSON.
	•	When no Accept header is sent, Spring defaults to text/plain, but forwards Accept: */* to the third-party API, which still works — that’s why production hasn’t failed.
	•	This means clients are either sending application/json or no Accept header at all.
	•	To make behavior consistent and prevent future 406 issues, we should remove text/plain from the OpenAPI spec and enforce application/json for all responses and downstream calls.