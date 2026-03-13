Yes, this should be done through database design first, not by trying to be clever in service code 😄

Because your cleanup is using a native bulk delete query, JPA cascade/orphanRemoval will not reliably save you here. Bulk/native deletes bypass normal entity lifecycle behavior.

What you should do

Best approach

Connect:
	•	STOR_CONFIG → STOR_INGEST_TXN using STOR_CONFIG_ID ✅ already done
	•	STOR_INGEST_TXN → STOR_INGEST_FILE_TRACKING using INGEST_TXN_ID ✅ new

And for the new FK, use:

ON DELETE CASCADE

That way, when scheduler deletes rows from STOR_INGEST_TXN, Oracle automatically deletes all matching rows from STOR_INGEST_FILE_TRACKING.

That is the cleanest solution. No extra repository cleanup query is needed.

⸻

1) Rebuild script for third table

Important correction first

In your screenshot, STOR_INGEST_TXN.INGEST_TXN_ID is:

VARCHAR2(36 CHAR)

But the tracking table screenshot shows:

VARCHAR(20)

That is wrong for FK relation.

If child table references parent INGEST_TXN_ID, datatype and length must match logically.
So the tracking table should use:

INGEST_TXN_ID VARCHAR2(36 CHAR)

not 20.

⸻

Recommended table design

Since one transaction can have multiple file blocks, this is one-to-many.

So the tracking table should have a composite primary key like:
	•	INGEST_TXN_ID
	•	BLOCK_NUM

That is better than making only INGEST_TXN_ID primary key, because that would allow only one row per transaction, which defeats file-block tracking.

⸻

Rebuild script: STOR_INGEST_FILE_TRACKING

-- =========================================================
-- Rebuild STOR_INGEST_FILE_TRACKING Table Script
-- =========================================================

-- 1) Drop FK to STOR_INGEST_TXN if it exists
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_FILE_TRACKING DROP CONSTRAINT FK_FILE_TRK_INGEST_TXN';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN   -- ORA-02443: constraint does not exist
            RAISE;
        END IF;
END;
/
 
-- 2) Drop PK if it exists
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE STOR_INGEST_FILE_TRACKING DROP CONSTRAINT PK_STOR_INGEST_FILE_TRACKING';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2443 THEN
            RAISE;
        END IF;
END;
/
 
-- 3) Drop table if it exists
BEGIN
    EXECUTE IMMEDIATE 'DROP TABLE STOR_INGEST_FILE_TRACKING CASCADE CONSTRAINTS';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -942 THEN    -- ORA-00942: table does not exist
            RAISE;
        END IF;
END;
/
 
-- 4) Recreate table
CREATE TABLE STOR_INGEST_FILE_TRACKING (
    INGEST_TXN_ID      VARCHAR2(36 CHAR)   NOT NULL,
    BLOCK_NUM          NUMBER(4,0)         NOT NULL,
    HASH               VARCHAR2(50 BYTE)   NULL,
    STATUS             VARCHAR2(50 BYTE)   NOT NULL,
    FILE_BLOCK         BLOB                NULL
);
 
-- 5) Recreate Primary Key
ALTER TABLE STOR_INGEST_FILE_TRACKING
ADD CONSTRAINT PK_STOR_INGEST_FILE_TRACKING
PRIMARY KEY (INGEST_TXN_ID, BLOCK_NUM);
 
-- 6) Recreate Foreign Key to STOR_INGEST_TXN
-- ON DELETE CASCADE is the key part here
ALTER TABLE STOR_INGEST_FILE_TRACKING
ADD CONSTRAINT FK_FILE_TRK_INGEST_TXN
FOREIGN KEY (INGEST_TXN_ID)
REFERENCES STOR_INGEST_TXN (INGEST_TXN_ID)
ON DELETE CASCADE;
 
-- 7) Optional index on STATUS if you search by status often
-- CREATE INDEX IDX_FILE_TRK_STATUS
-- ON STOR_INGEST_FILE_TRACKING (STATUS);
 
