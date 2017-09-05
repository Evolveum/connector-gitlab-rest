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

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
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

/**
 * @author Lukas Skublik
 *
 */
public class MyTest {

	private static final Log LOGGER = Log.getLog(GitlabRestConnector.class);
	public static SearchResultsHandler handler = new SearchResultsHandler() {

		@Override
		public boolean handle(ConnectorObject connectorObject) {
			results.add(connectorObject);
			return true;
		}

		@Override
		public void handleResult(SearchResult result) {
			LOGGER.info("TEST:im handling {0}", result.getRemainingPagedResults());
		}
	};
	private static final ArrayList<ConnectorObject> results = new ArrayList<>();

	
	//@Test
	public void test() {
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();

		GitlabRestConfiguration conf = new GitlabRestConfiguration();
		conf.setLoginURL("172.16.1.253:8086");
		GuardedString privateToken = new GuardedString("JZUYRV3w1M2UVRdHAF5k".toCharArray());
		conf.setPrivateToken(privateToken);

//		 ObjectClass objectClass = new ObjectClass("__ACCOUNT__");
		ObjectClass objectClass = new ObjectClass("__GROUP__");
//		 ObjectClass objectClass = new ObjectClass("Project");
//		Set<Attribute> attributes = new HashSet<Attribute>();
		gitlabRestConnector.init(conf);

		// //User create:
		// attributes.add(AttributeBuilder.build("email","test21@example.com"));
		// GuardedString pass = new GuardedString("testtest1".toCharArray());
		// attributes.add(AttributeBuilder.build("__PASSWORD__",pass));
		// attributes.add(AttributeBuilder.build("__NAME__","michal21"));
		// attributes.add(AttributeBuilder.build("name","Mpppffppichal
		// Miller21"));
		// gitlabRestConnector.create(objectClass, attributes, null);

		// Group create:
		// attributes.add(AttributeBuilder.build("path","testtest"));
		// attributes.add(AttributeBuilder.build("__NAME__","My First"));
		// gitlabRestConnector.create(objectClass, attributes, null);

		// Project create:
		// attributes.add(AttributeBuilder.build("path","ProjectTest"));
		// attributes.add(AttributeBuilder.build("__NAME__","Project Test"));
		// gitlabRestConnector.create(objectClass, attributes, null);

		// User Update:
//		 Uid uid = new Uid("85");
//		 Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
//		AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
//		builder.setName("identities");
//		builder.addValueToRemove("evolveum:100");
//		deltaAttributes.add(builder.build());
		//// attributes.add(AttributeBuilder.build("provider","google"));
		// attributes.add(AttributeBuilder.build("name","Mpppffppichal
		// Miller21"));
		// gitlabRestConnector.update(objectClass, uid, attributes, null);

		// //Group Update:
//		 Uid uid = new Uid("18");
//		 attributes.add(AttributeBuilder.build("members", new
//		 String[]{"7:50:null", "8:20:null", "4:10:2017-05-05"}));
//		 gitlabRestConnector.update(objectClass, uid, attributes, null);
//		 gitlabRestConnector.addAttributeValues(objectClass, uid, attributes,
//		 null);

		// Project Update:
		// Uid uid = new Uid("2");
		// attributes.add(AttributeBuilder.build("public",true));
		// gitlabRestConnector.update(objectClass, uid, attributes, null);

		// User Delete:
		// Uid uid = new Uid("2");
		// gitlabRestConnector.delete(objectClass, uid, null);

		// //Group Delete:
		// Uid uid = new Uid("17");
		// gitlabRestConnector.delete(objectClass, uid, null);

		// //Member of group Delete:
//		Uid uid = new Uid("34");
//		Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
//		AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
//		builder.setName("tag_list");
//		builder.addValueToRemove("syn");
//		builder.addValueToRemove("tetka");
//		deltaAttributes.add(builder.build());
//		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
//		gitlabRestConnector.updateDelta(objectClass, uid, deltaAttributes, options);
		
		// Project of group Delete:
//		Uid uid = new Uid("11");
//		Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
//		AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
//		builder.setName("shared_with_groups_max_guest");
//		builder.addValueToAdd("34");
//		deltaAttributes.add(builder.build());
//		
//		gitlabRestConnector.updateDelta(objectClass, uid, deltaAttributes, null);
		
		// Project of group Delete:
		Uid uid = new Uid("22");
		Set<AttributeDelta> deltaAttributes = new HashSet<AttributeDelta>();
		AttributeDeltaBuilder builder = new AttributeDeltaBuilder();
		String key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDgAqsEqXVFe3Y1y2WxrOKqHlyyAUJR8yPpFLQjw0j4lUw2eoH86o/vBX2/FjkJ9GIR1x69yBCCoCKTeBAFqVbvQ9b7pbQb0Oh1biPudOynqSVijAOogTwkB2V50l3Q07DLemiNICZVRCrAlzzjszSkt/E1+EAC5Cp8eRGChpbJpbN3QBgYmoKRDE+wpZEFMMiDzFHpenzgmvPwEc3uOd8M8W+H+xkznrGVVgvBcMmi+sx2YcaKp+Uf1lh8Kc6RbWUFdm48p9XAjs23tR2JTfbipp/qn/Z1R1cJ+/tYrD467Pbql16Lte5lrMc0wMks9n241rlLOhd2g4o811/94BgR kristian.suchanovsky@student.tuke.sk";

		builder.setName("SSH_keys");
		builder.addValueToAdd(key);
		deltaAttributes.add(builder.build());
				
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
//		gitlabRestConnector.updateDelta(objectClass, uid, deltaAttributes, options);
		
		// Group Delete:
		// Uid uid = new Uid("2");
		// gitlabRestConnector.delete(objectClass, uid, null);

		// User Get:
		AttributeFilter filter;
//		filter = (ContainsAllValuesFilter) FilterBuilder.containsAllValues(AttributeBuilder.build("shared_with_groups_max_developer", new
//				 String[]{"34", "35"}));
//		filter = (EqualsFilter) FilterBuilder.equalTo(new Name("slavo201"));
		 filter = (EqualsFilter) FilterBuilder.equalTo(new Uid("7238"));
		// filter = (EqualsFilter)
		// FilterBuilder.equalTo(AttributeBuilder.build("skype", "tt"));
		// filter = (ContainsFilter)
		// FilterBuilder.contains(AttributeBuilder.build("__NAME__", "slavo"));
		// filter = (ContainsFilter)
		// FilterBuilder.contains(AttributeBuilder.build("name", "er"));
		// filter = (ContainsFilter)
		// FilterBuilder.contains(AttributeBuilder.build("name", "Feb 23"));
		// filter = (ContainsFilter)
		// FilterBuilder.contains(AttributeBuilder.build("__NAME__", "2"));
		// filter = (StartsWithFilter)
		// FilterBuilder.startsWith(AttributeBuilder.build("__NAME__", "sl"));
		// filter = (EndsWithFilter)
		// FilterBuilder.endsWith(AttributeBuilder.build("__NAME__", "1"));
		// Filter leftFilter = (ContainsFilter)
		// FilterBuilder.contains(AttributeBuilder.build("visibility_level",
		// "0"));
		//
		// Filter rightFilter = (ContainsFilter)
		// FilterBuilder.contains(AttributeBuilder.build("__NAME__", "g"));
		//
		// Filter filter = (AndFilter) FilterBuilder.and(leftFilter,
		// rightFilter);

		//Group Get:
//		 AttributeFilter filter;
//		 filter = (ContainsFilter)
//		 FilterBuilder.contains(AttributeBuilder.build("__NAME__", "M"));
		//
		// Map<String, Object> operationOptions = new HashMap<String, Object>();
		// operationOptions.put("ALLOW_PARTIAL_ATTRIBUTE_VALUES", true);
		// operationOptions.put(OperationOptions.OP_PAGED_RESULTS_OFFSET, 1);
		// operationOptions.put(OperationOptions.OP_PAGE_SIZE, 1);
		// OperationOptions options = new OperationOptions(operationOptions);
		//
		// gitlabRestConnector.getConfiguration().validate();
		// //LOGGER.info("attribute name {0}, attribute value {1}",
		// filter.getAttribute().getName(),filter.getAttribute().getValue().get(0).toString());
//		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		gitlabRestConnector.executeQuery(objectClass, filter, handler, options);
		// LOGGER.info("results {0}", results.toString());
		// gitlabRestConnector.test();

		// LOGGER.info("Schema {0}", gitlabRestConnector.schema().toString());
	}

	
}
