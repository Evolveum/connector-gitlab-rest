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

import java.util.ArrayList;
import java.util.HashMap;
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

public class ProjectProcessing extends GroupOrProjectProcessing {

	private static final String ATTR_DEFAULT_BRANCH = "default_branch";
	private static final String ATTR_TAG_LIST = "tag_list";
	private static final String ATTR_ARCHIVED = "archived";
	private static final String ATTR_SSH_URL_TO_REPO = "ssh_url_to_repo";
	private static final String ATTR_HTTP_URL_TO_REPO = "http_url_to_repo";
	private static final String ATTR_NAME_WITH_NAMESPACE = "name_with_namespace";
	private static final String ATTR_PATH_WITH_NAMESPACE = "path_with_namespace";
	private static final String ATTR_CONTAINER_REGISTRY_ENABLED = "container_registry_enabled";
	private static final String ATTR_ISSUES_ENABLED = "issues_enabled";
	private static final String ATTR_MERGE_REQUESTS_ENABLED = "merge_requests_enabled";
	private static final String ATTR_WIKI_ENABLED = "wiki_enabled";
	private static final String ATTR_BUILDS_ENABLED = "builds_enabled";
	private static final String ATTR_JOBS_ENABLED = "jobs_enabled";
	private static final String ATTR_SNIPPETS_ENABLED = "snippets_enabled";
	private static final String ATTR_LAST_ACTIVITY_AT = "last_activity_at";
	private static final String ATTR_SHARED_RUNNERS_ENABLED = "shared_runners_enabled";
	private static final String ATTR_CREATOR_ID = "creator_id";
	private static final String ATTR_NAMESPACE_ID = "namespace.id";
	private static final String ATTR_NAMESPACE_NAME = "namespace.name";
	private static final String ATTR_NAMESPACE_PATH = "namespace.path";
	private static final String ATTR_NAMESPACE_KIND = "namespace.kind";
	private static final String ATTR_NAMESPACE_FULL_PATH = "namespace.full_path";
	private static final String ATTR_STAR_COUNT = "star_count";
	private static final String ATTR_FORKS_COUNT = "forks_count";
	private static final String ATTR_OPEN_ISSUES_COUNT = "open_issues_count";
	private static final String ATTR_PUBLIC_BUILDS = "public_builds";
	private static final String ATTR_PUBLIC_JOBS = "public_jobs";
	private static final String ATTR_SHARED_WITH_GROUPS = "shared_with_groups";
	private static final String ATTR_ONLY_ALLOW_MERGE_IF_PIPELINE_SUCCEEDS = "only_allow_merge_if_pipeline_succeeds";
	private static final String ATTR_ONLY_ALLOW_MERGE_IF_ALL_DISCUSSIONS_ARE_RESOLVED = "only_allow_merge_if_all_discussions_are_resolved";
	private static final String ATTR_RUNNERS_TOKEN = "runners_token";
	private static final String ATTR_IMPORT_STATUS = "import_status";
	private static final String ATTR_IMPORT_ERROR = "import_error";
	private static final String ATTR_PERMISSIONS_PROJECT_ACCESS_LEVEL = "permissions.project_access.access_level";
	private static final String ATTR_PERMISSIONS_PROJECT_ACCESS_NOTIFICATION_LEVEL = "permissions.project_access.notification_level";
	private static final String ATTR_PERMISSIONS_GROUP_ACCESS_LEVEL = "permissions.group_access.access_level";
	private static final String ATTR_PERMISSIONS_GROUP_ACCESS_NOTIFICATION_LEVEL = "permissions.group_access.notification_level";
	private static final String ATTR_OWNER_NAME = "owner.name";
	private static final String ATTR_OWNER_USERNAME = "owner.username";
	private static final String ATTR_OWNER_ID = "owner.id";
	private static final String ATTR_OWNER_STATE = "owner.state";
	private static final String ATTR_OWNER_AVATAR_URL = "owner.avatar_url";
	private static final String ATTR_OWNER_WEB_URL = "owner.web_url";

	private static final String ATTR_SHARED_WITH_GROUPS_ID_MAX_GUEST = "shared_with_groups_max_guest";
	private static final String ATTR_SHARED_WITH_GROUPS_ID_MAX_REPORTER = "shared_with_groups_reporter";
	private static final String ATTR_SHARED_WITH_GROUPS_ID_MAX_DEVELOPER = "shared_with_groups_max_developer";
	private static final String ATTR_SHARED_WITH_GROUPS_ID_MAX_MASTER = "shared_with_groups_max_master";
	private static final String ATTR_SHARED_WITH_GROUPS_WITH_NAME = "shared_with_groups.name-access_level";

	private static final String ATTR_GROUP_ID = "group_id";
	//private static final String ATTR_GROUP_NAME = "group_name";
	private static final String ATTR_GROUP_FULL_PATH = "group_full_path";
	private static final String ATTR_GROUP_ACCESS_LEVEL = "group_access_level";
	private static final String ATTR_GROUP_ACCESS = "group_access";

	public ProjectProcessing(GitlabRestConfiguration configuration, CloseableHttpClient httpclient) {
		super(configuration, httpclient);
	}

	public void buildProjectObjectClass(SchemaBuilder schemaBuilder) {
		ObjectClassInfoBuilder projectObjClassBuilder = new ObjectClassInfoBuilder();

		projectObjClassBuilder.setType(PROJECT_NAME);

		// optional
		// createable: TRUE && updateable: TRUE && readable: TRUE
		AttributeInfoBuilder attrPathBuilder = new AttributeInfoBuilder(ATTR_PATH);
		attrPathBuilder.setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPathBuilder.build());

