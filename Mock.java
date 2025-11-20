Here’s the clean, sharp explanation you’re looking for.
Nothing fluffy. Just the real reason and the right way to explain it.

⸻

Problem

Multiple modules in the AESIG ecosystem (common-util, common-dal, OSS-token, AESIG-API) were all declaring their own <parent> as aesig-parent, and each module was also adding extra shared dependencies (like common-util, common-dal) manually into the lower modules.

This created three risks:
	1.	Version drift
Example: AESIG-API upgrades aesig-parent to a newer version, but OSS-token still uses an older aesig-parent.
Now AESIG-API gets two different dependency versions:
	•	new ones from its own parent
	•	old ones transitively from OSS-token
	2.	Duplicate dependency paths
Same libraries were coming from:
	•	AESIG-parent
	•	common-util
	•	common-dal
	•	OSS-token
Causing conflicts and unpredictable resolution.
	3.	Inconsistent upgrades
Every time you upgrade Spring Boot or a shared dependency in aesig-parent, you must manually upgrade every child module.
If one module misses updating its parent version, AESIG-API inherits outdated libraries.

⸻

Solution

We simplified the dependency hierarchy:
	•	AESIG-API should inherit dependencies only from aesig-parent, not from common-util or common-dal.
	•	Modules should not re-declare dependencies already managed by parent.
	•	OSS-token should also inherit the latest aesig-parent, and AESIG-API should rely on OSS-token only for its functional logic, not for dependency management.

This ensures:
	•	One source of truth for all Spring and library versions
	•	Zero version drift
	•	No duplicate dependency chains
	•	Clean dependency resolution in AESIG-API
	•	Upgrading parent automatically upgrades the entire stack

⸻

Two-line version for your documentation

Problem:
Multiple modules were independently using aesig-parent and also importing each other’s shared libraries, creating version drift and duplicate dependency paths. This caused Spring and Tomcat to resolve different versions from different modules.

Solution:
We removed unnecessary direct dependencies and ensured AESIG-API inherits everything only through a clean hierarchy where aesig-parent is the single source of truth. This prevents version conflicts, simplifies upgrades, and guarantees consistent dependency management across all modules.

⸻

If you want, I can also create the full dependency hierarchy diagram or a Confluence-ready table summarizing each issue and fix.