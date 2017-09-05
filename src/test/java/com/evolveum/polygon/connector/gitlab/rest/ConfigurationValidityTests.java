/**
 * Copyright (c) 2010-2017 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.polygon.connector.gitlab.rest;

import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class ConfigurationValidityTests extends BasicFunctionForTests {

	@Test(expectedExceptions = ConnectorIOException.class)
	public void creteTestBrokeNetwork(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		conf.setLoginURL("none");
		gitlabRestConnector.init(conf);
		try {
		gitlabRestConnector.test();
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = ConfigurationException.class)
	public void creteTestConfigurationMandatoryValueMissing(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		conf.setLoginURL("");
		gitlabRestConnector.init(conf);
		try {
		gitlabRestConnector.test();
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
}
