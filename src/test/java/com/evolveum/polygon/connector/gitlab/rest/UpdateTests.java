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
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeDeltaBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfo;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class UpdateTests extends BasicFunctionForTests {

	@Test(priority = 13)
	public void updateTestAccountObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		Schema schema = gitlabRestConnector.schema();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreateAccount = new HashSet<Attribute>();
		attributesCreateAccount.add(AttributeBuilder.build("name","John Snow"));
		attributesCreateAccount.add(AttributeBuilder.build("email","john.snow@gameofthrones.com"));
		attributesCreateAccount.add(AttributeBuilder.build("__NAME__","snow"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesCreateAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Uid johnUid = gitlabRestConnector.create(objectClassAccount, attributesCreateAccount, options);
		
		
		Set<AttributeInfo> attributesInfoAccount = schema.findObjectClassInfo(ObjectClass.ACCOUNT_NAME).getAttributeInfo();
		Set<AttributeDelta> attributesUpdateAccount = new HashSet<AttributeDelta>();
		
		for(AttributeInfo attributeInfo : attributesInfoAccount){
			if(!attributeInfo.isMultiValued() && attributeInfo.isUpdateable() && attributeInfo.isReadable() && !attributeInfo.getName().equals("email")){
				if(attributeInfo.getName().equals("skype")){
					attributesUpdateAccount.add(AttributeDeltaBuilder.build(attributeInfo.getName(),"ľ,š,č,ť,ž,ý,á,í,é,ä,ú,ô,ö,ü,ß,$,#,@,%,^,*"));
				} else if(attributeInfo.getName().equals("external")){
					attributesUpdateAccount.add(AttributeDeltaBuilder.build(attributeInfo.getName(),false));
				} else if(attributeInfo.getType().equals(String.class)){
					attributesUpdateAccount.add(AttributeDeltaBuilder.build(attributeInfo.getName(),"user21222example"));
				} else if(attributeInfo.getType().equals(Integer.class)){
					attributesUpdateAccount.add(AttributeDeltaBuilder.build(attributeInfo.getName(),252));
				} else if(attributeInfo.getType().equals(Boolean.class)){
					attributesUpdateAccount.add(AttributeDeltaBuilder.build(attributeInfo.getName(),true));
				}
				
			} else if(attributeInfo.isMultiValued() && attributeInfo.isUpdateable() && attributeInfo.isReadable()){
				if(attributeInfo.getName().equals("identities")){
					AttributeDeltaBuilder attrDelta = new AttributeDeltaBuilder();
					attrDelta.setName(attributeInfo.getName());
					attrDelta.addValueToAdd("evolveum:user212222");
					attributesUpdateAccount.add(attrDelta.build());
				} else if(attributeInfo.getName().equals("SSH_keys")){
					AttributeDeltaBuilder attrDelta = new AttributeDeltaBuilder();
					attrDelta.setName(attributeInfo.getName());
					attrDelta.addValueToAdd("ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDgAqsEqXVFe3Y1y2WxrOKqHlyyAUJR8yPpFLQjw0j4lUw2eoH86o/vBX2/FjkJ9GIR1x69yBCCoCKTeBAFqVbvQ9b7pbQb0Oh1biPudOynqSVijAOogTwkB2V50l3Q07DLemiNICZVRCrAlzzjszSkt/E1+EAC5Cp8eRGChpbJpbN3QBgYmoKRDE+wpZEFMMiDzFHpenzgmvPwEc3uOd8M8W+H+xkznrGVVgvBcMmi+sx2YcaKp+Uf1lh8Kc6RbWUFdm48p9XAjs23tR2JTfbipp/qn/Z1R1cJ+/tYrD467Pbql16Lte5lrMc0wMks9n241rlLOhd2g4o811/94BgR kristian.suchanovsky@student.tuke.sk");
					attributesUpdateAccount.add(attrDelta.build());
				}
			}
		}
		
		attributesUpdateAccount.add(AttributeDeltaBuilder.build("__ENABLE__",false));
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, johnUid, attributesUpdateAccount, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		
		
		AttributeFilter filterAccount;
		filterAccount = (EqualsFilter) FilterBuilder.equalTo(johnUid);
		
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
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		for(AttributeDelta attr : attributesUpdateAccount){
			if(attr.getValuesToReplace() != null){
				attributesAccount.add(AttributeBuilder.build(attr.getName(), attr.getValuesToReplace()));
			} else if(attr.getValuesToAdd() != null){
				attributesAccount.add(AttributeBuilder.build(attr.getName(), attr.getValuesToAdd()));
			}
		}
		
		try {
			if(!resultsAccount.get(0).getAttributes().containsAll(attributesAccount)){
				throw new InvalidAttributeValueException("Attributes of created user and searched user is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 11)
	public void updateTestGroupObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		Schema schema = gitlabRestConnector.schema();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesGroup = new HashSet<Attribute>();
		attributesGroup.add(AttributeBuilder.build("__NAME__","Stark family"));
		attributesGroup.add(AttributeBuilder.build("path","stark"));
			
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Uid starkUid = gitlabRestConnector.create(objectClassGroup, attributesGroup, options);
		
		
		Set<AttributeInfo> attributesInfoGroup = schema.findObjectClassInfo(ObjectClass.GROUP_NAME).getAttributeInfo();
		Set<AttributeDelta> attributesUpdateGroup = new HashSet<AttributeDelta>();
		
		for(AttributeInfo attributeInfo : attributesInfoGroup){
			if(!attributeInfo.isMultiValued() && attributeInfo.isUpdateable() && attributeInfo.isReadable() && !attributeInfo.getName().equals("email")){
				if(attributeInfo.getName().equals("visibility")){
					attributesUpdateGroup.add(AttributeDeltaBuilder.build(attributeInfo.getName(),"public"));
				} else if(attributeInfo.getType().equals(String.class)){
					attributesUpdateGroup.add(AttributeDeltaBuilder.build(attributeInfo.getName(),"group145example"));
				} else if(attributeInfo.getType().equals(Integer.class)){
					attributesUpdateGroup.add(AttributeDeltaBuilder.build(attributeInfo.getName(),278));
				} else if(attributeInfo.getType().equals(Boolean.class)){
					attributesUpdateGroup.add(AttributeDeltaBuilder.build(attributeInfo.getName(),true));
				}
			} 
		}
		
		try {
			gitlabRestConnector.updateDelta(objectClassGroup, starkUid, attributesUpdateGroup, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassGroup, starkUid, options);			
			gitlabRestConnector.dispose();
		}
		
		
		AttributeFilter filterAccount;
		filterAccount = (EqualsFilter) FilterBuilder.equalTo(starkUid);
		
		final ArrayList<ConnectorObject> resultsGroup = new ArrayList<>();
		SearchResultsHandler handlerProject = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				resultsGroup.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		
		gitlabRestConnector.executeQuery(objectClassGroup, filterAccount, handlerProject, options);
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		for(AttributeDelta attr : attributesUpdateGroup){
			if(attr.getValuesToReplace() != null){
				attributesAccount.add(AttributeBuilder.build(attr.getName(), attr.getValuesToReplace()));
			} else if(attr.getValuesToAdd() != null){
				attributesAccount.add(AttributeBuilder.build(attr.getName(), attr.getValuesToAdd()));
			}
		}
		
		try {
			if(!resultsGroup.get(0).getAttributes().containsAll(attributesAccount)){
				throw new InvalidAttributeValueException("Attributes of created group and searched group is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassGroup, starkUid, options);			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 12)
	public void updateTestProjectObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		Schema schema = gitlabRestConnector.schema();
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesUpdateProject = new HashSet<Attribute>();
		attributesUpdateProject.add(AttributeBuilder.build("__NAME__","Battle of Bastards"));
			
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Uid projUid = gitlabRestConnector.create(objectClassProject, attributesUpdateProject, options);
		
		
		Set<AttributeInfo> attributesInfoProject = schema.findObjectClassInfo("Project").getAttributeInfo();
		Set<AttributeDelta> attributesDeltaUpdateProject = new HashSet<AttributeDelta>();
		
		for(AttributeInfo attributeInfo : attributesInfoProject){
			if(!attributeInfo.isMultiValued() && attributeInfo.isUpdateable() && attributeInfo.isReadable() && !attributeInfo.getName().equals("email")){
				if(attributeInfo.getName().equals("visibility")){
					attributesDeltaUpdateProject.add(AttributeDeltaBuilder.build(attributeInfo.getName(),"public"));
				} else if(attributeInfo.getType().equals(String.class)){
					attributesDeltaUpdateProject.add(AttributeDeltaBuilder.build(attributeInfo.getName(),"project897example"));
				} else if(attributeInfo.getType().equals(Integer.class)){
					attributesDeltaUpdateProject.add(AttributeDeltaBuilder.build(attributeInfo.getName(),874));
				} else if(attributeInfo.getType().equals(Boolean.class)){
					attributesDeltaUpdateProject.add(AttributeDeltaBuilder.build(attributeInfo.getName(),true));
				}
			} else if(attributeInfo.isMultiValued() && attributeInfo.isUpdateable() && attributeInfo.isReadable() && attributeInfo.getName().equals("tag_list")){
				AttributeDeltaBuilder attrDelta = new AttributeDeltaBuilder();
				attrDelta.setName(attributeInfo.getName());
				attrDelta.addValueToAdd("uncle");
				attrDelta.addValueToAdd("aunt");
				attributesDeltaUpdateProject.add(attrDelta.build());
			}
		}
		try {
			gitlabRestConnector.updateDelta(objectClassProject, projUid, attributesDeltaUpdateProject, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		
		Filter filterProject;
		filterProject = (EqualsFilter) FilterBuilder.equalTo(projUid);
		
		final ArrayList<ConnectorObject> resultsProject = new ArrayList<>();
		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				resultsProject.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		
		gitlabRestConnector.executeQuery(objectClassProject, filterProject, handlerAccount, options);
		
		Set<Attribute> attributesProject = new HashSet<Attribute>();
		for(AttributeDelta attr : attributesDeltaUpdateProject){
			if(attr.getValuesToReplace() != null){
				attributesProject.add(AttributeBuilder.build(attr.getName(), attr.getValuesToReplace()));
			} else if(attr.getValuesToAdd() != null){
				attributesProject.add(AttributeBuilder.build(attr.getName(), attr.getValuesToAdd()));
			}
		}
		
		
		attributesProject.remove(AttributeBuilder.build("default_branch","proj14"));
		try {
			if(!resultsProject.get(0).getAttributes().containsAll(attributesProject)){
				throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = UnknownUidException.class, priority = 16)
	public void updateTestUserWithUnknownUid(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());

		Set<AttributeDelta> attributesDeltaUpdateAccount = new HashSet<AttributeDelta>();
		attributesDeltaUpdateAccount.add(AttributeDeltaBuilder.build("name","Bastard of Ned Stark"));
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, new Uid("9999999999999999999999999999999999999999999"), attributesDeltaUpdateAccount, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = UnknownUidException.class, priority = 14)
	public void updateTestGroupWithUnknownUid(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());

		Set<AttributeDelta> attributesDeltaUpdateGroup = new HashSet<AttributeDelta>();
		attributesDeltaUpdateGroup.add(AttributeDeltaBuilder.build("path","Ned Stark family"));
			
		ObjectClass objectClassAccount = ObjectClass.GROUP;
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, new Uid("9999999999999999999999999999999999999999999"), attributesDeltaUpdateGroup, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = UnknownUidException.class, priority = 15)
	public void updateTestProjectWithUnknownUid(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());

		Set<AttributeDelta> attributesDeltaUpdateProject = new HashSet<AttributeDelta>();
		attributesDeltaUpdateProject.add(AttributeDeltaBuilder.build("name","TestProj"));
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, new Uid("9999999999999999999999999999999999999999999"), attributesDeltaUpdateProject, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 17)
	public void updateTestAccountObjectClassMultivaule(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreateAccount = new HashSet<Attribute>();
		attributesCreateAccount.add(AttributeBuilder.build("name","John Snow"));
		attributesCreateAccount.add(AttributeBuilder.build("email","john.snow@gameofthrones.com"));
		attributesCreateAccount.add(AttributeBuilder.build("__NAME__","snow"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesCreateAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Uid johnUid = gitlabRestConnector.create(objectClassAccount, attributesCreateAccount, options);
		
		
		Set<AttributeDelta> attributesAddAccount = new HashSet<AttributeDelta>();
		
		AttributeDeltaBuilder attrAddDelta = new AttributeDeltaBuilder();
		ArrayList<String> addValue = new ArrayList<String>();
		attrAddDelta.setName("identities");
		for(int i = 0; i<100;i++){
			addValue.add("evolveum"+i+":user"+i);
		}
		attrAddDelta.addValueToAdd(addValue);
		attributesAddAccount.add(attrAddDelta.build());
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, johnUid, attributesAddAccount, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		
		
		AttributeFilter filterAccount;
		filterAccount = (EqualsFilter) FilterBuilder.equalTo(johnUid);
		
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
		
		try {
			if(!addValue.containsAll(resultsAccount.get(0).getAttributeByName("identities").getValue())){
				throw new InvalidAttributeValueException("Attributes of created user and searched user is not same.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		
		Set<AttributeDelta> attributesRemoveAccount = new HashSet<AttributeDelta>();
		
		AttributeDeltaBuilder attrRemoveDelta = new AttributeDeltaBuilder();
		attrRemoveDelta.setName("identities");
		ArrayList<String> removeValue = new ArrayList<String>();
		for(int i = 0; i<50;i++){
			removeValue.add("evolveum"+i+":user"+i);
		}
		attrRemoveDelta.addValueToRemove(removeValue);
		attributesRemoveAccount.add(attrRemoveDelta.build());
		
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, johnUid, attributesRemoveAccount, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		resultsAccount.clear();
		gitlabRestConnector.executeQuery(objectClassAccount, filterAccount, handlerAccount, options);
		addValue.removeAll(removeValue);
		
		try {
			if(!addValue.containsAll(resultsAccount.get(0).getAttributeByName("identities").getValue())){
				throw new InvalidAttributeValueException("Attributes of created user and searched user is not same.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		
		AttributeDeltaBuilder attrRemoveAllDelta = new AttributeDeltaBuilder();
		removeValue.clear();
		attrRemoveAllDelta.setName("identities");
		for(int i = 50; i<100;i++){
			removeValue.add("evolveum"+i+":user"+i);
		}
		attrRemoveAllDelta.addValueToRemove(removeValue);
		attributesRemoveAccount.clear();
		attributesRemoveAccount.add(attrRemoveAllDelta.build());
		
		
		try {
			gitlabRestConnector.updateDelta(objectClassAccount, johnUid, attributesRemoveAccount, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		resultsAccount.clear();
		gitlabRestConnector.executeQuery(objectClassAccount, filterAccount, handlerAccount, options);
		try {
			if(resultsAccount.get(0).getAttributeByName("identities") != null){
				throw new InvalidAttributeValueException("Attributes of created user and searched user is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);			
			gitlabRestConnector.dispose();
		}
		
	}
	
	@Test(priority = 16)
	public void updateTestProjectObjectClassMultivalue(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesUpdateProject = new HashSet<Attribute>();
		attributesUpdateProject.add(AttributeBuilder.build("__NAME__","Battle of Bastards"));
			
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Uid projUid = gitlabRestConnector.create(objectClassProject, attributesUpdateProject, options);
		
		
		Set<AttributeDelta> attributesDeltaAddUpdateProject = new HashSet<AttributeDelta>();
		AttributeDeltaBuilder attrAddDelta = new AttributeDeltaBuilder();
		attrAddDelta.setName("tag_list");
		ArrayList<String> addValue = new ArrayList<String>();
		for(int i = 0; i<100;i++){
			addValue.add("tag"+i);
		}
		attrAddDelta.addValueToAdd(addValue);
		attributesDeltaAddUpdateProject.add(attrAddDelta.build());
		
		try {
			gitlabRestConnector.updateDelta(objectClassProject, projUid, attributesDeltaAddUpdateProject, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		
		Filter filterProject;
		filterProject = (EqualsFilter) FilterBuilder.equalTo(projUid);
		
		final ArrayList<ConnectorObject> resultsProject = new ArrayList<>();
		SearchResultsHandler handlerAccount = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				resultsProject.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		
		gitlabRestConnector.executeQuery(objectClassProject, filterProject, handlerAccount, options);
		
		try {
			if(!addValue.containsAll(resultsProject.get(0).getAttributeByName("tag_list").getValue())){
				throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		
		Set<AttributeDelta> attributesDeltaRemoveUpdateProject = new HashSet<AttributeDelta>();
		AttributeDeltaBuilder attrRemoveDelta = new AttributeDeltaBuilder();
		attrRemoveDelta.setName("tag_list");
		ArrayList<String> removeValue = new ArrayList<String>();
		for(int i = 0; i<50;i++){
			removeValue.add("tag"+i);
		}
		attrRemoveDelta.addValueToRemove(removeValue);
		attributesDeltaRemoveUpdateProject.add(attrRemoveDelta.build());
		
		try {
			gitlabRestConnector.updateDelta(objectClassProject, projUid, attributesDeltaRemoveUpdateProject, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		resultsProject.clear();
		
		gitlabRestConnector.executeQuery(objectClassProject, filterProject, handlerAccount, options);
		
		addValue.removeAll(removeValue);
		
		try {
			if(!addValue.containsAll(resultsProject.get(0).getAttributeByName("tag_list").getValue())){
				throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		
		attributesDeltaRemoveUpdateProject.clear();
		AttributeDeltaBuilder attrRemoveAllDelta = new AttributeDeltaBuilder();
		attrRemoveAllDelta.setName("tag_list");
		removeValue.clear();
		for(int i = 50; i<100;i++){
			removeValue.add("tag"+i);
		}
		attrRemoveAllDelta.addValueToRemove(removeValue);
		attributesDeltaRemoveUpdateProject.add(attrRemoveAllDelta.build());
		
		try {
			gitlabRestConnector.updateDelta(objectClassProject, projUid, attributesDeltaRemoveUpdateProject, options);
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		resultsProject.clear();
		
		gitlabRestConnector.executeQuery(objectClassProject, filterProject, handlerAccount, options);
		
		try {
			if(!resultsProject.get(0).getAttributeByName("tag_list").getValue().isEmpty()){
				throw new InvalidAttributeValueException("Attributes of created project and searched project is not same.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassProject, projUid, options);			
			gitlabRestConnector.dispose();
		}
		
	}
	
}
