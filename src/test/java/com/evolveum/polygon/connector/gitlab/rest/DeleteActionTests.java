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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class DeleteActionTests extends BasicFunctionForTests {
	
	@Test(expectedExceptions = UnknownUidException.class, priority = 24)
	public void deleteGroupTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
		attributesCreatedGroup.add(AttributeBuilder.build("__NAME__","targaryen Gameofthrones"));
		attributesCreatedGroup.add(AttributeBuilder.build("path","targaryen"));
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Uid targaryenUid = gitlabRestConnector.create(objectClassGroup, attributesCreatedGroup, options);
		
		gitlabRestConnector.delete(objectClassGroup, targaryenUid, options);
		
		final ArrayList<ConnectorObject> resultsGroup = new ArrayList<>();
		SearchResultsHandler handlerGroup = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				resultsGroup.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		
		try {
			AttributeFilter equalsFilter;
			equalsFilter = (EqualsFilter) FilterBuilder.equalTo(targaryenUid);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gitlabRestConnector.executeQuery(objectClassGroup, equalsFilter, handlerGroup, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = UnknownUidException.class, priority = 22)
	public void deleteProjectTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreatedProject = new HashSet<Attribute>();
		attributesCreatedProject.add(AttributeBuilder.build("__NAME__","Attack on Casterly Rock"));
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Uid casterlyRockUid = gitlabRestConnector.create(objectClassProject, attributesCreatedProject, options);
		
		gitlabRestConnector.delete(objectClassProject, casterlyRockUid, options);
		
		final ArrayList<ConnectorObject> resultsProject = new ArrayList<>();
		SearchResultsHandler handlerProject = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				resultsProject.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		
		try {
			AttributeFilter equalsFilter;
			equalsFilter = (EqualsFilter) FilterBuilder.equalTo(casterlyRockUid);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gitlabRestConnector.executeQuery(objectClassProject, equalsFilter, handlerProject, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = UnknownUidException.class, priority = 23)
	public void deleteUserTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreatedAccount = new HashSet<Attribute>();
		attributesCreatedAccount.add(AttributeBuilder.build("name","John Snow6"));
		attributesCreatedAccount.add(AttributeBuilder.build("email","john.snow6@gameofthrones.com"));
		attributesCreatedAccount.add(AttributeBuilder.build("__NAME__","snow6"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesCreatedAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		
		Uid johnUid = gitlabRestConnector.create(objectClassAccount, attributesCreatedAccount, options);
		gitlabRestConnector.delete(objectClassAccount, johnUid, options);
		
		
		final ArrayList<ConnectorObject> resultsAccount = new ArrayList<>();
		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				resultsAccount.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		
		try {
			AttributeFilter equalsFilter;
			equalsFilter = (EqualsFilter) FilterBuilder.equalTo(johnUid);
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			gitlabRestConnector.executeQuery(objectClassAccount, equalsFilter, handlerAccount, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
}
