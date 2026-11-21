/*
 * Copyright 2026 the Operaton contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.container.impl.jboss.test;

import org.jboss.as.controller.Extension;
import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs WildFly's {@link AbstractSubsystemBaseTest} checks on JUnit Jupiter.
 *
 * <p>
 * downstream: the WildFly subsystem test framework declares its tests and lifecycle with JUnit 4 annotations. This
 * fork runs no tests on JUnit 4 (no vintage engine), so the inherited methods are called from Jupiter methods here.
 * The JUnit 4 jar stays on the test class path only as a dependency of the WildFly framework.
 * </p>
 */
public abstract class AbstractJupiterSubsystemBaseTest extends AbstractSubsystemBaseTest {

  protected AbstractJupiterSubsystemBaseTest(String mainSubsystemName, Extension mainExtension) {
    super(mainSubsystemName, mainExtension);
  }

  @BeforeEach
  void initializeSubsystemTest() throws Exception {
    super.initializeParser();
  }

  @AfterEach
  void cleanupSubsystemTest() throws Exception {
    super.cleanup();
  }

  @Test
  @Override
  public void testSubsystem() throws Exception {
    super.testSubsystem();
  }

  @Test
  @Override
  public void testSchema() throws Exception {
    super.testSchema();
  }
}
