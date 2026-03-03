Yes — if configured properly in /etc/fstab, the mount will persist across server restarts and patching cycles.

Here’s the explanation you can copy-paste into Teams 👇

⸻

If we configure the NAS mount in /etc/fstab, then the OS will automatically mount the drive during boot. That means after a server restart or patching cycle, the mount will come back up without us having to manually run any script.

As long as:
	•	The fstab entry is correct
	•	The credentials file exists (if we use one)
	•	The NAS is reachable over the network

the system will re-establish the mount when the server starts.

We can also include _netdev in the fstab options so the system waits for the network before attempting to mount. That prevents boot issues if the network isn’t ready yet.

So yes — with a proper fstab configuration, the mount is persistent and survives reboot and patching.

⸻

If you want, I can also give you a slightly more technical version depending on who you’re sending it to.