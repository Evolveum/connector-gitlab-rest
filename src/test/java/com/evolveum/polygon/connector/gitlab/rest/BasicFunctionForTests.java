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
import java.util.List;
import java.util.Map;

import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.SearchResultsHandler;

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
		conf.setPrivateToken(parser.getPrivateToken());
		return conf;
	}
	
	public void deleteUsedObject(){
		
		List<String> namesOfUsers = new ArrayList<String>();
		namesOfUsers.add("snow");
		namesOfUsers.add("stark");
		namesOfUsers.add("user21222example");
		for(int i = 0; i < 500; i++){
			namesOfUsers.add("testUserPer"+i);
		}
		for(int i = 2; i < 9; i++){
			namesOfUsers.add("snow"+i);
		}
		
		List<String> namesOfGroups = new ArrayList<String>();
		namesOfGroups.add("stark");
		namesOfGroups.add("targaryen Gameofthrones");
		namesOfGroups.add("lannister");
		namesOfGroups.add("group145example");
		namesOfGroups.add("Name testGroupPerformance");
		for(int i = 0; i < 500; i++){
			namesOfGroups.add("Name testGroupPer"+i);
		}
		for(int i = 0; i < 500; i++){
			namesOfGroups.add("Name testGroupPer"+i+" Update");
		}
		
		List<String> namesOfProjects = new ArrayList<String>();
		namesOfProjects.add("Battle of Bastards");
		namesOfProjects.add("Attack on Casterly Rock");
		namesOfProjects.add("Attack on Casterly Rock _.-");
		namesOfProjects.add("Attack on Highgarden ľščťžýáíéôúňä");
		namesOfProjects.add("project897example");
		for(int i = 0; i < 500; i++){
			namesOfProjects.add("Name testProjectPer"+i);
		}
		for(int i = 0; i < 500; i++){
			namesOfProjects.add("Name testProjectPer"+i+" Update");
		}
		
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		final ArrayList<ConnectorObject> result = new ArrayList<>();
		final ArrayList<ConnectorObject> partOfResult = new ArrayList<>();
		SearchResultsHandler handler = new SearchResultsHandler() {

			@Override
			public boolean handle(ConnectorObject connectorObject) {
				partOfResult.add(connectorObject);
				return true;
			}

			@Override
			public void handleResult(SearchResult result) {
			}
		};
		int i = 1;
		do{
			Map<String, Object> operationOptions = new HashMap<String, Object>();
			operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
			operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, i);
			operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
			OperationOptions searchOptions = new OperationOptions(operationOptions);
			partOfResult.clear();
			gitlabRestConnector.init(conf);
			gitlabRestConnector.executeQuery(objectClassAccount, null, handler, searchOptions);
			gitlabRestConnector.dispose();
			result.addAll(partOfResult);
			i++;
		} while(partOfResult.size() == 100);
		
		i = 1;
		do{
			Map<String, Object> operationOptions = new HashMap<String, Object>();
			operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
			operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, i);
			operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
			OperationOptions searchOptions = new OperationOptions(operationOptions);
			partOfResult.clear();
			gitlabRestConnector.init(conf);
			gitlabRestConnector.executeQuery(objectClassGroup, null, handler, searchOptions);
			gitlabRestConnector.dispose();
			result.addAll(partOfResult);
			i++;
		} while(partOfResult.size() == 100);
		
		i = 1;
		do{
			Map<String, Object> operationOptions = new HashMap<String, Object>();
			operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
			operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, i);
			operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
			OperationOptions searchOptions = new OperationOptions(operationOptions);
			partOfResult.clear();
			gitlabRestConnector.init(conf);
			gitlabRestConnector.executeQuery(objectClassProject, null, handler, searchOptions);
			gitlabRestConnector.dispose();
			result.addAll(partOfResult);
			i++;
		} while(partOfResult.size() == 100);
		
		for(ConnectorObject obj : result){
			String nameObj = (String)obj.getAttributeByName(Name.NAME).getValue().get(0);
			
			if(namesOfUsers.contains(nameObj)){
				gitlabRestConnector.init(conf);
				gitlabRestConnector.delete(objectClassAccount, (Uid)obj.getAttributeByName(Uid.NAME), options);
				gitlabRestConnector.dispose();
			} else if(namesOfGroups.contains(nameObj)){
				gitlabRestConnector.init(conf);
				gitlabRestConnector.delete(objectClassGroup, (Uid)obj.getAttributeByName(Uid.NAME), options);
				gitlabRestConnector.dispose();
			} else if(namesOfProjects.contains(nameObj)){
				gitlabRestConnector.init(conf);
				gitlabRestConnector.delete(objectClassProject, (Uid)obj.getAttributeByName(Uid.NAME), options);
				gitlabRestConnector.dispose();
			}
		}
		gitlabRestConnector.dispose();
	}
}