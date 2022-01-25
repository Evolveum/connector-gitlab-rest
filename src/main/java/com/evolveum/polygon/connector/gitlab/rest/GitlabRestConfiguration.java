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

/**
 * @author Lukas Skublik
 *
 */

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;
import org.identityconnectors.framework.spi.StatefulConfiguration;

public class GitlabRestConfiguration extends AbstractConfiguration implements StatefulConfiguration{


	private String loginUrl;
    private String protocol;
	private GuardedString privateToken;
    private String groupsToManage;
    private String objectAvatar;
	private static final Log LOGGER = Log.getLog(GitlabRestConnector.class);
        


	@ConfigurationProperty(order = 1, displayMessageKey = "privateToken.display", helpMessageKey = "privateToken.help", required = true, confidential = true)
	public GuardedString getPrivateToken() {
		return privateToken;
	}
	/**
	 * Setter method for the "privateToken" attribute.
	 *
	 * @param privateToken
	 * the privateToken string value.
	 */
	public void setPrivateToken(GuardedString privateToken) {
		this.privateToken = privateToken;
	}
        
	@ConfigurationProperty(order = 3, displayMessageKey = "loginUrl.display", helpMessageKey = "loginUrl.help", required = true, confidential = false)
	public String getLoginURL() {
		return loginUrl;
	}

	public void setLoginURL(String loginURL) {
		this.loginUrl = loginURL;
	}
        
    // Add protocol configuration property to support https        
    @ConfigurationProperty(order = 4, displayMessageKey = "protocol.display", helpMessageKey = "protocol.help", required = false, confidential = false)
	public String getProtocol() {
	    return protocol;
	}
       
    // Add groupsToManage configuration property to limit number of groups and memberships in these groupd that will be managed by connector. If null or empty then all groups. Symbol Coma "," is delimiter       
    @ConfigurationProperty(order = 5, displayMessageKey = "groupsToManage.display", helpMessageKey = "groupsToManage.help", required = false, confidential = false)

    public String getGroupsToManage() {
        return groupsToManage;
    }

    
    // Add objectAvatar configuration property to support choose objectAvatar is selected or no by Groups and Project
    // Workaroud for issue https://gitlab.com/gitlab-org/gitlab/-/issues/25498
    //@ConfigurationProperty(order = 6, displayMessageKey = "objectAvatar.display", helpMessageKey = "objectAvatar.help", required = true, confidential = false)
    
    public String getObjectAvatar() {
	    return objectAvatar;
    }    
    
    public void setGroupsToManage(String groupsToManage) {
         this.groupsToManage = groupsToManage;
    }

    public void setProtocol(String protocol) {
         this.protocol = protocol;
    }
    
    public void setObjectAvatar(String objectAvatar) {
    	this.objectAvatar = objectAvatar;
    }
    
        
	    

	@Override
	public void validate() {
		LOGGER.info("Processing trough configuration validation procedure.");
		if (StringUtil.isBlank(loginUrl)) {
			throw new ConfigurationException("Login url cannot be empty.");
		}
		if ("".equals(privateToken)) {
			throw new ConfigurationException("Private Token cannot be empty.");
		}
                
        if (protocol==null || !(protocol.equals("http") || protocol.equals("https") || protocol.isEmpty())) {
		    throw new ConfigurationException("Protocol should be http or https.");
        }
		if (objectAvatar==null || !(objectAvatar.equals("true") || objectAvatar.equals("false") || objectAvatar.isEmpty())) {
			throw new ConfigurationException("objectAvatar should be true or false.");
		}
		
		LOGGER.info("Configuration valid");
	}
	
	@Override
	public void release() {
		LOGGER.info("The release of configuration resources is being performed");
		this.loginUrl = null;                
		this.privateToken.dispose();
                this.protocol = null;
                this.groupsToManage=null;
	}

	@Override
	public String toString() {
		return "ScimConnectorConfiguration{" +
				", loginUrl='" + loginUrl + '\'' +
				'}';
	}
}