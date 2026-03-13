Good you clarified this. Because the relationship design completely changes depending on that assumption. Let’s slow down and reason properly for a minute.

You said:

each transaction in STOR_INGEST_TXN can have only one file in tracking table

If that is guaranteed by design, then yes — the relationship is One-to-One.

But the structure you showed earlier included BLOCK_NUM, which strongly suggests file chunking / block uploads. That normally means multiple rows per transaction. So I want you to double-check that assumption with your implementation.

Two possible architectures exist.

⸻

Case 1 (Most likely): File is uploaded in blocks

Relationship: One-to-Many

STOR_CONFIG
     │
     │
     ▼
STOR_INGEST_TXN
     │
     │
     ▼
STOR_INGEST_FILE_TRACKING

Example data:

INGEST_TXN_ID	BLOCK_NUM	STATUS
TXN1	1	SUCCESS
TXN1	2	SUCCESS
TXN1	3	SUCCESS

That means:

1 transaction → many file blocks

Entity mapping:

@OneToMany(mappedBy = "transaction")
List<StorIngestFileTracking> trackingEntries;

This is what your earlier schema looked like.

⸻

Case 2 (What you are describing now)

Relationship: One-to-One

Meaning:

1 transaction → 1 tracking row

Example:

INGEST_TXN_ID	HASH	STATUS
TXN1	8a7c…	SUCCESS

Then BLOCK_NUM should not exist, because it serves no purpose.

⸻

If your design is truly One-to-One

Then the tracking table should look like this.

Table structure

STOR_INGEST_FILE_TRACKING
----------------------------------
INGEST_TXN_ID   VARCHAR2(36) PK/FK
HASH            VARCHAR2(50)
STATUS          VARCHAR2(50)
FILE_BLOCK      BLOB

Notice something important:

INGEST_TXN_ID is BOTH
PRIMARY KEY
FOREIGN KEY

This enforces true one-to-one relationship.

⸻

Rebuild Script (One-to-One)

-- Drop FK
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_FILE_TRACKING DROP CONSTRAINT FK_FILE_TRK_INGEST_TXN';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN
            RAISE;
        END IF;
END;
/

-- Drop PK
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_FILE_TRACKING DROP CONSTRAINT PK_STOR_INGEST_FILE_TRACKING';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN
            RAISE;
        END IF;
END;
/

-- Drop table
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE STOR_INGEST_FILE_TRACKING CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN
            RAISE;
        END IF;
END;
/

-- Create table
CREATE TABLE STOR_INGEST_FILE_TRACKING (
    INGEST_TXN_ID VARCHAR2(36 CHAR) NOT NULL,
    HASH          VARCHAR2(50 BYTE),
    STATUS        VARCHAR2(50 BYTE) NOT NULL,
    FILE_BLOCK    BLOB
);

-- PK
ALTER TABLE STOR_INGEST_FILE_TRACKING
ADD CONSTRAINT PK_STOR_INGEST_FILE_TRACKING
PRIMARY KEY (INGEST_TXN_ID);

-- FK with cascade delete
ALTER TABLE STOR_INGEST_FILE_TRACKING
ADD CONSTRAINT FK_FILE_TRK_INGEST_TXN
FOREIGN KEY (INGEST_TXN_ID)
REFERENCES STOR_INGEST_TXN (INGEST_TXN_ID)
ON DELETE CASCADE;

Now when your scheduler deletes:

DELETE FROM STOR_INGEST_TXN

Oracle will automatically delete the tracking row.

No service logic needed.

⸻

Java Entity Mapping (One-to-One)

Parent (StorTransaction)

Add:

@OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
private StorIngestFileTracking fileTracking;


⸻

Child (StorIngestFileTracking)

@Entity
@Table(name = "STOR_INGEST_FILE_TRACKING")
@Data
@NoArgsConstructor
public class StorIngestFileTracking {

    @Id
    @Column(name = "INGEST_TXN_ID")
    private String ingestTxnId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "INGEST_TXN_ID")
    private StorTransaction transaction;

    @Column(name = "HASH")
    private String hash;

    @Column(name = "STATUS")
    private String status;

    @Lob
    @Column(name = "FILE_BLOCK")
    private byte[] fileBlock;
}

The key piece here is:

@MapsId

This means the child shares the same primary key as the parent.

⸻

Repository change?

None needed.

Your current cleanup query is perfect once the FK has cascade.

DELETE FROM stor_ingest_txn

Oracle will delete:

STOR_INGEST_TXN
        │
        ▼
STOR_INGEST_FILE_TRACKING

automatically.

⸻

One last thing (important)

Before implementing One-to-One, answer this honestly:

Are you ever going to store file chunks or retry blocks?

Because your earlier table design included:

BLOCK_NUM
FILE_BLOCK

That smells like chunked upload tracking, which is not one-to-one.

If that happens later, you will regret the One-to-One schema.

⸻

✅ If you want, I can also show you the production-grade schema I would personally use at TD for this system, which handles:
	•	chunk uploads
	•	retries
	•	partial failures
	•	scheduler cleanup safely

It’s slightly different from what you designed and much safer.