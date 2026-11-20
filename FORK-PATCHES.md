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
  setting a downstream version (adapted in the next entry).
- **Kept on purpose:** `.mvn/` (Maven wrapper pinning 3.10.1, `maven.config` with the Java 25 XML limit and test time
  zone, build-cache extension), `.devenv/clirr-ignore.xml` (used by the `check-api-compatibility` profile in
  `parent/pom.xml`), and the build, code-cleanup, smoke-test and tool scripts under `.devenv/scripts/`.
- **Why:** the branch is built locally; GitHub CI, releases and IDE connected-mode settings are not used.
- **Verified:** `.\mvnw.cmd -N validate` still succeeds with Maven 3.10.1 on JDK 25. Upstream docs (`CONTRIBUTING.md`,
  `TESTING.md`, `.devenv/scripts/README.md`) still mention some removed files.
- **Drop when:** never needed; keep them deleted when taking a newer upstream base.

### Version `2.2.2-orange.1` (2026-11-20)

- **Changed:** project version `2.2.2` → `2.2.2-orange.1` in every reactor POM (`versions:set`), the non-reactor
  `distro/run` POMs, `engine-rest/docs` and `webapps/frontend` `package.json`, and the root entries of their
  `package-lock.json` (dependency entries untouched; `node_modules/ent` stays at its own `2.2.2`).
- **Not changed on purpose:** `qa/test-db-instance-migration/test-fixture-725/pom.xml` keeps
  `operaton.version.current=2.2.2`: it builds the 7.25 upgrade fixture with the released upstream engine, so the
  suite tests an upgrade from upstream 2.2.2 to downstream.
- **Script adapted** (`.devenv/scripts/maintenance/set-project-version.sh`): in-place edits with `perl -pi` instead of
  BSD `sed -i ''`; versions matched literally (`\Q...\E`), not as a regex; `-DgenerateBackupPoms=false`; without npm,
  only the root version of each `package-lock.json` is changed; the jreleaser changelog step and the unused
  next-version calculation removed; the leftover check matches the exact old version.
- **Why:** a local `install` no longer overwrites the released `org.operaton:*:2.2.2` in `~/.m2`, and archives
  (`operaton-bpm-wildfly-2.2.2-orange.1.tar.gz`, `*-2.2.2-orange.1.war`) can be told apart from upstream's. The
  engine treats the version as an opaque string (`ProductPropertiesUtil`, `ParseUtil`, Spring Boot
  `OperatonBpmVersion`); nothing parses it as `x.y.z`.
- **Verified:** `.\mvnw.cmd validate` succeeds for all 130 reactor projects at `2.2.2-orange.1`.
- **Drop when:** never; set the next number (`2.2.2-orange.2`) for each build handed to deployment, and a new base
  version (for example `2.2.3-orange.1`) when taking a newer upstream tag.

### `.gitignore` additions (2026-11-20)

- **Added** (marked block at the end of the root `.gitignore`): H2 2.x database files `*.mv.db` and `*.trace.db`
  (upstream only ignores the H2 1.x `*.h2.db`), `*.orig` / `*.rej` left by cherry-picks and backports, and
  `.claude/settings.local.json` (Claude Code personal settings).
- **Verified:** `git check-ignore` matches each pattern; `git ls-files -ci --exclude-standard` shows no tracked file
  affected.
- **Drop when:** never needed; re-apply on a newer upstream base unless upstream adds the same patterns.

## Code fixes

### F-2: `OptimisticLockingException` instead of NPE when a process instance vanishes (2026-11-20)

- **Changed:** `ExecutionEntity.ensureExecutionTreeInitialized` (execution tree prefetch enabled, the default) and
  `ExecutionEntity.ensureProcessInstanceInitialized` (prefetch disabled) throw an `OptimisticLockingException`
  (`ENGINE-03111`, new `EnginePersistenceLogger.processInstanceDeletedConcurrently`) when the process instance of an
  already loaded execution was deleted by a concurrent transaction. Before, both paths ended in a
  `NullPointerException`; the job executor then decremented retries and finally created an incident for a job whose
  process instance no longer exists. With the OLE, `JobRetryCmd` does not decrement retries and the failed-job
  handling skips the vanished job.
- **Upstream:** issue [#4017](https://github.com/operaton/operaton/issues/4017), open PR
  [4018](https://github.com/operaton/operaton/pull/4018) (commits `bc1bc01543` and `5e46da064b` by shardtVLH), taken
  over unchanged; not merged upstream on 2026-11-20. Findings log: `..\Review\FINDINGS.md` F-2.
- **Not covered (as in the PR):** `ensureParentInitialized` with prefetch *disabled* can still return a `null` parent
  in the same race; with the default (prefetch enabled) it goes through the fixed `ensureExecutionTreeInitialized`.
- **Test:** `engine/src/test/java/org/operaton/bpm/engine/test/concurrency/CompetingProcessInstanceDeletionTest.java`
  (from the PR; 4 cases: direct load and full job execution, each with prefetch on and off). Without the fix all 4
  fail with the NPEs from the issue; with the fix all 4 pass (H2, JDK 25). Full engine suite with the fix: 16230
  tests, 0 failures, 0 errors, 89 skipped.
- **Drop when:** upstream merges PR 4018 (or an equivalent fix) into the base we use; then take upstream's version.
