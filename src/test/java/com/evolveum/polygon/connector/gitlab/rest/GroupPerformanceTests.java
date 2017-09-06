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
import java.util.Map;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeDeltaBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class GroupPerformanceTests extends BasicFunctionForTests {

	private Uid johnSnow;
	private Set<Uid> groupsUid = new HashSet<Uid>();
	
	
	@Test(priority = 30)
	public void Create500Groups(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		for(int i=0; i<500;i++){
			
			Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
			attributesCreatedGroup.add(AttributeBuilder.build("__NAME__","Name testGroupPer"+i));
			attributesCreatedGroup.add(AttributeBuilder.build("path","testGroupPer"+i));
			gitlabRestConnector.init(conf);
			Uid groupUid = gitlabRestConnector.create(objectClassGroup, attributesCreatedGroup, options);
			gitlabRestConnector.dispose();
			groupsUid.add(groupUid);
			
		}
		
	}
	
	@Test(priority = 31)
	public void Update500GroupsTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		int i =0;
		for(Uid groupUid : groupsUid){
			
			Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("__NAME__");
			builder.addValueToReplace("Name testGroupPer"+ i + " Update");
			deltaAttributes.add(builder.build());
			gitlabRestConnector.init(conf);
			gitlabRestConnector.updateDelta(objectClassGroup, groupUid, deltaAttributes, options);
			gitlabRestConnector.dispose();
			i++;
		}
	}
	
	@Test(priority = 32)
	public void CreateUserAndAddUserToEachGroupTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("name","John Snow3"));
		attributesAccount.add(AttributeBuilder.build("email","john.snow3@gameofthrones.com"));
		attributesAccount.add(AttributeBuilder.build("__NAME__","snow3"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		gitlabRestConnector.init(conf);
		johnSnow = gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		gitlabRestConnector.dispose();
		
		for(Uid groupUid : groupsUid){
			
			Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("developer_members");
			builder.addValueToAdd(johnSnow.getValue().get(0));
			deltaAttributes.add(builder.build());
			gitlabRestConnector.init(conf);
			gitlabRestConnector.updateDelta(objectClassGroup, groupUid, deltaAttributes, options);
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 33)
	public void Search100GroupsTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
		operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, 1);
		operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
		OperationOptions options = new OperationOptions(operationOptions);
		
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
		
		gitlabRestConnector.executeQuery(objectClassGroup, null, handlerGroup, options);
		
		gitlabRestConnector.dispose();
		
		if(resultsGroup.size() < 100){
			throw new InvalidAttributeValueException("Non exist 100 groups.");
		}
	}
	
	@Test(priority = 34)
	public void SearchGroupsWithContainsAllValuesFilterTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		OperationOptions options = new OperationOptions(operationOptions);
		
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
		
		AttributeFilter filter;
		filter = (ContainsAllValuesFilter) FilterBuilder.containsAllValues(AttributeBuilder.build("developer_members", (String)johnSnow.getValue().get(0)));
		
		gitlabRestConnector.executeQuery(objectClassGroup, filter, handlerGroup, options);
		
		gitlabRestConnector.dispose();
		
		if(resultsGroup.size() < 500){
			throw new InvalidAttributeValueException("User isn't member of each group.");
		}
	}
	
	@Test(priority = 35)
	public void Delete500GroupAndUserTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		GitlabRestConfiguration conf = getConfiguration();
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		for(Uid group : groupsUid){
			gitlabRestConnector.init(conf);
			gitlabRestConnector.delete(objectClassGroup, group, options);
			gitlabRestConnector.dispose();
		}
		
		gitlabRestConnector.init(conf);
		gitlabRestConnector.delete(objectClassAccount, johnSnow, options);
		gitlabRestConnector.dispose();
		
		
	}
}
