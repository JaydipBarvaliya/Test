Here is the crisp Problem / Solution pair for moving from field injection to constructor injection.

⸻

Migration from Field Injection to Constructor Injection

Problem:
Field injection made the code harder to test, hid required dependencies, and caused potential null-initialization issues since Spring cannot enforce dependency completeness at compile time.

Solution:
Replaced field injection with constructor injection so that all required dependencies are clearly defined, immutable, and validated at object creation time, improving testability, maintainability, and overall code quality.

⸻

If you want it rewritten to match your Confluence tone exactly, I can tune it.