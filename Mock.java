Here is the short, crisp, no-nonsense Problem / Solution pair for the MainApplicationRoute cleanup.

⸻

Cleanup of Main Application Route Class

Problem:
The MainApplicationRoute class was overloaded with filter registrations, excessive annotations, and duplicated configuration logic, making it hard to maintain and violating separation of concerns.

Solution:
Refactored the class by moving all filter registrations into a dedicated FilterChain configuration class and removing unnecessary annotations, resulting in a cleaner, modular, and more maintainable application entry point.

⸻

If you want, I can rewrite this in the exact tone used in your Confluence rows.