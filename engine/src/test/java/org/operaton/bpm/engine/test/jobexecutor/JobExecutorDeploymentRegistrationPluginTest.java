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
package org.operaton.bpm.engine.test.jobexecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.operaton.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.operaton.bpm.engine.impl.plugin.JobExecutorDeploymentRegistrationPlugin;
import org.operaton.bpm.engine.repository.ProcessApplicationDeployment;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.runtime.Job;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Downstream: after a restart, a deployment-aware job executor runs jobs of REST/Java-API deployments again once
 * {@link JobExecutorDeploymentRegistrationPlugin} has registered them; process application deployments stay with
 * their application.
 */
class JobExecutorDeploymentRegistrationPluginTest {

  private static final String RESOURCE = "org/operaton/bpm/engine/test/jobexecutor/simpleAsyncProcess.bpmn20.xml";

  @RegisterExtension
  static ProcessEngineExtension engineRule = ProcessEngineExtension.builder().build();

  ProcessEngine processEngine;
  ProcessEngineConfigurationImpl processEngineConfiguration;
  RepositoryService repositoryService;
  RuntimeService runtimeService;
  ManagementService managementService;

  final List<String> deploymentIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    processEngineConfiguration.setJobExecutorDeploymentAware(true);
  }

  @AfterEach
  void tearDown() {
    processEngineConfiguration.setJobExecutorDeploymentAware(false);
    for (String deploymentId : deploymentIds) {
      managementService.unregisterDeploymentForJobExecutor(deploymentId);
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }

  @Test
  void shouldRegisterAllButProcessApplicationDeploymentsAfterRestart() {
    // given: deployments via the Java API (no source, like REST), from the Modeler, and from a process application
    String javaApi = deploy(null);
    String modeler = deploy("Operaton Modeler");
    String processApplication = deploy(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE);
    List<String> jobIds = List.of(startAndGetJob(javaApi), startAndGetJob(modeler), startAndGetJob(processApplication));

    // and a restart: the in-memory registrations are gone, nothing is acquired
    deploymentIds.forEach(managementService::unregisterDeploymentForJobExecutor);
    assertThat(managementService.getRegisteredDeployments()).doesNotContainAnyElementsOf(deploymentIds);
    AcquiredJobs beforePlugin = acquireJobs();
    jobIds.forEach(jobId -> assertThat(beforePlugin.contains(jobId)).isFalse());

    // when
    new JobExecutorDeploymentRegistrationPlugin().postProcessEngineBuild(processEngine);

    // then
    assertThat(managementService.getRegisteredDeployments())
        .contains(javaApi, modeler)
        .doesNotContain(processApplication);
    AcquiredJobs acquired = acquireJobs();
    assertThat(acquired.contains(jobIds.get(0))).isTrue();
    assertThat(acquired.contains(jobIds.get(1))).isTrue();
    assertThat(acquired.contains(jobIds.get(2))).isFalse();
  }

  @Test
  void shouldRegisterNothingWithoutDeploymentAwareJobExecutor() {
    // given
    String deploymentId = deploy(null);
    managementService.unregisterDeploymentForJobExecutor(deploymentId);
    processEngineConfiguration.setJobExecutorDeploymentAware(false);

    // when
    new JobExecutorDeploymentRegistrationPlugin().postProcessEngineBuild(processEngine);

    // then
    assertThat(managementService.getRegisteredDeployments()).doesNotContain(deploymentId);
  }

  @Test
  void shouldRegisterDeploymentsBeyondOnePage() {
    // given: more deployments than one query page of the plugin (500)
    Set<String> expected = new HashSet<>();
    for (int i = 0; i < 501; i++) {
      expected.add(deploy(null));
    }
    deploymentIds.forEach(managementService::unregisterDeploymentForJobExecutor);

    // when
    new JobExecutorDeploymentRegistrationPlugin().postProcessEngineBuild(processEngine);

    // then
    assertThat(managementService.getRegisteredDeployments()).containsAll(expected);
  }

  private String deploy(String source) {
    String deploymentId = repositoryService.createDeployment()
        .addClasspathResource(RESOURCE)
        .source(source)
        .deploy()
        .getId();
    deploymentIds.add(deploymentId);
    return deploymentId;
  }

  private String startAndGetJob(String deploymentId) {
    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
        .deploymentId(deploymentId).singleResult();
    String processInstanceId = runtimeService.startProcessInstanceById(definition.getId()).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    return job.getId();
  }

  private AcquiredJobs acquireJobs() {
    return processEngineConfiguration.getCommandExecutorTxRequired()
        .execute(new AcquireJobsCmd(processEngineConfiguration.getJobExecutor()));
  }

}
