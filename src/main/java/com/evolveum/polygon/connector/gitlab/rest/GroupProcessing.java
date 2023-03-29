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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.ContainsAllValuesFilter;
import org.identityconnectors.framework.common.objects.filter.ContainsFilter;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.json.JSONArray;
import org.json.JSONObject;

public class GroupProcessing extends GroupOrProjectProcessing {

	private static final String ATTR_PROJECTS = "projects";
	private static final String ATTR_SHARED_PROJECTS = "shared_projects";
	private static final String ATTR_MEMBERSHIP_LOCK = "membership_lock";
	private static final String ATTR_SHARE_WITH_GROUP_LOCK = "share_with_group_lock";
	private static final String ATTR_PARENT_ID = "parent_id";
	private static final String ATTR_FULL_NAME = "full_name";
	private static final String ATTR_FULL_PATH = "full_path";

	
	public GroupProcessing(GitlabRestConfiguration configuration, CloseableHttpClient httpclient) {
		super(configuration, httpclient);
	}

	public void buildGroupObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder groupObjClassBuilder = new ObjectClassInfoBuilder();

		groupObjClassBuilder.setType(ObjectClass.GROUP_NAME);

		// required
		// ATTR_NAME and ATTR_PATH are required for creating groups
		// However, we don't define ATTR_PATH as createable and updateable because we
		// handle it from the value of ATTR_FULL_PATH which is
		// defined as secondary identifier (__NAME__)
		AttributeInfoBuilder attrNameBuilder = new AttributeInfoBuilder(ATTR_NAME);
		attrNameBuilder.setRequired(true).setType(String.class).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrNameBuilder.build());

		// optional
		// createable: FALSE && updateable: FALSE && readable: TRUE
		AttributeInfoBuilder attrPathBuilder = new AttributeInfoBuilder(ATTR_PATH);
		attrPathBuilder.setRequired(true).setType(String.class).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrPathBuilder.build());

		AttributeInfoBuilder attrAvatarUrlBuilder = new AttributeInfoBuilder(ATTR_AVATAR_URL);
		attrAvatarUrlBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrAvatarUrlBuilder.build());

		AttributeInfoBuilder attrFullNameBuilder = new AttributeInfoBuilder(ATTR_FULL_NAME);
		attrFullNameBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrFullNameBuilder.build());

		AttributeInfoBuilder attrFullPathBuilder = new AttributeInfoBuilder(ATTR_FULL_PATH);
		attrFullPathBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrFullPathBuilder.build());

		AttributeInfoBuilder avatarBuilder = new AttributeInfoBuilder(ATTR_AVATAR);
		avatarBuilder.setType(byte[].class).setCreateable(false).setUpdateable(false).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(avatarBuilder.build());

		AttributeInfoBuilder attrRequestAccessEnabledBuilder = new AttributeInfoBuilder(ATTR_REQUEST_ACCESS_ENABLED);
		attrRequestAccessEnabledBuilder.setType(Boolean.class).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrRequestAccessEnabledBuilder.build());

		AttributeInfoBuilder attrWebUrlBuilder = new AttributeInfoBuilder(ATTR_WEB_URL);
		attrWebUrlBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrWebUrlBuilder.build());

		// createable: TRUE && updateable: TRUE && readable: TRUE
		AttributeInfoBuilder attrDescriptionBuilder = new AttributeInfoBuilder(ATTR_DESCRIPTION);
		attrDescriptionBuilder.setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrDescriptionBuilder.build());

		// attr visibility can be private, internal, or public
		AttributeInfoBuilder attrVisibilityBuilder = new AttributeInfoBuilder(ATTR_VISIBILITY);
		attrVisibilityBuilder.setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrVisibilityBuilder.build());

		AttributeInfoBuilder attrLfsEnabledBuilder = new AttributeInfoBuilder(ATTR_LFS_ENABLED);
		attrLfsEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrLfsEnabledBuilder.build());

		// createable: TRUE && updateable: TRUE && readable: FALSE
		AttributeInfoBuilder attrMembershipLockBuilder = new AttributeInfoBuilder(ATTR_MEMBERSHIP_LOCK);
		attrMembershipLockBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(false)
				.setReturnedByDefault(false);
		groupObjClassBuilder.addAttributeInfo(attrMembershipLockBuilder.build());

		AttributeInfoBuilder attrShareWithGroupLockBuilder = new AttributeInfoBuilder(ATTR_SHARE_WITH_GROUP_LOCK);
		attrShareWithGroupLockBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrShareWithGroupLockBuilder.build());

		AttributeInfoBuilder attrParentIdBuilder = new AttributeInfoBuilder(ATTR_PARENT_ID);
		attrParentIdBuilder.setType(Integer.class).setCreateable(true).setUpdateable(true).setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrParentIdBuilder.build());

		// multivalued: TRUE && createable: FALSE && updateable: FALSE && readable: TRUE
		AttributeInfoBuilder attrProjectsBuilder = new AttributeInfoBuilder(ATTR_PROJECTS);
		attrProjectsBuilder.setType(String.class).setMultiValued(true).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrProjectsBuilder.build());

		AttributeInfoBuilder attrSharedProjectsBuilder = new AttributeInfoBuilder(ATTR_SHARED_PROJECTS);
		attrSharedProjectsBuilder.setType(String.class).setMultiValued(true).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrSharedProjectsBuilder.build());

		AttributeInfoBuilder attrMembersBuilder = new AttributeInfoBuilder(ATTR_MEMBERS_WITH_NAME);
		attrMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrMembersBuilder.build());

		// multivalued: TRUE && createable: TRUE && updateable: TRUE && readable: TRUE
		AttributeInfoBuilder attrGuestMembersBuilder = new AttributeInfoBuilder(ATTR_GUEST_MEMBERS);
		attrGuestMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrGuestMembersBuilder.build());

		AttributeInfoBuilder attrReporterMembersBuilder = new AttributeInfoBuilder(ATTR_REPORTER_MEMBERS);
		attrReporterMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrReporterMembersBuilder.build());

		AttributeInfoBuilder attrDeveloperMembersBuilder = new AttributeInfoBuilder(ATTR_DEVELOPER_MEMBERS);
		attrDeveloperMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrDeveloperMembersBuilder.build());

		AttributeInfoBuilder attrMasterMembersBuilder = new AttributeInfoBuilder(ATTR_MASTER_MEMBERS);
		attrMasterMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrMasterMembersBuilder.build());

		AttributeInfoBuilder attrOwnerMembersBuilder = new AttributeInfoBuilder(ATTR_OWNER_MEMBERS);
		attrOwnerMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		groupObjClassBuilder.addAttributeInfo(attrOwnerMembersBuilder.build());

		schemaBuilder.defineObjectClass(groupObjClassBuilder.build());
	}

	public Uid createOrUpdateGroup(Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {

		LOGGER.info("Start createOrUpdateGroup, Uid: {0}, attributes: {1}", uid, attributes);

		// create or update
		boolean create = uid == null;

		JSONObject json = new JSONObject();

		// mandatory attributes
		putRequestedAttrIfExists(create, attributes, ATTR_NAME, json);
		putRequestedAttrIfExists(create, attributes, Name.NAME, json, ATTR_PATH);

		// optional attributes
		putAttrIfExists(attributes, ATTR_DESCRIPTION, String.class, json);
		putAttrIfExists(attributes, ATTR_VISIBILITY, String.class, json);
		putAttrIfExists(attributes, ATTR_LFS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_REQUEST_ACCESS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_MEMBERSHIP_LOCK, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_SHARE_WITH_GROUP_LOCK, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_PARENT_ID, Integer.class, json);

		if (json.has(ATTR_PATH) && json.getString(ATTR_PATH).contains("/")) {
			// determine the path from fullPath
			String fullPath = json.getString(ATTR_PATH);
			String[] s = fullPath.split("/");
			String path = s[s.length - 1];
			json.put(ATTR_PATH, path);

			// If no parentId, find it by the specified fullPath
			if (!json.has(ATTR_PARENT_ID) && s.length > 1) {
				String parentFullPath = fullPath.substring(0, fullPath.lastIndexOf('/'));

				JSONObject parent = findGroupByFullPath(parentFullPath, operationOptions);
				if (parent != null) {
					json.put(ATTR_PARENT_ID, parent.getInt(UID));
				}
			}
		}

		LOGGER.info("Group request: {0}", json.toString());
		LOGGER.info("Json  length: {0}", json.length());

		Uid returnUid = null;
		if (json.length() != 0) {
			returnUid = createPutOrPostRequest(uid, GROUPS, json, create, ATTR_FULL_PATH);
		}
		return returnUid;
	}

	public ConnectorObjectBuilder convertGroupJSONObjectToConnectorObject(JSONObject group, byte[] avatarPhoto) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setObjectClass(ObjectClass.GROUP);

		getUIDIfExists(group, UID, builder);
		getNAMEIfExists(group, ATTR_FULL_PATH, builder);

		getIfExists(group, ATTR_NAME, String.class, builder);
		getIfExists(group, ATTR_PATH, String.class, builder);
		getIfExists(group, ATTR_WEB_URL, String.class, builder);
		getIfExists(group, ATTR_DESCRIPTION, String.class, builder);
		getIfExists(group, ATTR_VISIBILITY, String.class, builder);
		getIfExists(group, ATTR_LFS_ENABLED, Boolean.class, builder);
		getIfExists(group, ATTR_REQUEST_ACCESS_ENABLED, Boolean.class, builder);
		getIfExists(group, ATTR_SHARE_WITH_GROUP_LOCK, Boolean.class, builder);
		getIfExists(group, ATTR_PARENT_ID, Integer.class, builder);
		getIfExists(group, ATTR_FULL_NAME, String.class, builder);
		getIfExists(group, ATTR_FULL_PATH, String.class, builder);
		
		getMultiIfExists(group, ATTR_PROJECTS, builder);
		getMultiIfExists(group, ATTR_SHARED_PROJECTS, builder);

		addAttr(builder, ATTR_AVATAR, avatarPhoto);

		return builder;
	}

	public void executeQueryForGroup(Filter query, ResultsHandler handler, OperationOptions options) {
		if (query instanceof EqualsFilter) {
			if (((EqualsFilter) query).getAttribute() instanceof Uid) {
				Uid uid = (Uid) ((EqualsFilter) query).getAttribute();
				if (uid.getUidValue() == null) {
					invalidAttributeValue("Uid", query);
				}
				StringBuilder sbPath = new StringBuilder();
				sbPath.append(GROUPS).append("/").append(uid.getUidValue());
				JSONObject group = (JSONObject) executeGetRequest(sbPath.toString(), null, options, false);
				processingObjectFromGET(group, handler, sbPath.toString());
			} else if (((EqualsFilter) query).getAttribute() instanceof Name) {
				Name name = (Name) ((EqualsFilter) query).getAttribute();
				if (name.getNameValue() == null) {
					invalidAttributeValue("Name", query);
				}
				JSONObject group = findGroupByFullPath(name.getNameValue(), options);
				if (group != null) {
					StringBuilder sbPath = new StringBuilder();
					sbPath.append(GROUPS).append("/").append(group.getInt(UID));
					processingObjectFromGET(group, handler, sbPath.toString());
				}
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Illegal search with attribute ").append(((EqualsFilter) query).getAttribute().getName())
						.append(" for query: ").append(query);
				LOGGER.error(sb.toString());
				throw new InvalidAttributeValueException(sb.toString());
			}
		} else if (query instanceof ContainsFilter) {
			if (((ContainsFilter) query).getAttribute().getName().equals("__NAME__")
					|| ((ContainsFilter) query).getAttribute().getName().equals(ATTR_PATH)) {

				List<Object> allValues = ((ContainsFilter) query).getAttribute().getValue();
				if (allValues == null || allValues.get(0) == null) {
					invalidAttributeValue("__NAME__", query);
				}
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put(SEARCH, allValues.get(0).toString());
				JSONArray groups = (JSONArray) executeGetRequest(GROUPS, parameters, options, true);
				processingObjectFromGET(groups, handler);
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Illegal search with attribute ").append(((ContainsFilter) query).getAttribute().getName())
						.append(" for query: ").append(query);
				LOGGER.error(sb.toString());
				throw new InvalidAttributeValueException(sb.toString());
			}
		} else if (query instanceof ContainsAllValuesFilter) {
			// Implemented the use of the "/memberships" route to optimize the query of the
			// accesses of each user
			if (((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_GUEST_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_REPORTER_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_DEVELOPER_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_MASTER_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_OWNER_MEMBERS)) {

				List<Object> allValues = ((ContainsAllValuesFilter) query).getAttribute().getValue();

				for (Object value : allValues) {
					if (value == null) {
						invalidAttributeValue(((ContainsAllValuesFilter) query).getAttribute().getName(), query);
					}
				}

				String uid = String.valueOf(allValues.get(0));

				JSONArray groupsWithMPMembers = new JSONArray();

				UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
				Map<Integer, Integer> groupByAccess = userProcessing.getUserAccess(USERS + "/" + uid + "/" + USERS_MEMBERSHIPS_URL, UserProcessing.TYPE_MEMBERSHIPS_GROUP);


				for (int groupID : groupByAccess.keySet()) {
					URIBuilder uribuilderMember = createRequestForMembers(GROUPS + "/" + groupID);
					Map<Integer, List<String>> mapMembersGroup = getMembers(uribuilderMember);


					final List<String> membersGroup;
					switch (((ContainsAllValuesFilter) query).getAttribute().getName()) {
						case ATTR_GUEST_MEMBERS:
							membersGroup = mapMembersGroup.get(10);
							break;
						case ATTR_REPORTER_MEMBERS:
							membersGroup = mapMembersGroup.get(20);
							break;
						case ATTR_DEVELOPER_MEMBERS:
							membersGroup = mapMembersGroup.get(30);
							break;
						case ATTR_MASTER_MEMBERS:
							membersGroup = mapMembersGroup.get(40);
							break;
						case ATTR_OWNER_MEMBERS:
							membersGroup = mapMembersGroup.get(50);
							break;
						default:
							membersGroup = null;
							break;

					}

					if (membersGroup == null) {
						continue;
					}

					if (new HashSet<>(membersGroup).containsAll(allValues)) {
						final JSONObject group = findGroupByID(Integer.toString(groupID), options);
						groupsWithMPMembers.put(group);
					}
				}
				LOGGER.info("groupsWithMPMembers -  members: {0}", groupsWithMPMembers);
				processingObjectFromGET(groupsWithMPMembers, handler);
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Illegal search with attribute ").append(((ContainsAllValuesFilter) query).getAttribute().getName())
						.append(" for query: ").append(query);
				LOGGER.error(sb.toString());
				throw new InvalidAttributeValueException(sb.toString());
			}
		} else if (query == null) {
			JSONArray groups = (JSONArray) executeGetRequest(GROUPS, null, options, true);
			processingObjectFromGET(groups, handler);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Unexpected filter ").append(query.getClass());
			LOGGER.error(sb.toString());
			throw new ConnectorIOException(sb.toString());
		}
	}

	private JSONObject findGroupByFullPath(String fullPath, OperationOptions options) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("search", fullPath);
		JSONArray groups = (JSONArray) executeGetRequest(GROUPS, parameters, options, true);

		for (int i = 0; i < groups.length(); i++) {
			JSONObject group = groups.getJSONObject(i);
			// full_path is case-insensitive
			if (group.getString(ATTR_FULL_PATH).equalsIgnoreCase(fullPath)) {
				return group;
			}
		}
		return null;
	}

	private JSONObject findGroupByID(String groupID, OperationOptions options) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("with_custom_attributes", "no");
		parameters.put("with_projects", "no");
		StringBuilder sbPath = new StringBuilder();

		sbPath.append(GROUPS).append("/").append(groupID);
		JSONObject group = (JSONObject) executeGetRequest(sbPath.toString(), parameters, options, false);
		if (group.getInt(UID) == Integer.parseInt(groupID)) {
			return group;
		}
		return null;
	}

	private void processingObjectFromGET(JSONObject group, ResultsHandler handler, String sbPath) {
		byte[] avatarPhoto = getAvatarPhoto(group, ATTR_AVATAR_URL, ATTR_AVATAR);
		ConnectorObjectBuilder builder = convertGroupJSONObjectToConnectorObject(group, avatarPhoto);
		addAttributeForMembers(builder, handler, sbPath);
		ConnectorObject connectorObject = builder.build();
		handler.handle(connectorObject);
	}

	private void processingObjectFromGET(JSONArray groups, ResultsHandler handler) {
		JSONObject group;
		for (int i = 0; i < groups.length(); i++) {
			group = groups.getJSONObject(i);
			processingObjectFromGET(group, handler, GROUPS + "/" + group.get(UID));
		}
	}

	public void updateDeltaMultiValues(Uid uid, Set<AttributeDelta> attributes, OperationOptions options) {
		updateDeltaMultiValuesForGroupOrProject(uid, attributes, options, GROUPS);
	}

}
