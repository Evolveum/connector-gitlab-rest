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
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class UserPerformanceTests extends BasicFunctionForTests {

	private Uid targaryenUid;
	private Set<Uid> usersUid = new HashSet<Uid>();
	
	@Test(priority = 25)
	public void CreateGroupAnd500UsersTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
		attributesCreatedGroup.add(AttributeBuilder.build("__NAME__","targaryen Gameofthrones"));
		attributesCreatedGroup.add(AttributeBuilder.build("path","targaryen"));
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		gitlabRestConnector.init(conf);
		targaryenUid = gitlabRestConnector.create(objectClassGroup, attributesCreatedGroup, options);
		gitlabRestConnector.dispose();
		
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		for(int i=0; i<500;i++){
			Set<Attribute> attributes = new HashSet<Attribute>();
			attributes.add(AttributeBuilder.build("email","testUserPer"+i+"@performance.com"));
			GuardedString pass = new GuardedString(("testUserPer"+i).toCharArray());
			attributes.add(AttributeBuilder.build("__PASSWORD__",pass));
			attributes.add(AttributeBuilder.build("__NAME__","testUserPer"+i));
			attributes.add(AttributeBuilder.build("name","Name testUserPer"+i));
			gitlabRestConnector.init(conf);
			Uid userUid = gitlabRestConnector.create(objectClassAccount, attributes, options);
			gitlabRestConnector.dispose();
			usersUid.add(userUid);
			
		}
		
	}
	
	@Test(priority = 26)
	public void Update500UsersTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		int i = 0;
		for(Uid user : usersUid){
			Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("name");
			builder.addValueToReplace("Name Update testUserPer"+i);
			deltaAttributes.add(builder.build());
			gitlabRestConnector.init(conf);
			gitlabRestConnector.updateDelta(objectClassAccount, user, deltaAttributes, options);
			gitlabRestConnector.dispose();
			i++;
		}
		
	}
	
	
	@Test(priority = 27)
	public void CreateMembershipToGroupFor500UsersTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
		
		for(Uid user : usersUid){
			 
			AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
			builder.setName("developer_members");
			builder.addValueToAdd(user.getValue().get(0));
			deltaAttributes.add(builder.build());
			
		}
		gitlabRestConnector.init(conf);
		gitlabRestConnector.updateDelta(objectClassGroup, targaryenUid, deltaAttributes, options);
		gitlabRestConnector.dispose();
	}
	
	@Test(priority = 28)
	public void Search100UsersTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
		operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, 1);
		operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
		OperationOptions options = new OperationOptions(operationOptions);
		
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
		
		gitlabRestConnector.init(conf);
		gitlabRestConnector.executeQuery(objectClassAccount, null, handlerAccount, options);
		gitlabRestConnector.dispose();
		
		if(resultsAccount.size() < 100){
			throw new InvalidAttributeValueException("Non exist 100 users.");
		}
	}
	
	@Test(priority = 29)
	public void SearchGroupWith500UsersTest(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		AttributeFilter equalsFilter;
		equalsFilter = (EqualsFilter) FilterBuilder.equalTo(targaryenUid);
		
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
		
		gitlabRestConnector.init(conf);
		gitlabRestConnector.executeQuery(objectClassGroup, equalsFilter, handlerGroup, options);
		gitlabRestConnector.dispose();
		
		if(resultsGroup.size() == 0 || resultsGroup.get(0).getAttributeByName("developer_members") == null || resultsGroup.get(0).getAttributeByName("developer_members").getValue().size() != 500){
			throw new InvalidAttributeValueException("Group doesn't 500 members.");
		}		
	}
	
	@Test(priority = 30)
	public void DeleteGroupWith500UsersAnd500UsersTest(){
		
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		GitlabRestConfiguration conf = getConfiguration();
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		gitlabRestConnector.init(conf);
		gitlabRestConnector.delete(objectClassGroup, targaryenUid, options);
		gitlabRestConnector.dispose();
		
		for(Uid user : usersUid){
			gitlabRestConnector.init(conf);
			gitlabRestConnector.delete(objectClassAccount, user, options);
			gitlabRestConnector.dispose();
		}
	}
}
