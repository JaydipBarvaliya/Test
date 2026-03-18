Good instinct 👍 — you’re thinking about performance. But don’t swing back into the same trap.

Let’s be very clear:

✅ Caching config = good
❌ Mutating shared fields per request = bad

⸻

🔥 What you should do instead

You can store both URLs, just not like this:

this.batchDocUrl = ...

That’s the problem.

⸻

✅ Correct pattern (safe + fast)

👉 Load once, store immutable values

private String defaultBatchDocUrl;
private String tdiClaimsDevBatchDocUrl;

@PostConstruct
public void init() {
    this.defaultBatchDocUrl = configurationProperties.getConfigProperty(
        ApiConstants.DEFAULT,
        ApiConstants.BATCHDOC_URL
    );

    this.tdiClaimsDevBatchDocUrl = configurationProperties.getConfigProperty(
        ApiConstants.TDI_CLAIMS_DEV,
        ApiConstants.BATCHDOC_URL
    );
}


⸻

👉 Then just SELECT, not MODIFY

private String resolveBatchDocUrl(String lobId) {
    if (ApiConstants.TDI_CLAIMS_DEV.equals(lobId)) {
        return tdiClaimsDevBatchDocUrl;
    }
    return defaultBatchDocUrl;
}


⸻

👉 Usage

String batchDocUrl = resolveBatchDocUrl(lobId);


⸻

💣 Why this works
	•	✅ No mutation per request
	•	✅ Thread-safe
	•	✅ No repeated DB/config calls
	•	✅ Fast lookup (just if/else)

⸻

⚠️ What you were about to do (don’t do this)

You were heading toward:
	•	“store one variable and keep switching it”

That’s:
	•	❌ shared mutable state
	•	❌ race condition
	•	❌ incorrect behavior

⸻

🧠 Think of it like this

Instead of:

“What is the current URL?”

Think:

“Given this request, which URL should I use?”

That mindset shift fixes the design.

⸻

🚀 Slightly better (scalable if more LOBs come)

If tomorrow you get 5 LOBs:

private Map<String, String> batchDocUrlMap;

@PostConstruct
public void init() {
    batchDocUrlMap = Map.of(
        ApiConstants.DEFAULT, configurationProperties.getConfigProperty(...),
        ApiConstants.TDI_CLAIMS_DEV, configurationProperties.getConfigProperty(...)
    );
}

Then:

private String resolveBatchDocUrl(String lobId) {
    return batchDocUrlMap.getOrDefault(lobId, batchDocUrlMap.get(ApiConstants.DEFAULT));
}


⸻

🧠 Final verdict (no sugarcoating)
	•	Your concern about repeated fetching = valid 👍
	•	Your original approach = unsafe ❌
	•	The correct solution = cache once, never mutate

⸻

If you want, next step I’d recommend:
👉 Move this into a dedicated UrlResolver or ConfigService so your gateway stays clean.