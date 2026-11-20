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

### GitHub, IDE and upstream-release files removed (2026-11-20)

- **Removed:** `.github/` (workflows, actions, issue/PR templates, labels, Dependabot, CODEOWNERS, CI scripts),
  `jreleaser.yml` (GitHub release tooling), `.devcontainer/`, `.sonarlint/`, `.devenv/ide/`,
  `.devenv/scripts/release/`, `.devenv/scripts/maintenance/update-slack-link.sh`,
  `.devenv/scripts/maintenance/update-nodejs-version.sh` (edits GitHub workflows), and the stray
  `.mvn/wrapper/.!94984!maven-wrapper.jar` (a leftover partial download; the wrapper uses `distributionType=only-script`).
- **Moved:** `.github/scripts/set-project-version.sh` to `.devenv/scripts/maintenance/set-project-version.sh`, kept for
  setting a downstream version. Before use, adapt it: it still updates `.github/jreleaser/changelog.tpl` (deleted) and
  uses BSD `sed -i ''`, which GNU sed in Git Bash does not accept.
- **Kept on purpose:** `.mvn/` (Maven wrapper pinning 3.10.1, `maven.config` with the Java 25 XML limit and test time
  zone, build-cache extension), `.devenv/clirr-ignore.xml` (used by the `check-api-compatibility` profile in
  `parent/pom.xml`), and the build, code-cleanup, smoke-test and tool scripts under `.devenv/scripts/`.
- **Why:** the branch is built locally; GitHub CI, releases and IDE connected-mode settings are not used.
- **Verified:** `.\mvnw.cmd -N validate` still succeeds with Maven 3.10.1 on JDK 25. Upstream docs (`CONTRIBUTING.md`,
  `TESTING.md`, `.devenv/scripts/README.md`) still mention some removed files.
- **Drop when:** never needed; keep them deleted when taking a newer upstream base.

## Code fixes

None yet.
