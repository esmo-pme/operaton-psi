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
package org.operaton.bpm.engine.impl.plugin;

import java.util.List;
import java.util.Set;

import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.impl.ProcessEngineLogger;
import org.operaton.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.impl.jobexecutor.JobExecutorLogger;
import org.operaton.bpm.engine.repository.Deployment;
import org.operaton.bpm.engine.repository.ProcessApplicationDeployment;

/**
 * Registers deployments with a deployment-aware job executor when the process engine starts.
 *
 * <p>With {@code jobExecutorDeploymentAware=true} the job executor only acquires jobs of deployments registered in
 * memory. A deployment is registered when it is made, and a process application registers its deployments again
 * when it starts; deployments made through the REST API, the Modeler or the Java API are not registered again after
 * a restart, so their jobs and timers stop running until they are deployed anew.</p>
 *
 * <p>This plugin registers every deployment whose source is not
 * {@value ProcessApplicationDeployment#PROCESS_APPLICATION_DEPLOYMENT_SOURCE} once the engine is built. Process
 * application deployments are left to their application, so their jobs still run only while it is deployed (with
 * its classes). Without a deployment-aware job executor the plugin does nothing.</p>
 *
 * <p>Downstream addition (not in upstream Operaton).</p>
 */
public class JobExecutorDeploymentRegistrationPlugin extends AbstractProcessEnginePlugin {

  private static final JobExecutorLogger LOG = ProcessEngineLogger.JOB_EXECUTOR_LOGGER;

  protected static final int PAGE_SIZE = 500;

  @Override
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    ProcessEngineConfigurationImpl configuration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();
    if (!configuration.isJobExecutorDeploymentAware()) {
      LOG.deploymentRegistrationNotNeeded(processEngine.getName());
      return;
    }

    // what RegisterDeploymentCmd does after its permission check; at engine start no user is authenticated
    Set<String> registeredDeployments = configuration.getRegisteredDeployments();
    int registered = 0;
    int skipped = 0;
    int firstResult = 0;
    List<Deployment> page;
    do {
      page = processEngine.getRepositoryService().createDeploymentQuery()
          .orderByDeploymentId().asc()
          .listPage(firstResult, PAGE_SIZE);
      for (Deployment deployment : page) {
        if (ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE.equals(deployment.getSource())) {
          skipped++;
        } else {
          registeredDeployments.add(deployment.getId());
          registered++;
        }
      }
      firstResult += page.size();
    } while (page.size() == PAGE_SIZE);

    LOG.registeredDeploymentsForJobExecutor(processEngine.getName(), registered, skipped);
  }

}
