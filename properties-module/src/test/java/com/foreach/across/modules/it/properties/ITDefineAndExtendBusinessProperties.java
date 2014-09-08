/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.across.modules.it.properties;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.info.AcrossContextInfo;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.core.revision.Revision;
import com.foreach.across.modules.it.properties.definingmodule.DefiningModule;
import com.foreach.across.modules.it.properties.definingmodule.business.*;
import com.foreach.across.modules.it.properties.definingmodule.registry.UserPropertyRegistry;
import com.foreach.across.modules.it.properties.definingmodule.services.RevisionPropertyService;
import com.foreach.across.modules.it.properties.definingmodule.services.UserPropertyService;
import com.foreach.across.modules.it.properties.extendingmodule.ExtendingModule;
import com.foreach.across.modules.it.properties.extendingmodule.business.ClientProperties;
import com.foreach.across.modules.it.properties.extendingmodule.config.ClientPropertiesConfig;
import com.foreach.across.modules.it.properties.extendingmodule.config.UserPropertiesConfig;
import com.foreach.across.modules.it.properties.extendingmodule.services.ClientPropertyService;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.test.AcrossTestConfiguration;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITDefineAndExtendBusinessProperties.Config.class)
public class ITDefineAndExtendBusinessProperties
{
	@Autowired
	private UserPropertyService userPropertyService;

	@Autowired
	private ClientPropertyService clientPropertyService;

	@Autowired
	private UserPropertyRegistry userPropertyRegistry;

	@Autowired
	private RevisionPropertyService revisionPropertyService;

	@Autowired
	private AcrossContextInfo acrossContextInfo;

	@Test
	public void forceTrackingUpdate() throws InterruptedException {
		AcrossModuleInfo moduleInfo = acrossContextInfo.getModuleInfo( "ExtendingModule" );

		Thread.sleep( 1000 );

		userPropertyRegistry.register( moduleInfo, UserPropertiesConfig.BOOLEAN, Boolean.class );
	}

	@Test
	public void defaultPropertyValues() {
		User userOne = new User( 1, "one" );

		UserProperties propsOne = userPropertyService.getProperties( userOne.getId() );
		assertFalse( (boolean) propsOne.getValue( UserPropertiesConfig.BOOLEAN ) );
		assertNull( propsOne.getValue( UserPropertiesConfig.DATE ) );
		assertEquals( new BigDecimal( 0 ), propsOne.getValue( UserPropertiesConfig.DECIMAL ) );

		ClientProperties clientProps = clientPropertyService.getProperties( userOne.getId() );
		assertTrue( (boolean) clientProps.getValue( ClientPropertiesConfig.BOOLEAN ) );
	}

	@Test
	public void propertyMapPersistence() {
		User userTwo = new User( 2, "two" );

		UUID uuid = UUID.randomUUID();
		Date date = new Date();
		String randomLongString = RandomStringUtils.randomAscii( 2000 );

		UserProperties created = userPropertyService.getProperties( userTwo.getId() );
		created.set( UserPropertiesConfig.BOOLEAN, true );
		//created.set( UserPropertiesConfig.DATE, date );
		created.set( UserPropertiesConfig.DECIMAL, new BigDecimal( "31.268" ) );
		created.set( "some number", 995 );
		created.set( "uuid", uuid );
		created.set( "some string", randomLongString );

		userPropertyService.saveProperties( created );

		ClientProperties createdClient = clientPropertyService.getProperties( userTwo.getId() );
		createdClient.set( "some string", "other string value" );
		createdClient.set( "uuid", uuid );
		clientPropertyService.saveProperties( createdClient );

		UserProperties fetched = userPropertyService.getProperties( userTwo.getId() );
		ClientProperties fetchedClient = clientPropertyService.getProperties( userTwo.getId() );

		assertNotSame( created, fetched );
		assertFalse( fetched.isEmpty() );
		assertEquals( true, fetched.getValue( UserPropertiesConfig.BOOLEAN ) );
		//assertEquals( date, fetched.getValue( UserPropertiesConfig.DATE ) );
		assertEquals( new BigDecimal( "31.268" ), fetched.getValue( UserPropertiesConfig.DECIMAL ) );
		assertEquals( "995", fetched.getValue( "some number" ) );
		assertEquals( Integer.valueOf( 995 ), fetched.getValue( "some number", Integer.class ) );
		assertEquals( randomLongString, fetched.getValue( "some string" ) );
		assertEquals( uuid, fetched.getValue( "uuid", UUID.class ) );

		assertNotSame( createdClient, fetchedClient );
		assertEquals( 2, fetchedClient.size() );
		assertEquals( "other string value", fetchedClient.getValue( "some string" ) );
		assertEquals( uuid, fetchedClient.getValue( "uuid", UUID.class ) );

		userPropertyService.deleteProperties( userTwo.getId() );
		clientPropertyService.deleteProperties( userTwo.getId() );

		assertTrue( userPropertyService.getProperties( userTwo.getId() ).isEmpty() );
		assertTrue( clientPropertyService.getProperties( userTwo.getId() ).isEmpty() );
	}

