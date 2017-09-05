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

import com.evolveum.polygon.connector.gitlab.rest.GitlabRestConfiguration;

/**
 * @author Lukas Skublik
 *
 */
public class BasicFunctionForTests {

	private PropertiesParser parser = new PropertiesParser();
	
	protected GitlabRestConfiguration getConfiguration(){
		GitlabRestConfiguration conf = new GitlabRestConfiguration();
		conf.setLoginURL(parser.getLoginUrl());
		//GuardedString privateToken = new GuardedString("JZUYRV3w1M2UVRdHAF5k".toCharArray());
		conf.setPrivateToken(parser.getPrivateToken());
		return conf;
	}
}
