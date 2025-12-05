Perfect. No diagrams, no overkill.
I’ll keep it tight, clean, and strictly focused on the final design text you need for the Confluence page.

Below is the final design content, ready to paste into Confluence, and aligned 100 percent with your last screenshot + all your reasoning.

⸻

Reject Attachment – New Design (Final Version)

Overview

This update simplifies the Reject Attachment operation by removing unnecessary fields, reducing client payload complexity, and shifting the heavy lifting to AESIG.
Instead of requiring the client to provide the full attachment list, AESIG now fetches all attachments from OneSpan, applies only the requested updates, and sends the full updated list back to OneSpan.

This avoids the previous scaling issues where OneSpan required the entire attachment array even when only one attachment was being rejected.

⸻

Existing Behavior (Old Design Issues)
	1.	If all attachments changed status to REJECTED or COMPLETE, description was not required.
	2.	For individual rejection among multiple attachments, description was required.
	3.	If no description was provided, OneSpan applied optional rules inconsistently.
	4.	All properties (name, description, required flag, etc.) were required even for simple rejection.
	5.	The client had to include all attachments in the payload, even when rejecting one.

This created unnecessary complexity for LOBs, clients, and development teams.

⸻

New Behavior (Final Simplified Rules)

What client sends (minimal input):

Client only sends:
	•	attachmentId (mandatory)
	•	status (mandatory, allowed: REJECTED or COMPLETE)
	•	commentTxt (optional)

Nothing else is required.

What AESIG does:
	1.	Fetch all attachments from OneSpan for the package.
	2.	Validate incoming items:
	•	Each attachmentId must exist in the OneSpan list.
	•	IDs must be unique.
	3.	Update only the attachments client wants to modify.
	4.	Keep everything else as-is.
	5.	Send the entire updated attachment list back to OneSpan.
	6.	Return success to the client.

⸻

Final Field Rules

Mandatory
	•	attachmentId
	•	status
	•	Allowed values: REJECTED, COMPLETE

Optional
	•	commentTxt

Removed From Request Payload
	•	name
	•	description
	•	required flag

These fields either never change or exist only for OneSpan’s internal verification, so they are no longer part of AESIG request payload.

⸻

Reverse Reject (Un-Reject)

The endpoint now supports reversing a reject operation.
If the client sends:

"status": "COMPLETE"

AESIG updates the attachment back to a non-rejected state.
We keep the same endpoint name (/attachments/reject) because the action is determined entirely by the status field.

⸻

New Request Body (Final)

This is the new, official request payload:

{
  "attachmentRequirements": [
    {
      "commentTxt": "",
      "attachmentId": "{{attachmentID1}}",
      "status": "REJECTED"
    },
    {
      "commentTxt": "",
      "attachmentId": "{{attachmentID2}}",
      "status": "COMPLETE"
    }
  ]
}

This supports:
	•	single reject
	•	multi-reject
	•	reverse reject
	•	mixed operations in one request

⸻

Validation Rules

AESIG returns HTTP 400 when:
	•	Attachment ID does not exist in OneSpan list
	•	Attachment ID not found: <id>
	•	Duplicate IDs appear in request
	•	Duplicate attachment ID detected: <id>
	•	Status is anything other than REJECTED or COMPLETE

⸻

Error Handling

AESIG directly returns OneSpan exceptions as-is.
No masking, no transformation, no retry logic added in this scope.

⸻

Outcome

This new design:
	•	Removes unnecessary fields
	•	Removes OneSpan complexity from client
	•	Makes AESIG responsible for merging and updating
	•	Supports reverse reject
	•	Simplifies validation
	•	Allows clean, minimal payloads
	•	Works efficiently even when packages have hundreds or thousands of attachments

⸻

If you want, I can also format this in a perfect Confluence-style structure (headings, spacing, code blocks, bullet formatting exactly as Confluence renders).