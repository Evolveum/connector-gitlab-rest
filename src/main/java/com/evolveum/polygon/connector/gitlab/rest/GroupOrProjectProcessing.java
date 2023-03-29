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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.AttributeDelta;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;
import org.json.JSONArray;
import org.json.JSONObject;

public class GroupOrProjectProcessing extends ObjectProcessing {

	protected static final String ATTR_PATH = "path";

	protected static final String ATTR_DESCRIPTION = "description";
	protected static final String ATTR_VISIBILITY = "visibility";
	protected static final String ATTR_LFS_ENABLED = "lfs_enabled";
	protected static final String ATTR_REQUEST_ACCESS_ENABLED = "request_access_enabled";
	protected static final String ATTR_USER_ID = "user_id";
	protected static final String ATTR_ACCESS_LEVEL = "access_level";
	protected static final String ATTR_GUEST_MEMBERS = "guest_members";
	protected static final String ATTR_REPORTER_MEMBERS = "reporter_members";
	protected static final String ATTR_DEVELOPER_MEMBERS = "developer_members";
	protected static final String ATTR_MASTER_MEMBERS = "master_members";
	protected static final String ATTR_OWNER_MEMBERS = "owner_members";
	protected static final String ATTR_GUEST_MEMBERS_FULLPATH = "guest_members";
	protected static final String ATTR_REPORTER_MEMBERS_FULLPATH = "reporter_members_fullpath";
	protected static final String ATTR_DEVELOPER_MEMBERS_FULLPATH = "developer_members_fullpath";
	protected static final String ATTR_MASTER_MEMBERS_FULLPATH = "master_members_fullpath";
	protected static final String ATTR_OWNER_MEMBERS_FULLPATH = "owner_members_fullpath";
	protected static final String ATTR_MEMBERS_WITH_NAME = "members_with_name";
	protected static final String ATTR_EXPIRES_AT = "expires_at";

	public GroupOrProjectProcessing(GitlabRestConfiguration configuration, CloseableHttpClient httpclient) {
		super(configuration, httpclient);
	}

