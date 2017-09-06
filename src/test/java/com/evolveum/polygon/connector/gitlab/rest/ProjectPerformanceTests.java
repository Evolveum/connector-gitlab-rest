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
public class ProjectPerformanceTests extends BasicFunctionForTests {

	private Set<Uid> projectsUid = new HashSet<Uid>();
	private Uid johnSnow;
	private Uid groupUid;
	
	@Test(priority = 36)
	public void Create500Projects(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		for(int i=0; i<500;i++){
			
			Set<Attribute> attributesCreatedProject = new HashSet<Attribute>();
			attributesCreatedProject.add(AttributeBuilder.build("__NAME__","Name testProjectPer"+i));
			gitlabRestConnector.init(conf);
			Uid groupUid = gitlabRestConnector.create(objectClassProject, attributesCreatedProject, options);
			gitlabRestConnector.dispose();
			projectsUid.add(groupUid);
			
		}
	}
	
	@Test(priority = 37)
	public void Update500ProjectsTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		int i =0;
		for(Uid projectUid : projectsUid){
			
			Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("__NAME__");
			builder.addValueToReplace("Name testProjectPer"+ i + " Update");
			deltaAttributes.add(builder.build());
			gitlabRestConnector.init(conf);
			gitlabRestConnector.updateDelta(objectClassProject, projectUid, deltaAttributes, options);
			gitlabRestConnector.dispose();
			i++;
		}
	}
	
	@Test(priority = 38)
	public void CreateUserAndAddUserToEachProjectTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("name","John Snow8"));
		attributesAccount.add(AttributeBuilder.build("email","john.snow8@gameofthrones.com"));
		attributesAccount.add(AttributeBuilder.build("__NAME__","snow8"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		gitlabRestConnector.init(conf);
		johnSnow = gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		gitlabRestConnector.dispose();
		
		for(Uid projectUid : projectsUid){
			
			Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("developer_members");
			builder.addValueToAdd(johnSnow.getValue().get(0));
			deltaAttributes.add(builder.build());
			gitlabRestConnector.init(conf);
			gitlabRestConnector.updateDelta(objectClassProject, projectUid, deltaAttributes, options);
			gitlabRestConnector.dispose();
		}
		
	}
	
	@Test(priority = 39)
	public void CreateGroupAndAddGroupToEachProjectTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
		attributesCreatedGroup.add(AttributeBuilder.build("__NAME__","Name testGroupPerformance"));
		attributesCreatedGroup.add(AttributeBuilder.build("path","testGroupPerformance"));
		gitlabRestConnector.init(conf);
		groupUid = gitlabRestConnector.create(objectClassGroup, attributesCreatedGroup, options);
		gitlabRestConnector.dispose();
		
		for(Uid projectUid : projectsUid){
			
			Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("shared_with_groups_max_developer");
			builder.addValueToAdd(groupUid.getValue().get(0));
			deltaAttributes.add(builder.build());
			gitlabRestConnector.init(conf);
			gitlabRestConnector.updateDelta(objectClassProject, projectUid, deltaAttributes, options);
			gitlabRestConnector.dispose();
		}
		
	}
	
	@Test(priority = 40)
	public void Search100ProjectsTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
		operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, 1);
		operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
		OperationOptions options = new OperationOptions(operationOptions);
		
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
		
		gitlabRestConnector.executeQuery(objectClassProject, null, handlerProject, options);
		
		gitlabRestConnector.dispose();
		
		if(resultsProject.size() < 100){
			throw new InvalidAttributeValueException("Non exist 100 projects.");
		}
	}
	
	@Test(priority = 41)
	public void SearchProjectsWithContainsAllValuesFilterForMembersTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		OperationOptions options = new OperationOptions(operationOptions);
		
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
		
		AttributeFilter filter;
		filter = (ContainsAllValuesFilter) FilterBuilder.containsAllValues(AttributeBuilder.build("developer_members", (String)johnSnow.getValue().get(0)));
		
		gitlabRestConnector.executeQuery(objectClassProject, filter, handlerProject, options);
		
		gitlabRestConnector.dispose();
		
		if(resultsProject.size() < 500){
			throw new InvalidAttributeValueException("User isn't member of each project.");
		}
	}
	
	@Test(priority = 42)
	public void SearchProjectsWithContainsAllValuesFilterForShareProjectsTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
			
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		ObjectClass objectClassProject = new ObjectClass("Project");
			
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		OperationOptions options = new OperationOptions(operationOptions);
			
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
			
		AttributeFilter filter;
		filter = (ContainsAllValuesFilter) FilterBuilder.containsAllValues(AttributeBuilder.build("shared_with_groups_max_developer", (String) groupUid.getValue().get(0)));
			
		gitlabRestConnector.executeQuery(objectClassProject, filter, handlerProject, options);
		gitlabRestConnector.dispose();
		
		if(resultsProject.size() < 500){
			throw new InvalidAttributeValueException("Each project didn't shared with group.");
		}
	}
	
	@Test(priority = 43)
	public void Delete500ProjectsAndUserAndGroupTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		GitlabRestConfiguration conf = getConfiguration();
		ObjectClass objectClassProject = new ObjectClass("Project");
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		for(Uid projectUid : projectsUid){
			gitlabRestConnector.init(conf);
			gitlabRestConnector.delete(objectClassProject, projectUid, options);
			gitlabRestConnector.dispose();
		}
		
		gitlabRestConnector.init(conf);
		gitlabRestConnector.delete(objectClassAccount, johnSnow, options);
		gitlabRestConnector.dispose();
		
		gitlabRestConnector.init(conf);
		gitlabRestConnector.delete(objectClassGroup, groupUid, options);
		gitlabRestConnector.dispose();
		
	}
}
