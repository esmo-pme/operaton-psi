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
package org.operaton.bpm.qa.upgrade.scenarios730.boundary;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.management.ActivityStatistics;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.qa.upgrade.Origin;
import org.operaton.bpm.qa.upgrade.ScenarioUnderTest;
import org.operaton.bpm.qa.upgrade.UpgradeTestExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Thorben Lindhauer
 *
 */
@ScenarioUnderTest("NonInterruptingBoundaryEventScenario")
@Origin("7.24.0")
public class NonInterruptingBoundaryEventScenarioTest {

  @RegisterExtension
  public UpgradeTestExtension rule = new UpgradeTestExtension();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitActivityInstanceStatistics() {
    // given
    ProcessInstance processInstance = rule.processInstance();

    // when
    List<ActivityStatistics> activityStatistics = rule.getManagementService()
      .createActivityStatisticsQuery(processInstance.getProcessDefinitionId())
      .list();

    // then
    assertThat(activityStatistics.size()).isEqualTo(2);

    ActivityStatistics outerTaskStatistics = getStatistics(activityStatistics, "outerTask");
    assertThat(outerTaskStatistics).isNotNull();
    assertThat(outerTaskStatistics.getId()).isEqualTo("outerTask");
    assertThat(outerTaskStatistics.getInstances()).isEqualTo(1);

    ActivityStatistics afterBoundaryStatistics = getStatistics(activityStatistics, "afterBoundaryTask");
    assertThat(afterBoundaryStatistics).isNotNull();
    assertThat(afterBoundaryStatistics.getId()).isEqualTo("afterBoundaryTask");
    assertThat(afterBoundaryStatistics.getInstances()).isEqualTo(1);
  }

  protected ActivityStatistics getStatistics(List<ActivityStatistics> activityStatistics, String activityId) {
    for (ActivityStatistics statistics : activityStatistics) {
      if (activityId.equals(statistics.getId())) {
        return statistics;
      }
    }

    return null;
  }
}
