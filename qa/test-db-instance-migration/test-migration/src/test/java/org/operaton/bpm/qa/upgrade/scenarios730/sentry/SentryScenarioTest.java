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
package org.operaton.bpm.qa.upgrade.scenarios730.sentry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.CaseService;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.operaton.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor;
import org.operaton.bpm.engine.runtime.CaseInstance;
import org.operaton.bpm.qa.upgrade.Origin;
import org.operaton.bpm.qa.upgrade.ScenarioUnderTest;
import org.operaton.bpm.qa.upgrade.UpgradeTestExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Roman Smirnov
 *
 */
@ScenarioUnderTest("SentryScenario")
@Origin("7.24.0")
public class SentryScenarioTest {

  @RegisterExtension
  public UpgradeTestExtension rule = new UpgradeTestExtension();

  protected CaseService caseService;

  @BeforeEach
  public void setUp() {
    ProcessEngine processEngine = rule.getProcessEngine();
    caseService = processEngine.getCaseService();
  }

  @Test
  @ScenarioUnderTest("newSentryInstance.1")
  public void testNewSentryInstance() {
    // given
    String secondStageId = rule.caseExecutionQuery().activityId("PI_Stage_2").singleResult().getId();

    // when
    // start human task
    caseService.manuallyStartCaseExecution(secondStageId);

    // then
    // a new sentry instance should be created
    CaseSentryPartEntity sentryPart = createCaseSentryPartQuery()
      .caseExecutionId(secondStageId)
      .singleResult();
    assertThat(sentryPart).isNotNull();
    assertThat(sentryPart.isSatisfied()).isFalse();
    assertThat(sentryPart.getSourceCaseExecutionId()).isNull();
    assertThat(sentryPart.getSource()).isEqualTo("PI_HumanTask_1");
  }

  @Test
  @ScenarioUnderTest("completeInstance.1")
  public void testCompleteInstance() {
    // given

    // when
    String firstHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_1").singleResult().getId();
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    String secondHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_2").singleResult().getId();
    caseService.manuallyStartCaseExecution(secondHumanTaskId);
    caseService.completeCaseExecution(secondHumanTaskId);

    String secondStageId = rule.caseExecutionQuery().activityId("PI_Stage_2").singleResult().getId();
    caseService.manuallyStartCaseExecution(secondStageId);

    firstHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_1").singleResult().getId();
    caseService.manuallyStartCaseExecution(firstHumanTaskId);
    caseService.completeCaseExecution(firstHumanTaskId);

    secondHumanTaskId = rule.caseExecutionQuery().activityId("PI_HumanTask_2").singleResult().getId();
    caseService.manuallyStartCaseExecution(secondHumanTaskId);
    caseService.completeCaseExecution(secondHumanTaskId);

    // then
    CaseInstance caseInstance = rule.caseInstance();
    assertThat(caseInstance).isNotNull();
    assertThat(caseInstance.isCompleted()).isTrue();

    caseService.closeCaseInstance(caseInstance.getId());
    assertThat(rule.processInstanceQuery().singleResult()).isNull();
  }

  // queries /////////////////////////////////

  protected CaseSentryPartQueryImpl createCaseSentryPartQuery() {
    ProcessEngine processEngine = rule.getProcessEngine();
    ProcessEngineConfigurationImpl processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequiresNew();
    return new CaseSentryPartQueryImpl(commandExecutor);
  }

}
