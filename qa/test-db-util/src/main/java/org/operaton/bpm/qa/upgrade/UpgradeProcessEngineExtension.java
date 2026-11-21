/*
 * Copyright 2026 the Operaton contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.qa.upgrade;

import org.junit.jupiter.api.extension.ExtensionContext;

import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.diagnostics.PlatformDiagnosticsRegistry;
import org.operaton.bpm.engine.impl.test.TestHelper;
import org.operaton.bpm.engine.impl.util.ClockUtil;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

import static java.util.Objects.requireNonNull;

/**
 * {@link ProcessEngineExtension} for tests that run against a database prepared by a test fixture.
 *
 * <p>
 * Unlike its parent, it does not clear the user operation log after each test: the fixtures
 * create user operation log entries that the tests read, and all test classes share the
 * same database. Otherwise, the clean-up after each test is the same as in the parent.
 * </p>
 */
public class UpgradeProcessEngineExtension extends ProcessEngineExtension {

  public UpgradeProcessEngineExtension() {
  }

  public UpgradeProcessEngineExtension(String configurationResource) {
    this.configurationResource = configurationResource;
  }

  @Override
  public void afterEach(ExtensionContext context) {
    getIdentityService().clearAuthentication();

    final String testMethod = context.getRequiredTestMethod().getName();
    final Class<?> testClass = context.getRequiredTestClass();

    ProcessEngineConfigurationImpl configuration = requireNonNull(processEngineConfiguration);
    try {
      configuration.setTenantCheckEnabled(false);
      TestHelper.annotationDeploymentTearDown(processEngine, deploymentId, testClass, testMethod);
      deploymentId = null;
      for (String additionalDeployment : additionalDeployments) {
        TestHelper.deleteDeployment(processEngine, additionalDeployment);
      }
      additionalDeployments.clear();
    } finally {
      configuration.setTenantCheckEnabled(true);
    }

    TestHelper.resetIdGenerator(configuration);
    ClockUtil.reset();
    PlatformDiagnosticsRegistry.clear();

    // the user operation log is kept: it holds entries created by the test fixtures

    if (ensureCleanAfterTest) {
      TestHelper.assertAndEnsureCleanDbAndCache(processEngine);
    }
    if (closeEngineEachTest) {
      getProcessEngine().close();
    }
  }
}