-- 8) Verify column order
SELECT column_name, column_id, nullable, data_type
FROM user_tab_columns
WHERE table_name = 'STOR_INGEST_FILE_TRACKING'
ORDER BY column_id;
 
-- 9) Verify constraints
SELECT constraint_name, constraint_type, status
FROM user_constraints
WHERE table_name = 'STOR_INGEST_FILE_TRACKING'
ORDER BY constraint_type, constraint_name;
 
-- 10) Verify indexes
SELECT index_name, uniqueness
FROM user_indexes
WHERE table_name = 'STOR_INGEST_FILE_TRACKING'
ORDER BY index_name;


⸻

2) Entity relationship changes in Java

This is not one-to-one.
This is:
	•	one StorTransaction
	•	many StorIngestFileTracking

So:
	•	parent side in StorTransaction → @OneToMany
	•	child side in tracking entity → @ManyToOne

⸻

StorTransaction.java changes

Add this field:

@OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
private List<StorIngestFileTracking> fileTrackingEntries = new ArrayList<>();

Full relevant snippet

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "STOR_INGEST_TXN")
@Data
@NoArgsConstructor
public class StorTransaction {

    @Id
    @Column(name = "INGEST_TXN_ID", nullable = false, updatable = false, length = 36)
    private String ingestTxnId;

    @Column(name = "LOB_ID", nullable = false)
    private String lobId;

    @Column(name = "TRACEABILITY_ID", nullable = false)
    private String traceabilityId;

    @Column(name = "DGVL_DRAWER_ID", nullable = false)
    private String dgvlDrawerId;

    @Column(name = "DGVL_FOLDER_ID", nullable = false)
    private String dgvlFolderId;

    @Column(name = "DGVL_FILE_TOKEN", nullable = false)
    private String dgvlFileToken;

    @Column(name = "DGVL_FILE_NAME")
    private String dgvlFileName;

    @Column(name = "NAS_PATH")
    private String nasPath;

    @Column(name = "STOR_FILE_ID")
    private String storFileId;

    @Column(name = "STOR_TXN_ID")
    private String storTxnId;

    @Column(name = "STOR_FILE_NAME")
    private String storFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private TxnStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATE", nullable = false)
    private TxnState state;

    @Column(name = "CREATION_DTTM", nullable = false)
    private OffsetDateTime creationDttm;

    @Column(name = "LAST_UPDATE_DTTM", nullable = false)
    private OffsetDateTime lastUpdateDttm;

    @Column(name = "RETRY_COUNT", nullable = false)
    private int retryCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STOR_CONFIG_ID", nullable = false)
    private StorConfig config;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<StorIngestFileTracking> fileTrackingEntries = new ArrayList<>();
}


⸻

New child entity: StorIngestFileTracking.java

Because your table has composite PK (INGEST_TXN_ID, BLOCK_NUM), use @Embeddable + @EmbeddedId.

StorIngestFileTrackingId.java

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StorIngestFileTrackingId implements Serializable {

    @Column(name = "INGEST_TXN_ID", nullable = false, length = 36)
    private String ingestTxnId;

    @Column(name = "BLOCK_NUM", nullable = false)
    private Integer blockNum;
}


⸻

StorIngestFileTracking.java

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STOR_INGEST_FILE_TRACKING")
@Data
@NoArgsConstructor
public class StorIngestFileTracking {

    @EmbeddedId
    private StorIngestFileTrackingId id;

    @MapsId("ingestTxnId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGEST_TXN_ID", nullable = false)
    private StorTransaction transaction;

    @Column(name = "HASH")
    private String hash;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Lob
    @Column(name = "FILE_BLOCK")
    private byte[] fileBlock;
}


⸻

3) Repository changes for delete cleanup

Best answer

If you add FK with ON DELETE CASCADE, then you do not need a second delete query for tracking table.

Your existing cleanup query can stay basically the same.

Existing query can remain

@Modifying
@Transactional
@Query(value = """
    DELETE FROM stor_ingest_txn
    WHERE status = 'SUCCESS'
      AND state = 'COMPLETE'
      AND last_update_dttm < SYSTIMESTAMP - :retentionDays
    """, nativeQuery = true)
