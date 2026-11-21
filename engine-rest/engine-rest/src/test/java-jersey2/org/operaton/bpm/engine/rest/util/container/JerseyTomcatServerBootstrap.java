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
package org.operaton.bpm.engine.rest.util.container;

import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;

public class JerseyTomcatServerBootstrap extends TomcatServerBootstrap {

  public JerseyTomcatServerBootstrap(String webXmlPath) {
    super(webXmlPath);
  }

  @Override
  protected void addRuntimeSpecificLibraries(WebArchive wa, PomEquippedResolveStage resolver) {
    // downstream: add no Jersey to the WAR. The embedded Tomcat's web application sees the test classpath, which
    // already has Jersey (Jakarta). The former jersey-container-servlet:2.25.1 (javax.servlet) failed to load in
    // Tomcat 11 (every request 404), and a second Jersey copy in WEB-INF/lib clashes with the classpath one
    // (ClassCastException between class loaders).
  }
}
