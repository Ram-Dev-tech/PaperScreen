# AGENTS.md: PaperScreen Project Memory

## PaperScreen Architecture Rules
- Version: 1.0.0
- Do NOT implement MediaProjection-based global screen filtering, screen capture -> processing -> overlay loops, or hidden SurfaceFlinger/ColorDisplayManager APIs.
- Do NOT use AccessibilityService as a disguised screen filter, root exploits, or security bypasses.
- PaperScreen acts as an Android HOME Launcher. The #D8D6CF / #444444 two-tone transformation is implemented ONLY where PaperScreen owns the rendering surface.
- The actual filter engine is an AGSL RuntimeShader applied via a Compose `PaperEnvironment`.

## Multi-Agent Rules
- Do not allow multiple agents to edit the same files simultaneously.
- Specialist roles: Lead Agent, UI/UX Agent, Android Platform Agent, Icon Engine Agent, QA/Test Agent, Performance Agent, Security/Privacy Agent, Paper Content Engine Agent.
- Lead Agent owns final integration.

## Android Limitations
- Strict constraints on unrooted Android 15 (HyperOS). Background services and overlays face extreme restrictions.

## Coding Rules
- Do not artificially increase code size. Prefer simple, maintainable, production-quality code.
- Never put secrets into source code.

## Git / GitHub Workflow
- Before significant changes: `git status`, `git diff`
- After every meaningful completed milestone:
  1. Build
  2. Test
  3. Inspect git diff
  4. Update PROJECT_STATE.md
  5. Update TASK_BOARD.md
  6. Commit with a descriptive message
  7. Push to GitHub if authorized
- Never commit passwords, API keys, private credentials, signing keys, keystores, local.properties, .env files, private certificates.
- Never force-push or delete Git history.
- Never reset the project to an earlier state without explicit instruction.

## Testing Rules
- Maintain a buildable project at every major milestone.

## Security Rules
- No security bypasses or root exploits.

> **IMPORTANT**: Before performing development work, every agent must read the project-memory files and inspect the current Git state.
