Yeah that makes sense — local will always be a bit tricky with multiple certs.
If we rely on JWKS + kid matching as primary, that should be more reliable anyway.
For local, maybe we can iterate through all available certs instead of assuming one?
That would remove the “latest only” limitation.