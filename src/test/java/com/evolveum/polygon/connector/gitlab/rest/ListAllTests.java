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

import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.SearchResult;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.spi.SearchResultsHandler;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class ListAllTests extends BasicFunctionForTests {

	@Test(priority = 21)
	public void filteringEmptyPageTestGroupObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesCreatedGroup = new HashSet<Attribute>();
		attributesCreatedGroup.add(AttributeBuilder.build("__NAME__","targaryen Gameofthrones"));
		attributesCreatedGroup.add(AttributeBuilder.build("path","targaryen"));
		
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Uid targaryenUid = gitlabRestConnector.create(objectClassGroup, attributesCreatedGroup, options);
		
		Map<String, Object> operationOptions = new HashMap<String, Object>();
		operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
		operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, Integer.valueOf((String)targaryenUid.getValue().get(0))/100+1);
		operationOptions.put(OperationOptions.OP_PAGE_SIZE, 100);
		options = new OperationOptions(operationOptions);
		
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
		
		try {
			if(!resultsGroup.isEmpty()){
				throw new InvalidAttributeValueException("Searched page is not empty.");
			}
		} finally {
			gitlabRestConnector.delete(objectClassGroup, targaryenUid, options);
			gitlabRestConnector.dispose();
		}
	}
}
