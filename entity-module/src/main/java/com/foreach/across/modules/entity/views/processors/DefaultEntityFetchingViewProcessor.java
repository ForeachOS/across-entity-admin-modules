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

package com.foreach.across.modules.entity.views.processors;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.entity.query.*;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

/**
 * Default implementation that attempts to fetch the items based on the {@link com.foreach.across.modules.entity.views.context.EntityViewContext}.
 * Will use the repository attached to the {@link com.foreach.across.modules.entity.registry.EntityConfiguration} that is being used, and will
 * attempt to resolve association properties.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Exposed
@Component
@Scope("prototype")
@RequiredArgsConstructor
@Slf4j
public final class DefaultEntityFetchingViewProcessor extends AbstractEntityFetchingViewProcessor
{
	/**
	 * Can hold an optional EQL statement that should be applied to the query being executed.
	 */
	public static final String EQL_PREDICATE_ATTRIBUTE_NAME = EntityQueryFilterProcessor.EQL_PREDICATE_ATTRIBUTE_NAME;

	/**
	 * Default order that this processor will have if it has been added through the {@link com.foreach.across.modules.entity.views.builders.ListViewInitializer}.
	 */
	public static final int DEFAULT_ORDER = Ordered.LOWEST_PRECEDENCE;

	private final EntityQueryFacadeResolver entityQueryFacadeResolver;

	/**
	 * The basic filter that should always be applied.
	 * This can only be an EQL statement and will force the {@link EntityQueryExecutor} to be used.
	 */
	@Setter
	private String baseEqlPredicate;

	@Override
	protected Iterable fetchItems( EntityViewRequest entityViewRequest, EntityView entityView, Sort sort ) {
		return fetchItems( entityViewRequest, entityView, null, sort );
	}

	@Override
	protected Iterable fetchItems( EntityViewRequest entityViewRequest, EntityView entityView, Pageable pageable ) {
		return fetchItems( entityViewRequest, entityView, pageable, null );
	}

	private Iterable fetchItems( EntityViewRequest entityViewRequest, EntityView entityView, Pageable pageable, Sort sort ) {
		EntityViewContext entityViewContext = entityViewRequest.getEntityViewContext();
		EntityQueryFacade entityQueryFacade = entityQueryFacadeResolver.forEntityViewRequest( entityViewRequest );
		String additionalPredicate = entityView.getAttribute( EQL_PREDICATE_ATTRIBUTE_NAME, String.class );

		// set to null so we would favour regular repository if no specific query necessary
		EntityQuery entityQuery = null;

		if ( baseEqlPredicate != null || additionalPredicate != null ) {
			if ( entityQueryFacade == null ) {
				throw new IllegalStateException(
						"An EntityQuery predicate was specified but the EntityConfiguration does not have a valid EntityQueryExecutor setup: "
								+ entityViewContext.getEntityConfiguration().getName() );
			}

			entityQuery = buildEntityQuery( entityQueryFacade, additionalPredicate );
		}

		if ( entityViewContext.isForAssociation() ) {
			return fetchItemsForEntityAssociation(
					entityViewContext.getEntityAssociation(),
					entityViewContext.getParentContext().getEntity( Object.class ),
					entityQueryFacade,
					entityQuery,
					pageable,
					sort
			);
		}

		return fetchItemsForEntityConfiguration( entityViewContext.getEntityConfiguration(), entityQueryFacade, entityQuery, pageable, sort );
	}

	@SuppressWarnings("unchecked")
	private Iterable<Object> fetchItemsForEntityAssociation( EntityAssociation association,
	                                                         Object parentEntity,
	                                                         EntityQueryFacade entityQueryFacade,
	                                                         EntityQuery entityQuery,
	                                                         Pageable pageable,
	                                                         Sort sort ) {
		AssociatedEntityQueryExecutor associatedEntityQueryExecutor = association.getAttribute( AssociatedEntityQueryExecutor.class );

		if ( associatedEntityQueryExecutor != null ) {
			LOG.trace( "An AssociatedEntityQueryExecutor is registered - using it for fetching the items" );
			return associatedEntityQueryExecutor.findAll( parentEntity, entityQuery != null ? entityQuery : EntityQuery.all(), pageable );
		}

		return fetchItemsForEntityConfiguration(
				association.getTargetEntityConfiguration(),
				entityQueryFacade,
				EntityQueryUtils.and( entityQuery, EntityQueryUtils.createAssociationPredicate( association, parentEntity ) ),
				pageable,
				sort );
	}

	@SuppressWarnings("unchecked")
	private Iterable<Object> fetchItemsForEntityConfiguration( EntityConfiguration entityConfiguration,
	                                                           EntityQueryFacade entityQueryFacade,
	                                                           EntityQuery entityQuery,
	                                                           Pageable pageable,
	                                                           Sort sort ) {
		Repository repository = entityConfiguration.getAttribute( Repository.class );

		boolean shouldOnlySort = pageable == null;
		if ( entityQuery == null ) {
			if ( repository instanceof PagingAndSortingRepository ) {
				if ( shouldOnlySort ) {
					return ( (PagingAndSortingRepository) repository ).findAll( sort );
				}
				return ( (PagingAndSortingRepository) repository ).findAll( pageable );
			}
		}

		if ( entityQueryFacade != null ) {
			EntityQuery query = entityQuery != null ? entityQuery : EntityQuery.all();
			if ( shouldOnlySort ) {
				return entityQueryFacade.findAll( query, sort );
			}
			return entityQueryFacade.findAll( query, pageable );
		}

		if ( entityQuery != null ) {
			throw new IllegalStateException(
					"An EntityQuery predicate was specified but the EntityConfiguration does not have a valid EntityQueryExecutor setup: " + entityConfiguration
							.getName() );
		}

		if ( repository instanceof CrudRepository ) {
			return ( (CrudRepository) repository ).findAll();
		}

		throw new IllegalStateException(
				"Neither a CrudRepository nor an EntityQueryExecutor are configured on entity configuration " + entityConfiguration.getName() );
	}

	private EntityQuery buildEntityQuery( EntityQueryFacade entityQueryFacade, String additionalPredicate ) {
		EntityQuery query = EntityQuery.all();

		if ( baseEqlPredicate != null ) {
			query = EntityQuery.and( query, entityQueryFacade.convertToExecutableQuery( baseEqlPredicate ) );
		}

		if ( additionalPredicate != null ) {
			query = EntityQuery.and( query, entityQueryFacade.convertToExecutableQuery( additionalPredicate ) );
		}

		return query;
	}
}
