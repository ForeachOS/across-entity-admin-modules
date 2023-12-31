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

package com.foreach.across.modules.entity.config.builders;

import com.foreach.across.modules.entity.query.EntityQueryFacadeResolver;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.views.DispatchingEntityViewFactory;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.processors.*;
import com.foreach.across.modules.entity.views.processors.query.EntityQueryFilterConfiguration;
import com.foreach.across.modules.entity.views.processors.support.EntityViewProcessorRegistry;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class TestEntityListViewFactoryBuilder
{
	@Mock
	private AutowireCapableBeanFactory beanFactory;

	@Mock
	private DispatchingEntityViewFactory dispatchingViewFactory;

	private EntityListViewFactoryBuilder builder;
	private EntityViewProcessorRegistry processors;

	@BeforeEach
	public void before() {
		processors = new EntityViewProcessorRegistry();

		when( dispatchingViewFactory.getProcessorRegistry() ).thenReturn( processors );
		when( beanFactory.createBean( DispatchingEntityViewFactory.class ) ).thenReturn( dispatchingViewFactory );

		builder = new EntityListViewFactoryBuilder( beanFactory ).factoryType( DispatchingEntityViewFactory.class );
	}

	@Test
	public void andAppliesAdditionalConsumer() {
		Consumer<EntityViewFactoryBuilder> consumer = mock( Consumer.class );
		assertSame( builder, builder.and( consumer ) );
		verify( consumer ).accept( builder );
	}

	@Test
	public void propertiesToShowAndViewElementMode() {
		SortableTableRenderingViewProcessor tableRenderingViewProcessor = new SortableTableRenderingViewProcessor();
		when( beanFactory.createBean( SortableTableRenderingViewProcessor.class ) ).thenReturn( tableRenderingViewProcessor );

		assertSame( builder, builder.showProperties( "one", "two" )
		                            .showProperties( "three", "." )
		                            .viewElementMode( ViewElementMode.CONTROL )
		                            .sortableOn( "three", "four" )
		                            .showResultNumber( false ) );
		assertSame( dispatchingViewFactory, builder.build() );

		Optional<SortableTableRenderingViewProcessor> processor
				= processors.getProcessor( SortableTableRenderingViewProcessor.class.getName(), SortableTableRenderingViewProcessor.class );
		assertTrue( processor.isPresent() );
		processor.ifPresent( p -> assertSame( tableRenderingViewProcessor, p ) );

		SortableTableRenderingViewProcessor expected = new SortableTableRenderingViewProcessor();
		expected.setViewElementMode( ViewElementMode.CONTROL );
		expected.setSortableProperties( Arrays.asList( "three", "four" ) );
		expected.setShowResultNumber( false );
		expected.setPropertySelector( EntityPropertySelector.of( "one", "two", "three" ) );
		assertEquals( expected, tableRenderingViewProcessor );
	}

	@Test
	public void pageableProperties() {
		PageableExtensionViewProcessor pageableProcessor = new PageableExtensionViewProcessor();
		when( beanFactory.createBean( PageableExtensionViewProcessor.class ) ).thenReturn( pageableProcessor );

		assertSame( builder, builder.pageSize( 200 ) );
		assertSame( dispatchingViewFactory, builder.build() );

		Optional<PageableExtensionViewProcessor> processor
				= processors.getProcessor( PageableExtensionViewProcessor.class.getName(), PageableExtensionViewProcessor.class );
		assertTrue( processor.isPresent() );
		processor.ifPresent( p -> assertSame( pageableProcessor, p ) );

		PageableExtensionViewProcessor expected = new PageableExtensionViewProcessor();
		expected.setDefaultPageable( PageRequest.of( 0, 200 ) );
		assertEquals( expected, pageableProcessor );

		builder.defaultSort( "name" ).build();
		expected.setDefaultPageable( PageRequest.of( 0, 200, Sort.by( "name" ) ) );
		assertEquals( expected, pageableProcessor );

		builder.pageSize( 10 ).build();
		expected.setDefaultPageable( PageRequest.of( 0, 10, Sort.by( "name" ) ) );
		assertEquals( expected, pageableProcessor );
	}

	@Test
	public void entityQueryFilterEnabled() {
		EntityQueryFilterProcessor queryFilterProcessor = mock( EntityQueryFilterProcessor.class );
		when( queryFilterProcessor.getFilterConfiguration() ).thenReturn( EntityQueryFilterConfiguration.builder().build() );
		when( beanFactory.createBean( EntityQueryFilterProcessor.class ) ).thenReturn( queryFilterProcessor );

		builder.entityQueryFilter( true ).build();

		Optional<EntityQueryFilterProcessor> processor
				= processors.getProcessor( EntityQueryFilterProcessor.class.getName(), EntityQueryFilterProcessor.class );
		assertTrue( processor.isPresent() );
		processor.ifPresent( p -> assertSame( queryFilterProcessor, p ) );

		builder.entityQueryFilter( true ).build();
		processor = processors.getProcessor( EntityQueryFilterProcessor.class.getName(), EntityQueryFilterProcessor.class );
		assertTrue( processor.isPresent() );
		processor.ifPresent( p -> assertSame( queryFilterProcessor, p ) );

		builder.entityQueryFilter( false ).build();
		assertFalse( processors.contains( EntityQueryFilterProcessor.class.getName() ) );
	}

	@Test
	public void customPageFetcher() {
		BiFunction<EntityViewContext, Pageable, Iterable<?>> consumer = ( ctx, pageable ) -> null;

		assertSame( builder, builder.pageFetcher( consumer ) );
		assertSame( dispatchingViewFactory, builder.build() );

		Optional<EntityViewProcessorRegistry.EntityViewProcessorRegistration> registration
				= processors.getProcessorRegistration( DelegatingEntityFetchingViewProcessor.class.getName() );
		assertTrue( registration.isPresent() );
		registration.ifPresent( r -> {
			assertEquals( DelegatingEntityFetchingViewProcessor.DEFAULT_ORDER, r.getOrder() );
			assertEquals( new DelegatingEntityFetchingViewProcessor( consumer ), r.getProcessor() );
		} );

		builder.pageFetcher( pageable -> Collections.emptyList() ).build();

		registration = processors.getProcessorRegistration( DelegatingEntityFetchingViewProcessor.class.getName() );
		assertTrue( registration.isPresent() );
		registration.ifPresent( r -> {
			assertEquals( DelegatingEntityFetchingViewProcessor.DEFAULT_ORDER, r.getOrder() );
			assertNotNull( r.getProcessor( DelegatingEntityFetchingViewProcessor.class ) );
			assertNotEquals( new DelegatingEntityFetchingViewProcessor( consumer ), r.getProcessor() );
		} );
	}

	@Test
	public void requestedActionWithDefaultEntityFetchingViewProcessor() {
		processors.addProcessor( DefaultEntityFetchingViewProcessor.class.getName(),
		                         new DefaultEntityFetchingViewProcessor( mock( EntityQueryFacadeResolver.class ) ) );
		AllowableAction action = AllowableAction.READ;
		builder.showOnlyItemsWithAction( action ).build();

		Optional<DefaultEntityFetchingViewProcessor> processor
				= processors.getProcessor( DefaultEntityFetchingViewProcessor.class.getName(), DefaultEntityFetchingViewProcessor.class );
		Assertions.assertThat( processor )
		          .isPresent()
		          .get()
		          .hasFieldOrPropertyWithValue( "showOnlyItemsWithAction", action );
	}

	@Test
	public void requestedActionWithEntityQueryFilterProcessor() {
		EntityQueryFilterProcessor queryFilterProcessor = new EntityQueryFilterProcessor();
		queryFilterProcessor.setFilterConfiguration( EntityQueryFilterConfiguration.builder().build() );
		when( beanFactory.createBean( EntityQueryFilterProcessor.class ) ).thenReturn( queryFilterProcessor );
		AllowableAction action = AllowableAction.READ;

		builder.showOnlyItemsWithAction( action ).entityQueryFilter( true ).build();

		Optional<EntityQueryFilterProcessor> processor
				= processors.getProcessor( EntityQueryFilterProcessor.class.getName(), EntityQueryFilterProcessor.class );
		Assertions.assertThat( processor )
		          .isPresent()
		          .get()
		          .isEqualTo( queryFilterProcessor )
		          .hasFieldOrPropertyWithValue( "showOnlyItemsWithAction", action );
	}
}
