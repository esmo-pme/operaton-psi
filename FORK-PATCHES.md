# Changes on branch 2.2.2-orange

Folder: `C:\Users\pmetz\repos\Operaton\downstream`. Base: upstream tag `v2.2.2` (commit `76a6873d`). The branch stays
local and is never pushed. "Upstream" is the official Operaton project, "downstream" is this local copy.

Each entry: what changed, why, upstream issue (if any), the test that proves it, and when the change can be dropped.

## Repository housekeeping

### Agent instructions replaced by `CLAUDE.md` (2026-11-20)

- **Added:** `CLAUDE.md` (Claude Code instructions for this branch) and this file.
- **Removed:** `AGENTS.md`, `.github/copilot-instructions.md`, `.github/agents/dependency-upgrade.agent.md`, and the
  upstream Claude skills `.claude/skills/operaton-adr/` and `.claude/skills/operaton-release/`.
- **Changed:** `webapps-neo/frontend/CLAUDE.md`: removed the "Session start" section, which told agents to compare with
  upstream `main` and suggest pulling.
- **Why:** Copilot is no longer used; the useful build, test and style rules from `AGENTS.md` were folded into
  `CLAUDE.md`, the rest was stale or about upstream's process. The ADR skill posts to an external Etherpad and opens
  upstream issues, and the release skill covers upstream publishing; neither applies to a local branch.
- **Upstream issue:** none. **Test:** none (no code change).
- **Drop when:** never needed; when taking a newer upstream base, re-apply these deletions or resolve the conflicts by
  keeping them deleted.

## Code fixes

None yet.
