/esignatureevents/{eventId}:
  delete:
    summary: Delete one eSignature transaction
    parameters:
      - $ref: '#/components/parameters/eventId'
      - $ref: '#/components/parameters/lobId'
      - $ref: '#/components/parameters/messageId'
      - $ref: '#/components/parameters/traceabilityId'
    requestBody:
      required: false    # <— optional body; we're only using this to advertise allowed content types
      content:
        text/plain:      # allow this if a body is ever sent
          schema: { type: string }
        application/json:
          schema: { type: object, additionalProperties: false }
    responses:
      '200':
        content:
          text/plain:
            schema: { type: string, example: "Transaction Deleted." }