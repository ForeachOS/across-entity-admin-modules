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

import com.foreach.across.modules.entity.bind.EntityPropertiesBinder.ChildPropertyPropertiesBinder;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyBindingContext;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyController;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyValue;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@SuppressWarnings({ "Duplicates", "unchecked" })
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TestSingleEntityPropertyBinder
{
	@Mock
	private EntityPropertiesBinder.EntityPropertiesBinderValueBindingContext bindingContext;

	@Mock
	private EntityPropertiesBinder binder;

	@Mock
	private EntityPropertyController controller;

	private EntityPropertyDescriptor descriptor;
	private SingleEntityPropertyBinder property;

	@BeforeEach
	@SuppressWarnings("unchecked")
	public void resetMocks() {
		reset( binder, controller );

		when( bindingContext.getEntity() ).thenReturn( "entity" );
		when( bindingContext.getTarget() ).thenReturn( "entity" );
		when( bindingContext.isReadonly() ).thenReturn( true );
		when( binder.getValueBindingContext() ).thenReturn( bindingContext );

		when( controller.fetchValue( bindingContext ) ).thenReturn( 1 );

		doAnswer( i -> i.getArgument( 0 ) )
				.when( binder )
				.convertIfNecessary( any(), eq( TypeDescriptor.valueOf( Integer.class ) ), eq( "properties[prop].value" ) );

		descriptor = spy(
				EntityPropertyDescriptor
						.builder( "prop" )
						.controller( controller )
						.propertyType( Integer.class )
						.build()
		);

		property = new SingleEntityPropertyBinder( binder, descriptor );
		property.setBinderPath( "properties[prop]" );
		when( binder.getProperties() ).thenReturn( binder );
		when( binder.get( "prop" ) ).thenReturn( property );
	}

	@Test
	public void defaultProperties() {
		when( controller.getOrder() ).thenReturn( 33 );

		assertThat( property.isBound() ).isFalse();
		assertThat( property.isModified() ).isFalse();
		assertThat( property.getControllerOrder() ).isEqualTo( 33 );

		assertThat( property.getSortIndex() ).isEqualTo( 0 );
		property.setSortIndex( 123 );
		assertThat( property.getSortIndex() ).isEqualTo( 123 );
	}

	@Test
	public void initialValueGetsLazyLoadedFromTheController() {
		verify( controller, never() ).fetchValue( any() );
		assertThat( property.getValue() ).isEqualTo( 1 );
	}

	@Test
	public void updatingTheValueDoesNotUseTheController() {
		property.setValue( 123 );
		verify( controller ).fetchValue( bindingContext );

		assertThat( property.getValue() ).isEqualTo( 123 );
		verifyNoMoreInteractions( controller );
		property.setValue( 567 );
		assertThat( property.getValue() ).isEqualTo( 567 );
		verifyNoMoreInteractions( controller );
	}

	@Test
	public void originalValueVsCurrentValue() {
		assertThat( property.getOriginalValue() ).isEqualTo( 1 );
		assertThat( property.getValue() ).isEqualTo( 1 );
		property.setValue( 123 );
		assertThat( property.getOriginalValue() ).isEqualTo( 1 );
		assertThat( property.getValue() ).isEqualTo( 123 );
	}

	@Test
	public void settingBoundToTrueMarksAsDirty() {
		assertThat( property.isDirty() ).isFalse();
		property.setBound( true );
		assertThat( property.isDirty() ).isTrue();
	}

	@Test
	public void settingBoundToFalseDoesNotImpactDirty() {
		assertThat( property.isDirty() ).isFalse();
		property.setBound( false );
		assertThat( property.isDirty() ).isFalse();
	}

	@Test
	public void propertyNotDirtyIfValueIsBeingFetched() {
		assertThat( property.getOriginalValue() ).isEqualTo( 1 );
		assertThat( property.getValue() ).isEqualTo( 1 );
		assertThat( property.isDeleted() ).isFalse();
		assertThat( property.isDirty() ).isFalse();

		verify( binder, never() ).markDirty();
	}

	@Test
	public void binderIsDirtyIfDeletedCalled() {
		property.setDeleted( false );
		assertThat( property.isDirty() ).isTrue();

		verify( binder ).markDirty();
	}

	@Test
	public void binderIsDirtyIfSetValueCalledWithAnyValue() {
		property.setValue( 1 );
		assertThat( property.isDirty() ).isTrue();

		verify( binder ).markDirty();
	}

	@Test
	public void onlyApplyValueRemovesDirtyFlag() {
		property.setValue( null );
		assertThat( property.isDirty() ).isTrue();

		property.applyValue();
		assertThat( property.isDirty() ).isFalse();

		property.setDeleted( true );
		assertThat( property.isDirty() ).isTrue();

		property.save();
		assertThat( property.isDirty() ).isTrue();

		property.validate( mock( Errors.class ) );
		assertThat( property.isDirty() ).isTrue();

		verify( binder, times( 2 ) ).markDirty();
	}

	@Test
	public void updatingWithSameValueDoesNotModify() {
		assertThat( property.isModified() ).isFalse();
		property.setValue( 1 );
		assertThat( property.isModified() ).isFalse();
	}

	@Test
	public void updatingWithOtherValueModifies() {
		assertThat( property.isModified() ).isFalse();
		property.setValue( 123 );
		assertThat( property.isModified() ).isTrue();
	}

	@Test
	public void boundButNotSetValueModifies() {
		property.setBound( true );
		assertThat( property.isDirty() ).isTrue();
		assertThat( property.isModified() ).isTrue();
	}

	@Test
	public void boundButSameValueDoesNotModify() {
		property.setBound( true );
		property.setValue( 1 );
		assertThat( property.isModified() ).isFalse();

		resetMocks();

		property.setValue( 1 );
		property.setBound( true );
		assertThat( property.isModified() ).isFalse();
	}

	@Test
	public void boundButNotSetIsConsideredDeleted() {
		property.setBound( true );
		assertThat( property.getValue() ).isNull();
		assertThat( property.isDeleted() ).isTrue();
	}

	@Test
	public void notBoundButExplicitlyDeleted() {
		assertThat( property.isDeleted() ).isFalse();
		property.setDeleted( true );
		assertThat( property.isDeleted() ).isTrue();
	}

	@Test
	public void settingNullValueIsNotSameAsDeleted() {
		property.setValue( null );
		assertThat( property.getValue() ).isNull();
		assertThat( property.isDeleted() ).isFalse();
	}

	@Test
	public void settingEmptyStringOnNonStringPropertyResultsInNullValue() {
		property.setValue( "" );
		assertThat( property.getValue() ).isNull();
		verify( controller ).fetchValue( bindingContext );
		verifyNoMoreInteractions( controller );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void emptyStringOnStringPropertyIsAllowed() {
		when( descriptor.getPropertyType() ).thenReturn( (Class) String.class );
		when( descriptor.getPropertyTypeDescriptor() ).thenReturn( TypeDescriptor.valueOf( String.class ) );

		doAnswer( i -> i.getArgument( 0 ) )
				.when( binder )
				.convertIfNecessary( any(), eq( TypeDescriptor.valueOf( String.class ) ), eq( "properties[prop].value" ) );

		property.setValue( "" );
		assertThat( property.getValue() ).isEqualTo( "" );

		verify( controller ).fetchValue( bindingContext );
		verifyNoMoreInteractions( controller );
	}

	@Test
	public void initializeValueCreatesNewValueButDoesNotUpdatePropertyItself() {
		when( binder.createValue( controller ) )
				.thenReturn( "hello" );
		Object value = property.createNewValue();
		assertThat( value ).isEqualTo( "hello" );

		assertThat( property.getValue() ).isEqualTo( 1 );
	}

	@Test
	public void getInitializedValueDoesNotModifyCurrentValue() {
		assertThat( property.getInitializedValue() ).isEqualTo( 1 );
		verify( binder, never() ).createValue( any() );
	}

	@Test
	public void getInitializedValueInitializesNewValueIfNull() {
		property.setValue( null );
		when( binder.createValue( controller ) )
				.thenReturn( 123 );
		assertThat( property.getInitializedValue() ).isEqualTo( 123 );
		assertThat( property.getValue() ).isEqualTo( 123 );
	}

	@Test
	public void applyValueFlushesToTheControllerWithTheOriginalValue() {
		when( controller.applyValue( bindingContext, new EntityPropertyValue<>( 1, 1, false ) ) ).thenReturn( true );
		assertThat( property.applyValue() ).isTrue();

		property.setValue( 123 );
		assertThat( property.applyValue() ).isFalse();
		verify( controller ).applyValue( bindingContext, new EntityPropertyValue<>( 1, 123, false ) );
	}

	@Test
	public void applyValueAppliesNullIfDeleted() {
		when( controller.applyValue( bindingContext, new EntityPropertyValue<>( 1, null, true ) ) ).thenReturn( true );
		property.setBound( true );
		assertThat( property.applyValue() ).isTrue();
		verify( controller ).applyValue( bindingContext, new EntityPropertyValue<>( 1, null, true ) );
	}

	@Test
	public void validateOnValue() {
		Errors errors = new BeanPropertyBindingResult( binder, "" );
		errors.pushNestedPath( "properties[prop]" );

		assertThat( property.validate( errors, "hint" ) ).isTrue();
		assertThat( errors.getErrorCount() ).isEqualTo( 0 );

		property.setValue( 123 );

		doAnswer( invocation -> {
			EntityPropertyBindingContext bindingContext = invocation.getArgument( 0 );
			assertThat( bindingContext ).isSameAs( this.bindingContext );

			EntityPropertyValue<Object> propertyValue = invocation.getArgument( 1 );
			assertThat( propertyValue ).isEqualTo( new EntityPropertyValue<Object>( 1, 123, false ) );

			Errors err = invocation.getArgument( 2 );
			err.rejectValue( "", "bad-value" );

			return null;
		} )
				.when( controller )
				.validate( any(), any(), eq( errors ), eq( "hint" ) );

		assertThat( property.validate( errors, "hint" ) ).isFalse();

		errors.popNestedPath();

		assertThat( errors.getErrorCount() ).isEqualTo( 1 );
		assertThat( errors.getFieldError( "properties[prop].value" ) )
				.isNotNull()
				.satisfies( fe -> {
					assertThat( fe.isBindingFailure() ).isFalse();
					assertThat( fe.getField() ).isEqualTo( "properties[prop].value" );
					assertThat( fe.getRejectedValue() ).isEqualTo( 123 );
					assertThat( fe.getCode() ).isEqualTo( "bad-value" );
				} );
	}

	@Test
	public void validateOnInitializedValue() {
		Errors errors = new BeanPropertyBindingResult( binder, "" );
		errors.pushNestedPath( "properties[prop]" );

		assertThat( property.validate( errors, "hint" ) ).isTrue();
		assertThat( errors.getErrorCount() ).isEqualTo( 0 );

		property.setValue( null );
		when( binder.createValue( controller ) )
				.thenReturn( 123 );
		property.getInitializedValue();

		doAnswer( invocation -> {
			EntityPropertyBindingContext bindingContext = invocation.getArgument( 0 );
			assertThat( bindingContext ).isSameAs( this.bindingContext );

			EntityPropertyValue<Object> propertyValue = invocation.getArgument( 1 );
			assertThat( propertyValue ).isEqualTo( new EntityPropertyValue<Object>( 1, 123, false ) );

			Errors err = invocation.getArgument( 2 );
			err.rejectValue( "class", "bad-value" );

			return null;
		} )
				.when( controller )
				.validate( any(), any(), eq( errors ), eq( "hint" ) );

		assertThat( property.validate( errors, "hint" ) ).isFalse();

		errors.popNestedPath();

		assertThat( errors.getErrorCount() ).isEqualTo( 1 );
		assertThat( errors.getFieldError( "properties[prop].initializedValue.class" ) )
				.isNotNull()
				.satisfies( fe -> {
					assertThat( fe.isBindingFailure() ).isFalse();
					assertThat( fe.getField() ).isEqualTo( "properties[prop].initializedValue.class" );
					assertThat( fe.getRejectedValue() ).isEqualTo( Integer.class );
					assertThat( fe.getCode() ).isEqualTo( "bad-value" );
				} );
	}

	@Test
	public void propertiesGetCreatedFirstTime() {
		property.setValue( null );

		when( binder.createValue( controller ) )
				.thenReturn( "hello" );
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		assertThat( property.getProperties() ).isSameAs( childBinder );
		assertThat( property.getProperties() ).isSameAs( childBinder );
		verify( binder, times( 1 ) ).createChildPropertyPropertiesBinder();
	}

	@Test
	public void propertiesGetRecreatedIfValueHasChanged() {
		when( binder.createChildPropertyPropertiesBinder() )
				.thenReturn( mock( ChildPropertyPropertiesBinder.class ) )
				.thenReturn( mock( ChildPropertyPropertiesBinder.class ) );

		val first = property.getProperties();
		assertThat( property.getProperties() ).isNotNull().isSameAs( first );

		property.setValue( 1 );
		assertThat( property.getProperties() ).isSameAs( first );

		verify( binder, times( 1 ) ).createChildPropertyPropertiesBinder();

		property.setValue( 123 );
		assertThat( property.getProperties() ).isNotNull().isNotSameAs( first );

		verify( binder, times( 2 ) ).createChildPropertyPropertiesBinder();
	}

	@Test
	public void propertiesAreNotAppliedIfNotDirty() {
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		assertThat( property.getValue() ).isEqualTo( 1 );
		verifyNoInteractions( childBinder );

		assertThat( property.getProperties() ).isSameAs( childBinder );
		assertThat( property.getValue() ).isEqualTo( 1 );
		verify( childBinder, never() ).createController();
	}

	@Test
	public void propertiesAreAppliedBeforeReturningValueIfDirty() {
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		EntityPropertiesBinderController propertiesController = mock( EntityPropertiesBinderController.class );
		when( childBinder.createController() ).thenReturn( propertiesController );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		property.markDirty();
		assertThat( property.getValue() ).isEqualTo( 1 );
		verifyNoInteractions( childBinder );

		assertThat( property.getProperties() ).isSameAs( childBinder );
		assertThat( property.getValue() ).isEqualTo( 1 );
		verify( propertiesController ).applyValues();
	}

	@Test
	public void propertyNoLongerDeletedIfPropertiesAccessed() {
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		property.setBound( true );
		assertThat( property.isDeleted() ).isTrue();

		assertThat( property.getProperties() ).isSameAs( childBinder );
		assertThat( property.isDeleted() ).isFalse();
	}

	@Test
	public void resolveValidChildProperty() {
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		EntityPropertyDescriptor childDescriptor = mock( EntityPropertyDescriptor.class );
		when( childDescriptor.isNestedProperty() ).thenReturn( true );
		when( childDescriptor.getParentDescriptor() ).thenReturn( descriptor );
		when( childDescriptor.getTargetPropertyName() ).thenReturn( "user.name" );

		EntityPropertyBinder one = mock( EntityPropertyBinder.class );
		when( childBinder.get( "user.name" ) ).thenReturn( one );

		assertThat( property.resolvePropertyBinder( childDescriptor ) ).isSameAs( one );
	}

	@Test
	public void resolveNonExistingChildProperty() {
		assertThat( property.resolvePropertyBinder( mock( EntityPropertyDescriptor.class ) ) ).isNull();
		verifyNoInteractions( binder );
	}

	@Test
	public void resolveSelfProperty() {
		assertThat( property.resolvePropertyBinder( descriptor ) ).isSameAs( property );
	}

	@Test
	public void resolveNestedChildProperty() {
		when( descriptor.getName() ).thenReturn( "user" );

		EntityPropertyDescriptor address = mock( EntityPropertyDescriptor.class );
		when( address.getName() ).thenReturn( "user.address" );
		when( address.getTargetPropertyName() ).thenReturn( "address" );
		when( address.isNestedProperty() ).thenReturn( true );
		when( address.getParentDescriptor() ).thenReturn( descriptor );

		EntityPropertyDescriptor street = mock( EntityPropertyDescriptor.class );
		when( street.isNestedProperty() ).thenReturn( true );
		when( street.getParentDescriptor() ).thenReturn( address );

		ChildPropertyPropertiesBinder userBinder = mock( ChildPropertyPropertiesBinder.class );
		SingleEntityPropertyBinder addressProperty = mock( SingleEntityPropertyBinder.class );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( userBinder );
		when( userBinder.get( "address" ) ).thenReturn( addressProperty );

		EntityPropertyBinder streetProperty = mock( EntityPropertyBinder.class );
		when( addressProperty.resolvePropertyBinder( street ) ).thenReturn( streetProperty );

		assertThat( property.resolvePropertyBinder( street ) ).isSameAs( streetProperty );
	}

	@Test
	public void saveFlushesToTheControllerWithTheOriginalValue() {
		when( controller.save( bindingContext, new EntityPropertyValue<>( 1, 1, false ) ) ).thenReturn( true );
		assertThat( property.save() ).isTrue();

		property.setValue( 123 );
		assertThat( property.save() ).isFalse();
		verify( controller ).save( bindingContext, new EntityPropertyValue<>( 1, 123, false ) );
	}

	@Test
	public void saveWithNullIfDeleted() {
		when( controller.save( bindingContext, new EntityPropertyValue<>( 1, null, true ) ) ).thenReturn( true );
		property.setBound( true );
		assertThat( property.save() ).isTrue();
		verify( controller ).save( bindingContext, new EntityPropertyValue<>( 1, null, true ) );
	}

	@Test
	public void ifNotDeletedAndPropertiesPresentControllerIsUsed() {
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		EntityPropertiesBinderController propertiesController = mock( EntityPropertiesBinderController.class );
		when( childBinder.createController() ).thenReturn( propertiesController );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		assertThat( property.getProperties() ).isNotNull();

		when( propertiesController.addEntitySaveCallback( any() ) ).thenReturn( propertiesController );

		when( controller.save( bindingContext, new EntityPropertyValue<>( 1, 1, false ) ) ).thenReturn( true );

		assertThat( property.save() ).isTrue();
		verify( controller, never() ).save( any(), any() );

		ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass( Runnable.class );
		verify( propertiesController ).addEntitySaveCallback( runnable.capture() );
		verify( propertiesController ).save();

		runnable.getValue().run();

		verify( controller ).save( bindingContext, new EntityPropertyValue<>( 1, 1, false ) );
	}

	@Test
	public void ifDeletedPresentPropertiesAreIgnoredForSave() {
		ChildPropertyPropertiesBinder childBinder = mock( ChildPropertyPropertiesBinder.class );
		when( binder.createChildPropertyPropertiesBinder() ).thenReturn( childBinder );

		assertThat( property.getProperties() ).isNotNull();

		when( controller.save( bindingContext, new EntityPropertyValue<>( 1, null, true ) ) ).thenReturn( true );

		property.setDeleted( true );
		assertThat( property.save() ).isTrue();

		verify( childBinder, never() ).createController();
		verify( controller ).save( bindingContext, new EntityPropertyValue<>( 1, null, true ) );
	}
}
