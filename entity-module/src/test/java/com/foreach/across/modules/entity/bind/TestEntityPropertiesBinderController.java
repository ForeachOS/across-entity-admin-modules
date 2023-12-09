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

import com.foreach.across.modules.entity.registry.properties.EntityPropertyController;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.validation.Errors;

import static com.foreach.across.modules.entity.registry.properties.EntityPropertyController.AFTER_ENTITY;
import static com.foreach.across.modules.entity.registry.properties.EntityPropertyController.BEFORE_ENTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestEntityPropertiesBinderController
{
	@Mock
	private EntityPropertyController<?> propOne;

	@Mock
	private EntityPropertyController<?> propTwo;

	@Mock
	private Runnable callbackOne;

	@Mock
	private Runnable callbackTwo;

	private EntityPropertiesBinder binder;
	private EntityPropertiesBinderController controller;

	@BeforeEach
	public void before() {
		MutableEntityPropertyRegistry registry = mock( MutableEntityPropertyRegistry.class );
		binder = new EntityPropertiesBinder( registry );
		binder.setTarget( "nothing" );

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

		assertThat( binder.isDirty() ).isTrue();
	}

	@Test
	public void controllersAreOrderedForApplyValues() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		when( propOne.getOrder() ).thenReturn( BEFORE_ENTITY );
		when( propTwo.getOrder() ).thenReturn( AFTER_ENTITY );

		binder.createController().applyValues();

		InOrder inOrder = inOrder( propTwo, propOne );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( propTwo ).applyValue( any(), any() );

		reset( propOne, propTwo );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );

		binder.setDirty( true );
		binder.createController().applyValues();

		inOrder = inOrder( propTwo, propOne );
		inOrder.verify( propTwo ).applyValue( any(), any() );
		inOrder.verify( propOne ).applyValue( any(), any() );

		assertThat( binder.isDirty() ).isFalse();
	}

	@Test
	public void illegalStateExceptionsOnControllerInReadonlyMode() {
		binder.setTarget( null );

		assertThatExceptionOfType( IllegalStateException.class )
				.isThrownBy( () -> binder.createController().validate( mock( Errors.class ) ) )
				.withMessage( "Unable to perform EntityPropertiesBinderController actions - no target has been set" );

		assertThatExceptionOfType( IllegalStateException.class )
				.isThrownBy( () -> binder.createController().applyValues() )
				.withMessage( "Unable to perform EntityPropertiesBinderController actions - no target has been set" );

		assertThatExceptionOfType( IllegalStateException.class )
				.isThrownBy( () -> binder.createController().save() )
				.withMessage( "Unable to perform EntityPropertiesBinderController actions - no target has been set" );
	}

	@Test
	public void applyValuesIsSkippedIfNotDirty() {
		binder.get( "propOne" ).setValue( "one" );
		binder.get( "propTwo" ).setValue( "two" );

		binder.setDirty( false );
		binder.createController().applyValues();

		verify( propOne, never() ).applyValue( any(), any() );
		verify( propTwo, never() ).applyValue( any(), any() );

		assertThat( binder.isDirty() ).isFalse();
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

		assertThat( binder.isDirty() ).isTrue();
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

		Errors errors = mock( Errors.class );
		controller.addEntityValidationCallback( callbackOne, callbackTwo ).applyValuesAndValidate( errors );

		InOrder inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( propTwo ).applyValue( any(), any() );
		inOrder.verify( propOne ).validate( any(), any(), any() );
		inOrder.verify( callbackOne ).run();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( propTwo ).validate( any(), any(), any() );

		reset( propOne, propTwo, callbackOne, callbackTwo );

		when( propOne.getOrder() ).thenReturn( AFTER_ENTITY );
		when( propTwo.getOrder() ).thenReturn( BEFORE_ENTITY );

		binder.setDirty( true );
		assertThat( binder.createController().addEntityValidationCallback( callbackTwo, callbackOne ).applyValuesAndValidate( errors ) )
				.isTrue();

		inOrder = inOrder( propTwo, callbackOne, propOne, callbackTwo, errors );
		inOrder.verify( propTwo ).applyValue( any(), any() );
		inOrder.verify( propOne ).applyValue( any(), any() );
		inOrder.verify( errors ).pushNestedPath( "properties[propTwo]" );
		inOrder.verify( propTwo ).validate( any(), any(), any() );
		inOrder.verify( errors ).popNestedPath();
		inOrder.verify( callbackTwo ).run();
		inOrder.verify( callbackOne ).run();
		inOrder.verify( errors ).pushNestedPath( "properties[propOne]" );
		inOrder.verify( propOne ).validate( any(), any(), any() );
		inOrder.verify( errors ).popNestedPath();

		assertThat( binder.isDirty() ).isFalse();
	}
}

