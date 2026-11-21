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

### `.gitattributes`: LF where CRLF breaks the build or the Linux distribution (2026-11-20/21)

- **Added:** `.gitattributes` with `text=auto eol=lf` for `webapps/frontend/**`, `webapps-neo/frontend/**`,
  `engine-rest/docs/**`, `distro/**` and `webapps-neo/assembly/**`, and `text eol=lf` for `*.sh` and `*.conf`.
  Upstream has no `.gitattributes`.
- **Second step (2026-11-21):** the first archive built on this machine contained `start.sh` and WildFly
  `bin/standalone.conf` (from `distro/wildfly/assembly/resources/`) with CRLF; `standalone.conf` is sourced by
  `standalone.sh` on Linux, so the archive was not fit for a Linux container. Hence `distro/**`, `*.sh` and
  `*.conf`. `webapps-neo/assembly/**` fixes two tests that compare resource contents with LF strings
  (`QuerySessionFactoryTest.buildMappingsShouldBuildValidXml`, `TemplateFilterTest.getWebResourceContents...`).
  A rule for all files is not possible: 198 Java/BPMN/XML/XSD files are stored with CRLF or mixed endings and
  would show as modified.
- **Why:** on this Windows machine (`core.autocrlf=true`) the frontend sources were checked out with CRLF, and the
  legacy webapps build (`npm run build`, ESLint with `prettier/prettier`) failed with thousands of "Delete `␍`"
  errors, so the WildFly distribution could not be built. The repository stores these files as LF (checked: 1518
  LF, 63 binary, none CRLF or mixed), so the attribute changes only the checkout, not the content. Other files keep
  the machine's setting (e.g. `mvnw.cmd` stays CRLF).
- **Verified:** after re-checkout the folders are LF in the working tree and `git status` is clean apart from the new
  file; the WildFly distribution build passes the webapps frontend (see the build entry in
  `..\Review\FINDINGS.md` / `..\Artifacts\BUILD-INFO-*.md`).
- **Drop when:** never needed on Linux/macOS; keep it as long as downstream is built on Windows.

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

### F-3: failing `formKey` expression no longer breaks task lists (2026-11-20)

- **Changed:** `TaskEntity.initializeFormKeyFromTaskDefinition` catches an exception from evaluating a user task's
  `formKey` expression, logs a warning (`ENGINE-03112`, new `EnginePersistenceLogger.logFormKeyExpressionEvaluationException`)
  and leaves `formKey = null`. Before, one task with a failing expression (e.g. `${nonExistentVariable}`) made every
  `TaskQuery.initializeFormKeys()` call and every task filter list (the Tasklist path) fail, healthy tasks included.
  `operaton:formRef` handling is unchanged.
- **Behaviour change (accepted by the owner):** callers no longer get a `ProcessEngineException` for a broken
  form-key expression; `getFormKey() == null` now means either "no form key" or "evaluation failed" (only the log
  tells them apart). This is why upstream maintainers object to the PR.
- **Upstream:** draft PR [3110](https://github.com/operaton/operaton/pull/3110) (commit `d4e35353a2` by Julian Haupt,
  backported from Fluxnova commit `0e8e2ec390` by Sowmya, HL), taken over unchanged except the log code: the PR uses
  `111`, which downstream already uses for F-2, so downstream uses `112`. No upstream issue. Findings log:
  `..\Review\FINDINGS.md` F-3.
- **Test:** the PR's `TaskQueryTest.testInitializeFormKeyWithInvalidExpressionReturnsNullForSingleTask` and
  `...DoesNotBlockTaskListRetrieval` with `oneTaskWithInvalidFormKeyExpressionProcess.bpmn20.xml`: both fail without
  the fix (`ProcessEngineException: Unknown property used in expression`), pass with it. `TaskQueryTest` and all
  `Filter*Test`: 377 tests green. The filter path was checked with a temporary probe (not committed): filter list
  returns both tasks, the broken one with `formKey=null`. Full engine suite with the fix: 16232 tests, 0 failures,
  0 errors, 89 skipped.
