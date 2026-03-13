Yes — if your Ops team will actually use that information, then returning the duplicate IDs is perfectly reasonable. Admin APIs often expose more diagnostic detail than normal public APIs. The key is to keep the response structure clean and predictable.

Let’s design it properly so it doesn’t become messy later.

⸻

1️⃣ Recommended Response Structure

Add two new fields:
	•	duplicateCount
	•	duplicateTxnIds

Final response model:

{
  "requestedTxnCount": 4,
  "successCount": 1,
  "notFoundCount": 0,
  "duplicateCount": 3,
  "successTxnIds": ["A"],
  "notFoundTxnIds": [],
  "duplicateTxnIds": ["A", "A", "A"]
}

This makes it crystal clear for Ops.

⸻

2️⃣ Update Your OpenAPI Schema

Add these fields.

duplicateCount:
  type: integer
  description: Number of duplicate transaction IDs detected in request.

duplicateTxnIds:
  type: array
  description: List of duplicate transaction IDs received in request.
  items:
    type: string


⸻

3️⃣ Detect Duplicates in Service

You should detect duplicates before any DB query.

Example:

List<String> inputTxnIds = request.getTxnsToReprocess();

Set<String> uniqueTxnIds = new LinkedHashSet<>();
List<String> duplicateTxnIds = new ArrayList<>();

for (String id : inputTxnIds) {
    if (!uniqueTxnIds.add(id)) {
        duplicateTxnIds.add(id);
    }
}

List<String> requestedTxnIds = new ArrayList<>(uniqueTxnIds);

Now you have:

inputTxnIds      → original request
requestedTxnIds  → unique IDs for DB processing
duplicateTxnIds  → duplicates


⸻

4️⃣ Continue with Bulk Processing

Then run your bulk logic on requestedTxnIds only.

SELECT existing IDs
UPDATE FAILURE → ERROR

Exactly like we discussed earlier.

⸻

5️⃣ Build Response

AdminIngestRs response = new AdminIngestRs();

response.setRequestedTxnCount(inputTxnIds.size());
response.setSuccessCount(successIds.size());
response.setNotFoundCount(notFoundIds.size());
response.setDuplicateCount(duplicateTxnIds.size());

response.setSuccessTxnIds(successIds);
response.setNotFoundTxnIds(notFoundIds);
response.setDuplicateTxnIds(duplicateTxnIds);


⸻

6️⃣ Example Scenarios

Case 1 — duplicates

Input:

[A, B, A, C]

Response:

requestedTxnCount = 4
duplicateCount = 1
successTxnIds = [A, B, C]
duplicateTxnIds = [A]


⸻

Case 2 — heavy duplicate

Input:

[A, A, A, A]

Response:

requestedTxnCount = 4
duplicateCount = 3
successTxnIds = [A]
duplicateTxnIds = [A, A, A]


⸻

7️⃣ Logging (important)

Also log duplicates:

if (!duplicateTxnIds.isEmpty()) {
    log.warn("Duplicate txnIds detected in admin ingest request: {}",
             LogSanitizeUtil.sanitizeLogObj(duplicateTxnIds));
}

This helps debugging later.

⸻

8️⃣ Final Advice

For admin/debug APIs, it is totally acceptable to expose:
	•	duplicates
	•	skipped IDs
	•	counts

because the audience is internal operations teams, not external clients.

Your approach is reasonable.

⸻

If you want, I can also show you one very clean response structure used in internal APIs at banks that makes debugging even easier (it avoids multiple arrays like success/notFound/duplicate and instead uses a single result object per txn).