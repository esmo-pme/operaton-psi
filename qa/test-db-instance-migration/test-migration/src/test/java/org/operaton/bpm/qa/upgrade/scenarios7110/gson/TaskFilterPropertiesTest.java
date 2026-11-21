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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.FilterService;
import org.operaton.bpm.engine.filter.Filter;
import org.operaton.bpm.qa.upgrade.Origin;
import org.operaton.bpm.qa.upgrade.ScenarioUnderTest;
import org.operaton.bpm.qa.upgrade.UpgradeProcessEngineExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Tassilo Weidner
 */
@ScenarioUnderTest("TaskFilterPropertiesScenario")
@Origin("7.24.0")
public class TaskFilterPropertiesTest {

  @RegisterExtension
  public UpgradeProcessEngineExtension engineRule = new UpgradeProcessEngineExtension("operaton.cfg.xml");

  @ScenarioUnderTest("initTaskFilterProperties.1")
  @Test
  public void testMapContainingListContainingMapProperty_DeserializePrimitives() {
    FilterService filterService = engineRule.getFilterService();

    // when
    Filter filterOne = filterService.createFilterQuery().filterName("taskFilterOne").singleResult();

    Map deserialisedProperties = filterOne.getProperties();

    List list = (List) deserialisedProperties.get("foo");
    Map map = (Map) list.get(0);

    // then
    assertThat(deserialisedProperties.size()).isEqualTo(1);
    assertThat((String) map.get("string")).isEqualTo("aStringValue");
    assertThat((int) map.get("int")).isEqualTo(47);
    assertThat((long) map.get("intOutOfRange")).isEqualTo(Integer.MAX_VALUE + 1L);
    assertThat((long) map.get("long")).isEqualTo(Long.MAX_VALUE);
    assertThat((double) map.get("double")).isEqualTo(3.14159265359D);
    assertThat((boolean) map.get("boolean")).isEqualTo(true);
    assertThat(map.get("null")).isNull();
  }

  @ScenarioUnderTest("initTaskFilterProperties.1")
  @Test
  public void testMapContainingMapContainingListProperty_DeserializePrimitives() {
    FilterService filterService = engineRule.getFilterService();

    // when
    Filter filterTwo = filterService.createFilterQuery().filterName("taskFilterTwo").singleResult();

    Map deserialisedProperties = filterTwo.getProperties();

    List list = (List) ((Map) deserialisedProperties.get("foo")).get("bar");

    // then
    assertThat(deserialisedProperties.size()).isEqualTo(1);

    assertThat((String) list.get(0)).isEqualTo("aStringValue");
    assertThat((int) list.get(1)).isEqualTo(47);
    assertThat((long) list.get(2)).isEqualTo(Integer.MAX_VALUE + 1L);
    assertThat((long) list.get(3)).isEqualTo(Long.MAX_VALUE);
    assertThat((double) list.get(4)).isEqualTo(3.14159265359D);
    assertThat((boolean) list.get(5)).isEqualTo(true);
    assertThat(list.get(6)).isNull();
  }

}