	// return map with access lever, that represent integer, and list of user's id
	// (access lever "0" represent name of each members with their access level)
	protected Map<Integer, List<String>> getMembers(URIBuilder uribuilderMember) {
		LOGGER.info("MAP getMembers Start");
		JSONArray objectsMember = new JSONArray();
		JSONArray partOfObjectsMember = new JSONArray();
		int ii = 1;
		do {
			URI uriMember;
			try {
				uribuilderMember.clearParameters();
				uribuilderMember.addParameter(PAGE, String.valueOf(ii));
				uribuilderMember.addParameter(PER_PAGE, "100");
				uriMember = uribuilderMember.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It was not possible create URI from UriBuider:").append(uribuilderMember).append(";")
						.append(e.getLocalizedMessage());
				LOGGER.error(sb.toString());
				throw new ConnectorException(sb.toString(), e);
			}

			HttpRequestBase requestMember = new HttpGet(uriMember);

			partOfObjectsMember = callRequestForJSONArray(requestMember, true);
			Iterator<Object> iterator = partOfObjectsMember.iterator();
			while (iterator.hasNext()) {
				Object member = iterator.next();
				objectsMember.put(member);
			}
			ii++;
		} while (partOfObjectsMember.length() == 100);

		List<String> guestMembers = new ArrayList<>();
		List<String> reporterMembers = new ArrayList<>();
		List<String> developerMembers = new ArrayList<>();
		List<String> masterMembers = new ArrayList<>();
		List<String> ownerMembers = new ArrayList<>();
		List<String> membersWithName = new ArrayList<>();
		Map<Integer, List<String>> members = new HashMap<Integer, List<String>>();
		for (int i = 0; i < objectsMember.length(); i++) {
			JSONObject jsonObjectMember = objectsMember.getJSONObject(i);
			String userId = String.valueOf(jsonObjectMember.get(UID));
			String userName = String.valueOf(jsonObjectMember.get(ATTR_USERNAME));
			String expiresDate = String.valueOf(jsonObjectMember.get(ATTR_EXPIRES_AT));
			int access_level = (int) jsonObjectMember.get(ATTR_ACCESS_LEVEL);
			if (access_level == 10) {
				guestMembers.add(userId);
			}
			if (access_level == 20) {
				reporterMembers.add(userId);
			}
			if (access_level == 30) {
				developerMembers.add(userId);
			}
			if (access_level == 40) {
				masterMembers.add(userId);
			}
			if (access_level == 50) {
				ownerMembers.add(userId);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(userName).append(":").append(access_level).append(":").append(expiresDate);
			membersWithName.add(sb.toString());
		}
		members.put(0, membersWithName);
		members.put(10, guestMembers);
		members.put(20, reporterMembers);
		members.put(30, developerMembers);
		members.put(40, masterMembers);
		members.put(50, ownerMembers);
		LOGGER.info("MAP getMembers End");
		LOGGER.info("MAP getMembers -  members: {0}", members);
		return members;
	}
	
	
	protected Map<Integer, List<String>> getMembersUserRoute(URIBuilder uribuilderMember) {
		LOGGER.info("MAP getMembersUserRoute Start");
		JSONArray objectsMember = new JSONArray();
		JSONArray partOfObjectsMember = new JSONArray();
		int ii = 1;
		do {
			URI uriMember;
			try {
				uribuilderMember.clearParameters();
				uribuilderMember.addParameter(PAGE, String.valueOf(ii));
				uribuilderMember.addParameter(PER_PAGE, "100");
				uriMember = uribuilderMember.build();
			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It was not possible create URI from UriBuider:").append(uribuilderMember).append(";")
						.append(e.getLocalizedMessage());
				LOGGER.error(sb.toString());
				throw new ConnectorException(sb.toString(), e);
			}

			HttpRequestBase requestMember = new HttpGet(uriMember);

			partOfObjectsMember = callRequestForJSONArray(requestMember, true);
			Iterator<Object> iterator = partOfObjectsMember.iterator();
			while (iterator.hasNext()) {
				Object member = iterator.next();
				objectsMember.put(member);
			}
			ii++;
		} while (partOfObjectsMember.length() == 100);

		List<String> guestMembers = new ArrayList<>();
		List<String> reporterMembers = new ArrayList<>();
		List<String> developerMembers = new ArrayList<>();
		List<String> masterMembers = new ArrayList<>();
		List<String> ownerMembers = new ArrayList<>();
		List<String> membersWithName = new ArrayList<>();
		Map<Integer, List<String>> members = new HashMap<Integer, List<String>>();
		for (int i = 0; i < objectsMember.length(); i++) {
			JSONObject jsonObjectMember = objectsMember.getJSONObject(i);
			String userId = String.valueOf(jsonObjectMember.get(UID));
			String userName = String.valueOf(jsonObjectMember.get(ATTR_USERNAME));
			String expiresDate = String.valueOf(jsonObjectMember.get(ATTR_EXPIRES_AT));
			int access_level = (int) jsonObjectMember.get(ATTR_ACCESS_LEVEL);
			if (access_level == 10) {
				guestMembers.add(userId);
			}
			if (access_level == 20) {
				reporterMembers.add(userId);
			}
			if (access_level == 30) {
				developerMembers.add(userId);
			}
			if (access_level == 40) {
				masterMembers.add(userId);
			}
			if (access_level == 50) {
				ownerMembers.add(userId);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(userName).append(":").append(access_level).append(":").append(expiresDate);
			membersWithName.add(sb.toString());
		}
		members.put(0, membersWithName);
		members.put(10, guestMembers);
		members.put(20, reporterMembers);
		members.put(30, developerMembers);
		members.put(40, masterMembers);
		members.put(50, ownerMembers);
		LOGGER.info("MAP getMembersUserRoute End");
		LOGGER.info("MAP getMembers -  members: {0}", members);
		return members;
	}

	protected URIBuilder createRequestForMembers(String path) {
		URIBuilder uribuilderMember = getURIBuilder();
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(path).append(MEMBERS);
		uribuilderMember.setPath(sbPath.toString());
		return uribuilderMember;
	}

	protected void addAttributeForMembers(ConnectorObjectBuilder builder, ResultsHandler handler, String path) {
		URIBuilder uribuilderMember = createRequestForMembers(path);
		Map<Integer, List<String>> members = getMembers(uribuilderMember);

		if (!members.get(10).isEmpty()) {
			builder.addAttribute(ATTR_GUEST_MEMBERS, members.get(10).toArray());
		}
		if (!members.get(20).isEmpty()) {
			builder.addAttribute(ATTR_REPORTER_MEMBERS, members.get(20).toArray());
		}
		if (!members.get(30).isEmpty()) {
			builder.addAttribute(ATTR_DEVELOPER_MEMBERS, members.get(30).toArray());
		}
		if (!members.get(40).isEmpty()) {
			builder.addAttribute(ATTR_MASTER_MEMBERS, members.get(40).toArray());
		}
		if (!members.get(50).isEmpty()) {
			builder.addAttribute(ATTR_OWNER_MEMBERS, members.get(50).toArray());
		}
		if (!members.get(0).isEmpty()) {
			builder.addAttribute(ATTR_MEMBERS_WITH_NAME, members.get(0).toArray());
		}
	}

	public void updateDeltaMultiValuesForGroupOrProject(Uid uid, Set<AttributeDelta> attributesDelta,
			OperationOptions options, String path) {

		LOGGER.info("updateDeltaMultiValuesForGroupOrProject on uid: {0}, attrDelta: {1}, options: {2}", uid.getValue(),
				attributesDelta, options);

		for (AttributeDelta attrDelta : attributesDelta) {

			if (ATTR_GUEST_MEMBERS.equals(attrDelta.getName())) {
				createOrDeleteMember(uid, attrDelta, path, 10);

			}
			if (ATTR_REPORTER_MEMBERS.equals(attrDelta.getName())) {
				createOrDeleteMember(uid, attrDelta, path, 20);

			}
			if (ATTR_DEVELOPER_MEMBERS.equals(attrDelta.getName())) {
				createOrDeleteMember(uid, attrDelta, path, 30);

			}
			if (ATTR_MASTER_MEMBERS.equals(attrDelta.getName())) {
				createOrDeleteMember(uid, attrDelta, path, 40);

			}
			if (ATTR_OWNER_MEMBERS.equals(attrDelta.getName())) {
				createOrDeleteMember(uid, attrDelta, path, 50);

			}
		}
	}

	private void createOrDeleteMember(Uid uid, AttributeDelta attrDelta, String path, int accessLevel) {
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(path).append("/").append(uid.getUidValue()).append(MEMBERS);

		List<Object> addValues = attrDelta.getValuesToAdd();
		List<Object> removeValues = attrDelta.getValuesToRemove();
		if (addValues != null && !addValues.isEmpty()) {

			for (Object addValue : addValues) {
				if (addValue != null) {

					JSONObject json = new JSONObject();
					String userID = (String) addValue;
					json.put(ATTR_USER_ID, userID);
					json.put(ATTR_ACCESS_LEVEL, accessLevel);

					Boolean create = true;
					LOGGER.ok("json: {0}", json.toString());
					createPutOrPostRequest(new Uid(userID), sbPath.toString(), json, create);
				}
			}
		}
		if (removeValues != null && !removeValues.isEmpty()) {
			for (Object removeValue : removeValues) {
				if (removeValue != null) {

					String userID = (String) removeValue;
					executeDeleteOperation(new Uid(userID), sbPath.toString());
				}
			}
		}
	}
}
