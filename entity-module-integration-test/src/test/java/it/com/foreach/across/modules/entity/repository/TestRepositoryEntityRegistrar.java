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

package it.com.foreach.across.modules.entity.repository;

import com.foreach.across.core.support.ReadableAttributes;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.annotations.EntityValidator;
import com.foreach.across.modules.entity.query.EntityQueryExecutor;
import com.foreach.across.modules.entity.query.jpa.EntityQueryJpaExecutor;
import com.foreach.across.modules.entity.query.querydsl.EntityQueryQueryDslExecutor;
import com.foreach.across.modules.entity.registry.*;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.testmodules.solr.SolrTestModule;
import com.foreach.across.testmodules.solr.business.Product;
import com.foreach.across.testmodules.springdata.SpringDataJpaModule;
import com.foreach.across.testmodules.springdata.business.*;
import com.foreach.across.testmodules.springdata.repositories.ClientRepository;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import com.foreach.across.modules.entity.views.EntityViewFactoryAttributes;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.hibernate.jpa.config.HibernateJpaConfiguration;
import com.foreach.across.modules.spring.security.SpringSecurityModule;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.test.AcrossTestConfiguration;
import com.foreach.across.test.AcrossWebAppConfiguration;
import it.com.foreach.across.modules.entity.utils.EntityPropertyDescriptorVerifier;
import it.com.foreach.across.modules.entity.utils.EntityVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactoryInformation;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

import javax.validation.metadata.PropertyDescriptor;
import java.io.Serializable;

import static com.foreach.across.modules.entity.views.EntityView.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@AcrossWebAppConfiguration
public class TestRepositoryEntityRegistrar
{
	@Autowired
	private EntityRegistry entityRegistry;

	@Autowired
	private ConversionService mvcConversionService;

	@Autowired
	private ClientRepository clientRepository;

	@EntityValidator
	private SmartValidator entityValidator;

	@Test
	public void expectedEntitiesShouldBeRegisteredWithTheirAssociations() {
		verify( Client.class )
				.isVisible( true )
				.hasRepository()
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, HibernateJpaConfiguration.TRANSACTION_MANAGER )
				.hasAssociation( "client.groups", false ).from( "groups" ).to( ClientGroup.class, "id.client" ).and()
				.hasAssociation( "clientGroup.id.client", true ).from( null ).to( ClientGroup.class, "id.client" );

