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

import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.query.EntityQueryFacade;
import com.foreach.across.modules.entity.query.EntityQueryFacadeResolver;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.hibernate.business.IdBasedEntity;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDefaultEntityFetchingViewProcessor
{
	private static final String PARENT = "parentEntity";

	Entry one = new Entry( 1L ), two = new Entry( 2L );
	List<Entry> entries = Arrays.asList( one, two );

	@Mock
	private EntityQueryFacadeResolver entityQueryFacadeResolver;

	@Mock
	private EntityViewRequest viewRequest;

	@Mock
	private EntityViewContext viewContext;

	@Mock
	private EntityConfiguration entityConfiguration;

	@Mock
	private EntityAssociation entityAssociation;

	@Mock
	private Pageable pageable;

	@Mock
	private Page<Object> items;

	private DefaultEntityFetchingViewProcessor processor;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		processor = new DefaultEntityFetchingViewProcessor( entityQueryFacadeResolver );

		when( viewRequest.getEntityViewContext() ).thenReturn( viewContext );

		EntityViewContext parentContext = mock( EntityViewContext.class );
		when( parentContext.getEntity( Object.class ) ).thenReturn( PARENT );
		when( viewContext.getParentContext() ).thenReturn( parentContext );

		when( viewContext.getEntityConfiguration() ).thenReturn( entityConfiguration );
		when( viewContext.getEntityAssociation() ).thenReturn( entityAssociation );
	}

	@Test
	public void entityConfigurationUsesPagingAndSortingRepositoryIfPossible() {
		PagingAndSortingRepository repository = mock( PagingAndSortingRepository.class );
		when( entityConfiguration.getAttribute( Repository.class ) ).thenReturn( repository );
		when( repository.findAll( pageable ) ).thenReturn( items );

		verifyItems();
	}

	@Test
	public void entityQueryExecutorIsUsedIfNoPagingAndSortingRepository() {
		EntityQueryFacade queryExecutor = mock( EntityQueryFacade.class );
		when( entityQueryFacadeResolver.forEntityViewRequest( viewRequest ) ).thenReturn( queryExecutor );
		when( entityConfiguration.getAttribute( Repository.class ) ).thenReturn( mock( CrudRepository.class ) );
		when( queryExecutor.findAll( EntityQuery.all(), pageable ) ).thenReturn( items );

		verifyItems();
	}

	@Test(expected = IllegalStateException.class)
	public void exceptionIsThrownIfNeitherCrudRepositoryNorEntityQueryExecutor() {
		when( entityConfiguration.getAttribute( Repository.class ) ).thenReturn( mock( Repository.class ) );
		verifyItems();
	}

	@Test(expected = IllegalStateException.class)
	public void exceptionIsThrownIfAssociationButNoAssociatedEntityQueryExecutor() {
		when( viewContext.isForAssociation() ).thenReturn( true );
		verifyItems();
	}

	@Test
	public void associatedEntityQueryExecutorIsUsedWithParentEntity() {
		when( viewContext.isForAssociation() ).thenReturn( true );
		AssociatedEntityQueryExecutor queryExecutor = mock( AssociatedEntityQueryExecutor.class );
		when( entityAssociation.getAttribute( AssociatedEntityQueryExecutor.class ) ).thenReturn( queryExecutor );
		when( queryExecutor.findAll( PARENT, EntityQuery.all(), pageable ) ).thenReturn( items );

		verifyItems();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void pagingAndSortingRepositoryUsesSortIfPageableIsNotPresent() {
		Sort sort = new Sort( Sort.Direction.ASC, "name" );
		PagingAndSortingRepository repository = mock( PagingAndSortingRepository.class );
		when( entityConfiguration.getAttribute( Repository.class ) ).thenReturn( repository );
		when( repository.findAll( sort ) ).thenReturn( entries );

		assertThat( processor.fetchItems( viewRequest, mock( EntityView.class ), sort ) )
				.isNotNull()
				.containsExactly( one, two );
		verify( repository, times( 1 ) ).findAll( sort );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void entityQueryFacadeUsesSortIfPageableIsNotPresent() {
		Sort sort = new Sort( Sort.Direction.ASC, "name" );
		processor.setShowOnlyItemsWithAction( AllowableAction.READ );
		EntityQueryFacade queryExecutor = mock( EntityQueryFacade.class );
		when( entityQueryFacadeResolver.forEntityViewRequest( viewRequest ) ).thenReturn( queryExecutor );
		when( entityConfiguration.getAttribute( Repository.class ) ).thenReturn( mock( CrudRepository.class ) );
		EntityQuery query = EntityQuery.all();
		when( queryExecutor.findAll( query, sort ) ).thenReturn( entries );

		assertThat( processor.fetchItems( viewRequest, mock( EntityView.class ), sort ) )
				.isNotNull()
				.isInstanceOf( List.class )
				.containsExactly( one, two );
		verify( queryExecutor, times( 1 ) ).findAll( query, sort );
	}

	private void verifyItems() {
		assertSame( items, processor.fetchItems( viewRequest, mock( EntityView.class ), pageable ) );
	}

	@Data
	@RequiredArgsConstructor
	public static class Entry implements IdBasedEntity
	{
		private final Long id;
	}
}
