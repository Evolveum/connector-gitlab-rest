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

/**
 * @author Lukas Skublik
 *
 */

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeDeltaBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.Connector;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateDeltaOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;

@ConnectorClass(displayNameKey = "connector.gitlab.rest.display", configurationClass = GitlabRestConfiguration.class)
public class GitlabRestConnector
		implements TestOp, SchemaOp, Connector, CreateOp, DeleteOp, UpdateDeltaOp, SearchOp<Filter> {

	private static final Log LOGGER = Log.getLog(GitlabRestConnector.class);
	private GitlabRestConfiguration configuration;

	private static final String USERS = "/users";
	private static final String GROUPS = "/groups";
	private static final String PROJECTS = "/projects";

	private static final String PROJECT_NAME = "Project";

	private Schema schema = null;
	private CloseableHttpClient httpclient;

	@Override
	public void test() {
		ObjectProcessing objectProcessing = new ObjectProcessing(configuration, httpclient);
		objectProcessing.test();
	}

	@Override
	public void init(Configuration configuration) {
		LOGGER.info("Initialize");

		this.configuration = (GitlabRestConfiguration) configuration;
		this.configuration.validate();
		httpclient = HttpClientBuilder.create().build();
	}

	@Override
	public void dispose() {
		LOGGER.info("Configuration cleanup");
		configuration = null;
		if (httpclient != null) {
			try {
				httpclient.close();
			} catch (IOException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It was not possible close httpclient;").append(e.getLocalizedMessage());
				throw new ConnectorIOException(sb.toString(), e);
			}
		}
	}

	@Override
	public Schema schema() {
		if (this.schema == null) {
			SchemaBuilder schemaBuilder = new SchemaBuilder(GitlabRestConnector.class);
			UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
			GroupProcessing groupProcessing = new GroupProcessing(configuration, httpclient);
			ProjectProcessing projectProcessing = new ProjectProcessing(configuration, httpclient);

			userProcessing.buildUserObjectClass(schemaBuilder);
			groupProcessing.buildGroupObjectClass(schemaBuilder);
			projectProcessing.buildProjectObjectClass(schemaBuilder);
			return schemaBuilder.build();
		}
		return this.schema;
	}

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions operationOptions) {
		if (objectClass == null) {
			LOGGER.error("Attribute of type ObjectClass not provided.");
			throw new InvalidAttributeValueException("Attribute of type ObjectClass not provided.");
		}
		if (attributes == null) {
			LOGGER.error("Attribute of type Set<Attribute> not provided.");
			throw new InvalidAttributeValueException("Attribute of type Set<Attribute> not provided.");
		}

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) { // __ACCOUNT__
			UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
			return userProcessing.createOrUpdateUser(null, attributes);
		} else if (objectClass.is(ObjectClass.GROUP_NAME)) { // __GROUP__
			GroupProcessing groupProcessing = new GroupProcessing(configuration, httpclient);
			return groupProcessing.createOrUpdateGroup(null, attributes, operationOptions);
		} else if (objectClass.is(PROJECT_NAME)) { // Project
			ProjectProcessing projectProcessing = new ProjectProcessing(configuration, httpclient);
			return projectProcessing.createOrUpdateProject(null, attributes, operationOptions);
		} else {
			LOGGER.error("Attribute of type ObjectClass is not supported.");
			throw new UnsupportedOperationException("Attribute of type ObjectClass is not supported.");
		}
	}

	@Override
	public void delete(ObjectClass objectClass, Uid uid, OperationOptions operationOptions) {

		if (objectClass == null) {
			LOGGER.error("Attribute of type ObjectClass not provided.");
			throw new InvalidAttributeValueException("Attribute of type ObjectClass not provided.");
		}
		if (uid.getUidValue() == null || uid.getUidValue().isEmpty()) {
			LOGGER.error("Attribute of type Uid not provided or is empty.");
			throw new InvalidAttributeValueException("Attribute of type Uid not provided or is empty.");
		}

		LOGGER.info("Delete on {0}, uid: {1}, options: {2}", objectClass, uid.getValue(), operationOptions);

		ObjectProcessing objectProcessing = new ObjectProcessing(configuration, httpclient);

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) { // __ACCOUNT__
			objectProcessing.executeDeleteOperation(uid, USERS);
		} else if (objectClass.is(ObjectClass.GROUP_NAME)) { // __GROUP__
			objectProcessing.executeDeleteOperation(uid, GROUPS);
		} else if (objectClass.is(PROJECT_NAME)) {
			objectProcessing.executeDeleteOperation(uid, PROJECTS);
		} else {
			LOGGER.error("Attribute of type ObjectClass is not supported.");
			throw new UnsupportedOperationException("Attribute of type ObjectClass is not supported.");
		}
	}

	@Override
	public FilterTranslator<Filter> createFilterTranslator(ObjectClass arg0, OperationOptions arg1) {
		return new FilterTranslator<Filter>() {
			@Override
			public List<Filter> translate(Filter filter) {
				return CollectionUtil.newList(filter);
			}
		};
	}

	@Override
	public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {

		if (objectClass == null) {
			LOGGER.error("Attribute of type ObjectClass not provided.");
			throw new InvalidAttributeValueException("Attribute of type ObjectClass is not provided.");
		}

		if (handler == null) {
			LOGGER.error("Attribute of type ResultsHandler not provided.");
			throw new InvalidAttributeValueException("Attribute of type ResultsHandler is not provided.");
		}

		if (options == null) {
			LOGGER.error("Attribute of type OperationOptions not provided.");
			throw new InvalidAttributeValueException("Attribute of type OperationOptions is not provided.");
		}

		LOGGER.info("executeQuery on {0}, filter: {1}, options: {2}", objectClass, query, options);

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
			UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
			userProcessing.executeQueryForUser(query, handler, options);

		} else if (objectClass.is(ObjectClass.GROUP_NAME)) {
			GroupProcessing groupProcessing = new GroupProcessing(configuration, httpclient);
			groupProcessing.executeQueryForGroup(query, handler, options);

		} else if (objectClass.is(PROJECT_NAME)) {
			ProjectProcessing projectProcessing = new ProjectProcessing(configuration, httpclient);
			projectProcessing.executeQueryForProject(query, handler, options);
		} else {
			LOGGER.error("Attribute of type ObjectClass is not supported.");
			throw new UnsupportedOperationException("Attribute of type ObjectClass is not supported.");
		}
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public Set<AttributeDelta> updateDelta(ObjectClass objectClass, Uid uid, Set<AttributeDelta> attrsDelta,
			OperationOptions options) {

		if (objectClass == null) {
			LOGGER.error("Parameter of type ObjectClass not provided.");
			throw new InvalidAttributeValueException("Parameter of type ObjectClass not provided.");
		}

		if (uid.getUidValue() == null || uid.getUidValue().isEmpty()) {
			LOGGER.error("Parameter of type Uid not provided or is empty.");
			throw new InvalidAttributeValueException("Parameter of type Uid not provided or is empty.");
		}

		if (attrsDelta == null) {
			LOGGER.error("Parameter of type Set<AttributeDelta> not provided.");
			throw new InvalidAttributeValueException("Parameter of type Set<AttributeDelta> not provided.");
		}

		if (options == null) {
			LOGGER.error("Parameter of type OperationOptions not provided.");
			throw new InvalidAttributeValueException("Parameter of type OperationOptions not provided.");
		}

		LOGGER.info("updateDelta on {0}, uid: {1}, attrDelta: {2}, options: {3}", objectClass, uid.getValue(),
				attrsDelta, options);

		Set<Attribute> attributeReplace = new HashSet<Attribute>();
		Set<AttributeDelta> attrsDeltaMultivalue = new HashSet<AttributeDelta>();
		for (AttributeDelta attrDelta : attrsDelta) {
			List<Object> replaceValue = attrDelta.getValuesToReplace();
			if (replaceValue != null) {
				attributeReplace.add(AttributeBuilder.build(attrDelta.getName(), replaceValue));
			} else {
				attrsDeltaMultivalue.add(attrDelta);
			}
		}

		if (objectClass.is(ObjectClass.ACCOUNT_NAME)) { // __ACCOUNT__
			Set<AttributeDelta> ret = new HashSet<AttributeDelta>();
			Uid newUid = null;
			if (!attributeReplace.isEmpty()) {
				UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
				newUid = userProcessing.createOrUpdateUser(uid, attributeReplace);
			}
			if (!attrsDeltaMultivalue.isEmpty()) {
				UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
				userProcessing.updateDeltaMultiValues(uid, attrsDeltaMultivalue, options);
			}
			if (newUid == null || newUid.equals(uid)) {
				return ret;
			} else {

				AttributeDelta newUidAttributeDelta = AttributeDeltaBuilder.build(Uid.NAME, newUid.getValue());
				ret.add(newUidAttributeDelta);
				return ret;
			}

		} else if (objectClass.is(ObjectClass.GROUP_NAME)) { // __GROUP__
			Set<AttributeDelta> ret = new HashSet<AttributeDelta>();
			Uid newUid = null;
			if (!attributeReplace.isEmpty()) {
				GroupProcessing groupProcessing = new GroupProcessing(configuration, httpclient);
				newUid = groupProcessing.createOrUpdateGroup(uid, attributeReplace, options);
			}
			if (!attrsDeltaMultivalue.isEmpty()) {
				GroupProcessing groupProcessing = new GroupProcessing(configuration, httpclient);
				groupProcessing.updateDeltaMultiValues(uid, attrsDeltaMultivalue, options);
			}
			if (newUid == null || newUid.equals(uid)) {
				return ret;
			} else {
				AttributeDelta newUidAttributeDelta = AttributeDeltaBuilder.build(Uid.NAME, newUid.getValue());
				ret.add(newUidAttributeDelta);
				return ret;
			}
		}
		if (objectClass.is(PROJECT_NAME)) { // Project
			Set<AttributeDelta> ret = new HashSet<AttributeDelta>();
			Uid newUid = null;
			if (!attributeReplace.isEmpty()) {
				ProjectProcessing projectProcessing = new ProjectProcessing(configuration, httpclient);
				newUid = projectProcessing.createOrUpdateProject(uid, attributeReplace, options);
			}
			if (!attrsDeltaMultivalue.isEmpty()) {
				ProjectProcessing projectProcessing = new ProjectProcessing(configuration, httpclient);
				projectProcessing.updateDeltaMultiValues(uid, attrsDeltaMultivalue, options);
			}
			if (newUid == null || newUid.equals(uid)) {
				return ret;
			} else {
				AttributeDelta newUidAttributeDelta = AttributeDeltaBuilder.build(Uid.NAME, newUid.getValue());
				ret.add(newUidAttributeDelta);
				return ret;
			}
		} else {
			LOGGER.error("The value of the ObjectClass parameter is unsupported.");
			throw new UnsupportedOperationException("The value of the ObjectClass parameter is unsupported.");
		}
	}
}