		verify( Company.class )
				.isVisible( true )
				.hasRepository()
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, HibernateJpaConfiguration.TRANSACTION_MANAGER )
				.hasAssociation( "client.company", true ).from( null ).to( Client.class, "company" ).and()
				.hasAssociation( "company.representatives", false ).from( "representatives" ).to( Representative.class );

		// not a JpaSpecificationExecutor so associations can't be built
		verify( Car.class )
				.isVisible( true )
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, HibernateJpaConfiguration.TRANSACTION_MANAGER )
				.hasRepository();

		verify( Group.class )
				.isVisible( true )
				.hasRepository()
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, HibernateJpaConfiguration.TRANSACTION_MANAGER )
				.hasAssociation( "company.group", true ).from( null ).to( Company.class, "group" ).and()
				.hasAssociation( "clientGroup.id.group", ClientGroup.class, true );

		verify( ClientGroup.class )
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, HibernateJpaConfiguration.TRANSACTION_MANAGER )
				.isVisible( true )
				.hasRepository();

		verify( Representative.class )
				.isVisible( true )
				.hasAttribute( EntityAttributes.TRANSACTION_MANAGER_NAME, "otherTransactionManager" )
				.hasRepository()
				.hasAssociation( "company.representatives", true ).from( null ).to( Company.class, "representatives" );

		verify( Product.class )
				.isVisible( true )
				.hasRepository();

		// enum entity should not be visible
		verify( Country.class )
				.isVisible( false );
	}

	private EntityVerifier verify( Class<?> entityType ) {
		return new EntityVerifier( entityRegistry, entityType );
	}

	@Test
	public void clientShouldBeRegisteredWithRepositoryInformation() {
		assertEquals( 8, entityRegistry.getEntities().size() );
		assertTrue( entityRegistry.contains( Client.class ) );

		EntityConfiguration<?> configuration = entityRegistry.getEntityConfiguration( Client.class );
		assertNotNull( configuration );

		CrudRepository<Client, Long> repository = configuration.getAttribute( Repository.class );
		assertNotNull( repository );

		RepositoryFactoryInformation<Client, Long> repositoryFactoryInformation
				= configuration.getAttribute( RepositoryFactoryInformation.class );
		assertNotNull( repositoryFactoryInformation );

		PersistentEntity persistentEntity = configuration.getAttribute( PersistentEntity.class );
		assertNotNull( persistentEntity );
		assertEquals( persistentEntity.getType(), Client.class );

		EntityPropertyDescriptor propertyDescriptor = configuration.getPropertyRegistry().getProperty( "name" );
		PersistentProperty persistentProperty = propertyDescriptor.getAttribute( PersistentProperty.class );
		assertNotNull( persistentProperty );
		assertSame( persistentProperty, persistentEntity.getPersistentProperty( "name" ) );

		EntityModel model = configuration.getEntityModel();
		assertNotNull( model );

		EntityViewFactory viewFactory = configuration.getViewFactory( LIST_VIEW_NAME );
		assertNotNull( viewFactory );
	}

	@Test
	public void companyShouldHaveAnAssociationToItsRepresentatives() throws Exception {
		EntityConfiguration configuration = entityRegistry.getEntityConfiguration( Company.class );
		EntityAssociation association = configuration.association( "company.representatives" );

		assertNotNull( association );
		assertTrue( association.hasView( LIST_VIEW_NAME ) );
	}

	@Test
	public void representativeShouldHaveAnAssociationToItsCompanies() throws Exception {
		EntityConfiguration configuration = entityRegistry.getEntityConfiguration( Representative.class );
		EntityAssociation association = configuration.association( "company.representatives" );

		assertNotNull( association );
		assertTrue( association.hasView( LIST_VIEW_NAME ) );
	}

	@Test
	public void verifyPropertyRegistry() {
		EntityConfiguration configuration = entityRegistry.getEntityConfiguration( Client.class );
		EntityPropertyRegistry registry = configuration.getPropertyRegistry();

		assertProperty( registry, "name", "Name", true, true )
				.hasAttribute( EntityAttributes.PROPERTY_REQUIRED, true );
		assertProperty( registry, "id", "Id", true, false )
				.doesNotHaveAttribute( EntityAttributes.PROPERTY_REQUIRED );
		assertProperty( registry, "company", "Company", true, false )
				.doesNotHaveAttribute( EntityAttributes.PROPERTY_REQUIRED );
		assertProperty( registry, "newEntityId", "New entity id", false, false )
				.doesNotHaveAttribute( EntityAttributes.PROPERTY_REQUIRED );
		assertProperty( registry, "nameWithId", "Name with id", false, false )
				.doesNotHaveAttribute( EntityAttributes.PROPERTY_REQUIRED );
		assertProperty( registry, "class", "Class", false, false )
				.doesNotHaveAttribute( EntityAttributes.PROPERTY_REQUIRED );
	}

	@Test
	public void validatorShouldBeRegistered() {
		EntityConfiguration configuration = entityRegistry.getEntityConfiguration( Client.class );
		Validator validator = configuration.getAttribute( Validator.class );

		assertNotNull( validator );
		assertSame( entityValidator, validator );

		configuration = entityRegistry.getEntityConfiguration( Company.class );
		assertNotNull( configuration );

		validator = configuration.getAttribute( Validator.class );
		assertNotNull( validator );
		assertNotSame( entityValidator, validator );

		Errors errors = mock( Errors.class );
		validator.validate( new Company(), errors );
	}

	private EntityPropertyDescriptorVerifier assertProperty( EntityPropertyRegistry registry,
	                                                         String propertyName,
	                                                         String displayName,
	                                                         boolean sortable,
	                                                         boolean hasValidators ) {
		EntityPropertyDescriptor descriptor = registry.getProperty( propertyName );
		assertNotNull( propertyName );
		assertEquals( propertyName, descriptor.getName() );
		assertEquals( displayName, descriptor.getDisplayName() );

		if ( sortable ) {
			assertEquals( propertyName, descriptor.getAttribute( Sort.Order.class ).getProperty() );
		}
		else {
			assertFalse( descriptor.hasAttribute( Sort.Order.class ) );
		}

		if ( hasValidators ) {
			assertNotNull( descriptor.getAttribute( PropertyDescriptor.class ) );
		}
		else {
			assertFalse( descriptor.hasAttribute( PropertyDescriptor.class ) );
		}

		return new EntityPropertyDescriptorVerifier( descriptor );
	}

	@Test
	public void entityConfigurationFromConversionService() {
		EntityConfiguration clientConfiguration = mvcConversionService.convert( "client", EntityConfiguration.class );

		assertNotNull( clientConfiguration );
		assertEquals( Client.class, clientConfiguration.getEntityType() );

		EntityConfiguration notExisting = mvcConversionService.convert( "someUnexistingEntity",
		                                                                EntityConfiguration.class );
		assertNull( notExisting );
	}

	@Test
	public void entityConverter() {
		Client client = new Client();
		client.setNewEntityId( 123L );
		client.setName( "Known client name" );

		clientRepository.save( client );

		Client converted = mvcConversionService.convert( "", Client.class );
		assertNull( converted );

		converted = mvcConversionService.convert( 123, Client.class );
		assertEquals( client, converted );

		converted = mvcConversionService.convert( "123", Client.class );
		assertEquals( client, converted );
	}

	@Test
	public void verifyEntityModel() {
		EntityConfiguration<Client> configuration = entityRegistry.getEntityConfiguration( Client.class );
		EntityModel<Client, Serializable> model = configuration.getEntityModel();

		Client existing = model.findOne( 10L );
		assertNull( existing );

		Client created = model.createNew();
		assertNotNull( created );
		assertTrue( model.isNew( created ) );

		created.setNewEntityId( 10L );
		created.setName( "Some name" );

		assertEquals( "Some name", model.getLabel( created ) );

		created = model.save( created );
		assertEquals( Long.valueOf( 10 ), created.getId() );
		assertFalse( model.isNew( created ) );

		existing = model.findOne( 10L );
		assertNotNull( existing );
		assertEquals( "Some name", existing.getName() );
		assertEquals( "Some name", model.getLabel( created ) );

		Client dto = model.createDto( created );
		assertNotSame( created, dto );
		assertEquals( created.getId(), dto.getId() );
		assertEquals( created.getName(), dto.getName() );

		dto.setName( "Modified name" );
		model.save( dto );

		existing = model.findOne( 10L );
		assertNotNull( existing );
		assertEquals( "Modified name", existing.getName() );
		assertEquals( "Modified name", model.getLabel( existing ) );

		model.delete( existing );
		assertNull( model.findOne( 10L ) );
	}

	@Test
	public void defaultViewsShouldBePresent() {
		EntityConfiguration<Client> configuration = entityRegistry.getEntityConfiguration( Client.class );
		assertViewAttributes( configuration, LIST_VIEW_NAME, AllowableAction.READ );
		assertViewAttributes( configuration, CREATE_VIEW_NAME, AllowableAction.CREATE );
		assertViewAttributes( configuration, UPDATE_VIEW_NAME, AllowableAction.UPDATE );
		assertViewAttributes( configuration, DELETE_VIEW_NAME, AllowableAction.DELETE );
	}

	private void assertViewAttributes( EntityConfiguration configuration, String viewName, AllowableAction action ) {
		assertTrue( configuration.hasView( viewName ) );
		ReadableAttributes attributes = configuration.getViewFactory( viewName );
		assertEquals( viewName, attributes.getAttribute( EntityViewFactoryAttributes.VIEW_NAME ) );
		assertEquals( action, attributes.getAttribute( AllowableAction.class ) );
		assertSame( configuration, attributes.getAttribute( EntityViewRegistry.class ) );
		assertSame( EntityViewFactoryAttributes.defaultAccessValidator(), attributes.getAttribute( EntityViewFactoryAttributes.ACCESS_VALIDATOR ) );
	}

	@Test
	public void clientShouldHaveAJpaExecutor() {
		EntityConfiguration<Client> configuration = entityRegistry.getEntityConfiguration( Client.class );
		EntityQueryExecutor<Client> queryExecutor = configuration.getAttribute( EntityQueryExecutor.class );

		assertNotNull( queryExecutor );
		assertTrue( queryExecutor instanceof EntityQueryJpaExecutor );
	}

	@Test
	public void companyShouldHaveAQueryDslExecutor() {
		EntityConfiguration<Company> configuration = entityRegistry.getEntityConfiguration( Company.class );
		EntityQueryExecutor<Company> queryExecutor = configuration.getAttribute( EntityQueryExecutor.class );

		assertNotNull( queryExecutor );
		assertTrue( queryExecutor instanceof EntityQueryQueryDslExecutor );
	}

	@Test
	public void generatedPropertiesShouldBeHiddenAndNotWritable() {
		EntityConfiguration<Car> car = entityRegistry.getEntityConfiguration( Car.class );
		EntityPropertyDescriptor descriptor = car.getPropertyRegistry().getProperty( "id" );
		assertTrue( descriptor.isWritable() );
		assertFalse( descriptor.isHidden() );

		EntityConfiguration<Client> client = entityRegistry.getEntityConfiguration( Client.class );
		descriptor = client.getPropertyRegistry().getProperty( "id" );
		assertFalse( descriptor.isWritable() );
		assertTrue( descriptor.isHidden() );
	}

	@Configuration
	@AcrossTestConfiguration(modules = { EntityModule.NAME, AdminWebModule.NAME, SpringSecurityModule.NAME })
	public static class Config
	{
		@Bean
		public AcrossHibernateJpaModule acrossHibernateJpaModule() {
			AcrossHibernateJpaModule hibernateModule = new AcrossHibernateJpaModule();
			hibernateModule.setHibernateProperty( "hibernate.hbm2ddl.auto", "create-drop" );
			return hibernateModule;
		}

		@Bean
		public SpringDataJpaModule springDataJpaModule() {
			return new SpringDataJpaModule();
		}

		@Bean
		public SolrTestModule solrTestModule() {
			return new SolrTestModule();
		}
	}
}