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

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Schema;
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
public class SchemaConsistencyTests extends BasicFunctionForTests {
	

	@Test(priority = 1)
	public void schemaTestGroupObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		Schema schema = gitlabRestConnector.schema();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<AttributeInfo> attributesInfoGroup = schema.findObjectClassInfo(ObjectClass.GROUP_NAME).getAttributeInfo();
		Set<Attribute> attributesGroup = new HashSet<Attribute>();
		
		for(AttributeInfo attributeInfo : attributesInfoGroup){
			if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
				if(attributeInfo.getName().equals("visibility")){
					attributesGroup.add(AttributeBuilder.build(attributeInfo.getName(),"public"));
				} else if(attributeInfo.getType().equals(String.class)){
					attributesGroup.add(AttributeBuilder.build(attributeInfo.getName(),"group145example"));
				} else if(attributeInfo.getType().equals(Integer.class)){
					attributesGroup.add(AttributeBuilder.build(attributeInfo.getName(),157));
				} else if(attributeInfo.getType().equals(Boolean.class)){
					attributesGroup.add(AttributeBuilder.build(attributeInfo.getName(),true));
				}
				
			}
		}
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		Uid uidGroup = gitlabRestConnector.create(objectClassGroup, attributesGroup, options);
		
		AttributeFilter filterGroup;
		filterGroup = (EqualsFilter) FilterBuilder.equalTo(uidGroup);
		
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
		
		gitlabRestConnector.executeQuery(objectClassGroup, filterGroup, handlerGroup, options);
		
		try {
			if(!resultsGroup.get(0).getAttributes().containsAll(attributesGroup)){
				throw new InvalidAttributeValueException("Attributes of created group and searched group is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassGroup, uidGroup, options);
			gitlabRestConnector.dispose();
		}
	}
	
	@Test (priority = 2)
	public void schemaTestProjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		Schema schema = gitlabRestConnector.schema();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<AttributeInfo> attributesInfoProject = schema.findObjectClassInfo("Project").getAttributeInfo();
		
		Set<Attribute> attributesProject = new HashSet<Attribute>();
		
		for(AttributeInfo attributeInfo : attributesInfoProject){
			if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
				if(attributeInfo.getName().equals("visibility")){
					attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),"public"));
				} else if(attributeInfo.getType().equals(String.class)){
					attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),"project897example"));
				} else if(attributeInfo.getType().equals(Integer.class)){
					attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),113));
				} else if(attributeInfo.getType().equals(Boolean.class)){
					attributesProject.add(AttributeBuilder.build(attributeInfo.getName(),true));
				}
				
			} else if(attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable() && attributeInfo.getName().equals("tag_list")){
				ArrayList<String> tagList = new ArrayList<String>();
				tagList.add("ujko");
				tagList.add("tetka");
				attributesProject.add(AttributeBuilder.build(attributeInfo.getName(), tagList.toArray()));
			}
		}
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Uid uidProject = gitlabRestConnector.create(objectClassProject, attributesProject, options);
		
		AttributeFilter filterProject;
		filterProject = (EqualsFilter) FilterBuilder.equalTo(uidProject);
		
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
		
		gitlabRestConnector.executeQuery(objectClassProject, filterProject, handlerProject, options);
		
		try {
			if(!resultsProject.get(0).getAttributes().containsAll(attributesProject)){
				throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassProject, uidProject, options);			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 3)
	public void schemaTestAccountObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		Schema schema = gitlabRestConnector.schema();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<AttributeInfo> attributesInfoAccount = schema.findObjectClassInfo(ObjectClass.ACCOUNT_NAME).getAttributeInfo();
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		
		for(AttributeInfo attributeInfo : attributesInfoAccount){
			if(!attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
				if(attributeInfo.getName().equals("skype")){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"ľ, š, č, ť, ž, ý, á, í, é, ä, ú, ô, ö, ü, ß, $, #, @, %, ^, *, < , > , &"));
				} else if(attributeInfo.getName().equals("external")){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),false));
				} else if(attributeInfo.getName().equals("email")){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"user21222@test.com"));
				} else if(attributeInfo.getType().equals(String.class)){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"user21222example"));
				} else if(attributeInfo.getType().equals(Integer.class)){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),252));
				} else if(attributeInfo.getType().equals(Boolean.class)){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),true));
				}
				
			} else if(attributeInfo.isMultiValued() && attributeInfo.isCreateable() && attributeInfo.isReadable()){
				if(attributeInfo.getName().equals("identities")){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"evolveum:user212222"));
				} else if(attributeInfo.getName().equals("SSH_keys")){
					attributesAccount.add(AttributeBuilder.build(attributeInfo.getName(),"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDgAqsEqXVFe3Y1y2WxrOKqHlyyAUJR8yPpFLQjw0j4lUw2eoH86o/vBX2/FjkJ9GIR1x69yBCCoCKTeBAFqVbvQ9b7pbQb0Oh1biPudOynqSVijAOogTwkB2V50l3Q07DLemiNICZVRCrAlzzjszSkt/E1+EAC5Cp8eRGChpbJpbN3QBgYmoKRDE+wpZEFMMiDzFHpenzgmvPwEc3uOd8M8W+H+xkznrGVVgvBcMmi+sx2YcaKp+Uf1lh8Kc6RbWUFdm48p9XAjs23tR2JTfbipp/qn/Z1R1cJ+/tYrD467Pbql16Lte5lrMc0wMks9n241rlLOhd2g4o811/94BgR matko.janko@example.sk"));
				}
			}
		}
		
		GuardedString pass = new GuardedString("password1".toCharArray());
		attributesAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		attributesAccount.add(AttributeBuilder.build("__ENABLE__",true));
		
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Uid uidAccount = gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		
		AttributeFilter filterAccount;
		filterAccount = (EqualsFilter) FilterBuilder.equalTo(uidAccount);
		
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
		
		gitlabRestConnector.executeQuery(objectClassAccount, filterAccount, handlerAccount, options);
		
		attributesAccount.remove(AttributeBuilder.build("__PASSWORD__",pass));
		
		try {
			if(!resultsAccount.get(0).getAttributes().containsAll(attributesAccount)){
				throw new InvalidAttributeValueException("Attributes of created user and searched user is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassAccount, uidAccount, options);			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test
	public void testValidConfiguration(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		try {
			gitlabRestConnector.test();
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
}
