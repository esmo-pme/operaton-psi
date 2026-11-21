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
package org.operaton.bpm.qa.upgrade.scenarios7110.gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.filter.Filter;
import org.operaton.bpm.engine.impl.QueryEntityRelationCondition;
import org.operaton.bpm.engine.impl.QueryOperator;
import org.operaton.bpm.engine.impl.QueryOrderingProperty;
import org.operaton.bpm.engine.impl.TaskQueryImpl;
import org.operaton.bpm.engine.impl.TaskQueryVariableValue;
import org.operaton.bpm.engine.impl.persistence.entity.SuspensionState;
import org.operaton.bpm.engine.task.DelegationState;
import org.operaton.bpm.engine.variable.type.ValueType;
import org.operaton.bpm.qa.upgrade.Origin;
import org.operaton.bpm.qa.upgrade.ScenarioUnderTest;
import org.operaton.bpm.qa.upgrade.UpgradeProcessEngineExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("TaskFilterScenario")
@Origin("7.24.0")
public class TaskFilterTest {

  @RegisterExtension
  public UpgradeProcessEngineExtension engineRule = new UpgradeProcessEngineExtension("operaton.cfg.xml");

  @ScenarioUnderTest("initTaskFilter.1")
  @Test
  public void testTaskFilter() {
    String testString = "test";
    Integer testInteger = 1;
    DelegationState testDelegationState = DelegationState.PENDING;
    Date testDate = new Date(0);
    String[] testActivityInstances = new String[] {"a", "b", "c"};
    String[] testKeys = new String[] {"d", "e"};
    List<String> testCandidateGroups = new ArrayList<>();
    testCandidateGroups.add("group");
    testCandidateGroups.add("anotherGroup");

    String[] variableNames = new String[] {"a", "b", "c", "d", "e", "f"};
    Object[] variableValues = new Object[] {1, 2, "3", "4", 5, 6};

    QueryOperator[] variableOperators = new QueryOperator[] {QueryOperator.EQUALS, QueryOperator.GREATER_THAN_OR_EQUAL, QueryOperator.LESS_THAN, QueryOperator.LIKE, QueryOperator.NOT_EQUALS, QueryOperator.LESS_THAN_OR_EQUAL};
    boolean[] isTaskVariable = new boolean[] {true, true, false, false, false, false};
    boolean[] isProcessVariable = new boolean[] {false, false, true, true, false, false};

    TaskQueryImpl expectedOrderingPropertiesQuery = new TaskQueryImpl();
    expectedOrderingPropertiesQuery.orderByExecutionId().desc();
    expectedOrderingPropertiesQuery.orderByDueDate().asc();
    expectedOrderingPropertiesQuery.orderByProcessVariable("var", ValueType.STRING).desc();

    List<QueryOrderingProperty> expectedOrderingProperties = expectedOrderingPropertiesQuery.getOrderingProperties();

    Filter filter = engineRule.getFilterService().createTaskFilterQuery().filterName("filter").singleResult();

    TaskQueryImpl query = filter.getQuery();
    assertThat(query.getTaskId()).isEqualTo(testString);
    assertThat(query.getName()).isEqualTo(testString);
    assertThat(query.getNameNotEqual()).isEqualTo(testString);
    assertThat(query.getNameNotLike()).isEqualTo(testString);
    assertThat(query.getNameLike()).isEqualTo(testString);
    assertThat(query.getDescription()).isEqualTo(testString);
    assertThat(query.getDescriptionLike()).isEqualTo(testString);
    assertThat(query.getPriority()).isEqualTo(testInteger);
    assertThat(query.getMinPriority()).isEqualTo(testInteger);
    assertThat(query.getMaxPriority()).isEqualTo(testInteger);
    assertThat(query.getAssignee()).isEqualTo(testString);
    assertThat(query.getExpressions().get("taskAssignee")).isEqualTo(testString);
    assertThat(query.getAssigneeLike()).isEqualTo(testString);
    assertThat(query.getExpressions().get("taskAssigneeLike")).isEqualTo(testString);
    assertThat(query.getInvolvedUser()).isEqualTo(testString);
    assertThat(query.getExpressions().get("taskInvolvedUser")).isEqualTo(testString);
    assertThat(query.getOwner()).isEqualTo(testString);
    assertThat(query.getExpressions().get("taskOwner")).isEqualTo(testString);
    assertThat(query.isUnassigned()).isTrue();
    assertThat(query.isAssigned()).isTrue();
    assertThat(query.getDelegationState()).isEqualTo(testDelegationState);
    assertThat(query.getCandidateGroups()).isEqualTo(testCandidateGroups);
    assertThat(query.isWithCandidateGroups()).isTrue();
    assertThat(query.isWithoutCandidateGroups()).isTrue();
    assertThat(query.isWithCandidateUsers()).isTrue();
    assertThat(query.isWithoutCandidateUsers()).isTrue();
    assertThat(query.getExpressions().get("taskCandidateGroupIn")).isEqualTo(testString);
    assertThat(query.getProcessInstanceId()).isEqualTo(testString);
    assertThat(query.getExecutionId()).isEqualTo(testString);
    assertThat(query.getActivityInstanceIdIn().length).isEqualTo(testActivityInstances.length);
    for (int i = 0; i < query.getActivityInstanceIdIn().length; i++) {
      assertThat(query.getActivityInstanceIdIn()[i]).isEqualTo(testActivityInstances[i]);
    }
    assertThat(query.getCreateTime()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("taskCreatedOn")).isEqualTo(testString);
    assertThat(query.getCreateTimeBefore()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("taskCreatedBefore")).isEqualTo(testString);
    assertThat(query.getCreateTimeAfter()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("taskCreatedAfter")).isEqualTo(testString);
    assertThat(query.getKey()).isEqualTo(testString);
    assertThat(query.getKeys().length).isEqualTo(testKeys.length);
    for (int i = 0; i < query.getKeys().length; i++) {
      assertThat(query.getKeys()[i]).isEqualTo(testKeys[i]);
    }
    assertThat(query.getKeyLike()).isEqualTo(testString);
    assertThat(query.getProcessDefinitionKey()).isEqualTo(testString);
    for (int i = 0; i < query.getProcessDefinitionKeys().length; i++) {
      assertThat(query.getProcessDefinitionKeys()[i]).isEqualTo(testKeys[i]);
    }
    assertThat(query.getProcessDefinitionId()).isEqualTo(testString);
    assertThat(query.getProcessDefinitionName()).isEqualTo(testString);
    assertThat(query.getProcessDefinitionNameLike()).isEqualTo(testString);
    assertThat(query.getProcessInstanceBusinessKey()).isEqualTo(testString);
    assertThat(query.getExpressions().get("processInstanceBusinessKey")).isEqualTo(testString);
    for (int i = 0; i < query.getProcessInstanceBusinessKeys().length; i++) {
      assertThat(query.getProcessInstanceBusinessKeys()[i]).isEqualTo(testKeys[i]);
    }
    assertThat(query.getProcessInstanceBusinessKeyLike()).isEqualTo(testString);
    assertThat(query.getExpressions().get("processInstanceBusinessKeyLike")).isEqualTo(testString);

    // variables
    List<TaskQueryVariableValue> variables = query.getVariables();
    for (int i = 0; i < variables.size(); i++) {
      TaskQueryVariableValue variable = variables.get(i);
      assertThat(variable.getName()).isEqualTo(variableNames[i]);
      assertThat(variable.getValue()).isEqualTo(variableValues[i]);
      assertThat(variable.getOperator()).isEqualTo(variableOperators[i]);
      assertThat(variable.isLocal()).isEqualTo(isTaskVariable[i]);
      assertThat(variable.isProcessInstanceVariable()).isEqualTo(isProcessVariable[i]);
    }

    assertThat(query.getDueDate()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("dueDate")).isEqualTo(testString);
    assertThat(query.getDueBefore()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("dueBefore")).isEqualTo(testString);
    assertThat(query.getDueAfter()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("dueAfter")).isEqualTo(testString);
    assertThat(query.getFollowUpDate()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("followUpDate")).isEqualTo(testString);
    assertThat(query.getFollowUpBefore()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("followUpBefore")).isEqualTo(testString);
    assertThat(query.getFollowUpAfter()).isEqualTo(testDate);
    assertThat(query.getExpressions().get("followUpAfter")).isEqualTo(testString);
    assertThat(query.isExcludeSubtasks()).isTrue();
    assertThat(query.getSuspensionState()).isEqualTo(SuspensionState.SUSPENDED);
    assertThat(query.getCaseDefinitionKey()).isEqualTo(testString);
    assertThat(query.getCaseDefinitionId()).isEqualTo(testString);
    assertThat(query.getCaseDefinitionName()).isEqualTo(testString);
    assertThat(query.getCaseDefinitionNameLike()).isEqualTo(testString);
    assertThat(query.getCaseInstanceId()).isEqualTo(testString);
    assertThat(query.getCaseInstanceBusinessKey()).isEqualTo(testString);
    assertThat(query.getCaseInstanceBusinessKeyLike()).isEqualTo(testString);
    assertThat(query.getCaseExecutionId()).isEqualTo(testString);

    // ordering
    verifyOrderingProperties(expectedOrderingProperties, query.getOrderingProperties());
  }

