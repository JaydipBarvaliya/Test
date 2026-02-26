// Step 1: Lock a batch of rows that are in ERROR state.
//
// We use "FOR UPDATE SKIP LOCKED" so that if multiple schedulers
// run at the same time (maybe on different JVMs), they do not
// pick the same rows.
//
// The lock makes sure that once this scheduler selects these rows,
// no other scheduler can select them at the same time.


List<String> ids = repo.lockErrorTransactions()
        .stream()
        .limit(batchSize)
        .toList();

if (!ids.isEmpty()) {

    // Step 2: Change status from ERROR -> ACTIVE.
    //
    // The lock is temporary and will be released when this
    // transaction finishes (commit).
    //
    // If we do not change the status, then after the lock is released,
    // another scheduler can pick the same rows again because they
    // are still in ERROR state.
    //
    // So:
    // - Lock prevents two schedulers from picking the same row at the same time.
    // - Changing status prevents the same row from being picked again later.
    
    repo.markActive(ids);
}

// Transaction commits here
return ids;