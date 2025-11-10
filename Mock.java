paths:
  /esignatureevents/{eventId}/parties/{partyKey}/signs:
    post:
      summary: Apply signature to event
      operationId: signs
      parameters:
        - name: eventId
          in: path
          required: true
          schema:
            type: string
        - name: partyKey
          in: path
          required: true
          schema:
            type: string
        - name: Accept
          in: header
          required: true
          schema:
            type: string
            enum: [application/json]
          description: >
            Must be `application/json`. OneSpan integration supports only JSON accept type.
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AddSignsRq'
      responses:
        '200':
          description: Successfully applied signature
          content:
            application/json:
              schema:
                type: object