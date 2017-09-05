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
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.AttributeFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.FilterBuilder;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class FilteringTests extends BasicFunctionForTests {

	@Test(priority = 20)
	public void filteringTestAccountObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesFirstCreateAccount = new HashSet<Attribute>();
		attributesFirstCreateAccount.add(AttributeBuilder.build("name","John Snow ľ,š,č,ť,ž,ý,á,í,é,ô,ú,ň,ä"));
		attributesFirstCreateAccount.add(AttributeBuilder.build("email","john.snow@gameofthrones.com"));
		attributesFirstCreateAccount.add(AttributeBuilder.build("__NAME__","snow"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesFirstCreateAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Uid johnUid = gitlabRestConnector.create(objectClassAccount, attributesFirstCreateAccount, options);
		
		Set<Attribute> attributesSecondCreateAccount = new HashSet<Attribute>();
		attributesSecondCreateAccount.add(AttributeBuilder.build("name","Ned Stark $,#,@,!,|,?,*,^,%,~,`,(,),_,-,+,=,[,],;,',\\,.,/,\",:,},{"));
		attributesSecondCreateAccount.add(AttributeBuilder.build("email","ned.stark@gameofthrones.com"));
		attributesSecondCreateAccount.add(AttributeBuilder.build("__NAME__","stark"));
		attributesSecondCreateAccount.add(AttributeBuilder.build("identities","evolveum:user"));
		attributesSecondCreateAccount.add(AttributeBuilder.build("external",true));
		
		attributesSecondCreateAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
		Uid nedUid = gitlabRestConnector.create(objectClassAccount, attributesSecondCreateAccount, options);
		
		
		AttributeFilter containsFilterAccount;
		containsFilterAccount = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("email", "gameofthrones"));
		
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
		
		gitlabRestConnector.executeQuery(objectClassAccount, containsFilterAccount, handlerAccount, options);
		
		ArrayList<Uid> listUid = new ArrayList<Uid>();
		for(ConnectorObject obj : resultsAccount){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(johnUid) || !listUid.contains(nedUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return both user.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);
			gitlabRestConnector.delete(objectClassAccount, nedUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter equelsFilterAccount1;
		equelsFilterAccount1 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build("__NAME__", "stark"));
		
		resultsAccount.clear();
		
		gitlabRestConnector.executeQuery(objectClassAccount, equelsFilterAccount1, handlerAccount, options);
		
		try {
			if(!resultsAccount.get(0).getAttributes().contains(nedUid)){
				throw new InvalidAttributeValueException("EqualsFilter not return searched user. Search with username.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);	
			gitlabRestConnector.delete(objectClassAccount, nedUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter equelsFilterAccount2;
		equelsFilterAccount2 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build("identities", "evolveum:user"));
		
		resultsAccount.clear();
		
		gitlabRestConnector.executeQuery(objectClassAccount, equelsFilterAccount2, handlerAccount, options);
		
		try {
			if(!resultsAccount.get(0).getAttributes().contains(nedUid)){
				throw new InvalidAttributeValueException("EqualsFilter not return searched user. Search with identities.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);
			gitlabRestConnector.delete(objectClassAccount, nedUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter equelsFilterAccount3;
		equelsFilterAccount3 = (EqualsFilter) FilterBuilder.equalTo(AttributeBuilder.build("external", true));
		
		resultsAccount.clear();
		
		gitlabRestConnector.executeQuery(objectClassAccount, equelsFilterAccount3, handlerAccount, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsAccount){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(nedUid)){
				throw new InvalidAttributeValueException("EqualsFilter not return searched user. Search with external.");
			}
		} catch (Exception e){
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);
			gitlabRestConnector.delete(objectClassAccount, nedUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter containsFilterAccount1;
		containsFilterAccount1 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("__NAME__", "ľ,š,č,ť,ž,ý,á,í,é,ô,ú,ň,ä"));
		
		resultsAccount.clear();
		
		gitlabRestConnector.executeQuery(objectClassAccount, containsFilterAccount1, handlerAccount, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsAccount){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(johnUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return searched user. Search with username with special characters(ľ,š,č,ť,ž,ý,á,í).");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);	
			gitlabRestConnector.delete(objectClassAccount, nedUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter containsFilterAccount2;
		containsFilterAccount2 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("__NAME__", "$,#,@,!,|,?,*,^,%,~,`,(,),_,-,+,=,[,],;,',\\\\,.,/,\\\",:,},{"));
		
		resultsAccount.clear();
		
		gitlabRestConnector.executeQuery(objectClassAccount, containsFilterAccount2, handlerAccount, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsAccount){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(nedUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return searched user. Search with username with special characters($,#,@,!,|,?,*,^,%,~,`,(,),_,-,+,=,[,],;,',\\,.,/,\",:,},{)." + resultsAccount);
			}
		} finally {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);	
			gitlabRestConnector.delete(objectClassAccount, nedUid, options);
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 18)
	public void filteringTestGroupObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesFirstCreateGroup = new HashSet<Attribute>();
		attributesFirstCreateGroup.add(AttributeBuilder.build("__NAME__","stark Gameofthrones _.-"));
		attributesFirstCreateGroup.add(AttributeBuilder.build("path","stark"));
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Uid starkUid = gitlabRestConnector.create(objectClassGroup, attributesFirstCreateGroup, options);
		
		Set<Attribute> attributesSecondCreateGroup = new HashSet<Attribute>();
		attributesSecondCreateGroup.add(AttributeBuilder.build("__NAME__","lannister Gameofthrones ľščťžýáíéôúňä"));
		attributesSecondCreateGroup.add(AttributeBuilder.build("path","lannister"));
		
		Uid lannisterUid = gitlabRestConnector.create(objectClassGroup, attributesSecondCreateGroup, options);
		
		
		AttributeFilter containsFilterGroup1;
		containsFilterGroup1 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("__NAME__", "gameofthrones"));
		
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
		
		gitlabRestConnector.executeQuery(objectClassGroup, containsFilterGroup1, handlerGroup, options);
		
		ArrayList<Uid> listUid = new ArrayList<Uid>();
		for(ConnectorObject obj : resultsGroup){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(starkUid) || !listUid.contains(lannisterUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return both group.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassGroup, starkUid, options);
			gitlabRestConnector.delete(objectClassGroup, lannisterUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter containsFilterGroup2;
		containsFilterGroup2 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("__NAME__", "_.-"));
		
		resultsGroup.clear();
		
		gitlabRestConnector.executeQuery(objectClassGroup, containsFilterGroup2, handlerGroup, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsGroup){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(starkUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return group with special characters(_.-).");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassGroup, starkUid, options);
			gitlabRestConnector.delete(objectClassGroup, lannisterUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter containsFilterGroup3;
		containsFilterGroup3 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("__NAME__", "ľščťžýáíéôúňä"));
		
		resultsGroup.clear();
		
		gitlabRestConnector.executeQuery(objectClassGroup, containsFilterGroup3, handlerGroup, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsGroup){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(lannisterUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return group with special characters(ľščťžýáíéôúňä).");
			}
		} finally {
			gitlabRestConnector.delete(objectClassGroup, starkUid, options);
			gitlabRestConnector.delete(objectClassGroup, lannisterUid, options);
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(priority = 19)
	public void filteringTestProjectObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesFirstCreateProject = new HashSet<Attribute>();
		attributesFirstCreateProject.add(AttributeBuilder.build("__NAME__","Attack on Casterly Rock _.-"));
		attributesFirstCreateProject.add(AttributeBuilder.build("path","winCasterlyRock"));
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		Uid casterlyRockUid = gitlabRestConnector.create(objectClassProject, attributesFirstCreateProject, options);
		
		Set<Attribute> attributesSecondCreateProject = new HashSet<Attribute>();
		attributesSecondCreateProject.add(AttributeBuilder.build("__NAME__","Attack on Highgarden ľščťžýáíéôúňä"));
		attributesSecondCreateProject.add(AttributeBuilder.build("path","winHighgarden"));
		
		Uid highgardenUid = gitlabRestConnector.create(objectClassProject, attributesSecondCreateProject, options);
		
		
		AttributeFilter containsFilterProject;
		containsFilterProject = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("path", "win"));
		
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
		
		gitlabRestConnector.executeQuery(objectClassProject, containsFilterProject, handlerProject, options);
		
		ArrayList<Uid> listUid = new ArrayList<Uid>();
		for(ConnectorObject obj : resultsProject){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(casterlyRockUid) || !listUid.contains(highgardenUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return both group.");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, casterlyRockUid, options);
			gitlabRestConnector.delete(objectClassProject, highgardenUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter containsFilterProject1;
		containsFilterProject1 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("path", "_.-"));
		
		resultsProject.clear();
		
		gitlabRestConnector.executeQuery(objectClassProject, containsFilterProject1, handlerProject, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsProject){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(casterlyRockUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return group with special characters(_.-).");
			}
		} catch (Exception e) {
			gitlabRestConnector.delete(objectClassProject, casterlyRockUid, options);
			gitlabRestConnector.delete(objectClassProject, highgardenUid, options);
			gitlabRestConnector.dispose();
		}
		
		AttributeFilter containsFilterProject2;
		containsFilterProject2 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("path", "ľščťžýáíéôúňä"));
		
		resultsProject.clear();
		
		gitlabRestConnector.executeQuery(objectClassProject, containsFilterProject2, handlerProject, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsProject){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
		try {
			if(!listUid.contains(highgardenUid)){
				throw new InvalidAttributeValueException("ContainsFilter not return groupwith special characters(ľščťžýáíéôúňä).");
			}
		} finally {
			gitlabRestConnector.delete(objectClassProject, casterlyRockUid, options);
			gitlabRestConnector.delete(objectClassProject, highgardenUid, options);
			gitlabRestConnector.dispose();
		}
	}
}
