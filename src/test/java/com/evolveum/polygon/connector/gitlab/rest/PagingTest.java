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
public class PagingTest extends BasicFunctionForTests {

	@Test(priority = 100)
	public void pagingTestProjectObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		
		
		AttributeFilter containsFilterProject=null;
		
		
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
		

		
//		AttributeFilter containsFilterProject1;
//		containsFilterProject1 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("path", "_.-"));
//		
//		resultsProject.clear();
//		
//		gitlabRestConnector.executeQuery(objectClassProject, containsFilterProject1, handlerProject, options);
//		
//		listUid.clear();
//		for(ConnectorObject obj : resultsProject){
//			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
//		}
		
//		try {
//			if(!listUid.contains(casterlyRockUid)){
//				throw new InvalidAttributeValueException("ContainsFilter not return group with special characters(_.-).");
//			}
//		} catch (Exception e) {
//			gitlabRestConnector.delete(objectClassProject, casterlyRockUid, options);
//			gitlabRestConnector.delete(objectClassProject, highgardenUid, options);
//			gitlabRestConnector.dispose();
//		}
		
		AttributeFilter containsFilterProject2;
		containsFilterProject2 = (ContainsFilter) FilterBuilder.contains(AttributeBuilder.build("path", "ľščťžýáíéôúňä"));
		
		resultsProject.clear();
		
		gitlabRestConnector.executeQuery(objectClassProject, containsFilterProject2, handlerProject, options);
		
		listUid.clear();
		for(ConnectorObject obj : resultsProject){
			listUid.add((Uid)obj.getAttributeByName(Uid.NAME));
		}
		
//		try {
//			if(!listUid.contains(highgardenUid)){
//				throw new InvalidAttributeValueException("ContainsFilter not return groupwith special characters(ľščťžýáíéôúňä).");
//			}
//		} finally {
//			gitlabRestConnector.delete(objectClassProject, casterlyRockUid, options);
//			gitlabRestConnector.delete(objectClassProject, highgardenUid, options);
//			gitlabRestConnector.dispose();
//		}
	}
}
