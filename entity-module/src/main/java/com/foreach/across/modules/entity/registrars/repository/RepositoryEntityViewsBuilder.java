package com.foreach.across.modules.entity.registrars.repository;

import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyFilters;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.registry.properties.MergingEntityPropertyRegistry;
import com.foreach.across.modules.entity.registry.properties.SimpleEntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.*;
import com.foreach.across.modules.entity.views.helpers.SpelValueFetcher;
import com.foreach.across.modules.hibernate.business.Auditable;
import com.foreach.across.modules.spring.security.infrastructure.business.SecurityPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Attempts to create default views for an EntityConfiguration.
 * Creates a list, read, create, update and delete view if possible.
 */
public class RepositoryEntityViewsBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger( RepositoryEntityViewsBuilder.class );

	@Autowired
	private BeanFactory beanFactory;

	@Autowired(required = false)
	private ConversionService conversionService;

	@PostConstruct
	protected void createDefaultConversionService() {
		if ( conversionService == null ) {
			LOG.info(
					"No ConversionService found for the EntityModule - creating default conversion service for views." );
			conversionService = new DefaultConversionService();
		}
	}

	public void createViews( MutableEntityConfiguration entityConfiguration ) {
		buildCreateView( entityConfiguration );

		// todo: support regular repository to be used instead of specific CrudRepository interface (use repo information)
		buildListView( entityConfiguration, (CrudRepository) entityConfiguration.getAttribute( Repository.class ) );
	}

	private void buildCreateView( MutableEntityConfiguration entityConfiguration ) {
		EntityCreateViewFactory viewFactory = beanFactory.getBean( EntityCreateViewFactory.class );
		viewFactory.setMessagePrefixes( "entityViews.createView", "entityViews" );

		EntityPropertyRegistry registry = new MergingEntityPropertyRegistry(
				entityConfiguration.getPropertyRegistry()
		);

		viewFactory.setPropertyRegistry( registry );
		viewFactory.setTemplate( EntityCreateView.VIEW_TEMPLATE );

		entityConfiguration.registerView( EntityCreateView.VIEW_NAME, viewFactory );
	}

	private void buildListView( MutableEntityConfiguration entityConfiguration, CrudRepository repository ) {
		EntityListViewFactory viewFactory = beanFactory.getBean( EntityListViewFactory.class );
		viewFactory.setConversionService( conversionService );
		viewFactory.setMessagePrefixes( "entityViews.listView", "entityViews" );

		EntityPropertyRegistry registry = new MergingEntityPropertyRegistry(
				entityConfiguration.getPropertyRegistry()
		);

		viewFactory.setPropertyRegistry( registry );
		viewFactory.setTemplate( EntityListView.VIEW_TEMPLATE );
		viewFactory.setPageFetcher( new RepositoryEntityListViewPageFetcher( repository ) );

		LinkedList<String> defaultProperties = new LinkedList<>();
		if ( registry.contains( "name" ) ) {
			defaultProperties.add( "name" );
		}
		if ( registry.contains( "title" ) ) {
			defaultProperties.add( "title" );
		}

		if ( defaultProperties.isEmpty() ) {
			if ( !registry.contains( "#generatedLabel" ) ) {
				SimpleEntityPropertyDescriptor label = new SimpleEntityPropertyDescriptor();
				label.setName( "#generatedLabel" );
				label.setDisplayName( "Generated label" );
				label.setValueFetcher( new SpelValueFetcher( "toString()" ) );

				registry.register( label );
			}

			defaultProperties.add( "#generatedLabel" );
		}

		if ( SecurityPrincipal.class.isAssignableFrom( entityConfiguration.getEntityType() ) ) {
			defaultProperties.addFirst( "principalName" );
		}

		if ( Auditable.class.isAssignableFrom( entityConfiguration.getEntityType() ) ) {
			defaultProperties.add( "createdDate" );
			defaultProperties.add( "createdBy" );
			defaultProperties.add( "lastModifiedDate" );
			defaultProperties.add( "lastModifiedBy" );
		}

		viewFactory.setPropertyFilter( EntityPropertyFilters.includeOrdered( defaultProperties ) );
		viewFactory.setDefaultSort( determineDefaultSort( defaultProperties ) );

		entityConfiguration.registerView( EntityListView.VIEW_NAME, viewFactory );
	}

	private Sort determineDefaultSort( Collection<String> defaultProperties ) {
		String propertyName = null;

		if ( defaultProperties.contains( "name" ) ) {
			propertyName = "name";
		}
		else if ( defaultProperties.contains( "title" ) ) {
			propertyName = "title";
		}

		if ( propertyName != null ) {
			return new Sort( propertyName );
		}

		if ( defaultProperties.contains( "createdDate" ) ) {
			return new Sort( Sort.Direction.DESC, "createdDate" );
		}

		return null;
	}
}
