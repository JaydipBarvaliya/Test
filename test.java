Ah yes — that’s a very common issue when pasting Markdown tables or formatted text from ChatGPT into Confluence.
Confluence (especially the TD on-premise or Atlassian Cloud instance) doesn’t render Markdown formatting directly when pasted. It strips table and emoji formatting into plain text.

Let’s fix that for you 👇

⸻

✅ Here are your three options (ranked easiest → most flexible):

Option 1 – Use Confluence Table Format (Ready-to-Paste Version)

Below is the same section you showed but rewritten in Confluence table syntax (so when you paste it into Confluence’s editor, it’ll render perfectly).

⸻

⚙️ Behavior Comparison (Confluence Table-Ready)

Scenario	Current Behavior	After Change	Impact
Accept: application/json	✅ 200 OK	✅ 200 OK	None
Accept: text/plain	✅ 200 OK	❌ 406 Not Acceptable	Must use JSON
No Accept header	Defaults to text/plain	Defaults to application/json	Verify consumer parsing logic


⸻

Option 2 – Use the Confluence Markdown Macro

If your space supports the Markdown macro, do this instead:
	1.	In Confluence, type /markdown → select the “Markdown” macro.
	2.	Paste the Markdown content inside it, like this:

| Scenario | Current Behavior | After Change | Impact |
|-----------|------------------|--------------|---------|
| `Accept: application/json` | ✅ 200 OK | ✅ 200 OK | None |
| `Accept: text/plain` | ✅ 200 OK | ❌ 406 Not Acceptable | Must use JSON |
| No `Accept` header | Defaults to `text/plain` | Defaults to `application/json` | Verify consumer parsing logic |

✅ The table will render perfectly formatted inside the macro.

⸻

Option 3 – Copy as “Code Block” Style (for docs or design drafts)

If you prefer the table to appear like code (non-editable visual reference, often used in TD internal design Confluence pages):

Paste this as a code block using /code macro in Confluence:

Scenario                     Current Behavior     After Change           Impact
---------------------------------------------------------------------------------
Accept: application/json      200 OK              200 OK                 None
Accept: text/plain            200 OK              406 Not Acceptable     Must use JSON
No Accept header              text/plain          application/json       Verify consumer parsing logic

It will render aligned like a readable table inside a bordered code block — which is very common for design review pages.

⸻

💡 My Suggestion

For technical design pages, use the Confluence table format (Option 1).
For release notes / change control tickets, use code block format (Option 3) because it’s easier to copy from Confluence into JIRA tickets.

⸻

Would you like me to reformat all four endpoint documents into Confluence table format (Option 1) so you can paste each directly without losing alignment?
I can do that for all pages with perfect spacing and header alignment.