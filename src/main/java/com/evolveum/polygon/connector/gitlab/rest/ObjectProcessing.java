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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.GuardedString.Accessor;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.InvalidPasswordException;
import org.identityconnectors.framework.common.exceptions.OperationTimeoutException;
import org.identityconnectors.framework.common.exceptions.PermissionDeniedException;
import org.identityconnectors.framework.common.exceptions.PreconditionFailedException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.spi.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

public class ObjectProcessing {

	protected static final Log LOGGER = Log.getLog(GitlabRestConnector.class);

	private static final String HOST_POSTFIX_API = "/api/v4";
	private static final String HTTP_PROTOCOL = "http";

	protected static final String USERS = "/users";
	protected static final String USERS_MEMBERSHIPS_URL = "memberships";
	protected static final String GROUPS = "/groups";
	protected static final String PROJECTS = "/projects";
	protected static final String MEMBERS = "/members";
	protected static final String SHARE = "/share";
	protected static final String KEYS = "/keys";

	protected static final String USER = "user";
	protected static final String GROUP = "group";
	protected static final String GROUP_MEMBER = "group_member";
	protected static final String PROJECT_MEMBER = "project_member";
	protected static final String PROJECT = "project";
	protected static final String SEARCH = "search";
	protected static final String PAGE = "page";
	protected static final String PER_PAGE = "per_page";
	protected static final String PROJECT_NAME = "Project";

	protected static final String UPLOAD_URL = "/uploads/-/";
	protected static final String PROTOCOL_APPENDER = "://";

	protected static final String ATTR_NAME = "name";
	protected static final String ATTR_WEB_URL = "web_url";
	protected static final String ATTR_AVATAR_URL = "avatar_url";
	protected static final String ATTR_AVATAR = "avatar";
	protected static final String ATTR_CREATED_AT = "created_at";

	protected static final String UID = "id"; // ID
	protected static final String ATTR_USERNAME = "username";
	private URIBuilder uriBuilder;
	protected CloseableHttpClient httpclient;

	protected GitlabRestConfiguration configuration;

	public long firstStartTime;
	public long firstEndTime;
	public long secondStartTime;
	public long secondEndTime;
	public long thirdStartTime;
	public long thirdEndTime;
	public long firstDuration;
	public long secondDuration;
	public long thirdDuration;

	public ObjectProcessing(GitlabRestConfiguration configuration, CloseableHttpClient httpclient) {
		this.configuration = configuration;
		this.httpclient = httpclient;

		StringBuilder sbHost = new StringBuilder();
		sbHost.append(this.configuration.getLoginURL()).append(HOST_POSTFIX_API);
		// Add https support
		String protocol = HTTP_PROTOCOL;
		if (this.configuration.getProtocol() != null && !this.configuration.getProtocol().isEmpty()) {
			protocol = this.configuration.getProtocol();
		}
		this.uriBuilder = new URIBuilder().setScheme(protocol).setHost(sbHost.toString());
	}

	public void test() {
		LOGGER.info("Start test.");
		URIBuilder uriBuilder = getURIBuilder();

		URI uri;
		uriBuilder.setPath(USERS);
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It was not possible create URI from UriBuider:").append(uriBuilder).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
		HttpRequestBase request = new HttpGet(uri);
		callRequest(request, false);
	}

	public URIBuilder getURIBuilder() {
		return this.uriBuilder;
	}

	public CloseableHttpResponse execute(HttpUriRequest request) {
		try {
			CloseableHttpResponse response = httpclient.execute(request);
			LOGGER.info("request: {0}", request);
			return response;
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It was not possible execute HttpUriRequest:").append(request).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorIOException(sb.toString(), e);
		}

	}