int deleteExpiredTransactions(@Param("retentionDays") int retentionDays);

But honestly, this expression is a little sloppy for Oracle timestamp arithmetic.

Better Oracle-safe version

Use NUMTODSINTERVAL.

@Modifying
@Transactional
@Query(value = """
    DELETE FROM stor_ingest_txn
    WHERE status = 'SUCCESS'
      AND state = 'COMPLETE'
      AND last_update_dttm < SYSTIMESTAMP - NUMTODSINTERVAL(:retentionDays, 'DAY')
    """, nativeQuery = true)
int deleteExpiredTransactions(@Param("retentionDays") int retentionDays);

That is cleaner and more explicit.

⸻

Do you need another repository method?

If FK has ON DELETE CASCADE

No.
No extra child delete method needed.

If you do not use ON DELETE CASCADE

Then yes, you would need:
	1.	Find expired transaction IDs
	2.	Delete child rows from STOR_INGEST_FILE_TRACKING
	3.	Delete parent rows from STOR_INGEST_TXN

But that is worse design and more code for nothing.

⸻

4) Strong recommendation: do not rely on JPA cascade for this scheduler delete

This is the trap.

If you write:

@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)

that only helps when entities are deleted through EntityManager / repository entity operations, not when you run a native bulk SQL DELETE.

Your scheduler is deleting directly in SQL.
So DB-level cascade is the correct solution.

⸻

5) What exactly should you implement

Database

Add new table:
	•	STOR_INGEST_FILE_TRACKING

Add FK:
	•	FK_FILE_TRK_INGEST_TXN
	•	child INGEST_TXN_ID → parent STOR_INGEST_TXN.INGEST_TXN_ID
	•	with ON DELETE CASCADE

Use PK:
	•	(INGEST_TXN_ID, BLOCK_NUM)

⸻

Java

In StorTransaction

Add:

@OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
private List<StorIngestFileTracking> fileTrackingEntries = new ArrayList<>();

Create new entity
	•	StorIngestFileTracking
	•	StorIngestFileTrackingId

⸻

Repository

Keep only parent cleanup delete.

Preferred:

@Modifying
@Transactional
@Query(value = """
    DELETE FROM stor_ingest_txn
    WHERE status = 'SUCCESS'
      AND state = 'COMPLETE'
      AND last_update_dttm < SYSTIMESTAMP - NUMTODSINTERVAL(:retentionDays, 'DAY')
    """, nativeQuery = true)
int deleteExpiredTransactions(@Param("retentionDays") int retentionDays);

No separate delete for tracking table needed.

⸻

6) One more thing you should not ignore

Your old Confluence screenshot for tracking table says:

INGEST_TXN_ID VARCHAR(20)

That will bite you later.

Since main table uses:

VARCHAR2(36 CHAR)

fix the documentation and rebuild script now. Otherwise you’ll create mismatch between:
	•	DB schema doc
	•	actual DB
	•	Java entity
	•	FK compatibility

That kind of mismatch causes stupid production bugs later.

⸻

7) My blunt recommendation

Do this:
	•	Use DB FK with ON DELETE CASCADE
	•	Use one-to-many / many-to-one mapping
	•	Do not write manual child cleanup query unless absolutely necessary
	•	Fix tracking table INGEST_TXN_ID length to 36
	•	Use composite PK (INGEST_TXN_ID, BLOCK_NUM)

That is the right design for your case.

If you want, next I can give you a full Confluence-ready documentation page for this new third table and cleanup behavior, in the same style as your existing DGVLM schema page.


@Entity
@Table(name = "STOR_INGEST_FILE_TRACKING")
@Data
@NoArgsConstructor
public class StorIngestFileTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRACKING_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGEST_TXN_ID", nullable = false)
    private StorTransaction transaction;

    @Column(name = "BLOCK_NUM")
    private Integer blockNum;

    @Column(name = "HASH")
    private String hash;

    @Column(name = "STATUS")
    private String status;

    @Lob
    @Column(name = "FILE_BLOCK")
    private byte[] fileBlock;
}
