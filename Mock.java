You’re thinking in the right direction — the key is observability for Ops. The question is how much detail the API should expose vs what should just be handled internally.

Let’s break it down honestly.

⸻

1️⃣ Your scenario

Input:

txnIds = [A, A, A, A]

Current behavior if you deduplicate silently:

requestedTxnCount = 4
successCount = 1
notFoundCount = 0

Ops might think:

4 requested
1 success
0 notFound
→ what happened to the other 3?

So yes — your concern is valid.

⸻

2️⃣ The clean solution (best practice)

Instead of returning duplicateTxnIds, return a duplicateCount.

This keeps the API clean but still explains the discrepancy.

Example response:

{
  "requestedTxnCount": 4,
  "processedTxnCount": 1,
  "duplicateCount": 3,
  "successCount": 1,
  "notFoundCount": 0,
  "successTxnIds": ["A"],
  "notFoundTxnIds": []
}

This tells Ops everything:

4 received
3 duplicates
1 processed

No confusion.

⸻

3️⃣ Why NOT return duplicateTxnIds list

Returning a full list like:

duplicateTxnIds: [A, A, A]

is noisy and not very useful. Ops already knows the ID.

A count is enough.

⸻

4️⃣ How to implement duplicate detection

Very simple.

List<String> inputTxnIds = request.getTxnsToReprocess();

Set<String> uniqueTxnIds = new LinkedHashSet<>(inputTxnIds);

int duplicateCount = inputTxnIds.size() - uniqueTxnIds.size();

List<String> requestedTxnIds = new ArrayList<>(uniqueTxnIds);


⸻

5️⃣ Update your response

AdminIngestRs response = new AdminIngestRs();

response.setRequestedTxnCount(inputTxnIds.size());
response.setProcessedTxnCount(requestedTxnIds.size());
response.setDuplicateCount(duplicateCount);
response.setSuccessCount(successIds.size());
response.setNotFoundCount(notFoundIds.size());
response.setSuccessTxnIds(successIds);
response.setNotFoundTxnIds(notFoundIds);


⸻

6️⃣ Example outcomes

Case 1

Input:

[A, B, C]

Response:

requested = 3
duplicate = 0
success = 3
notFound = 0


⸻

Case 2

Input:

[A, B, A, C]

Response:

requested = 4
duplicate = 1
success = 3
notFound = 0


⸻

Case 3

Input:

[A, A, A, A]

Response:

requested = 4
duplicate = 3
success = 1
notFound = 0

Ops immediately understands.

⸻

7️⃣ Final recommendation

Your response model should be:

requestedTxnCount
processedTxnCount
duplicateCount
successCount
notFoundCount
successTxnIds
notFoundTxnIds

Clean and operationally clear.

⸻

8️⃣ One more improvement (very important for Ops APIs)

You should limit request size:

max 500 txnIds per request

Otherwise someone can send 10,000 IDs and your API will hurt the DB.

⸻

✅ Final answer

Yes — you should handle duplicates.
But expose only:

duplicateCount

Not duplicateTxnIds.

⸻

If you want, I can also show you one serious bug that can occur in your current bulk update logic (it can cause success responses even when nothing was updated). It’s a subtle but common issue in APIs like this.