  protected void verifyOrderingProperties(List<QueryOrderingProperty> expectedProperties, List<QueryOrderingProperty> actualProperties) {
    assertThat(actualProperties.size()).isEqualTo(expectedProperties.size());

    for (int i = 0; i < expectedProperties.size(); i++) {
      QueryOrderingProperty expectedProperty = expectedProperties.get(i);
      QueryOrderingProperty actualProperty = actualProperties.get(i);

      assertThat(actualProperty.getRelation()).isEqualTo(expectedProperty.getRelation());
      assertThat(actualProperty.getDirection()).isEqualTo(expectedProperty.getDirection());
      assertThat(actualProperty.isContainedProperty()).isEqualTo(expectedProperty.isContainedProperty());
      assertThat(actualProperty.getQueryProperty()).isEqualTo(expectedProperty.getQueryProperty());

      List<QueryEntityRelationCondition> expectedRelationConditions = expectedProperty.getRelationConditions();
      List<QueryEntityRelationCondition> actualRelationConditions = expectedProperty.getRelationConditions();

      if (expectedRelationConditions != null && actualRelationConditions != null) {
        assertThat(actualRelationConditions.size()).isEqualTo(expectedRelationConditions.size());

        for (QueryEntityRelationCondition expectedFilteringProperty : expectedRelationConditions) {

          assertThat(expectedFilteringProperty.getProperty()).isEqualTo(expectedFilteringProperty.getProperty());
          assertThat(expectedFilteringProperty.getComparisonProperty()).isEqualTo(expectedFilteringProperty.getComparisonProperty());
          assertThat(expectedFilteringProperty.getScalarValue()).isEqualTo(expectedFilteringProperty.getScalarValue());
        }
      } else if (expectedRelationConditions != null || actualRelationConditions != null) {
        fail("Expected filtering properties: %s. Actual filtering properties: %s".formatted(expectedRelationConditions, actualRelationConditions));
      }
    }
  }

}
