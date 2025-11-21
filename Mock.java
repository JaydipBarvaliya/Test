Here are your three problems and their clean, sharp two–three line explanations.
Use these directly in your documentation.

⸻

✅ 1. Version Drift Between Parent Versions

When AESIG-API directly uses AESIG-Parent but OSS-Token still uses an older parent version, the API ends up inheriting two different parent versions: one directly and one transitively through OSS-Token. This creates version drift, inconsistent Spring Boot versions, and unpredictable build behavior.

⸻

✅ 2. Duplicate / Conflicting Dependencies

Because AESIG-API was pulling parent, util, and DAL both directly and indirectly through OSS-Token, the project often ended up with duplicate dependencies, conflicting versions, and clashing transitive trees. This produced Maven warnings, IntelliJ red marks, runtime risks, and unnecessary complexity.

⸻

✅ 3. Incorrect Util Usage Without DAL Validation

Previously, OSS-Token could import Common-Util directly. A util change that worked in OSS-Token could still break Common-DAL, causing inconsistent behavior across the ecosystem. To avoid this, we enforced a clean hierarchy: Parent → Util → DAL → Token → API, ensuring DAL validates every util change before OSS-Token or any API consumes it.

⸻

If you want, I can format this into a Confluence-ready paragraph or a bullet-style design decision section.