# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

Operaton is a BPMN 2.0 / DMN process engine for the JVM, a fork of Camunda 7 (packages are `org.operaton.bpm`, not
`org.camunda.bpm`). This file replaces upstream's `AGENTS.md` and Copilot instructions, which were removed from this
branch; upstream's `TESTING.md` and `CONTRIBUTING.md` are still accurate references.

## This checkout

- Workspace layout and shared rules (upstream/downstream, push rules, topic folders) are in `..\CLAUDE.md`.
- This folder is a git worktree of `..\upstream` on the local branch **`2.2.2-orange`**, created from the upstream
  release tag **`v2.2.2`** (commit `76a6873d`), not from upstream `main`. The `origin` remote is upstream; fetch only. The `fork` remote
  (`https://github.com/esmo-pme/operaton-psi.git`) is the only allowed push target, branch `2.2.2-orange` only,
  never `main`.
- Project version is **`2.2.2-orange.3`** (all POMs and `package.json` files). Change it only with
  `.devenv/scripts/maintenance/set-project-version.sh <version>` from Git Bash, then run `.\mvnw.cmd validate`.
- Upstream maintains `release/2.2.x` after 2.2.2. Check it (and `main`) for an existing fix before writing one.
- Record every deviation from `v2.2.2` in `FORK-PATCHES.md` (reason, upstream issue, proving test, removal condition).
  Review notes, findings and reproducers live in `..\Review\`, not in this repository.
- Versions: trust the root `pom.xml` (for `v2.2.2`: Spring Boot 4.1.3, Spring 7.0.11, Quarkus 3.40.1.2, MyBatis 3.5.19,
  Liquibase 4.33.0) and `parent/pom.xml` (WildFly 41.0.1.Final). Upstream docs list older versions.
- Toolchain: JDK 25 (Temurin) and Maven 3.10.1 through the wrapper. Keep `maven.compiler.release=17`.

## Build

Run from the repository root with `JAVA_HOME` set to JDK 25 (`./mvnw` in Git Bash, `.\mvnw.cmd` in PowerShell).

```
.\mvnw.cmd clean install                                     # full build incl. tests and frontend (~15-20 min)
.\mvnw.cmd clean install -DskipTests -Dskip.frontend.build=true   # fast build (~5-8 min)
.\mvnw.cmd clean install -pl parent,bom,commons,model-api,engine-dmn,juel -DskipTests   # engine prerequisites
.\mvnw.cmd clean install -pl engine -DskipTests              # one module, after its prerequisites
.\mvnw.cmd <args> -rf :operaton-engine                       # resume a failed reactor build
.\mvnw.cmd clean install -DskipTests -pl '!qa'               # skip QA modules (they download browsers/drivers)
```

- "package org.operaton.bpm.dmn.engine does not exist" means the engine prerequisites are not installed.
- Out of memory: `MAVEN_OPTS=-Xmx4g -XX:MaxMetaspaceSize=1g`.
- In PowerShell, quote any `-D` argument whose name contains a dot (`'-Dtest.includes=bpmn'`); unquoted, PowerShell
  splits it and Maven fails with "Unknown lifecycle phase".
- `.mvn/maven.config` applies to every wrapper build: `-Duser.timezone=Europe/Berlin`,
  `-Djdk.xml.elementAttributeLimit=200` (required on Java 25 for `BpmnParseTest`), `--batch-mode`, and the Maven
  build cache disabled (opt in with `-Dmaven.build.cache.enabled=true`). Tests run in Europe/Berlin unless overridden.
- `install` writes `org.operaton:*:2.2.2-orange.3` into `~/.m2/repository`, next to (not over) the released 2.2.2
  artifacts. Reproducers in `..\Review\reproducers` use released 2.2.2 by default; test a local build with
  `'-Doperaton.version=2.2.2-orange.3'`.
- The WildFly modules (`distro/wildfly/*`, incl. the subsystem and its tests) are only in the reactor with the
  profile `distro-wildfly`, which is not active by default. Naming a profile explicitly switches off all
  `activeByDefault` profiles, including `distro`, which holds the normal module list. So: subsystem tests alone with
  `.\mvnw.cmd test -Pdistro-wildfly -pl distro/wildfly/subsystem`; the WildFly archive with
  `.\mvnw.cmd clean install '-Pdistro,distro-wildfly' -DskipTests -pl distro/wildfly/distro -am` (quote the comma
  in PowerShell).
- The `validate-wildfly-modules` step of `distro/wildfly/modules` runs `bash validate-modules.sh`, which needs
  `xmllint`. On this machine plain `bash` is the WSL launcher (no distribution installed); `xmllint` comes from MSYS2
  (`C:\msys64\ucrt64\bin`, package `mingw-w64-ucrt-x86_64-libxml2`). For WildFly builds put Git Bash and MSYS2 first
  in `PATH` for that PowerShell session only:
  `$env:PATH = 'C:\Users\pmetz\AppData\Local\Programs\Git\bin;C:\msys64\ucrt64\bin;' + $env:PATH`.
  (Fallback without them: `'-Dexec.skip=true'` and check the module `resource-root` paths by hand.)
- The WildFly archive is `distro/wildfly/distro/target/operaton-bpm-wildfly-<version>.tar.gz`; the handoff to
  deployment (`..\Artifacts\`) is described in `..\Review\PLAN.md`. Check line endings of archive files with `file`,
  not `grep $'\r'` (Git Bash's grep hides carriage returns); `.gitattributes` keeps `distro/**`, `*.sh` and `*.conf`
  LF.
- Run module tests one module per Maven call: `ArchRulesTest` (from `test-utils/archunit`) imports with
  `DoNotIncludeJars`, so when model-api modules are in the same `-pl` reactor their `target/classes` are scanned too
  and it fails on model-api packages.
- The engine runs its tests in 4 JVMs (`surefire.forkCount=4` in `engine/pom.xml`; `-Dsurefire.forkCount=N` overrides
  it, the `database` profile sets 1). Test classes must not depend on each other's order or leftovers: a class that
  leaves engines, deployments or cache entries behind breaks another class in the same JVM. Keep the `**/*$*`
  exclude (otherwise `@Nested` classes run twice and lose their outer state) and do not name runnable test classes
  `*TestCase` (excluded). Do not fork `engine-rest` (its servers use the fixed port 38080).
- LDAP plugin tests (`engine-plugins/identity-ldap`) are skipped unless `-Pdocker` is given (Testcontainers starts
  `osixia/openldap`); run them after any change to the plugin.
- Helper scripts under `.devenv/scripts` are `.sh`; run them from Git Bash or WSL. GitHub CI (`.github/`) was removed
  from this branch; do not look for workflows. `.mvn/` is required by the wrapper and must stay.
- `qa/test-db-instance-migration/test-fixture-725` pins the released `2.2.2` engine on purpose (it creates the
  upgrade fixture); do not bump it to the downstream version.

## Tests

- Single class or method: `.\mvnw.cmd test -pl engine '-Dtest=BoundaryTimerEventTest'` or
  `'-Dtest=BoundaryTimerEventTest#testMultipleTimersOnUserTask'` (prerequisites installed first). With `-am`, also pass
  `'-Dsurefire.failIfNoSpecifiedTests=false'`.
- Without `-Dtest`, the engine POM selects tests by the regex `.*(${test.includes}).*Test.*` (`*TestCase.java` is
  excluded): `'-Dtest.includes=bpmn'`, `'-Dtest.excludes=bpmn.async'`, or the profiles `-PtestBpmn` /
  `-PtestExceptBpmn`. All engine tests: about 5 min (`test -pl engine`, 4 test JVMs since 2026-11-21; 9 min with one).
- Test output goes to files, not the console: `engine/target/surefire-reports/<TestClass>-output.txt`.
- Engine tests use `engine/src/test/resources/operaton.cfg.xml`: database from the `database` module profile (default
  `h2-in-memory`, `jdbc:h2:mem:operaton`), schema auto-created, history `full`, **`jobExecutorActivate=false`**. Jobs
  and timers do not run by themselves; tests execute them explicitly (`managementService.executeJob(id)`) and control
  time with `ClockUtil.setCurrentTime(...)`, resetting it afterwards.
- `@Deployment` without resources deploys `<package>/<TestClass>.<testMethod>.bpmn20.xml` (or `<TestClass>.bpmn20.xml`
  at class level) from the test's package.
- New tests: JUnit 5 with `@RegisterExtension ProcessEngineExtension` and AssertJ assertions; Mockito for mocks. Test
  support ships in the engine artifact: `engine/src/main/junit5` and `engine/src/main/junit-shared` (`@Deployment`,
  `TestHelper`), published as classifier `junit6` (identical copy: `junit5`).
- JUnit 4 is removed from this fork (2026-11-21): no JUnit 4 API (`ProcessEngineRule`, `DmnEngineRule`,
  `starter-test`), no vintage engine (Surefire and Failsafe exclude it), and the Enforcer rule `ban-junit4` in the
  root `pom.xml` fails the build if `junit:junit` comes back. The only allowed exception is the WildFly subsystem,
  whose third-party test framework needs the jar (its tests run on Jupiter via `AbstractJupiterSubsystemBaseTest`).
- Other databases: profiles in `database/pom.xml`, see `TESTING.md` (for example
  `-Pdatabase,postgresql -Ddatabase.url=...`, or `-Ppostgresql,testcontainers`). The upstream PostgreSQL profile uses
  PostgreSQL 14.
- Container integration tests live in `qa/`; compose profiles as `-P<testsuite>,<runtime>,<database>`, e.g.
  `-Pengine-integration,wildfly,h2` or `-Pwebapps-integration,wildfly,postgresql` (engine-integration supports only H2
  and PostgreSQL), or use `.devenv/scripts/build/build-and-run-integration-tests.sh --distro=wildfly --db=postgresql`.

## Engine internals worth knowing before a fix

- Every service call runs as a `Command` (`impl/cmd`) through the `CommandExecutor` interceptor chain
  (`impl/interceptor`). `CommandContextInterceptor` opens the `CommandContext`; entity changes are collected in
  `DbEntityManager` (`impl/db/entitymanager`) and flushed when the command completes, not when an entity changes.
- BPMN execution: the parser builds the process virtual machine model (`impl/pvm`), and each BPMN element's runtime
  behaviour is an `ActivityBehavior` in `impl/bpmn/behavior`.
- Persistence is MyBatis: statement XML per entity in
  `engine/src/main/resources/org/operaton/bpm/engine/impl/mapping/entity/` (registered in `mapping/mappings.xml`).
  Schema scripts per database are under `engine/src/main/resources/org/operaton/bpm/engine/db/` (`create`, `drop`,
  `upgrade`, `liquibase`). A schema change here breaks the upgrade path from upstream releases; avoid it unless agreed.
- Jobs and timers: job executor in `impl/jobexecutor`, acquisition in `impl/cmd/AcquireJobsCmd` with SQL in
  `mapping/entity/Job.xml`, timer due dates from the business calendars in `impl/calendar` (`CycleBusinessCalendar`
  for cycles and cron).
- CMMN is still compiled in and wired into the engine and distributions; do not remove it.
- Frontends: `webapps-neo` (Preact, current; see `webapps-neo/frontend/CLAUDE.md`) and the legacy AngularJS
  `webapps` (bug fixes only; `npm install`, `npm start`, `npm run lint`, `npm run prettier` in `webapps/frontend`).

## Code style

- Java: 2-space indent, 120 columns, braces on the same line. Keep diffs focused; no formatting-only changes.
- Import order (`rewrite.yml`): `java.*`, `jakarta.*`, blank line, others alphabetically, blank line, `org.operaton.*`,
  blank line, static imports. `.devenv/scripts/maintenance/code-cleanup.sh` applies the OpenRewrite recipes (~12 min).
- Engine core must not import Spring; use Operaton utilities (for example
  `org.operaton.bpm.engine.impl.util.StringUtil.hasText`, which rejects null and empty but not whitespace-only).
- New files get the Apache 2.0 header from `CONTRIBUTING.md` with the current year
  (`Copyright <year> the Operaton contributors.`).
- Commits: Conventional Commits, `<type>(<scope>): <subject>`, scopes such as `engine`, `engine-rest`, `webapps`.
- When taking a fix from another branch or project (upstream `release/2.2.x`/`main`, Camunda 7, CIB seven), adapt
  `org.camunda.bpm` to `org.operaton.bpm`, migrate JUnit 4 to JUnit 5/AssertJ, keep the original license header, and
  attribute it in the commit body:

  ```
  Related to <source issue or PR URL>

  Backported commit <hash> from <source repository>.
  Original author: <name> <email>
  ```