	private JSONObject callRequest(HttpEntityEnclosingRequestBase request, JSONObject json, Boolean parseResult) {
		LOGGER.info("request URI: {0}", request.getURI());

		// create header
		final StringBuilder privateToken = new StringBuilder();
		if (this.configuration.getPrivateToken() != null) {
			Accessor accessor = new GuardedString.Accessor() {
				@Override
				public void access(char[] chars) {
					privateToken.append(new String(chars));
				}
			};
			this.configuration.getPrivateToken().access(accessor);
		}
		request.addHeader("PRIVATE-TOKEN", privateToken.toString());
		request.addHeader("Content-Type", "application/json; charset=utf-8");

		// create entity
		HttpEntity entity;
		byte[] jsonByte;
		try {
			jsonByte = json.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed creating byte[] from JSONObject: ").append(json).append(", which was encoded by UTF-8;")
					.append(e.getLocalizedMessage());
			throw new ConnectorIOException(sb.toString(), e);
		}
		entity = new ByteArrayEntity(jsonByte);

		request.setEntity(entity);

		// execute request
		CloseableHttpResponse response = execute(request);
		LOGGER.info("response: {0}", response);

		processResponseErrors(response);
		

		if (!parseResult) {
			return null;
		}

		// result as output
		HttpEntity responseEntity = response.getEntity();
		try {
			// String result = EntityUtils.toString(responseEntity);
			byte[] byteResult = EntityUtils.toByteArray(responseEntity);
			String result = new String(byteResult, "ISO-8859-2");
			responseClose(response);
			LOGGER.info("result: {0}", result);
			return new JSONObject(result);
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed creating result from HttpEntity: ").append(responseEntity).append(";")
					.append(e.getLocalizedMessage());
			responseClose(response);
			throw new ConnectorIOException(sb.toString(), e);
		}
	}

	protected JSONObject callRequest(HttpRequestBase request, Boolean parseResult) {
		LOGGER.info("request URI: {0}", request.getURI());

		// create header
		final StringBuilder privateToken = new StringBuilder();
		if (this.configuration.getPrivateToken() != null) {
			Accessor accessor = new GuardedString.Accessor() {
				@Override
				public void access(char[] chars) {
					privateToken.append(new String(chars));
				}
			};
			this.configuration.getPrivateToken().access(accessor);
		}
		request.addHeader("PRIVATE-TOKEN", privateToken.toString());
		request.addHeader("Content-Type", "application/json; charset=utf-8");
		// execute request
		CloseableHttpResponse response = execute(request);
		LOGGER.info("response: {0}", response);
		processResponseErrors(response);

		if (!parseResult) {
			return null;
		}

		HttpEntity responseEntity = response.getEntity();
		try {
			byte[] byteResult = EntityUtils.toByteArray(responseEntity);
			String result = new String(byteResult, "UTF-8");
			// String result = EntityUtils.toString(responseEntity);
			responseClose(response);
			LOGGER.info("result: {0}", result);
			return new JSONObject(result);
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed creating result from HttpEntity: ").append(responseEntity).append(";")
					.append(e.getLocalizedMessage());
			responseClose(response);
			throw new ConnectorIOException(sb.toString(), e);
		}
	}

	protected JSONArray callRequestForJSONArray(HttpRequestBase request, Boolean parseResult) {
		LOGGER.info("request URI: {0}", request.getURI());

		// create header
		final StringBuilder privateToken = new StringBuilder();
		if (this.configuration.getPrivateToken() != null) {
			Accessor accessor = new GuardedString.Accessor() {
				@Override
				public void access(char[] chars) {
					privateToken.append(new String(chars));
				}
			};
			this.configuration.getPrivateToken().access(accessor);
		}
		request.addHeader("PRIVATE-TOKEN", privateToken.toString());
		request.addHeader("Content-Type", "application/json; charset=utf-8");

		// execute request
		CloseableHttpResponse response = execute(request);
		LOGGER.info("response: {0}", response);
		processResponseErrors(response);

		if (!parseResult) {
			return null;
		}
		response.getAllHeaders();
		HttpEntity responseEntity = response.getEntity();
		try {
			byte[] byteResult = EntityUtils.toByteArray(responseEntity);

			String result = new String(byteResult, "UTF-8");
			responseClose(response);
			LOGGER.info("result: {0}", result);
			return new JSONArray(result);
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed creating result from HttpEntity: ").append(responseEntity).append(";")
					.append(e.getLocalizedMessage());
			responseClose(response);
			throw new ConnectorIOException(sb.toString(), e);
		}
	}