- **Drop when:** upstream merges PR 3110 or an equivalent (possibly as an opt-in toggle; then switch to upstream's
  form and configure it); re-check the log code then.

### F-4: recognise SQL errors by code, not only by English message text (2026-11-20)

- **Changed:** `ExceptionUtil.checkValueTooLongException`, `checkConstraintViolationException` and
  `checkForeignKeyConstraintViolation` check SQLState and vendor codes first (`22001` and Oracle `12899`/`1461`/`1401`
  value too long; SQLState class `23` constraint violation; `23503`, `23506`, DB2 `-532`, SQL Server `547`, MySQL
  `1451`/`1452`, Oracle `2291`/`2292` foreign key) and keep the English message checks as fallback; messages are
  lowercased with `Locale.ROOT`, and a missing message no longer ends the check. Every check only gets broader.
- **Why:** with non-English database messages some errors were not recognised and surfaced as a generic
  `ProcessEnginePersistenceException` (no built-in error code, REST 500 instead of 400). On PostgreSQL 18.7 with
  `lc_messages = de_DE.UTF-8`, `22001: Wert zu lang für Typ character varying(3)` was not recognised as "value too
  long". With our default English messages nothing changes.
- **Upstream:** issue [#570](https://github.com/operaton/operaton/issues/570), open PR
  [4073](https://github.com/operaton/operaton/pull/4073) (commit `345d363be1` by Tim Zöller), taken over unchanged.
  Findings log: `..\Review\FINDINGS.md` F-4.
- **Test:** the PR's additions to `ExceptionUtilTest` (German messages per database, Turkish locale, no message):
  11 of 29 cases fail without the fix, all pass with it. Error-code, identity and variable tests (119 tests) green.
  Full engine suite with the fix: 16255 tests, 0 failures, 0 errors, 89 skipped.
- **Drop when:** upstream merges PR 4073 (or an equivalent) into the base we use.

### F-5: week durations and start/end intervals keep their exact length (2026-11-20)

- **Changed:** `DurationHelper` builds `PnW` as `n * 7` days and the period of a `start/end` interval as its exact
  elapsed time, instead of `DatatypeFactory.newDuration(long millis)`. That constructor turns milliseconds into
  years/months/days counted from 1970-01-01, so `P5W` became `P1M4D` and was 32 to 35 days long depending on the
  start date (`P52W` up to 367 days); start/end intervals repeated days off. `P5W` now behaves exactly like `P35D`.
- **Upstream:** no issue or PR (own fix; found by the code inspection). Findings log: `..\Review\FINDINGS.md` F-5.
- **Test:** `DurationHelperTest.shouldAddWeeksAsSevenDaysFromAnyStartDate`, `shouldRepeatWeeksAsSevenDaysFromAnyStartDate`,
  `shouldRepeatStartEndIntervalWithItsExactLength`: fail without the fix (e.g. 2026-03-05 instead of 2026-03-08),
  pass with it. Duration, calendar and timer tests (173) green. Full engine suite with F-5 to F-7: 16262 tests,
  0 failures, 0 errors, 89 skipped.
- **Drop when:** upstream fixes `DurationHelper` the same way.

### F-6: reject a cron expression that never matches (2026-11-20)

- **Changed:** `CronTimer.getDueDate` throws when cron-utils finds no next execution (it gives up after 100 years),
  instead of returning the start date. `CycleBusinessCalendar` turns that into `ENGINE-09026 Exception while parsing
  cycle expression '<expression>'`. Before, an expression such as `0 0 0 30 2 ?` (30 February) deployed fine, was due
  immediately and repeated endlessly (a timer start event started instances in a tight loop). Now such a deployment
  fails, and an existing timer with such an expression fails with an incident instead of looping.
- **Upstream:** no issue or PR (own fix). Findings log: `..\Review\FINDINGS.md` F-6.
- **Test:** `CycleBusinessCalendarTest.shouldRejectCronExpressionWithoutNextExecution` (2 cases) and
  `StartTimerEventTest.shouldRejectDeploymentOfCronCycleWithoutNextExecution`: fail without the fix, pass with it.
  Calendar, timer and cron tests (176) green. Full engine suite: see F-5.
- **Drop when:** upstream handles the missing next execution.

### F-7: job acquisition keeps `ROOT_PROC_INST_ID_` (2026-11-20)

- **Changed:** `Job.xml` `updateAcquirableJob` no longer writes `ROOT_PROC_INST_ID_`. The acquisition selects read
  that column only when acquiring exclusively over process hierarchies (off by default), so locking a job wrote NULL
  into it; queries by root process instance then missed every job that stayed in the table (retries left,
  incidents). Acquisition never changes the root process instance.
- **Upstream:** no issue or PR (own fix). Findings log: `..\Review\FINDINGS.md` F-7.
- **Test:** `AcquireJobsCmdTest.shouldKeepRootProcessInstanceIdWhenAcquiringJob` (job of a called subprocess, default
  setting): fails without the fix (`null` instead of the root id), passes with it. Job executor, job query and
  concurrency tests (347) green. Full engine suite: see F-5.
- **Drop when:** upstream fixes the acquisition update or select.

### F-9: WildFly job-executor pool uses its configured thread factory (2026-11-20)

- **Changed (WildFly subsystem):** `JobExecutorAdd` passes the installed `ThreadFactoryService` to the pool
  (`EnhancedQueueExecutorService.setThreadFactorySupplier`, used in `start`) and sets the name pattern `%G - %t`;
  `JobExecutorRemove` also removes the thread factory service. Before, the factory was installed but never used:
  job-executor threads were named `pool-N-thread-M` in the creating thread's group. Now they run in the group
  `Operaton BPM <pool name>` with names `Operaton BPM <pool name> - <n>` (thread dumps, monitoring). The threads'
  context class loader is unchanged (it is inherited from the creating thread with either factory).
- **Upstream:** no issue or PR (own fix). Findings log: `..\Review\FINDINGS.md` F-9.
- **Test:** `JBossSubsystemXMLTest.shouldRunJobExecutorThreadsWithConfiguredThreadFactory` (runs a task on the real
  MSC pool): fails without the fix (thread group `main`), passes with it. Subsystem tests (28) green, with
  `-Pdistro-wildfly`.
- **Drop when:** upstream wires the thread factory.

### F-10: LDAP user query by group pages again (2026-11-21)

- **Changed (`engine-plugins/identity-ldap`):** `LdapIdentityProviderSession.findUsersFromGroupMembers` applies
  `firstResult` and `maxResults` to the group's member list again (condition
  `userList.size() < maxResults && memberIndex >= firstResult`, as before upstream commit `eb8a88e2b1`, PR #2157,
  "reduce cognitive complexity", which dropped it). Before the fix `createUserQuery().memberOfGroup(..).listPage(..)`
  returned all members on every page and looked up every member in LDAP for each page. `addMembersToList` is
  unchanged: its skip does not consume elements, so the member list holds all members, as before the refactoring.
- **Upstream:** no issue or PR (own fix; owner decision 2026-11-21). Findings log: `..\Review\FINDINGS.md` F-10.
- **Test:** `LdapUserQueryTest.testPaginationWithMemberOfGroup` (existing; runs only with `-Pdocker`, OpenLDAP via
  Testcontainers): fails without the fix (12 users instead of 2), passes with it.
- **Drop when:** upstream restores the paging.

### Test: `QuerySessionFactoryTest` passes on Windows (2026-11-21)

- **Changed (test only):** `webapps-neo/assembly` `QuerySessionFactoryTest.buildMappingsShouldBuildValidXml` expects
  each mapper line to end with `System.lineSeparator()` instead of `"\n"`. `QuerySessionFactory.buildMappings` ends
  lines with `%n` (the platform line separator), so the test failed on Windows and passed on Linux. Production code
  unchanged; the generated MyBatis XML is valid with either line ending.
- **Upstream:** no issue or PR (own change; owner decision 2026-11-21). Findings log: `..\Review\FINDINGS.md` O-5.
- **Test:** `webapps-neo/assembly`: 435 tests, 0 failures on Windows (before: 1 failure).
- **Drop when:** upstream makes the test or the code platform-independent.