	@Test
	@Ignore
	public void revisionBasedPropertiesForNonRevision() {
		Entity entity = new Entity( 3 );
		EntityRevision revision = new EntityRevision( 0, false, true );

		RevisionProperties created = revisionPropertyService.getProperties( entity.getId(), revision );
		assertNotNull( created );
		assertTrue( created.isEmpty() );

		created.put( "integer", 123 );
		created.put( "string", "text" );

		revisionPropertyService.saveProperties( created, revision );

		RevisionProperties fetched = revisionPropertyService.getProperties( entity.getId(), revision );
		assertEquals( 2, fetched.size() );
		assertEquals( Integer.valueOf( 123 ), fetched.getValue( "integer", Integer.class ) );
		assertEquals( "text", fetched.getValue( "string" ) );

		fetched.put( "string", "modified" );
		fetched.remove( "integer" );

		UUID uuid = UUID.randomUUID();

		fetched.put( "uuid", uuid );

		revisionPropertyService.saveProperties( fetched, revision );

		RevisionProperties updated = revisionPropertyService.getProperties( entity.getId(), revision );
		assertEquals( 2, updated.size() );
		assertEquals( "modified", updated.getValue( "string" ) );
		assertEquals( uuid, updated.getValue( "uuid", UUID.class ) );
	}

	@Test
	public void revisionBasedProperties() {
		Entity entity = new Entity( 3 );
		EntityRevision latest = new EntityRevision( 1, false, true );
		EntityRevision draft = new EntityRevision( Revision.DRAFT, true, false );

		RevisionProperties created = revisionPropertyService.getProperties( entity.getId(), draft );
		assertNotNull( created );
		assertTrue( created.isEmpty() );

		created.put( "integer", 123 );
		created.put( "string", "text" );

		revisionPropertyService.saveProperties( created, draft );

		RevisionProperties latestProps = revisionPropertyService.getProperties( entity.getId(), latest );
		assertTrue( latestProps.isEmpty() );

		RevisionProperties draftProps = revisionPropertyService.getProperties( entity.getId(), draft );
		assertEquals( 2, draftProps.size() );
		assertEquals( Integer.valueOf( 123 ), draftProps.getValue( "integer", Integer.class ) );
		assertEquals( "text", draftProps.getValue( "string" ) );

		draftProps.put( "string", "modified" );

		UUID uuid = UUID.randomUUID();

		draftProps.put( "uuid", uuid );

		revisionPropertyService.saveProperties( draftProps, draft );

		RevisionProperties updated = revisionPropertyService.getProperties( entity.getId(), draft );
		assertEquals( 3, updated.size() );
		assertEquals( Integer.valueOf( 123 ), updated.getValue( "integer", Integer.class ) );
		assertEquals( "modified", updated.getValue( "string" ) );
		assertEquals( uuid, updated.getValue( "uuid", UUID.class ) );

		// Checkin and make the draft the new version
		revisionPropertyService.checkin( entity.getId(), draft, 1 );

		latestProps = revisionPropertyService.getProperties( entity.getId(), latest );
		assertEquals( 3, latestProps.size() );
		assertEquals( Integer.valueOf( 123 ), latestProps.getValue( "integer", Integer.class ) );
		assertEquals( "modified", latestProps.getValue( "string" ) );
		assertEquals( uuid, latestProps.getValue( "uuid", UUID.class ) );

		// remove property on draft
		updated = revisionPropertyService.getProperties( entity.getId(), draft );
		assertEquals( 3, updated.size() );
		assertEquals( Integer.valueOf( 123 ), updated.getValue( "integer", Integer.class ) );
		assertEquals( "modified", updated.getValue( "string" ) );
		assertEquals( uuid, updated.getValue( "uuid", UUID.class ) );

		updated.remove( "integer" );
		updated.put( "string", "modified again" );

		revisionPropertyService.saveProperties( updated, draft );

		// Fetch latest again, should not be modified
		latestProps = revisionPropertyService.getProperties( entity.getId(), latest );
		assertEquals( 3, latestProps.size() );
		assertEquals( Integer.valueOf( 123 ), latestProps.getValue( "integer", Integer.class ) );
		assertEquals( "modified", latestProps.getValue( "string" ) );
		assertEquals( uuid, latestProps.getValue( "uuid", UUID.class ) );

		RevisionProperties removed = revisionPropertyService.getProperties( entity.getId(), draft );
		assertEquals( 2, removed.size() );
		assertEquals( "modified again", updated.getValue( "string" ) );
		assertEquals( uuid, updated.getValue( "uuid", UUID.class ) );

		// checkin properties to version
		revisionPropertyService.checkin( entity.getId(), draft, 2 );

		EntityRevision versionOne = new EntityRevision( 1, false, false );
		latest = new EntityRevision( 2, false, true );

		RevisionProperties propsOne = revisionPropertyService.getProperties( entity.getId(), versionOne );
		assertEquals( 3, propsOne.size() );
		assertEquals( Integer.valueOf( 123 ), propsOne.getValue( "integer", Integer.class ) );
		assertEquals( "modified", propsOne.getValue( "string" ) );
		assertEquals( uuid, propsOne.getValue( "uuid", UUID.class ) );

		RevisionProperties propsTwo = revisionPropertyService.getProperties( entity.getId(), latest );
		assertEquals( 2, propsTwo.size() );
		assertEquals( "modified again", propsTwo.getValue( "string" ) );
		assertEquals( uuid, propsTwo.getValue( "uuid", UUID.class ) );

		RevisionProperties newDraftProps = revisionPropertyService.getProperties( entity.getId(), draft );
		assertEquals( 2, newDraftProps.size() );
		assertEquals( "modified again", newDraftProps.getValue( "string" ) );
		assertEquals( uuid, newDraftProps.getValue( "uuid", UUID.class ) );

	}

	@AcrossTestConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext acrossContext ) {
			acrossContext.addModule( new PropertiesModule() );
			acrossContext.addModule( new DefiningModule() );
			acrossContext.addModule( new ExtendingModule() );
		}
	}
}