		AttributeInfoBuilder attrDescriptionBuilder = new AttributeInfoBuilder(ATTR_DESCRIPTION);
		attrDescriptionBuilder.setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrDescriptionBuilder.build());

		AttributeInfoBuilder attrDefaultBranchBuilder = new AttributeInfoBuilder(ATTR_DEFAULT_BRANCH);
		attrDefaultBranchBuilder.setType(String.class).setCreateable(false).setUpdateable(true).setReadable(true)
				.setReturnedByDefault(true);
		projectObjClassBuilder.addAttributeInfo(attrDefaultBranchBuilder.build());

		// attr visibility can be private, internal, or public
		AttributeInfoBuilder attrVisibilityLevelBuilder = new AttributeInfoBuilder(ATTR_VISIBILITY);
		attrVisibilityLevelBuilder.setType(String.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrVisibilityLevelBuilder.build());

		AttributeInfoBuilder attrLfsEnabledBuilder = new AttributeInfoBuilder(ATTR_LFS_ENABLED);
		attrLfsEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrLfsEnabledBuilder.build());

		AttributeInfoBuilder attrRequestAccessEnabledBuilder = new AttributeInfoBuilder(ATTR_REQUEST_ACCESS_ENABLED);
		attrRequestAccessEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrRequestAccessEnabledBuilder.build());

		AttributeInfoBuilder attrPublicBuilder = new AttributeInfoBuilder(ATTR_PUBLIC_JOBS);
		attrPublicBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPublicBuilder.build());

		AttributeInfoBuilder attrContainerRegistryEnabledBuilder = new AttributeInfoBuilder(
				ATTR_CONTAINER_REGISTRY_ENABLED);
		attrContainerRegistryEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrContainerRegistryEnabledBuilder.build());

		AttributeInfoBuilder attrIssuesEnabledBuilder = new AttributeInfoBuilder(ATTR_ISSUES_ENABLED);
		attrIssuesEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrIssuesEnabledBuilder.build());

		AttributeInfoBuilder attrMergeRequestsEnabledBuilder = new AttributeInfoBuilder(ATTR_MERGE_REQUESTS_ENABLED);
		attrMergeRequestsEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrMergeRequestsEnabledBuilder.build());

		AttributeInfoBuilder attrWikiEnabledBuilder = new AttributeInfoBuilder(ATTR_WIKI_ENABLED);
		attrWikiEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrWikiEnabledBuilder.build());

//		AttributeInfoBuilder attrBuildsEnabledBuilder = new AttributeInfoBuilder(ATTR_BUILDS_ENABLED);
//		attrBuildsEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
//		projectObjClassBuilder.addAttributeInfo(attrBuildsEnabledBuilder.build());

		AttributeInfoBuilder attrSnippetsEnabledBuilder = new AttributeInfoBuilder(ATTR_SNIPPETS_ENABLED);
		attrSnippetsEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSnippetsEnabledBuilder.build());

		AttributeInfoBuilder attrSharedRunnersEnabledBuilder = new AttributeInfoBuilder(ATTR_SHARED_RUNNERS_ENABLED);
		attrSharedRunnersEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedRunnersEnabledBuilder.build());

		AttributeInfoBuilder attrJobsEnabledBuilder = new AttributeInfoBuilder(ATTR_JOBS_ENABLED);
		attrJobsEnabledBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrJobsEnabledBuilder.build());

//		AttributeInfoBuilder attrOnlyAllowMergeIfBuildSucceeedsBuilder = new AttributeInfoBuilder(ATTR_ONLY_ALLOW_MERGE_IF_BUILD_SUCCEEDS);
//		attrOnlyAllowMergeIfBuildSucceeedsBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(true);
//		projectObjClassBuilder.addAttributeInfo(attrOnlyAllowMergeIfBuildSucceeedsBuilder.build());

		AttributeInfoBuilder attrOnlyAllowMergeIfPipelineSucceeedsBuilder = new AttributeInfoBuilder(
				ATTR_ONLY_ALLOW_MERGE_IF_PIPELINE_SUCCEEDS);
		attrOnlyAllowMergeIfPipelineSucceeedsBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOnlyAllowMergeIfPipelineSucceeedsBuilder.build());

		AttributeInfoBuilder attrOnlyAllowMergeIfAllDiscussionsAreResolvedBuilder = new AttributeInfoBuilder(
				ATTR_ONLY_ALLOW_MERGE_IF_ALL_DISCUSSIONS_ARE_RESOLVED);
		attrOnlyAllowMergeIfAllDiscussionsAreResolvedBuilder.setType(Boolean.class).setCreateable(true)
				.setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOnlyAllowMergeIfAllDiscussionsAreResolvedBuilder.build());

