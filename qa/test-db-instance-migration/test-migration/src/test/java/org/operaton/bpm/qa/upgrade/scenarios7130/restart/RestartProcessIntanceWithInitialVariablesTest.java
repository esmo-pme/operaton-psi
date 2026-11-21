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
package org.operaton.bpm.qa.upgrade.scenarios7130.restart;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.history.HistoricVariableInstance;
import org.operaton.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.operaton.bpm.engine.runtime.Job;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.runtime.VariableInstance;
import org.operaton.bpm.qa.upgrade.UpgradeProcessEngineExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.engine.impl.test.TestHelper.executeJobIgnoringException;

public class RestartProcessIntanceWithInitialVariablesTest {

  @RegisterExtension
  public UpgradeProcessEngineExtension engineRule = new UpgradeProcessEngineExtension("operaton.cfg.xml");

  RuntimeService runtimeService;
  HistoryService historyService;

  @BeforeEach
  public void assignServices() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }

  @Test
  public void shouldRestartWithInitialVariablesJobExecutedIn712() {
    // given
    String businessKey = "712_ProcessIntanceExecuted";
    ProcessInstance processInstanceWithInitialVariables = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey)
        .singleResult();
    assertThat(processInstanceWithInitialVariables).isNotNull();

    runtimeService.deleteProcessInstance(processInstanceWithInitialVariables.getId(), "test");
    // when
    runtimeService.restartProcessInstances(processInstanceWithInitialVariables.getProcessDefinitionId())
      .startBeforeActivity("theTask")
      .processInstanceIds(processInstanceWithInitialVariables.getId())
      .initialSetOfVariables()
      .execute();

    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey)
        .active()
        .singleResult();

    // then
    restartedProcessInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey)
        .active()
        .singleResult();
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstance.getId()).list();
    assertThat(variables.size()).isEqualTo(1);
    assertThat(variables.get(0).getName()).isEqualTo("initial1");
    assertThat(variables.get(0).getValue()).isEqualTo("value1");
    List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(restartedProcessInstance.getId())
        .list();

    assertThat(list.size()).isEqualTo(1);
    HistoricVariableInstance historicVariableInstance = list.get(0);
    assertThat(historicVariableInstance.getActivityInstanceId()).isEqualTo(restartedProcessInstance.getId());
    assertThat(variables.get(0).getName()).isEqualTo("initial1");
    assertThat(variables.get(0).getValue()).isEqualTo("value1");

    HistoricVariableUpdateEventEntity detail = (HistoricVariableUpdateEventEntity) historyService.createHistoricDetailQuery()
        .processInstanceId(restartedProcessInstance.getId())
        .singleResult();
    assertThat(detail).isNotNull();
    assertThat(detail.isInitial()).isTrue();
    assertThat(detail.getVariableName()).isEqualTo("initial1");
    assertThat(detail.getTextValue()).isEqualTo("value1");
  }

  @Test
  public void shouldRestartWithInitialVariablesJobExecutedAndSetVariablesIn713() {
    // given
    String businessKey = "7120_ProcessIntanceWithoutExecuteAndSetVariables";
    ProcessInstance processInstanceWithInitialVariables = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey)
        .active()
        .singleResult();

    runtimeService.setVariable(processInstanceWithInitialVariables.getId(), "varIn713", "value");

    ManagementService managementService = engineRule.getManagementService();
    Job asyncJob = managementService.createJobQuery()
        .processDefinitionKey("asyncBeforeStartProcess_712")
        .processInstanceId(processInstanceWithInitialVariables.getId())
        .singleResult();
    executeJobIgnoringException(managementService, asyncJob.getId());
    // assume
    HistoricVariableUpdateEventEntity detail = (HistoricVariableUpdateEventEntity) historyService
        .createHistoricDetailQuery()
        .processInstanceId(processInstanceWithInitialVariables.getId())
        .activityInstanceId(processInstanceWithInitialVariables.getId())
        .singleResult();

    assertThat(detail.isInitial()).isTrue();

    runtimeService.deleteProcessInstance(processInstanceWithInitialVariables.getId(), "test");

    // when
    runtimeService.restartProcessInstances(processInstanceWithInitialVariables.getProcessDefinitionId())
      .startBeforeActivity("theTask")
      .processInstanceIds(processInstanceWithInitialVariables.getId())
      .initialSetOfVariables()
      .execute();

    ProcessInstance restartedProcessInstance = runtimeService.createProcessInstanceQuery()
        .processInstanceBusinessKey(businessKey)
        .active()
        .singleResult();

    // then
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery()
        .processInstanceIdIn(restartedProcessInstance.getId()).singleResult();
    assertThat(variableInstance).isNotNull();
    assertThat(variableInstance.getName()).isEqualTo("initial3");
    assertThat(variableInstance.getValue()).isEqualTo("value1");

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery()
        .processInstanceId(restartedProcessInstance.getId())
        .singleResult();
    assertThat(historicVariable).isNotNull();
    assertThat(historicVariable.getActivityInstanceId()).isEqualTo(restartedProcessInstance.getId());

    detail = (HistoricVariableUpdateEventEntity) historyService.createHistoricDetailQuery()
        .processInstanceId(restartedProcessInstance.getId())
        .singleResult();

    assertThat(detail.isInitial()).isTrue();
  }
}
