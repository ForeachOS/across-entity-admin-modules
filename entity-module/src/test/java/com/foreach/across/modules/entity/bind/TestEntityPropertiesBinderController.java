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

package com.foreach.across.modules.entity.bind;

import com.foreach.across.modules.entity.registry.properties.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.foreach.across.modules.entity.registry.properties.EntityPropertyController.AFTER_ENTITY;
import static com.foreach.across.modules.entity.registry.properties.EntityPropertyController.BEFORE_ENTITY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEntityPropertiesBinderController
{
	@Mock
	private EntityPropertyController<String, String> propOne;

	@Mock
	private EntityPropertyController<String, String> propTwo;

	@Mock
	private Runnable callbackOne;

	@Mock
	private Runnable callbackTwo;

	private EntityPropertiesBinder binder;
	private EntityPropertiesBinderController controller;

	@Before
	public void before() {
		MutableEntityPropertyRegistry registry = mock( MutableEntityPropertyRegistry.class );
		binder = new EntityPropertiesBinder( registry );
		binder.setBindingContext( EntityPropertyBindingContext.of( "nothing" ) );

		MutableEntityPropertyDescriptor propertyOne = EntityPropertyDescriptor.builder( "propOne" )
		                                                                      .propertyType( String.class )
		                                                                      .controller( propOne )
		                                                                      .build();
		when( registry.getProperty( "propOne" ) ).thenReturn( propertyOne );

		MutableEntityPropertyDescriptor propertyTwo = EntityPropertyDescriptor.builder( "propTwo" )
		                                                                      .propertyType( String.class )
		                                                                      .controller( propTwo )
		                                                                      .build();
		when( registry.getProperty( "propTwo" ) ).thenReturn( propertyTwo );

		controller = binder.createController();
	}

	@Test
	public void controllersAreOrderedForSave() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		when( propOne.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( propTwo.getOrder() ).thenReturn( AFTER_ENTITY );

		controller.addEntitySaveCallback( callbackOne, callbackTwo ).save();

		InOrder inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo );
		inOrder.verify( propOne ).save( any(), any() );
		inOrder.verify( callbackOne ).run();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( propTwo ).save( any(), any() );

		reset( propOne, propTwo, callbackOne, callbackTwo );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );

		binder.createController().addEntitySaveCallback( callbackTwo, callbackOne ).save();

		inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo );
		inOrder.verify( propTwo ).save( any(), any() );
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( callbackOne ).run();
		inOrder.verify( propOne ).save( any(), any() );
	}

	@Test
	public void controllersAreOrderedForValidate() {

	}

	@Test
	public void valueIsAppliedOnlyIfValidationDidNotFail() {

	}

	@Test
	public void validationFailsIfErrorsHaveBeenAdded() {
	}
}