//		//createable: TRUE && updateable: TRUE && readable: FALSE
//		AttributeInfoBuilder attrPublicBuildsBuilder = new AttributeInfoBuilder(ATTR_PUBLIC_BUILDS);
//		attrPublicBuildsBuilder.setType(Boolean.class).setCreateable(true).setUpdateable(true).setReadable(false);
//		projectObjClassBuilder.addAttributeInfo(attrPublicBuildsBuilder.build());

		// createable: FALSE && updateable: FALSE && readable: TRUE
		AttributeInfoBuilder attrNameWithNamespaceBuilder = new AttributeInfoBuilder(ATTR_NAME_WITH_NAMESPACE);
		attrNameWithNamespaceBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrNameWithNamespaceBuilder.build());

		AttributeInfoBuilder attrPathWithNamespaceBuilder = new AttributeInfoBuilder(ATTR_PATH_WITH_NAMESPACE);
		attrPathWithNamespaceBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPathWithNamespaceBuilder.build());

		AttributeInfoBuilder attrWebUrlBuilder = new AttributeInfoBuilder(ATTR_WEB_URL);
		attrWebUrlBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrWebUrlBuilder.build());

		AttributeInfoBuilder attrArchivedBuilder = new AttributeInfoBuilder(ATTR_ARCHIVED);
		attrArchivedBuilder.setType(Boolean.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrArchivedBuilder.build());

		AttributeInfoBuilder attrSSHUrlToRepoBuilder = new AttributeInfoBuilder(ATTR_SSH_URL_TO_REPO);
		attrSSHUrlToRepoBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSSHUrlToRepoBuilder.build());

		AttributeInfoBuilder attrHTTPUrlToRepoBuilder = new AttributeInfoBuilder(ATTR_HTTP_URL_TO_REPO);
		attrHTTPUrlToRepoBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrHTTPUrlToRepoBuilder.build());

		AttributeInfoBuilder attrCreateAtBuilder = new AttributeInfoBuilder(ATTR_CREATED_AT);
		attrCreateAtBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrCreateAtBuilder.build());

		AttributeInfoBuilder attrLastActivityAtBuilder = new AttributeInfoBuilder(ATTR_LAST_ACTIVITY_AT);
		attrLastActivityAtBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrLastActivityAtBuilder.build());

		AttributeInfoBuilder attrCreatorIDBuilder = new AttributeInfoBuilder(ATTR_CREATOR_ID);
		attrCreatorIDBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrCreatorIDBuilder.build());

		AttributeInfoBuilder attrNameSpaceIdBuilder = new AttributeInfoBuilder(ATTR_NAMESPACE_ID);
		attrNameSpaceIdBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrNameSpaceIdBuilder.build());

		AttributeInfoBuilder attrNameSpaceNameBuilder = new AttributeInfoBuilder(ATTR_NAMESPACE_NAME);
		attrNameSpaceNameBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrNameSpaceNameBuilder.build());

		AttributeInfoBuilder attrNameSpacePathBuilder = new AttributeInfoBuilder(ATTR_NAMESPACE_PATH);
		attrNameSpacePathBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrNameSpacePathBuilder.build());

		AttributeInfoBuilder attrNameSpaceKindBuilder = new AttributeInfoBuilder(ATTR_NAMESPACE_KIND);
		attrNameSpaceKindBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrNameSpaceKindBuilder.build());

		AttributeInfoBuilder attrNameSpaceFullPathBuilder = new AttributeInfoBuilder(ATTR_NAMESPACE_FULL_PATH);
		attrNameSpaceFullPathBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrNameSpaceFullPathBuilder.build());

		AttributeInfoBuilder attrStarCountBuilder = new AttributeInfoBuilder(ATTR_STAR_COUNT);
		attrStarCountBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrStarCountBuilder.build());

		AttributeInfoBuilder attrForksCountBuilder = new AttributeInfoBuilder(ATTR_FORKS_COUNT);
		attrForksCountBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrForksCountBuilder.build());

		AttributeInfoBuilder attrOpenIssuesCountBuilder = new AttributeInfoBuilder(ATTR_OPEN_ISSUES_COUNT);
		attrOpenIssuesCountBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOpenIssuesCountBuilder.build());

		AttributeInfoBuilder attrPermissionsGroupAccessLevelBuilder = new AttributeInfoBuilder(
				ATTR_PERMISSIONS_GROUP_ACCESS_LEVEL);
		attrPermissionsGroupAccessLevelBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPermissionsGroupAccessLevelBuilder.build());

		AttributeInfoBuilder attrPermissionsGroupNotificationLevelBuilder = new AttributeInfoBuilder(
				ATTR_PERMISSIONS_GROUP_ACCESS_NOTIFICATION_LEVEL);
		attrPermissionsGroupNotificationLevelBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPermissionsGroupNotificationLevelBuilder.build());

		AttributeInfoBuilder attrPermissionsProjectAccessLevelBuilder = new AttributeInfoBuilder(
				ATTR_PERMISSIONS_PROJECT_ACCESS_LEVEL);
		attrPermissionsProjectAccessLevelBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPermissionsProjectAccessLevelBuilder.build());

		AttributeInfoBuilder attrPermissionsProjectNotificationLevelBuilder = new AttributeInfoBuilder(
				ATTR_PERMISSIONS_PROJECT_ACCESS_NOTIFICATION_LEVEL);
		attrPermissionsProjectNotificationLevelBuilder.setType(Integer.class).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrPermissionsProjectNotificationLevelBuilder.build());

		AttributeInfoBuilder attrOwnerNameBuilder = new AttributeInfoBuilder(ATTR_OWNER_NAME);
		attrOwnerNameBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOwnerNameBuilder.build());

		AttributeInfoBuilder attrOwnerUsernameBuilder = new AttributeInfoBuilder(ATTR_OWNER_USERNAME);
		attrOwnerUsernameBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOwnerUsernameBuilder.build());

		AttributeInfoBuilder attrOwnerIdBuilder = new AttributeInfoBuilder(ATTR_OWNER_ID);
		attrOwnerIdBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOwnerIdBuilder.build());

		AttributeInfoBuilder attrOwnerStateBuilder = new AttributeInfoBuilder(ATTR_OWNER_STATE);
		attrOwnerStateBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOwnerStateBuilder.build());

		AttributeInfoBuilder attrOwnerAvatarUrlBuilder = new AttributeInfoBuilder(ATTR_OWNER_AVATAR_URL);
		attrOwnerAvatarUrlBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOwnerAvatarUrlBuilder.build());

		AttributeInfoBuilder attrOwnerWebUrlBuilder = new AttributeInfoBuilder(ATTR_OWNER_WEB_URL);
		attrOwnerWebUrlBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrOwnerWebUrlBuilder.build());

		AttributeInfoBuilder attrAvatarUrlBuilder = new AttributeInfoBuilder(ATTR_AVATAR_URL);
		attrAvatarUrlBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrAvatarUrlBuilder.build());

		AttributeInfoBuilder attrRunnersTokenBuilder = new AttributeInfoBuilder(ATTR_RUNNERS_TOKEN);
		attrRunnersTokenBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrRunnersTokenBuilder.build());

		AttributeInfoBuilder attrImportStatusBuilder = new AttributeInfoBuilder(ATTR_IMPORT_STATUS);
		attrImportStatusBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrImportStatusBuilder.build());

		AttributeInfoBuilder attrImportErrorBuilder = new AttributeInfoBuilder(ATTR_IMPORT_ERROR);
		attrImportErrorBuilder.setType(String.class).setCreateable(false).setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrImportErrorBuilder.build());

		AttributeInfoBuilder avatarBuilder = new AttributeInfoBuilder(ATTR_AVATAR);
		avatarBuilder.setType(byte[].class).setCreateable(false).setUpdateable(false).setReadable(true)
				.setReturnedByDefault(true);
		projectObjClassBuilder.addAttributeInfo(avatarBuilder.build());

		// multivalued: TRUE && createable: FALSE && updateable: FALSE && readable: TRUE
		AttributeInfoBuilder attrSharedWithGroupsBuilder = new AttributeInfoBuilder(ATTR_SHARED_WITH_GROUPS);
		attrSharedWithGroupsBuilder.setType(String.class).setMultiValued(true).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedWithGroupsBuilder.build());

		AttributeInfoBuilder attrMembersBuilder = new AttributeInfoBuilder(ATTR_MEMBERS_WITH_NAME);
		attrMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(false).setUpdateable(false)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrMembersBuilder.build());

		AttributeInfoBuilder attrSharedWithGroupsWithNameBuilder = new AttributeInfoBuilder(
				ATTR_SHARED_WITH_GROUPS_WITH_NAME);
		attrSharedWithGroupsWithNameBuilder.setType(String.class).setMultiValued(true).setCreateable(false)
				.setUpdateable(false).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedWithGroupsWithNameBuilder.build());

		// multivalued: TRUE && createable: TRUE && updateable: TRUE && readable:TRUE
		AttributeInfoBuilder attrTagListBuilder = new AttributeInfoBuilder(ATTR_TAG_LIST);
		attrTagListBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true).setReturnedByDefault(true);
		projectObjClassBuilder.addAttributeInfo(attrTagListBuilder.build());

		AttributeInfoBuilder attrGuestMembersBuilder = new AttributeInfoBuilder(ATTR_GUEST_MEMBERS);
		attrGuestMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrGuestMembersBuilder.build());

		AttributeInfoBuilder attrReporterMembersBuilder = new AttributeInfoBuilder(ATTR_REPORTER_MEMBERS);
		attrReporterMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrReporterMembersBuilder.build());

		AttributeInfoBuilder attrDeveloperMembersBuilder = new AttributeInfoBuilder(ATTR_DEVELOPER_MEMBERS);
		attrDeveloperMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrDeveloperMembersBuilder.build());

		AttributeInfoBuilder attrMasterMembersBuilder = new AttributeInfoBuilder(ATTR_MASTER_MEMBERS);
		attrMasterMembersBuilder.setType(String.class).setMultiValued(true).setCreateable(true).setUpdateable(true)
				.setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrMasterMembersBuilder.build());

		AttributeInfoBuilder attrSharedWithGroupsMaxGuestBuilder = new AttributeInfoBuilder(
				ATTR_SHARED_WITH_GROUPS_ID_MAX_GUEST);
		attrSharedWithGroupsMaxGuestBuilder.setType(String.class).setMultiValued(true).setCreateable(true)
				.setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedWithGroupsMaxGuestBuilder.build());

		AttributeInfoBuilder attrSharedWithGroupsMaxReporterBuilder = new AttributeInfoBuilder(
				ATTR_SHARED_WITH_GROUPS_ID_MAX_REPORTER);
		attrSharedWithGroupsMaxReporterBuilder.setType(String.class).setMultiValued(true).setCreateable(true)
				.setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedWithGroupsMaxReporterBuilder.build());

		AttributeInfoBuilder attrSharedWithGroupsMaxDeveloperBuilder = new AttributeInfoBuilder(
				ATTR_SHARED_WITH_GROUPS_ID_MAX_DEVELOPER);
		attrSharedWithGroupsMaxDeveloperBuilder.setType(String.class).setMultiValued(true).setCreateable(true)
				.setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedWithGroupsMaxDeveloperBuilder.build());

		AttributeInfoBuilder attrSharedWithGroupsMaxMasterBuilder = new AttributeInfoBuilder(
				ATTR_SHARED_WITH_GROUPS_ID_MAX_MASTER);
		attrSharedWithGroupsMaxMasterBuilder.setType(String.class).setMultiValued(true).setCreateable(true)
				.setUpdateable(true).setReadable(true);
		projectObjClassBuilder.addAttributeInfo(attrSharedWithGroupsMaxMasterBuilder.build());

		schemaBuilder.defineObjectClass(projectObjClassBuilder.build());
	}

	public Uid createOrUpdateProject(Uid uid, Set<Attribute> attributes, OperationOptions operationOptions) {

		LOGGER.info("Start createOrUpdateProject, Uid: {0}, attributes: {1}", uid, attributes);

		// create or update
		Boolean create = (uid == null) ? true : false;

		JSONObject json = new JSONObject();

		// mandatory attributes
		putRequestedAttrIfExists(create, attributes, "__NAME__", json, ATTR_NAME);
		putRequestedAttrIfExists(create, attributes, Name.NAME, json, ATTR_PATH_WITH_NAMESPACE);

		// optional attributes
		putAttrIfExists(attributes, ATTR_PATH, String.class, json);
		putAttrIfExists(attributes, ATTR_DESCRIPTION, String.class, json);
		putAttrIfExists(attributes, ATTR_ISSUES_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_MERGE_REQUESTS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_WIKI_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_BUILDS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_SNIPPETS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_SHARED_RUNNERS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_CONTAINER_REGISTRY_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_LFS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_PUBLIC_JOBS, Boolean.class, json, ATTR_PUBLIC_BUILDS);
		putAttrIfExists(attributes, ATTR_VISIBILITY, String.class, json);
		putAttrIfExists(attributes, ATTR_JOBS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_REQUEST_ACCESS_ENABLED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_ONLY_ALLOW_MERGE_IF_ALL_DISCUSSIONS_ARE_RESOLVED, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_ONLY_ALLOW_MERGE_IF_PIPELINE_SUCCEEDS, Boolean.class, json);
		putAttrIfExists(attributes, ATTR_DEFAULT_BRANCH, String.class, json);

		putTagListIfExists(attributes, json);

		LOGGER.info("Project request: {0}", json.toString());

		// Handling the case of Projects with fullpath
		return createPutOrPostRequest(uid, PROJECTS, json, create, ATTR_PATH_WITH_NAMESPACE);
		// return createPutOrPostRequest(uid, PROJECTS, json, create);
	}

	protected void putTagListIfExists(Set<Attribute> attributes, JSONObject json) {

		LOGGER.info("PutTagListIfExists attributes: {0}, json: {1}", attributes.toString(), json.toString());

		for (Attribute attr : attributes) {
			if (ATTR_TAG_LIST.equals(attr.getName())) {
				List<Object> vals = attr.getValue();
				if (vals != null && !vals.isEmpty()) {
					json.put(ATTR_TAG_LIST, vals);
				}

			}
		}
	}

	public ConnectorObjectBuilder convertProjectJSONObjectToConnectorObject(JSONObject project, byte[] avatarPhoto) {
		ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
		builder.setObjectClass(new ObjectClass(PROJECT_NAME));

		getUIDIfExists(project, UID, builder);
		// getNAMEIfExists(project, ATTR_NAME, builder);
		getNAMEIfExists(project, ATTR_PATH_WITH_NAMESPACE, builder);

		getIfExists(project, ATTR_PATH, String.class, builder);
		getIfExists(project, ATTR_DEFAULT_BRANCH, String.class, builder);
		getIfExists(project, ATTR_PUBLIC_JOBS, Boolean.class, builder);
		getIfExists(project, ATTR_WEB_URL, String.class, builder);
		getIfExists(project, ATTR_DESCRIPTION, String.class, builder);
		getIfExists(project, ATTR_VISIBILITY, String.class, builder);
		getIfExists(project, ATTR_LFS_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_REQUEST_ACCESS_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_ARCHIVED, Boolean.class, builder);
		getIfExists(project, ATTR_SSH_URL_TO_REPO, String.class, builder);
		getIfExists(project, ATTR_HTTP_URL_TO_REPO, String.class, builder);
		getIfExists(project, ATTR_CONTAINER_REGISTRY_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_NAME_WITH_NAMESPACE, String.class, builder);
		getIfExists(project, ATTR_PATH_WITH_NAMESPACE, String.class, builder);
		getIfExists(project, ATTR_ISSUES_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_MERGE_REQUESTS_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_WIKI_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_BUILDS_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_SNIPPETS_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_SHARED_RUNNERS_ENABLED, Boolean.class, builder);
		getIfExists(project, ATTR_CREATED_AT, String.class, builder);
		getIfExists(project, ATTR_STAR_COUNT, Integer.class, builder);
		getIfExists(project, ATTR_FORKS_COUNT, Integer.class, builder);
		getIfExists(project, ATTR_OPEN_ISSUES_COUNT, Integer.class, builder);
		getIfExists(project, ATTR_JOBS_ENABLED, Integer.class, builder);
		getIfExists(project, ATTR_ONLY_ALLOW_MERGE_IF_ALL_DISCUSSIONS_ARE_RESOLVED, Boolean.class, builder);
		getIfExists(project, ATTR_ONLY_ALLOW_MERGE_IF_PIPELINE_SUCCEEDS, Boolean.class, builder);

		getIfExistsClampedJSON(project, ATTR_NAMESPACE_ID, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_NAMESPACE_NAME, String.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_NAMESPACE_PATH, String.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_NAMESPACE_KIND, String.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_NAMESPACE_FULL_PATH, String.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_PERMISSIONS_GROUP_ACCESS_LEVEL, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_PERMISSIONS_GROUP_ACCESS_NOTIFICATION_LEVEL, Integer.class.toString(),
				builder);
		getIfExistsClampedJSON(project, ATTR_PERMISSIONS_PROJECT_ACCESS_LEVEL, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_PERMISSIONS_PROJECT_ACCESS_NOTIFICATION_LEVEL, Integer.class.toString(),
				builder);
		getIfExistsClampedJSON(project, ATTR_OWNER_NAME, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_OWNER_USERNAME, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_OWNER_ID, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_OWNER_STATE, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_OWNER_AVATAR_URL, Integer.class.toString(), builder);
		getIfExistsClampedJSON(project, ATTR_OWNER_WEB_URL, Integer.class.toString(), builder);

		getMultiIfExists(project, ATTR_TAG_LIST, builder);
		getMultiIfExists(project, ATTR_SHARED_WITH_GROUPS, builder);

		addAttr(builder, ATTR_AVATAR, avatarPhoto);

		return builder;
	}

	public void executeQueryForProject(Filter query, ResultsHandler handler, OperationOptions options) {
		if (query instanceof EqualsFilter) {

			if (((EqualsFilter) query).getAttribute() instanceof Uid) {

				Uid uid = (Uid) ((EqualsFilter) query).getAttribute();
				if (uid.getUidValue() == null) {
					invalidAttributeValue("Uid", query);
				}
				StringBuilder sbPath = new StringBuilder();
				sbPath.append(PROJECTS).append("/").append(uid.getUidValue());
				JSONObject project = (JSONObject) executeGetRequest(sbPath.toString(), null, options, false);
				processingObjectFromGET(project, handler, sbPath.toString());

			} else if (((EqualsFilter) query).getAttribute().getName().equals(ATTR_VISIBILITY)) {
				List<Object> allValues = ((EqualsFilter) query).getAttribute().getValue();
				if (allValues == null || allValues.get(0) == null) {
					invalidAttributeValue(ATTR_VISIBILITY, query);
				}
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put(ATTR_VISIBILITY, (String) allValues.get(0));
				JSONArray projects = (JSONArray) executeGetRequest(PROJECTS, parameters, options, true);
				processingObjectFromGET(projects, handler);
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Illegal search with attribute ").append(((EqualsFilter) query).getAttribute().getName())
						.append(" for query: ").append(query);
				LOGGER.error(sb.toString());
				throw new InvalidAttributeValueException(sb.toString());
			}

		} else if (query instanceof ContainsFilter) {

			if (((ContainsFilter) query).getAttribute().getName().equals("__NAME__")
					|| ((ContainsFilter) query).getAttribute().getName().equals(ATTR_PATH)
					|| ((ContainsFilter) query).getAttribute().getName().equals(ATTR_DESCRIPTION)) {

				List<Object> allValues = ((ContainsFilter) query).getAttribute().getValue();
				if (allValues == null || allValues.get(0) == null) {
					invalidAttributeValue("__NAME__", query);
				}
				Map<String, String> parameters = new HashMap<String, String>();
				parameters.put(SEARCH, allValues.get(0).toString());
				JSONArray projects = (JSONArray) executeGetRequest(PROJECTS, parameters, options, true);
				processingObjectFromGET(projects, handler);

			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Illegal search with attribute ").append(((ContainsFilter) query).getAttribute().getName())
						.append(" for query: ").append(query);
				LOGGER.error(sb.toString());
				throw new InvalidAttributeValueException(sb.toString());
			}
		} else if (query instanceof ContainsAllValuesFilter) {
			if (((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_GUEST_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_REPORTER_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_DEVELOPER_MEMBERS)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_MASTER_MEMBERS)) {

				List<Object> allValues = ((ContainsAllValuesFilter) query).getAttribute().getValue();
				if (allValues == null) {
					StringBuilder sb = new StringBuilder();
					sb.append("Searched attribute: ").append(((ContainsFilter) query).getAttribute().getName())
							.append(" do not have value for query: ").append(query);
					LOGGER.error(sb.toString());
					throw new InvalidAttributeValueException(sb.toString());
				}

				for (Object value : allValues) {
					if (value == null) {
						invalidAttributeValue(((ContainsAllValuesFilter) query).getAttribute().getName(), query);
					}
				}
				// Implemented the use of the "/memberships" route to optimize the query of the
				// accesses of each user

				String REGEX = "[\\[\\]]";
				String uid = (((ContainsAllValuesFilter) query).getAttribute().getValue()).toString();

				uid = uid.replaceAll(REGEX, "");

				StringBuilder sbPath = new StringBuilder();
				sbPath.append(USERS).append("/").append(uid).append("/").append(USERS_MEMBERSHIPS_URL);
				String TYPE_MEMBERSHIPS_GROUP = "Project";
				Map<Integer, Integer> projectByAccess = new HashMap<Integer, Integer>();

				JSONArray projectWithMPMembers = new JSONArray();

				Integer countOfSameMember = 0;

				UserProcessing userProcessing = new UserProcessing(configuration, httpclient);
				projectByAccess = userProcessing.getUserAccess(sbPath.toString(), TYPE_MEMBERSHIPS_GROUP);

				Iterator<Integer> it = projectByAccess.keySet().iterator();

				JSONObject project = new JSONObject();

				while (it.hasNext()) {
					Object projectID = it.next();

					StringBuilder sbProjectPath = new StringBuilder();
					sbProjectPath.append(PROJECTS).append("/").append(projectID);

					URIBuilder uribuilderMember = createRequestForMembers(sbProjectPath.toString());
					Map<Integer, List<String>> mapMembersProjects = getMembers(uribuilderMember);

					List<String> membersProject = null;
					if (((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_GUEST_MEMBERS)) {
						membersProject = mapMembersProjects.get(10);
					}
					if (((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_REPORTER_MEMBERS)) {
						membersProject = mapMembersProjects.get(20);
					}
					if (((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_DEVELOPER_MEMBERS)) {
						membersProject = mapMembersProjects.get(30);
					}
					if (((ContainsAllValuesFilter) query).getAttribute().getName().equals(ATTR_MASTER_MEMBERS)) {
						membersProject = mapMembersProjects.get(40);
					}
					if (membersProject != null) {
						for (Object MPProjectMember : allValues) {

							for (String projectMember : membersProject) {
								if (projectMember.equals((String) MPProjectMember)) {
									countOfSameMember++;
									break;
								}
							}
						}
						if (countOfSameMember == allValues.size()) {
							project = findProjectByID(projectID.toString(), options);
							projectWithMPMembers.put(project);
						}
					}
				}
				LOGGER.info("projectWithMPMembers -  members: {0}", projectWithMPMembers);
				processingObjectFromGET(projectWithMPMembers, handler);

			} else if (((ContainsAllValuesFilter) query).getAttribute().getName()
					.equals(ATTR_SHARED_WITH_GROUPS_ID_MAX_GUEST)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName()
							.equals(ATTR_SHARED_WITH_GROUPS_ID_MAX_REPORTER)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName()
							.equals(ATTR_SHARED_WITH_GROUPS_ID_MAX_DEVELOPER)
					|| ((ContainsAllValuesFilter) query).getAttribute().getName()
							.equals(ATTR_SHARED_WITH_GROUPS_ID_MAX_MASTER)) {

				List<Object> allValues = ((ContainsAllValuesFilter) query).getAttribute().getValue();
				if (allValues == null) {
					StringBuilder sb = new StringBuilder();
					sb.append("Searched attribute: ").append(((ContainsFilter) query).getAttribute().getName())
							.append(" do not have value for query: ").append(query);
					LOGGER.error(sb.toString());
					throw new InvalidAttributeValueException(sb.toString());
				}

				for (Object value : allValues) {
					if (value == null) {
						invalidAttributeValue(((ContainsAllValuesFilter) query).getAttribute().getName(), query);
					}
				}

				JSONArray projects = new JSONArray();
				JSONArray partOfProjects = new JSONArray();
				int iii = 1;

					Map<String, String> parameters = new HashMap<String, String>();
					parameters.put(PAGE, String.valueOf(iii));
					parameters.put(PER_PAGE, "100");

					partOfProjects = (JSONArray) executeGetRequest(PROJECTS, parameters, null, true);
					Iterator<Object> iterator = partOfProjects.iterator();
					while (iterator.hasNext()) {
						Object project = iterator.next();
						projects.put(project);
					}
					LOGGER.info("Value of PAGE : {0}", iii);
					LOGGER.info("Value of ParfOfProjects Lenght : {0}", partOfProjects.length());
					LOGGER.info("Value of Projects Lenght : {0}", projects.length());

				JSONArray projectsSharedWithMPGroups = new JSONArray();
				JSONObject project;
				for (int i = 0; i < projects.length(); i++) {
					project = projects.getJSONObject(i);
					Integer countOfSameGroups = 0;

					if (project.has(ATTR_SHARED_WITH_GROUPS)) {
						Object valueObject = project.get(ATTR_SHARED_WITH_GROUPS);
						if (valueObject != null && !JSONObject.NULL.equals(valueObject)
								&& valueObject instanceof JSONArray) {
							JSONArray objectArray = (JSONArray) valueObject;
							for (int ii = 0; ii < objectArray.length(); ii++) {
								if (objectArray.get(ii) instanceof JSONObject) {
									JSONObject jsonObject = objectArray.getJSONObject(ii);
									String groupId = String.valueOf(jsonObject.get(ATTR_GROUP_ID));
									for (Object MPSharedWithGroup : allValues) {

										if (groupId.equals((String) MPSharedWithGroup)) {
											countOfSameGroups++;
											break;
										}
									}
									if (countOfSameGroups == allValues.size()) {
										projectsSharedWithMPGroups.put(project);
									}
								}
							}
						}
					}
				}
				processingObjectFromGET(projectsSharedWithMPGroups, handler);
			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("Illegal search with attribute ").append(((ContainsFilter) query).getAttribute().getName())
						.append(" for query: ").append(query);
				LOGGER.error(sb.toString());
				throw new InvalidAttributeValueException(sb.toString());
			}
		} else if (query == null) {
			JSONArray projects = (JSONArray) executeGetRequest(PROJECTS, null, options, true);
			processingObjectFromGET(projects, handler);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Unexpected filter ").append(query.getClass());
			LOGGER.error(sb.toString());
			throw new ConnectorIOException(sb.toString());
		}
	}

	private JSONObject findProjectByID(String projectID, OperationOptions options) {
		Map<String, String> parameters = new HashMap<>();
		parameters.put("with_custom_attributes", "no");
		StringBuilder sbPath = new StringBuilder();

		sbPath.append(PROJECTS).append("/").append(projectID);
		JSONObject project = (JSONObject) executeGetRequest(sbPath.toString(), parameters, options, false);
		if (project.getInt(UID) == Integer.parseInt(projectID)) {
			return project;
		}
		return null;
	}

	private void addAttributeForSharedProjects(JSONObject object, ConnectorObjectBuilder builder) {

		List<String> guestSharedWithGroup = new ArrayList<>();
		List<String> reporterSharedWithGroup = new ArrayList<>();
		List<String> developerSharedWithGroup = new ArrayList<>();
		List<String> masterSharedWithGroup = new ArrayList<>();
		List<String> sharedWithGroupWithName = new ArrayList<>();

		if (object.has(ATTR_SHARED_WITH_GROUPS)) {
			Object valueObject = object.get(ATTR_SHARED_WITH_GROUPS);
			if (valueObject != null && !JSONObject.NULL.equals(valueObject)) {
				if (valueObject instanceof JSONArray) {
					JSONArray objectArray = object.getJSONArray(ATTR_SHARED_WITH_GROUPS);
					for (int i = 0; i < objectArray.length(); i++) {
						if (objectArray.get(i) instanceof JSONObject) {
							JSONObject jsonObject = objectArray.getJSONObject(i);
							String groupId = String.valueOf(jsonObject.get(ATTR_GROUP_ID));
							//String groupName = String.valueOf(jsonObject.get(ATTR_GROUP_NAME));
							String groupFullPath = String.valueOf(jsonObject.get(ATTR_GROUP_FULL_PATH));
							int access_level = (int) jsonObject.get(ATTR_GROUP_ACCESS_LEVEL);

							if (access_level == 10) {
								guestSharedWithGroup.add(groupId);
							}
							if (access_level == 20) {
								reporterSharedWithGroup.add(groupId);
							}
							if (access_level == 30) {
								developerSharedWithGroup.add(groupId);
							}
							if (access_level == 40) {
								masterSharedWithGroup.add(groupId);
							}
							StringBuilder sb = new StringBuilder();
							sb.append(groupFullPath).append(":").append(access_level);
							sharedWithGroupWithName.add(sb.toString());
						}
					}

					if (!guestSharedWithGroup.isEmpty()) {
						builder.addAttribute(ATTR_SHARED_WITH_GROUPS_ID_MAX_GUEST, guestSharedWithGroup.toArray());
					}
					if (!reporterSharedWithGroup.isEmpty()) {
						builder.addAttribute(ATTR_SHARED_WITH_GROUPS_ID_MAX_REPORTER,
								reporterSharedWithGroup.toArray());
					}
					if (!developerSharedWithGroup.isEmpty()) {
						builder.addAttribute(ATTR_SHARED_WITH_GROUPS_ID_MAX_DEVELOPER,
								developerSharedWithGroup.toArray());
					}
					if (!masterSharedWithGroup.isEmpty()) {
						builder.addAttribute(ATTR_SHARED_WITH_GROUPS_ID_MAX_MASTER, masterSharedWithGroup.toArray());
					}
					if (!sharedWithGroupWithName.isEmpty()) {
						builder.addAttribute(ATTR_SHARED_WITH_GROUPS_WITH_NAME, sharedWithGroupWithName.toArray());
					}
				}
			}
		}
	}

	private void processingObjectFromGET(JSONObject project, ResultsHandler handler, String sbPath) {
		byte[] avaratPhoto = getAvatarPhoto(project, ATTR_AVATAR_URL, ATTR_AVATAR);
		ConnectorObjectBuilder builder = convertProjectJSONObjectToConnectorObject(project, avaratPhoto);
		addAttributeForSharedProjects(project, builder);
		addAttributeForMembers(builder, handler, sbPath);
		ConnectorObject connectorObject = builder.build();
		LOGGER.info("addAtributeMembers, connectorObject: {0}", connectorObject.toString());
		handler.handle(connectorObject);
	}

	private void processingObjectFromGET(JSONArray projects, ResultsHandler handler) {
		JSONObject project;
		for (int i = 0; i < projects.length(); i++) {
			project = projects.getJSONObject(i);
			StringBuilder sbPath = new StringBuilder();
			sbPath.append(PROJECTS).append("/").append(project.get(UID));
			processingObjectFromGET(project, handler, sbPath.toString());
		}
	}

	public void updateDeltaMultiValues(Uid uid, Set<AttributeDelta> attributesDelta, OperationOptions options) {
		updateDeltaMultiValuesForGroupOrProject(uid, attributesDelta, options, PROJECTS);
		for (AttributeDelta attrDelta : attributesDelta) {

			if (ATTR_SHARED_WITH_GROUPS_ID_MAX_GUEST.equals(attrDelta.getName())) {
				createOrDeleteSharingWithGroup(uid, attrDelta, 10);
			}
			if (ATTR_SHARED_WITH_GROUPS_ID_MAX_REPORTER.equals(attrDelta.getName())) {
				createOrDeleteSharingWithGroup(uid, attrDelta, 20);
			}
			if (ATTR_SHARED_WITH_GROUPS_ID_MAX_DEVELOPER.equals(attrDelta.getName())) {
				createOrDeleteSharingWithGroup(uid, attrDelta, 30);
			}
			if (ATTR_SHARED_WITH_GROUPS_ID_MAX_MASTER.equals(attrDelta.getName())) {
				createOrDeleteSharingWithGroup(uid, attrDelta, 40);
			}
			if (ATTR_TAG_LIST.equals(attrDelta.getName())) {
				createOrDeleteTagList(uid, attrDelta);
			}
		}
	}

	private void createOrDeleteTagList(Uid uid, AttributeDelta attrDelta) {
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(PROJECTS).append("/").append(uid.getUidValue());

		JSONObject project = (JSONObject) executeGetRequest(sbPath.toString(), null, null, false);

		JSONArray jsonArrayTagList = (JSONArray) project.get(ATTR_TAG_LIST);
		List<Object> tagList = jsonArrayTagList.toList();

		List<Object> addValues = attrDelta.getValuesToAdd();
		List<Object> removeValues = attrDelta.getValuesToRemove();
		if (addValues != null && !addValues.isEmpty()) {

			for (Object addValue : addValues) {
				if (addValue != null && !tagList.contains(addValue)) {
					tagList.add(addValue);

				}
			}
		}
		if (removeValues != null && !removeValues.isEmpty()) {
			for (Object removeValue : removeValues) {
				if (removeValue != null && tagList.contains(removeValue)) {
					tagList.remove(removeValue);

				}
			}
		}
		JSONObject json = new JSONObject();
		json.put(ATTR_TAG_LIST, tagList);

		Boolean create = false;
		LOGGER.info("json: {0}", json.toString());
		createPutOrPostRequest(uid, PROJECTS, json, create);
	}

	private void createOrDeleteSharingWithGroup(Uid uid, AttributeDelta attrDelta, int accessLevel) {
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(PROJECTS).append("/").append(uid.getUidValue()).append(SHARE);

		List<Object> addValues = attrDelta.getValuesToAdd();
		List<Object> removeValues = attrDelta.getValuesToRemove();
		if (addValues != null && !addValues.isEmpty()) {

			for (Object addValue : addValues) {
				if (addValue != null) {

					JSONObject json = new JSONObject();
					String groupID = (String) addValue;
					json.put(ATTR_GROUP_ID, groupID);
					json.put(ATTR_GROUP_ACCESS, accessLevel);

					Boolean create = true;
					LOGGER.info("json: {0}", json.toString());
					createPutOrPostRequest(new Uid(groupID), sbPath.toString(), json, create);
				}
			}
		}
		if (removeValues != null && !removeValues.isEmpty()) {
			for (Object removeValue : removeValues) {
				if (removeValue != null) {

					String groupID = (String) removeValue;
					executeDeleteOperation(new Uid(groupID), sbPath.toString());
				}
			}
		}
	}
}
