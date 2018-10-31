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
import org.springframework.validation.Errors;

import static com.foreach.across.modules.entity.registry.properties.EntityPropertyController.AFTER_ENTITY;
import static com.foreach.across.modules.entity.registry.properties.EntityPropertyController.BEFORE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
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
	private EntityPropertyController<?> propOne;

	@Mock
	private EntityPropertyController<?> propTwo;

	@Mock
	private EntityPropertyController<?> childContext;

	@Mock
	private EntityPropertyController<?> propOneAsChildContext;

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
		binder.setBindingContext( EntityPropertyBindingContext.forReading( "nothing" ) );

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

		binder.getBindingContext()
		      .getOrCreateChildContext( "child", ( parent, builder ) -> builder.controller( childContext ) );
		binder.getBindingContext()
		      .getOrCreateChildContext( "propOne", ( parent, builder ) -> builder.controller( propOneAsChildContext ) );

		controller = binder.createController();
	}

	@Test
	public void controllersAreOrderedForSave() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		when( propOne.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( propTwo.getOrder() ).thenReturn( AFTER_ENTITY );
		when( childContext.getOrder() ).thenReturn( AFTER_ENTITY );

		controller.addEntitySaveCallback( callbackOne, callbackTwo ).save();

		InOrder inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo, childContext );
		inOrder.verify( propOne ).save( any(), any() );
		inOrder.verify( callbackOne ).run();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( propTwo ).save( any(), any() );
		inOrder.verify( childContext ).save( any(), any() );

		reset( propOne, propTwo, callbackOne, callbackTwo, childContext );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( childContext.getOrder() ).thenReturn( BEFORE_ENTITY );

		binder.createController().addEntitySaveCallback( callbackTwo, callbackOne ).save();

		inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo, childContext );
		inOrder.verify( propTwo ).save( any(), any() );
		inOrder.verify( childContext ).save( any(), any() );
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( callbackOne ).run();
		inOrder.verify( propOne ).save( any(), any() );

		verifyZeroInteractions( propOneAsChildContext );
	}

	@Test
	public void controllersAreOrderedForApplyValues() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		when( propOne.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( propTwo.getOrder() ).thenReturn( AFTER_ENTITY );
		when( childContext.getOrder() ).thenReturn( BEFORE_ENTITY );

		binder.createController().applyValues();

		InOrder inOrder = inOrder( propTwo, propOne, childContext );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( childContext ).applyValue( any(), any() );
		inOrder.verify( propTwo ).applyValue( any(), any() );

		reset( propOne, propTwo, childContext );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( childContext.getOrder() ).thenReturn( AFTER_ENTITY );

		binder.createController().applyValues();

		inOrder = inOrder( propTwo, propOne, childContext );
		inOrder.verify( propTwo ).applyValue( any(), any() );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( childContext ).applyValue( any(), any() );

		verifyZeroInteractions( propOneAsChildContext );
	}

	@Test
	public void controllersAreOrderedForValidate() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		when( propOne.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( propTwo.getOrder() ).thenReturn( AFTER_ENTITY );

		Errors errors = mock( Errors.class );
		controller.addEntityValidationCallback( callbackOne, callbackTwo ).validate( errors );

		InOrder inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo );
		inOrder.verify( propOne ).validate( any(), any(), any() );
		inOrder.verify( callbackOne ).run();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( propTwo ).validate( any(), any(), any() );

		reset( propOne, propTwo, callbackOne, callbackTwo );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );

		assertThat( binder.createController().addEntityValidationCallback( callbackTwo, callbackOne ).validate( errors ) )
				.isTrue();

		inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo, errors );
		inOrder.verify( errors ).pushNestedPath( "properties[propOne]" );
		inOrder.verify( propTwo ).validate( any(), any(), any() );
		inOrder.verify( errors ).popNestedPath();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( callbackOne ).run();
		inOrder.verify( errors ).pushNestedPath( "properties[propOne]" );
		inOrder.verify( propOne ).validate( any(), any(), any() );
		inOrder.verify( errors ).popNestedPath();

		verifyZeroInteractions( propOneAsChildContext, childContext );
	}

	@Test
	public void validationFailsIfErrorsHaveBeenAdded() {
		binder.get( "propOne" ).setValue( "one" );

		Errors errors = mock( Errors.class );
		when( errors.getErrorCount() ).thenReturn( 0 );

		doAnswer( invocation -> when( errors.getErrorCount() ).thenReturn( 1 ) )
				.when( propOne )
				.validate( any(), any(), any() );
		assertThat( binder.createController().applyValuesAndValidate( errors ) ).isFalse();
	}

	@Test
	public void applyValuesAndValidateDoesValidationAfterApplyingTheValues() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		when( propOne.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( propTwo.getOrder() ).thenReturn( AFTER_ENTITY );
		when( childContext.getOrder() ).thenReturn( BEFORE_ENTITY );

		Errors errors = mock( Errors.class );
		controller.addEntityValidationCallback( callbackOne, callbackTwo ).applyValuesAndValidate( errors );

		InOrder inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo, childContext );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( childContext ).applyValue( any(), any() );
		inOrder.verify( propTwo ).applyValue( any(), any() );
		inOrder.verify( propOne ).validate( any(), any(), any() );
		inOrder.verify( callbackOne ).run();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( propTwo ).validate( any(), any(), any() );

		reset( propOne, propTwo, callbackOne, callbackTwo, childContext );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( childContext.getOrder() ).thenReturn( AFTER_ENTITY );

		assertThat( binder.createController().addEntityValidationCallback( callbackTwo, callbackOne ).applyValuesAndValidate( errors ) )
				.isTrue();

		inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo, errors, childContext );
		inOrder.verify( propTwo ).applyValue( any(), any() );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( childContext ).applyValue( any(), any() );
		inOrder.verify( errors ).pushNestedPath( "properties[propTwo]" );
		inOrder.verify( propTwo ).validate( any(), any(), any() );
		inOrder.verify( errors ).popNestedPath();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( callbackOne ).run();
		inOrder.verify( errors ).pushNestedPath( "properties[propOne]" );
		inOrder.verify( propOne ).validate( any(), any(), any() );
		inOrder.verify( errors ).popNestedPath();

		verifyZeroInteractions( propOneAsChildContext );
	}
}

