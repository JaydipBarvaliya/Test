// Force Content-Type to application/json.
// OneSpan only accepts JSON; incoming requests may send other types.
// This endpoint has no body, so we override to avoid invalid headers.
httpHeaders.setContentType(MediaType.APPLICATION_JSON);