	protected Uid createPutOrPostRequest(Uid uid, String path, JSONObject json, Boolean create) {
		return createPutOrPostRequest(uid, path, json, create, null);
	}

	protected Uid createPutOrPostRequest(Uid uid, String path, JSONObject json, Boolean create, String nameHintKey) {
		URIBuilder uriBuilder = getURIBuilder();

		URI uri;

		// create URI for request
		if (create) {
			uriBuilder.setPath(path);
			LOGGER.info("CREATE POST REQUEST FOR: {0} ", uriBuilder);
		} else {
			StringBuilder sbPath = new StringBuilder();
			sbPath.append(path).append("/").append(uid.getUidValue());
			uriBuilder.setPath(sbPath.toString());
		}

		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It was not possible create URI from UriBuider:").append(uriBuilder).append("; ")
					.append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}

		HttpEntityEnclosingRequestBase request;
		if (create) {
			// create post with created URI
			request = new HttpPost(uri);
		} else {
			// create put with created URI
			request = new HttpPut(uri);
		}

		// execute request
		JSONObject jsonOutput;
		jsonOutput = callRequest(request, json, true);
		// throw exception if id not exist !!!
		int id = jsonOutput.getInt("id");
		String stringId = String.valueOf(id);
		LOGGER.info("acquired uid: {0}", stringId);

