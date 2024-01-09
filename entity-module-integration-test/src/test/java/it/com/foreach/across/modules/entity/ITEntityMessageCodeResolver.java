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

package it.com.foreach.across.modules.entity;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.test.AcrossWebAppConfiguration;
import com.foreach.across.testmodules.springdata.business.Client;
import com.foreach.across.testmodules.springdata.business.Company;
import it.com.foreach.across.modules.entity.repository.TestRepositoryEntityRegistrar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@AcrossWebAppConfiguration
@ContextConfiguration(classes = TestRepositoryEntityRegistrar.Config.class)
@TestPropertySource(properties = "entityModule.messageCodes[SolrTestModule]=solr")
public class ITEntityMessageCodeResolver
{
	private static final Locale NL = Locale.forLanguageTag( "nl-BE" );

	@Autowired
	private EntityRegistry entityRegistry;

	private EntityConfiguration<Client> entityConfiguration;
	private EntityMessageCodeResolver messages;

	@BeforeEach
	public void fetchResolver() {
		entityConfiguration = entityRegistry.getEntityConfiguration( Client.class );
		assertNotNull( entityConfiguration );

		messages = entityConfiguration.getEntityMessageCodeResolver();
		assertNotNull( messages );

		LocaleContextHolder.setLocale( Locale.ENGLISH );
	}

	@Test
	public void defaultCodeResolverForEntityConfigurations() {
		EntityMessageCodeResolver clientResolver = entityRegistry.getEntityConfiguration( Client.class ).getEntityMessageCodeResolver();
		assertArrayEquals(
				new String[] { "SpringDataJpaModule.entities.client.*" },
				clientResolver.buildMessageCodes( "*" )
		);
		assertArrayEquals(
				new String[] { "SpringDataJpaModule.entities.client.*", "SpringDataJpaModule.entities.*", "EntityModule.entities.*" },
				clientResolver.buildMessageCodes( "*", true )
		);
		EntityMessageCodeResolver companyResolver = entityRegistry.getEntityConfiguration( Company.class ).getEntityMessageCodeResolver();
		assertArrayEquals(
				new String[] { "SpringDataJpaModule.entities.company.*" },
				companyResolver.buildMessageCodes( "*" )
		);
		assertArrayEquals(
				new String[] { "SpringDataJpaModule.entities.company.*", "SpringDataJpaModule.entities.*", "EntityModule.entities.*" },
				companyResolver.buildMessageCodes( "*", true )
		);
	}

/*
	@Test
	public void customPrefixedResolver() {
		EntityMessageCodeResolver productResolver = entityRegistry.getEntityConfiguration( Product.class ).getEntityMessageCodeResolver();
		assertArrayEquals(
				new String[] { "solr.product.*" },
				productResolver.buildMessageCodes( "*" )
		);
		assertArrayEquals(
				new String[] { "solr.product.*", "solr.*", "EntityModule.entities.*" },
				productResolver.buildMessageCodes( "*", true )
		);
	}
*/

	@Test
	public void defaultCodeResolverForEntityAssociation() {
		EntityMessageCodeResolver codeResolver = entityRegistry.getEntityConfiguration( Company.class )
		                                                       .association( "client.company" )
		                                                       .getAttribute( EntityMessageCodeResolver.class );
		assertArrayEquals(
				new String[] { "SpringDataJpaModule.entities.company.associations[client.company].*", "SpringDataJpaModule.entities.client.*" },
				codeResolver.buildMessageCodes( "*" )
		);
		assertArrayEquals(
				new String[] {
						"SpringDataJpaModule.entities.company.associations[client.company].*",
						"SpringDataJpaModule.entities.client.*",
						"SpringDataJpaModule.entities.*",
						"EntityModule.entities.*"
				},
				codeResolver.buildMessageCodes( "*", true )
		);
	}

	@Test
	public void noDefaultValue() {
		assertEquals(
				"SpringDataJpaModule.entities.client.someCode",
				messages.getMessage( "someCode", null )
		);
	}

	@Test
	public void defaultEntityNames() {
		assertEquals( "Client", messages.getNameSingular() );
		assertEquals( "client", messages.getNameSingularInline() );
		assertEquals( "Clients", messages.getNamePlural() );
		assertEquals( "clients", messages.getNamePluralInline() );
	}

