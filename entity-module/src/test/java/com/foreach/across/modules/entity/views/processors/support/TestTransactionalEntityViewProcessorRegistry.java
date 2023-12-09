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

package com.foreach.across.modules.entity.views.processors.support;

import com.foreach.across.modules.entity.views.EntityViewProcessor;
import com.foreach.across.modules.entity.views.processors.EntityViewProcessorAdapter;
import com.foreach.across.modules.entity.views.processors.SimpleEntityViewProcessorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
public class TestTransactionalEntityViewProcessorRegistry
{
	private TransactionalEntityViewProcessorRegistry registry;

	@Mock
	private TransactionTemplate transactionTemplate;

	@Mock
	private EntityViewProcessor one;

	@Mock
	private SimpleEntityViewProcessorAdapter two;

	@Mock
	private EntityViewProcessorAdapter three;

	@BeforeEach
	public void setUp() throws Exception {
		registry = new TransactionalEntityViewProcessorRegistry();
		registry.setTransactionTemplate( transactionTemplate );

		registry.addProcessor( one );
		registry.addProcessor( two );
		registry.addProcessor( three );
	}

	@Test
	public void withoutTransactionTemplateAllDispatchingIsSameAsDefault() {
		registry.setTransactionTemplate( null );

		@SuppressWarnings("unchecked")
		Consumer<EntityViewProcessor> c = mock( Consumer.class );
		registry.dispatch( c, true );

		InOrder ordered = inOrder( c );
		ordered.verify( c ).accept( one );
		ordered.verify( c ).accept( two );
		ordered.verify( c ).accept( three );
	}

	@Test
	public void withoutTransactionTemplateAllTypeSpecificDispatchingIsSameAsDefault() {
		registry.setTransactionTemplate( null );

		@SuppressWarnings("unchecked")
		Consumer<SimpleEntityViewProcessorAdapter> c = mock( Consumer.class );
		registry.dispatch( c, SimpleEntityViewProcessorAdapter.class, true );

		InOrder ordered = inOrder( c );
		ordered.verify( c ).accept( two );
		verifyNoMoreInteractions( c );
	}

	@Test
	public void explicitDispatchingWithoutTransactionIsSameAsDefault() {
		@SuppressWarnings("unchecked")
		Consumer<EntityViewProcessor> c = mock( Consumer.class );
		registry.dispatch( c, false );

		InOrder ordered = inOrder( c );
		ordered.verify( c ).accept( one );
		ordered.verify( c ).accept( two );
		ordered.verify( c ).accept( three );
	}

	@Test
	public void explicitTypeSpecificDispatchingWithoutTransactionIsSameAsDefault() {
		@SuppressWarnings("unchecked")
		Consumer<SimpleEntityViewProcessorAdapter> c = mock( Consumer.class );
		registry.dispatch( c, SimpleEntityViewProcessorAdapter.class, false );

		InOrder ordered = inOrder( c );
		ordered.verify( c ).accept( two );
		verifyNoMoreInteractions( c );
	}

	@Test
	public void explicitTypeTransactionalDispatchingIsRunThroughTheTransactionTemplate() {
		AtomicReference<TransactionCallback> callback = new AtomicReference<>();
		doAnswer( invocation -> {
			callback.set( invocation.getArgument( 0 ) );
			return null;
		} ).when( transactionTemplate ).execute( any() );
		@SuppressWarnings("unchecked")
		Consumer<SimpleEntityViewProcessorAdapter> c = mock( Consumer.class );
		registry.dispatch( c, SimpleEntityViewProcessorAdapter.class, true );

		verifyNoMoreInteractions( c );
		assertNotNull( callback.get() );

		callback.get().doInTransaction( mock( TransactionStatus.class ) );

		InOrder ordered = inOrder( c );
		ordered.verify( c ).accept( two );
		verifyNoMoreInteractions( c );
	}

	@Test
	public void transactionalDispatchingIsRunThroughTheTransactionTemplate() {
		AtomicReference<TransactionCallback> callback = new AtomicReference<>();
		doAnswer( invocation -> {
			callback.set( invocation.getArgument( 0 ) );
			return null;
		} ).when( transactionTemplate ).execute( any() );

		@SuppressWarnings("unchecked")
		Consumer<EntityViewProcessor> c = mock( Consumer.class );
		registry.dispatch( c, true );

		verifyNoMoreInteractions( c );
		assertNotNull( callback.get() );

		callback.get().doInTransaction( mock( TransactionStatus.class ) );

		InOrder ordered = inOrder( c );
		ordered.verify( c ).accept( one );
		ordered.verify( c ).accept( two );
		ordered.verify( c ).accept( three );
	}
}
