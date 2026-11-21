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
package org.operaton.bpm.qa.upgrade.scenarios720.job;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.management.JobDefinition;
import org.operaton.bpm.engine.runtime.Job;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.qa.upgrade.Origin;
import org.operaton.bpm.qa.upgrade.ScenarioUnderTest;
import org.operaton.bpm.qa.upgrade.UpgradeTestExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ScenarioUnderTest("AsyncSequentialMultiInstanceScenario")
@Origin("7.24.0")
public class AsyncSequentialMultiInstanceScenarioTest {

  @RegisterExtension
  public UpgradeTestExtension rule = new UpgradeTestExtension();

  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.1")
  public void testInitAsyncBeforeSubprocessCompletion() {
    // given
    Job asyncJob = rule.jobQuery().singleResult();

    // when
    rule.getManagementService().executeJob(asyncJob.getId());

    // then the process can be completed successfully
    for (int i = 0; i < 3; i++) {
      Task subProcessTask = rule.taskQuery().singleResult();
      assertThat(subProcessTask).isNotNull();
      rule.getTaskService().complete(subProcessTask.getId());
    }

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.3")
  public void testInitAsyncBeforeSubprocessDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  /**
   * Note: this test is not really isolated since the job
   * definition is migrated when the process definition is accessed the first time.
   * This might happen already before this test case is executed.
   */
  @Test
  @ScenarioUnderTest("initAsyncBeforeSubprocess.4")
  public void testInitAsyncBeforeSubprocessJobDefinition() {
    // when the process is redeployed into the cache (instantiation should trigger that)
    rule.getRuntimeService().startProcessInstanceByKey("AsyncBeforeSequentialMultiInstanceSubprocess");

    // then the old job definition referencing "miSubProcess" has been migrated
    JobDefinition asyncJobDefinition = rule.jobDefinitionQuery().singleResult();
    assertThat(asyncJobDefinition.getActivityId()).isEqualTo("miSubProcess#multiInstanceBody");
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.1")
  public void testInitAsyncBeforeTaskCompletion() {
    // given
    Job asyncJob = rule.jobQuery().singleResult();

    // when
    rule.getManagementService().executeJob(asyncJob.getId());

    // then the process can be completed successfully
    for (int i = 0; i < 3; i++) {
      Task subProcessTask = rule.taskQuery().singleResult();
      assertThat(subProcessTask).isNotNull();
      rule.getTaskService().complete(subProcessTask.getId());
    }

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.3")
  public void testInitAsyncBeforeTaskDeletion() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), null);

    // then
    rule.assertScenarioEnded();
  }

  /**
   * Note: this test is not really isolated since the job
   * definition is migrated when the process definition is accessed the first time.
   * This might happen already before this test case is executed.
   */
  @Test
  @ScenarioUnderTest("initAsyncBeforeTask.4")
  public void testInitAsyncBeforeTaskJobDefinition() {
    // when the process is redeployed into the cache (instantiation should trigger that)
    rule.getRuntimeService().startProcessInstanceByKey("AsyncBeforeSequentialMultiInstanceTask");

    // then the old job definition referencing "miSubProcess" has been migrated
    JobDefinition asyncJobDefinition = rule.jobDefinitionQuery().singleResult();
    assertThat(asyncJobDefinition.getActivityId()).isEqualTo("miTask#multiInstanceBody");
  }

}