	@Test
	public void entityNamesWithLocale() {
		assertEquals( "Klant", messages.getNameSingular( NL ) );
		assertEquals( "klant", messages.getNameSingularInline( NL ) );
		assertEquals( "Klanten", messages.getNamePlural( NL ) );
		assertEquals( "klanten", messages.getNamePluralInline( NL ) );
	}

	@Test
	public void entityNamesInPrefixedContext() {
		EntityMessageCodeResolver prefixed = messages.prefixedResolver( "views[listView]", "views" );

		assertEquals( "Client", prefixed.getNameSingular() );
		assertEquals( "client member", prefixed.getNameSingularInline() );
		assertEquals( "Clients", prefixed.getNamePlural() );
		assertEquals( "client members", prefixed.getNamePluralInline() );

		assertEquals( "Klant", prefixed.getNameSingular( NL ) );
		assertEquals( "klanten lid", prefixed.getNameSingularInline( NL ) );
		assertEquals( "Klanten", prefixed.getNamePlural( NL ) );
		assertEquals( "client members", prefixed.getNamePluralInline( NL ) );
	}

	@Test
	public void customPluralizationTest() {
		EntityMessageCodeResolver prefixed = messages.prefixedResolver( "views[pluralizationOne]" );
		assertEquals( "Message key", prefixed.getNameSingular() );
		assertEquals( "message key", prefixed.getNameSingularInline() );
		assertEquals( "Message keys", prefixed.getNamePlural() );
		assertEquals( "message keys", prefixed.getNamePluralInline() );

		prefixed = messages.prefixedResolver( "views[pluralizationTwo]" );
		assertEquals( "Entry", prefixed.getNameSingular() );
		assertEquals( "entry", prefixed.getNameSingularInline() );
		assertEquals( "Entries", prefixed.getNamePlural() );
		assertEquals( "entries", prefixed.getNamePluralInline() );
	}

	@Test
	public void propertyDescriptors() {
		EntityPropertyDescriptor nameProperty = entityConfiguration.getPropertyRegistry().getProperty( "name" );
		EntityPropertyDescriptor idProperty = entityConfiguration.getPropertyRegistry().getProperty( "id" );

		assertEquals( "Name", messages.getPropertyDisplayName( nameProperty ) );
		assertEquals( "Naam", messages.getPropertyDisplayName( nameProperty, NL ) );
		assertEquals( "Identity", messages.getPropertyDisplayName( idProperty, NL ) );

		EntityMessageCodeResolver prefixed = messages.prefixedResolver( "views[listView]" );
		assertEquals( "Name", prefixed.getPropertyDisplayName( nameProperty ) );
		assertEquals( "Naam", prefixed.getPropertyDisplayName( nameProperty, NL ) );
		assertEquals( "Identiteit", prefixed.getPropertyDisplayName( idProperty, NL ) );
	}

	@Test
	public void customMessages() {
		String createMessage = messages.getMessage( "actions.create",
		                                            new Object[] { messages.getNameSingularInline() },
		                                            "Default for create" );
		assertEquals( "Default for create", createMessage );

		createMessage = messages.getMessageWithFallback( "actions.create",
		                                                 new Object[] { "", messages.getNameSingularInline() },
		                                                 "Default for create" );
		assertEquals( "Create a new client", createMessage );

		createMessage = messages.getMessageWithFallback( "actions.create",
		                                                 new Object[] { messages.getNameSingularInline( NL ) },
		                                                 "Default for create",
		                                                 NL );
		assertEquals( "Een nieuwe klant aanmaken", createMessage );

		LocaleContextHolder.setLocale( NL );
		createMessage = messages.getMessageWithFallback( "actions.create",
		                                                 new Object[] { messages.getNameSingularInline() },
		                                                 "Default for create"
		);
		assertEquals( "Een nieuwe klant aanmaken", createMessage );

		EntityMessageCodeResolver prefixed = messages.prefixedResolver( "views[listView]" );
		createMessage = prefixed.getMessageWithFallback( "actions.create",
		                                                 new Object[] { messages.getNameSingularInline() },
		                                                 "Default for create"
		);
		assertEquals( "Eentje aanmaken, nen kalant", createMessage );
	}
}
