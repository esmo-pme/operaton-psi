/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.operaton.bpm.engine.test.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Architecture rules, run in every module that lists this artifact in Surefire's {@code dependenciesToScan}.
 * Imports the module's own classes only (no jars, no tests).
 */
public class ArchRulesTest {

  static final ArchRule PUBLIC_API_SHOULD_NOT_EXPOSE_IMPLEMENTATION = classes().that()
      .areInterfaces().and().resideOutsideOfPackages("..impl..")
      .and().areNotAssignableTo("org.operaton.bpm.engine.variable.type.ValueType")
      // downstream: existing public SPI that returns the parser from an impl package (upstream design, Review
      // FINDINGS L-14). The rule never ran in model-api before (no JUnit 4 on its class path); kept as is.
      .and().doNotHaveFullyQualifiedName("org.operaton.bpm.model.bpmn.BpmnParserFactory")
      .and().doNotHaveFullyQualifiedName("org.operaton.bpm.model.cmmn.CmmnParserFactory")
      .and().doNotHaveFullyQualifiedName("org.operaton.bpm.model.dmn.DmnParserFactory")
      .should().onlyDependOnClassesThat().resideOutsideOfPackages("..impl..")
      .because("Interfaces that don't reside in an `impl` package are considered public API.");

  @Test
  void publicApiShouldNotExposeImplementation() {
    JavaClasses classes = new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeJars())
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPackages("org.operaton.bpm");

    PUBLIC_API_SHOULD_NOT_EXPOSE_IMPLEMENTATION.check(classes);
  }

}
