Alright — I went through your DB diagram + both Java classes carefully.
I’m going to be very direct so you don’t carry a hidden bug into prod.

⸻

✅ First: Your DB design (correct)

From your diagram:

PK: (INGEST_TXN_ID, BLOCK_NUM)
FK: INGEST_TXN_ID → STOR_INGEST_TXN

✔ One-to-many (txn → chunks)
✔ Composite PK (txn + block)
✔ Resume capability supported

So DB is perfectly aligned with your requirement 👍

⸻

⚠️ Now the problem: Your JPA mapping is incomplete

You currently have:

@EmbeddedId
private FileTrackingId id;

BUT you are missing the relationship mapping.

👉 Right now Hibernate does NOT know how this table connects to StorTransaction.

⸻

❌ What’s missing (critical)

You need this:

@MapsId("ingestTxnId")
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "INGEST_TXN_ID", nullable = false)
private StorTransaction transaction;


⸻

Why this is important (don’t skip this)

Without @MapsId:
	•	JPA treats INGEST_TXN_ID in ID separately
	•	And FK relationship separately
	•	This leads to:
	•	duplicate column mapping issues
	•	weird insert/update bugs
	•	Hibernate confusion

With @MapsId, you tell Hibernate:

INGEST_TXN_ID in PK == FK to parent


⸻

✅ Correct final entity (fix this)

Your class should look like this:

@Entity
@Table(name = "STOR_INGEST_FILE_TRACKING")
@Data
@NoArgsConstructor
public class StorIngestFileTracking {

    @EmbeddedId
    private FileTrackingId id;

    @MapsId("ingestTxnId")   // 🔥 critical
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGEST_TXN_ID", nullable = false)
    private StorTransaction transaction;

    @Column(name = "HASH")
    private String hash;

    @Column(name = "STATUS")
    private String status;

    @Lob
    @Column(name = "FILE_BLOCK")
    private byte[] fileBlock;
}


⸻

✅ Your ID class (this part is fine)

@Embeddable
@Data
public class FileTrackingId implements Serializable {

    @Column(name = "INGEST_TXN_ID", nullable = false)
    private String ingestTxnId;

    @Column(name = "BLOCK_NUM")
    private int blockNum;
}

✔ This is correct
✔ Matches DB PK
✔ Nothing wrong here

⸻

⚠️ Small improvement (important)

Change:

private int blockNum;

to:

private Integer blockNum;

Why?

👉 int = cannot be null → can cause issues during entity lifecycle
👉 Integer = safer for JPA

⸻

⚠️ Another issue: columnDefinition = "VARBINARY"

You wrote:

@Column(name = "FILE_BLOCK", columnDefinition = "VARBINARY")

But your DB is Oracle, not SQL Server.

👉 Oracle uses:

BLOB

So fix it:

@Lob
@Column(name = "FILE_BLOCK")
private byte[] fileBlock;

Remove columnDefinition.

⸻

⚠️ Parent side (very important for consistency)

In StorTransaction.java, you should have:

@OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
private List<StorIngestFileTracking> fileChunks = new ArrayList<>();


⸻

✔ Final alignment check

Layer	Status
DB schema	✅ Correct
Composite PK	✅ Correct
Resume logic	✅ Supported
ID class	✅ Correct
Entity mapping	❌ Missing @MapsId
BLOB mapping	⚠️ Needs fix


⸻

🧠 Bottom line

👉 Kiruthika’s structure is conceptually correct
👉 But implementation is missing one critical JPA piece (@MapsId)

Without that, you’ll eventually hit:
	•	weird persistence bugs
	•	FK mismatch issues
	•	duplicate column mapping errors

⸻

If you want next step

I can help you with:

✔ Repository queries for resume logic
✔ Batch insert optimization (very important for chunks)
✔ Retry-safe upload flow design
✔ Confluence-ready diagram for this whole flow

Just tell me 👍