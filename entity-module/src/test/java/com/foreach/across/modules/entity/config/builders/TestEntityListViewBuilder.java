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

import com.foreach.across.modules.entity.registry.EntityRegistryImpl;
import com.foreach.across.modules.entity.registry.MutableEntityConfiguration;
import com.foreach.across.modules.entity.registry.MutableEntityRegistry;
import com.foreach.across.modules.entity.testmodules.springdata.business.Client;
import com.foreach.across.modules.entity.testmodules.springdata.business.Company;
import com.foreach.across.modules.entity.views.EntityListViewFactory;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
public class TestEntityListViewBuilder
{
	private final static String VIEW = "listView";

	private EntitiesConfigurationBuilder entities;
	private MutableEntityRegistry entityRegistry;

	private EntityConfigurationBuilder builder;
	private MutableEntityConfiguration client, company;

	private EntityListViewBuilder view;

	private EntityListViewFactory viewFactory;

	@Before
	public void before() {
		entities = new EntitiesConfigurationBuilder();

		entityRegistry = new EntityRegistryImpl();

		client = mock( MutableEntityConfiguration.class );
		when( client.getEntityType() ).thenReturn( Client.class );
		when( client.getName() ).thenReturn( "client" );

		company = mock( MutableEntityConfiguration.class );
		when( company.getEntityType() ).thenReturn( Company.class );
		when( company.getName() ).thenReturn( "company" );

		entityRegistry.register( client );
		entityRegistry.register( company );

		builder = entities.entity( Client.class );

		view = builder.listView( VIEW );

		viewFactory = null;
		doAnswer(
				new Answer<Void>()
				{
					@Override
					public Void answer( InvocationOnMock invocation ) throws Throwable {
						viewFactory = (EntityListViewFactory) invocation.getArguments()[1];
						return null;
					}
				}
		).when( client ).registerView( eq( VIEW ), any( EntityViewFactory.class ) );

		reset( company );
	}

	@Test
	public void customTemplate() {
		view.template( "th/someTemplate" );
		view.apply( client );

		assertNotNull( viewFactory );
		assertEquals( "th/someTemplate", viewFactory.getTemplate() );
	}
}
