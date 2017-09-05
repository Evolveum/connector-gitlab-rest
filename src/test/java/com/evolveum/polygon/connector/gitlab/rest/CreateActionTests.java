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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.InvalidPasswordException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.Uid;
import org.testng.annotations.Test;

/**
 * @author Lukas Skublik
 *
 */
public class CreateActionTests extends BasicFunctionForTests {

	@Test(expectedExceptions = UnsupportedOperationException.class)
	public void creteTestNotSupportedObjectClass(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("skype","Taumatawha-katangihangako-auauotamateaturi-pukakapikimaun-gahoronuku-pokaiwhenu-akitanatahu"));
		
			
		ObjectClass objectClassAccount = new ObjectClass("HawkeBay");
		
		try {
			gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	} 
	
	@Test(expectedExceptions = InvalidAttributeValueException.class, priority = 6)
	public void creteTestWithNotFilledMandatoryAttributeForAccount(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("skype","Taumatawha-katangihangako-auauotamateaturi-pukakapikimaun-gahoronuku-pokaiwhenu-akitanatahu"));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		try {
			gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = InvalidAttributeValueException.class, priority = 7)
	public void creteTestWithoutPasswordForAccount(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("name","John Snow"));
		attributesAccount.add(AttributeBuilder.build("email","john.snow@gameofthrones.com"));
		attributesAccount.add(AttributeBuilder.build("__NAME__","snow"));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		try {
			gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = AlreadyExistsException.class, priority = 10)
	public void creteTestUserWithExistingLoginName(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("name","John Snow"));
		attributesAccount.add(AttributeBuilder.build("email","john.snow@gameofthrones.com"));
		attributesAccount.add(AttributeBuilder.build("__NAME__","snow"));
		
		GuardedString pass = new GuardedString("winteriscoming".toCharArray());
		attributesAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		
		Uid johnUid = gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		try {
			gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		} finally {
			gitlabRestConnector.delete(objectClassAccount, johnUid, options);
			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = InvalidAttributeValueException.class, priority = 4)
	public void creteTestWithNotFilledMandatoryAttributeForGroup(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesGroup = new HashSet<Attribute>();
		attributesGroup.add(AttributeBuilder.build("description","Family from winterfell"));
			
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		try {
			gitlabRestConnector.create(objectClassGroup, attributesGroup, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = InvalidAttributeValueException.class, priority = 5)
	public void creteTestWithNotFilledMandatoryAttributeForProject(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesProject = new HashSet<Attribute>();
		attributesProject.add(AttributeBuilder.build("description","Test"));
		
			
		ObjectClass objectClassProject = new ObjectClass("Project");
		
		try {
			gitlabRestConnector.create(objectClassProject, attributesProject, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = ConnectorIOException.class, priority = 8)
	public void creteTestGroupWithExistingLoginName(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesGroup = new HashSet<Attribute>();
		attributesGroup.add(AttributeBuilder.build("__NAME__","Stark family"));
		attributesGroup.add(AttributeBuilder.build("path","stark"));
			
		ObjectClass objectClassGroup = ObjectClass.GROUP;
		
		Uid starkUid = gitlabRestConnector.create(objectClassGroup, attributesGroup, options);
		try {
			gitlabRestConnector.create(objectClassGroup, attributesGroup, options);
		} finally {
			gitlabRestConnector.delete(objectClassGroup, starkUid, options);
			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = ConnectorIOException.class, priority = 9)
	public void creteTestProjectWithExistingLoginName(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesProject = new HashSet<Attribute>();
		attributesProject.add(AttributeBuilder.build("__NAME__","Battle of Bastards"));
			
		ObjectClass objectClassGroup = new ObjectClass("Project");
		
		Uid projUid = gitlabRestConnector.create(objectClassGroup, attributesProject, options);
		try {
			gitlabRestConnector.create(objectClassGroup, attributesProject, options);
		} finally {
			gitlabRestConnector.delete(objectClassGroup, projUid, options);
			
			gitlabRestConnector.dispose();
		}
	}
	
	@Test(expectedExceptions = InvalidPasswordException.class, priority = 25)
	public void creteTestUserWithWrongCredentials(){
		GitlabRestConnector gitlabRestConnector = new GitlabRestConnector();
		
		GitlabRestConfiguration conf = getConfiguration();
		gitlabRestConnector.init(conf);
		
		OperationOptions options = new OperationOptions(new HashMap<String,Object>());
		
		Set<Attribute> attributesAccount = new HashSet<Attribute>();
		attributesAccount.add(AttributeBuilder.build("name","John Snow"));
		attributesAccount.add(AttributeBuilder.build("email","john.snow@gameofthrones.com"));
		attributesAccount.add(AttributeBuilder.build("__NAME__","snow"));
		
		GuardedString pass = new GuardedString("wint".toCharArray());
		attributesAccount.add(AttributeBuilder.build("__PASSWORD__",pass));
		
			
		ObjectClass objectClassAccount = ObjectClass.ACCOUNT;
		try {
			gitlabRestConnector.create(objectClassAccount, attributesAccount, options);
		} finally {
			gitlabRestConnector.dispose();
		}
	}
	
}
