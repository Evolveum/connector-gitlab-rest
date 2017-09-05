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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;

/**
 * @author Lukas Skublik
 *
 */
public class PropertiesParser {

	private static final Log LOGGER = Log.getLog(PropertiesParser.class);
	private Properties properties;
	private String FilePath = "../connector-rest-gitlab/testProperties/propertiesForTests.properties";
	private final String PRIVATE_TOKEN = "privateToken";
	private final String LOGIN_URL = "loginUrl";

	public PropertiesParser() {

		try {
			InputStreamReader fileInputStream = new InputStreamReader(new FileInputStream(FilePath),
					StandardCharsets.UTF_8);
			properties = new Properties();
			properties.load(fileInputStream);
		} catch (FileNotFoundException e) {
			LOGGER.error("File not found: {0}", e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			LOGGER.error("IO exception occurred {0}", e.getLocalizedMessage());
			e.printStackTrace();
		}
	}

	public GuardedString getPrivateToken(){
		return new GuardedString(((String)properties.get(PRIVATE_TOKEN)).toCharArray());
	}
	
	public String getLoginUrl(){
		return (String)properties.get(LOGIN_URL);
	}
}