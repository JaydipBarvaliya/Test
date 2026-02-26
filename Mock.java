// Step 1: Atomically claim a batch of ERROR transactions using
// SELECT ... FOR UPDATE SKIP LOCKED.
//
// This ensures that if multiple scheduler instances (across JVMs)
// run concurrently, they cannot select and process the same rows.
// The lock guarantees exclusive ownership of these rows
// within this transaction while we transition their state.




// We do NOT rely on row locks to protect long-running processing.
// Locks are short-lived and only used to safely "claim" work.
// The ACTIVE state represents a durable ownership marker.