		if (nameHintKey != null) {
			String nameValue = jsonOutput.getString(nameHintKey);
			if (nameValue != null) {
				Name nameHint = new Name(nameValue);
				return new Uid(stringId, nameHint);
			}
		}
		return new Uid(stringId);
	}

	protected void putAttrIfExists(Set<Attribute> attributes, String attrNameFromMP, Class<?> type, JSONObject json) {

		LOGGER.info("PutAttrIfExists attributes: {0}, attrNameFromMP: {1}, type {2}, json: {3}", attributes.toString(),
				attrNameFromMP, type, json.toString());

		// put optional attribute

		if (type.equals(String.class)) {
			String valueAttr = getAttr(attributes, attrNameFromMP, String.class, null);
			if (valueAttr != null) {
				json.put(attrNameFromMP, valueAttr);
			}
		} else if (type.equals(Integer.class)) {
			Integer valueAttr = getAttr(attributes, attrNameFromMP, Integer.class, null);
			if (valueAttr != null) {
				json.put(attrNameFromMP, String.valueOf(valueAttr));
			}
		} else if (type.equals(Boolean.class)) {
			Boolean valueAttr = getAttr(attributes, attrNameFromMP, Boolean.class, null);
			if (valueAttr != null) {
				json.put(attrNameFromMP, String.valueOf(valueAttr));
			}
		}
	}

	protected void putAttrIfExists(Set<Attribute> attributes, String attrNameFromMP, Class<?> type, JSONObject json,
			String attrNameToGitlab) {

		LOGGER.info("PutAttrIfExists attributes: {0}, attrNameFromMP: {1}, type {2}, json: {3}, attrNameToGitlab: {4}",
				attributes.toString(), attrNameFromMP, type, json.toString(), attrNameToGitlab);

		if (attrNameToGitlab == null) {
			attrNameToGitlab = attrNameFromMP;
		}

		// put optional attribute

		if (type.equals(String.class)) {
			String valueAttr = getAttr(attributes, attrNameFromMP, String.class, null);
			if (valueAttr != null) {
				json.put(attrNameToGitlab, valueAttr);
			}
		} else if (type.equals(Integer.class)) {
			Integer valueAttr = getAttr(attributes, attrNameFromMP, Integer.class, null);
			if (valueAttr != null) {
				json.put(attrNameToGitlab, String.valueOf(valueAttr));
			}
		} else if (type.equals(Boolean.class)) {
			Boolean valueAttr = getAttr(attributes, attrNameFromMP, Boolean.class, null);
			if (valueAttr != null) {
				json.put(attrNameToGitlab, String.valueOf(valueAttr));
			}
		}
	}

	protected void putRequestedAttrIfExists(Boolean create, Set<Attribute> attributes, String attrNameFromMP,
			JSONObject json) {
		putRequestedAttrIfExists(create, attributes, attrNameFromMP, json, null);
	}

	protected void putRequestedAttrIfExists(Boolean create, Set<Attribute> attributes, String attrNameFromMP,
			JSONObject json, String attrNameToGitlab) {

		LOGGER.info(
				"putRequestedAttrIfExists create {0}, attributes: {1}, attrNameFromMP: {2} json: {3}, attrNameToGitlab: {4}",
				create.toString(), attributes.toString(), attrNameFromMP, json.toString(), attrNameToGitlab);

		if (attrNameToGitlab == null) {
			attrNameToGitlab = attrNameFromMP;
		}

		// put mandatory attribute
		String valueAttr = getAttr(attributes, attrNameFromMP, String.class, null);
		if (create && (StringUtil.isBlank(valueAttr) || valueAttr == null)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Missing value of required attribute:").append(attrNameFromMP).append("; for creating group");
			LOGGER.error(sb.toString());
			throw new InvalidAttributeValueException(sb.toString());
		}
		if (valueAttr != null) {
			json.put(attrNameToGitlab, valueAttr);
		}
	}

	public void executeDeleteOperation(Uid uid, String path) {
		LOGGER.info("Delete object, Uid: {0}, Path: {1}", uid, path);

		URIBuilder uriBuilder = getURIBuilder();
		URI uri;

		// create URI for request
		uriBuilder.setPath(path + "/" + uid.getUidValue());
		try {
			uri = uriBuilder.build();

			HttpRequestBase request;
			request = new HttpDelete(uri);

			callRequest(request, false);

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It was not possible create URI from UriBuider:").append(uriBuilder).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
	}
	
	protected int getTotalPagesByPath(String path) {
		LOGGER.info("getTotalPagesByPath path {0}", path);
		URIBuilder uribuilder = getURIBuilder();
		uribuilder.clearParameters();
		int totalPagesByPath;
		uribuilder.setPath(path);
		uribuilder.setParameter(PER_PAGE, "100");

		try {
			URI uri = uribuilder.build();
			// Get X-Total-Pages
			HttpRequestBase totalPagesrequest = new HttpGet(uri);
			totalPagesByPath = getTotalPages(totalPagesrequest);

			return totalPagesByPath;

		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It was not possible create URI from UriBuider:").append(uriBuilder).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
	}

	protected Object executeGetRequest(String path, Map<String, String> parameters, OperationOptions options,
			Boolean resultIsArray) {
		LOGGER.info("executeGetRequest path {0}, parameters: {1}, options: {2}, result is array: {3}", path, parameters,
				options, resultIsArray);
		URIBuilder uribuilder = getURIBuilder();
		uribuilder.clearParameters();
		int totalPages;
		uribuilder.setPath(path);
		if (options != null) {
			Integer page = options.getPagedResultsOffset();
			Integer perPage = options.getPageSize();
			if (page != null) {
				uribuilder.addParameter(PAGE, page.toString());
			}
			if (perPage != null) {
				uribuilder.addParameter(PER_PAGE, perPage.toString());
			}
		}
		if (parameters != null) {
			for (String key : parameters.keySet()) {
				if (parameters.get(key) != null) {
					uribuilder.addParameter(key, parameters.get(key));
				}
			}
		}

		try {
			URI uri = uribuilder.build();
			HttpRequestBase request = new HttpGet(uri);
			// Get X-Total-Pages
			HttpRequestBase totalPagesrequest = new HttpGet(uri);
			totalPages = getTotalPages(totalPagesrequest);

			if (resultIsArray) {
				if (totalPages == 1) {
					return callRequestForJSONArray(request, true);
				} else {
					JSONArray responce = new JSONArray();
					if (options == null || options.getPageSize() == null) {
						uribuilder.addParameter(PER_PAGE, "100");
					}
					for (int i = 1; i <= totalPages; i++) {
						URI uriPaged = uribuilder.setParameter(PAGE, Integer.toString(i)).build();
						HttpRequestBase requestPaged = new HttpGet(uriPaged);
						JSONArray responcePaged = callRequestForJSONArray(requestPaged, true);
						responce = mergeJSONArrays(responce, responcePaged);
					}
					return responce;

				}
			} else {
				return callRequest(request, true);
			}
		} catch (URISyntaxException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("It was not possible create URI from UriBuider:").append(uriBuilder).append(";")
					.append(e.getLocalizedMessage());
			throw new ConnectorException(sb.toString(), e);
		}
	}

	protected int getUIDIfExists(JSONObject object, String nameAttr, ConnectorObjectBuilder builder) {
		if (object.has(nameAttr)) {
			int uid = object.getInt(nameAttr);
			builder.setUid(new Uid(String.valueOf(uid)));
			return uid;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Missing required attribute: ").append(nameAttr)
					.append("for converting JSONObject to ConnectorObject.");
			throw new InvalidAttributeValueException(sb.toString());
		}
	}

	protected int getUIDIfExists(JSONObject object, String nameAttr) {
		if (object.has(nameAttr)) {
			int uid = object.getInt(nameAttr);
			return uid;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Missing required attribute: ").append(nameAttr)
					.append("for converting JSONObject to ConnectorObject.");
			throw new InvalidAttributeValueException(sb.toString());
		}
	}

	protected void getNAMEIfExists(JSONObject object, String nameAttr, ConnectorObjectBuilder builder) {
		if (object.has(nameAttr)) {
			builder.setName(object.getString(nameAttr));
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Missing required attribute: ").append(nameAttr)
					.append("for converting JSONObject to ConnectorObject.");
			throw new InvalidAttributeValueException(sb.toString());
		}
	}

	protected void invalidAttributeValue(String attrName, Filter query) {
		StringBuilder sb = new StringBuilder();
		sb.append("Value of").append(attrName).append("attribute not provided for query: ").append(query);
		throw new InvalidAttributeValueException(sb.toString());
	}

	protected byte[] getAvatarPhoto(JSONObject object, String attrURLName, String attrName) {

		if (this.configuration.getObjectAvatar().equals("false")) {
			return null;
		}

		if (object.has(attrURLName) && object.get(attrURLName) != null
				&& !JSONObject.NULL.equals(object.get(attrURLName))) {

			HttpEntity responseEntity = null;
			CloseableHttpResponse response = null;
			try {

				String attrURLValue = "";
				if (String.valueOf(object.get(attrURLName)).startsWith(UPLOAD_URL)) {
					attrURLValue = this.configuration.getProtocol() + PROTOCOL_APPENDER
							+ this.configuration.getLoginURL() + String.valueOf(object.get(attrURLName));
				} else {
					attrURLValue = String.valueOf(object.get(attrURLName));
				}
				URIBuilder uriPhoto = new URIBuilder(attrURLValue);
				URI uri = uriPhoto.build();

				LOGGER.ok("uri: {0}", uri);
				HttpRequestBase request = new HttpGet(uri);

				request.addHeader("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");

				final StringBuilder privateToken = new StringBuilder();
				if (this.configuration.getPrivateToken() != null) {
					Accessor accessor = new GuardedString.Accessor() {
						@Override
						public void access(char[] chars) {
							privateToken.append(new String(chars));
						}
					};
					this.configuration.getPrivateToken().access(accessor);
				}
				request.addHeader("PRIVATE-TOKEN", privateToken.toString());

				// execute request
				response = execute(request);
				LOGGER.info("responsePhoto: {0}", response);

			} catch (URISyntaxException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It was not possible create URI from UriBuider; ").append(e.getLocalizedMessage());
				throw new ConnectorException(sb.toString(), e);
			}

			processResponseErrors(response);
			responseEntity = response.getEntity();

			try {

				byte[] byteJPEG = EntityUtils.toByteArray(responseEntity);
				responseClose(response);
				return byteJPEG;

			} catch (IOException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("It was not possible create byte[] from response entity: ").append(responseEntity)
						.append("; ").append(e.getLocalizedMessage());
				responseClose(response);
				throw new ConnectorException(sb.toString(), e);
			}
		}
		return null;

	}

	protected void getIfExists(JSONObject object, String attrName, Class<?> type, ConnectorObjectBuilder builder) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))
				&& !String.valueOf(object.get(attrName)).isEmpty()) {
			if (type.equals(String.class)) {
				addAttr(builder, attrName, String.valueOf(object.get(attrName)));
			} else {
				addAttr(builder, attrName, object.get(attrName));
			}
		}
	}

	protected void getIfExists(JSONObject object, String attrName, Class<?> type, ConnectorObjectBuilder builder,
			String MPName) {
		if (object.has(attrName) && object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))
				&& !String.valueOf(object.get(attrName)).isEmpty()) {
			if (type.equals(String.class)) {
				addAttr(builder, MPName, String.valueOf(object.get(attrName)));
			} else {
				addAttr(builder, MPName, object.get(attrName));
			}
		}
	}

	protected void getIfExistsClampedJSON(JSONObject object, String attrName, String type,
			ConnectorObjectBuilder builder) {
		String fullName = attrName;
		JSONObject processingObject = object;
		while (fullName.contains(".")) {
			String partsName[] = fullName.split("[.]");
			String basicAttrName = partsName[0];
			if (processingObject.has(basicAttrName) && processingObject.get(basicAttrName) != null
					&& !JSONObject.NULL.equals(processingObject.get(basicAttrName))
					&& !String.valueOf(processingObject.get(basicAttrName)).isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (int i = 1; i < partsName.length; i++) {
					sb.append(partsName[i]);
					if (i + 1 != partsName.length) {
						sb.append(".");
					}
				}
				String clampedAttrName = sb.toString();
				JSONObject clampedObject = new JSONObject(String.valueOf(processingObject.get(basicAttrName)));

				if (!clampedAttrName.contains(".") && clampedObject.has(clampedAttrName)
						&& clampedObject.get(clampedAttrName) != null
						&& !JSONObject.NULL.equals(clampedObject.get(clampedAttrName))
						&& !String.valueOf(clampedObject.get(clampedAttrName)).isEmpty()) {
					if (type.equals(String.class.toString())) {
						addAttr(builder, attrName, String.valueOf(clampedObject.get(clampedAttrName)));
					} else {
						addAttr(builder, attrName, clampedObject.get(clampedAttrName));
					}
					break;
				} else {
					fullName = clampedAttrName;
					processingObject = clampedObject;
				}
			} else {
				break;
			}
		}
	}

	protected void getMultiIfExists(JSONObject object, String attrName, ConnectorObjectBuilder builder) {

		if (object.has(attrName)) {
			Object valueObject = object.get(attrName);
			if (valueObject != null && !JSONObject.NULL.equals(valueObject)) {
				List<String> values = new ArrayList<>();
				if (valueObject instanceof JSONArray) {
					JSONArray objectArray = object.getJSONArray(attrName);
					for (int i = 0; i < objectArray.length(); i++) {
						if (objectArray.get(i) instanceof JSONObject) {
							JSONObject jsonObject = objectArray.getJSONObject(i);
							values.add(jsonObject.toString());
						} else {
							values.add(String.valueOf(objectArray.get(i)));
						}
					}
					builder.addAttribute(attrName, values.toArray());
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append("Unsupported value: ").append(valueObject).append(" for attribute name:").append(attrName)
							.append(" from: ").append(object);
					throw new InvalidAttributeValueException(sb.toString());
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal)
			throws InvalidAttributeValueException {
		for (Attribute attr : attributes) {
			if (attrName.equals(attr.getName())) {
				List<Object> vals = attr.getValue();
				if (vals == null || vals.isEmpty()) {
					// set empty value
					return null;
				}
				if (vals.size() == 1) {
					Object val = vals.get(0);
					if (val == null) {
						// set empty value
						return null;
					}
					if (type.isAssignableFrom(val.getClass())) {
						return (T) val;
					}
					StringBuilder sb = new StringBuilder();
					sb.append("Unsupported type ").append(val.getClass()).append(" for attribute ").append(attrName)
							.append(", value: ").append(vals);
					throw new InvalidAttributeValueException(sb.toString());
				}
				StringBuilder sb = new StringBuilder();
				sb.append("More than one value for attribute ").append(attrName).append(", value: ").append(vals);
				throw new InvalidAttributeValueException(sb.toString());
			}
		}
		// set default value when attrName not in changed attributes
		return defaultVal;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	protected <T> T addAttr(ConnectorObjectBuilder builder, String attrName, T attrVal) {
		if (attrVal != null) {
			if (attrVal instanceof String) {
				String unescapeAttrVal = StringEscapeUtils.unescapeXml((String) attrVal);
				builder.addAttribute(attrName, unescapeAttrVal);
			} else {
				builder.addAttribute(attrName, attrVal);
			}
		}
		return attrVal;
	}

	public void processResponseErrors(CloseableHttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode >= 200 && statusCode <= 299) {
			return;
		}
		String responseBody = null;
		try {
			responseBody = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Cannot read response body: ").append(e);
			LOGGER.warn(sb.toString(), e);
		}

		StringBuilder sbMessage = new StringBuilder();
		sbMessage.append("HTTP error ").append(statusCode).append(" ")
				.append(response.getStatusLine().getReasonPhrase()).append(" : ").append(responseBody);

		String message = sbMessage.toString();
		LOGGER.error("{0}", message);
		if (statusCode == 400 || statusCode == 405 || statusCode == 406) {
			if (message.contains("password")) {
				responseClose(response);
				throw new InvalidPasswordException(message);
			} else if (message.contains("\\\"has already been taken\\\"")) {
				// Group and Project return 400 error if they already exist
				responseClose(response);
				throw new AlreadyExistsException(message);
			} else {
				responseClose(response);
				throw new ConnectorIOException(message);
			}
		} else if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 407) {
			responseClose(response);
			throw new PermissionDeniedException(message);
		} else if (statusCode == 404 || statusCode == 410) {
			responseClose(response);
			throw new UnknownUidException(message);
		} else if (statusCode == 408) {
			responseClose(response);
			throw new OperationTimeoutException(message);
		} else if (statusCode == 412) {
			responseClose(response);
			throw new PreconditionFailedException(message);
		} else if (statusCode == 409) {
			responseClose(response);
			throw new AlreadyExistsException(message);
		}
		// other codes
		responseClose(response);
		throw new ConnectorException(message);
	}

	private void responseClose(CloseableHttpResponse response) {
		try {
			response.close();
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder();
			sb.append("Failed close response: ").append(response);
			LOGGER.warn(e, sb.toString());
		}
	}

	private int getTotalPages(HttpRequestBase request) {
		LOGGER.info("request X-Total-Pages: {0}", request.getURI());
		int totalPages;

		final StringBuilder privateToken = new StringBuilder();
		if (this.configuration.getPrivateToken() != null) {
			Accessor accessor = new GuardedString.Accessor() {
				@Override
				public void access(char[] chars) {
					privateToken.append(new String(chars));
				}
			};
			this.configuration.getPrivateToken().access(accessor);
		}
		request.addHeader("PRIVATE-TOKEN", privateToken.toString());
		request.addHeader("Content-Type", "application/json; charset=utf-8");

		// execute request
		CloseableHttpResponse response = execute(request);
		Header responseHeaderTotalPage = response.getFirstHeader("X-Total-Pages");
		if (responseHeaderTotalPage != null) {
			totalPages = Integer.parseInt(responseHeaderTotalPage.getValue());
		} else {
			totalPages = 1;
		}
		LOGGER.info("X-Total-Pages: {0}", totalPages);
		responseClose(response);
		return totalPages;
	}

	private JSONArray mergeJSONArrays(JSONArray rootArr, JSONArray addArr) {

		JSONArray sourceArray = new JSONArray(rootArr.toString());
		JSONArray destinationArray = new JSONArray(addArr.toString());

		for (int i = 0; i < sourceArray.length(); i++) {
			destinationArray.put(sourceArray.getJSONObject(i));
		}
		return destinationArray;
	}

}
