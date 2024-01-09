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
import com.foreach.across.modules.entity.registry.properties.EntityPropertyValue;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings({ "Duplicates", "unchecked" })
public class TestMapEntityPropertyBinder
{
	private static final TypeDescriptor KEY = TypeDescriptor.valueOf( String.class );
	private static final TypeDescriptor VALUE = TypeDescriptor.valueOf( Integer.class );
	private static final TypeDescriptor COLLECTION = TypeDescriptor.map( LinkedHashMap.class, KEY, VALUE );
	private static final Map<String, Integer> ORIGINAL_VALUE = Collections.singletonMap( "one", 1 );

	@Mock
	private EntityPropertiesBinder.EntityPropertiesBinderValueBindingContext bindingContext;

	@Mock
	private EntityPropertiesBinder binder;

	@Mock
	private EntityPropertyController collectionController;

	@Mock
	private EntityPropertyController keyController;

	@Mock
	private EntityPropertyController valueController;

	@Mock
	private AbstractEntityPropertyBinder key;

	@Mock
	private AbstractEntityPropertyBinder value;

	@Mock
	private EntityPropertyTemplateValueResolver templateValueResolver;

	private EntityPropertyDescriptor collectionDescriptor;
	private EntityPropertyDescriptor keyDescriptor;
	private EntityPropertyDescriptor valueDescriptor;
	private MapEntityPropertyBinder property;

	@BeforeEach
	public void resetMocks() {
		reset( binder, collectionController, keyController, valueController );

		when( binder.getValueBindingContext() ).thenReturn( bindingContext );

		when( collectionController.fetchValue( bindingContext ) ).thenReturn( ORIGINAL_VALUE );

		doAnswer( i -> i.getArgument( 0 ) )
				.when( binder )
				.convertIfNecessary( any(), eq( COLLECTION ), eq( "properties[collection].entries" ) );

		doAnswer( i -> i.getArgument( 0 ) )
				.when( binder )
				.convertIfNecessary( any(), eq( COLLECTION ), eq( COLLECTION.getObjectType() ), eq( "properties[collection].value" ) );

		collectionDescriptor = spy(
				EntityPropertyDescriptor
						.builder( "collection" )
						.controller( collectionController )
						.propertyType( COLLECTION )
						.build()
		);

		keyDescriptor = spy(
				EntityPropertyDescriptor
						.builder( "key" )
						.controller( keyController )
						.propertyType( KEY )
						.build()
		);

		valueDescriptor = spy(
				EntityPropertyDescriptor
						.builder( "value" )
						.controller( valueController )
						.propertyType( VALUE )
						.build()
		);

		property = new MapEntityPropertyBinder( binder, collectionDescriptor, keyDescriptor, valueDescriptor );
		property.setBinderPath( "properties[collection]" );

		when( binder.createPropertyBinder( keyDescriptor ) ).thenReturn( key );
		when( binder.createPropertyBinder( valueDescriptor ) ).thenReturn( value );

		when( key.getValue() ).thenReturn( "one" );
		when( value.getValue() ).thenReturn( 1 );

		when( binder.getProperties() ).thenReturn( binder );
		when( binder.get( "prop" ) ).thenReturn( property );

		when( binder.getTemplateValueResolver() ).thenReturn( templateValueResolver );
	}

	@Test
	public void defaultProperties() {
		when( collectionController.getOrder() ).thenReturn( 33 );

		assertThat( property.isBound() ).isFalse();
		assertThat( property.isModified() ).isFalse();
		assertThat( property.isDeleted() ).isFalse();
		assertThat( property.getControllerOrder() ).isEqualTo( 33 );

		assertThat( property.getSortIndex() ).isEqualTo( 0 );
		property.setSortIndex( 123 );
		assertThat( property.getSortIndex() ).isEqualTo( 123 );
	}

	@Test
	public void templateGetsLazilyCreatedOnce() {
		when( templateValueResolver.resolveTemplateValue( any(), eq( keyDescriptor ) ) ).thenReturn( "key-val" );
		when( templateValueResolver.resolveTemplateValue( any(), eq( valueDescriptor ) ) ).thenReturn( "value-val" );

		AbstractEntityPropertyBinder templateKey = mock( AbstractEntityPropertyBinder.class );
		AbstractEntityPropertyBinder templateValue = mock( AbstractEntityPropertyBinder.class );

		when( binder.createPropertyBinder( keyDescriptor ) ).thenReturn( templateKey );
		when( binder.createPropertyBinder( valueDescriptor ) ).thenReturn( templateValue );

		assertThat( property.getTemplate().getKey() ).isSameAs( templateKey );
		assertThat( property.getTemplate().getValue() ).isSameAs( templateValue );

		verify( templateKey ).setOriginalValue( "key-val" );
		verify( templateValue ).setOriginalValue( "value-val" );
	}

	@Test
	public void initialValueGetsLazyLoadedFromTheController() {
		verify( collectionController, never() ).fetchValue( any() );
		assertThat( property.getOriginalValue() ).isEqualTo( ORIGINAL_VALUE );
	}

	@Test
	public void itemsAreLoaded() {
		val items = property.getEntries();
		assertThat( items )
				.isNotNull()
				.hasSize( 1 );

		assertThat( items.get( "0" ).getSortIndex() ).isEqualTo( 0 );

		assertThat( items.get( "0" ).getKey() ).isSameAs( key );
		verify( key ).setValueInternal( "one" );

		assertThat( items.get( "0" ).getValue() ).isSameAs( value );
		verify( value ).setValueInternal( 1 );
	}

	@Test
	public void settingBoundToTrueMarksAsDirty() {
		assertThat( property.isDirty() ).isFalse();
		property.setBound( true );
		assertThat( property.isDirty() ).isTrue();
	}

	@Test
	public void deletingAnEntryMarksAsDirty() {
		MapEntityPropertyBinder.Entry entry = property.getEntries().get( "0" );
		assertThat( entry ).isNotNull();
		assertThat( property.isDirty() ).isFalse();
		entry.setDeleted( true );
		assertThat( property.isDirty() ).isTrue();
		assertThat( property.isDeleted() ).isFalse();
	}

	@Test
	public void settingDifferentSortIndexMarksAsDirty() {
		MapEntityPropertyBinder.Entry entry = property.getEntries().get( "0" );
		assertThat( entry.getSortIndex() ).isEqualTo( 0 );
		assertThat( property.isDirty() ).isFalse();
		entry.setSortIndex( 0 );
		assertThat( property.isDirty() ).isFalse();
		entry.setSortIndex( 1 );
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
		assertThat( property.getOriginalValue() ).isNotNull();
		assertThat( property.getValue() ).isNotNull();
		assertThat( property.getEntries() ).isNotEmpty();
		assertThat( property.getTemplate() ).isNotNull();
		assertThat( property.isDeleted() ).isFalse();
		assertThat( property.isDirty() ).isFalse();

		verify( key, never() ).setValue( any() );
		verify( value, never() ).setValue( any() );

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
		property.setValue( Collections.emptyMap() );
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
	public void addingAnItemMarksAsDirty() {
		Map<String, MapEntityPropertyBinder.Entry> items = property.getEntries();
		assertThat( property.isDirty() ).isFalse();
		assertThat( items.get( "newItem" ) ).isNotNull();
		assertThat( property.isDirty() ).isTrue();
	}

	@Test
	public void dirtyKeyOrValueMarksThePropertyDirty() {
		assertThat( property.isDirty() ).isFalse();
		assertThat( property.getEntries() ).isNotEmpty();
		assertThat( property.isDirty() ).isFalse();

		when( key.isDirty() ).thenReturn( true );
		assertThat( property.isDirty() ).isTrue();

		when( key.isDirty() ).thenReturn( false );
		assertThat( property.isDirty() ).isFalse();

		when( value.isDirty() ).thenReturn( true );
		assertThat( property.isDirty() ).isTrue();
	}

	@Test
	public void valueIsInitializedEmptyIfNotIncrementalBindingAndBindingBusy() {
		property.enableBinding( true );

		assertThat( property.getEntries() ).isEmpty();
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
	}

	@Test
	public void originalValueVsValue() {
		assertThat( property.getOriginalValue() ).isEqualTo( ORIGINAL_VALUE );
		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );

		val keyOne = mock( AbstractEntityPropertyBinder.class );
		val keyTwo = mock( AbstractEntityPropertyBinder.class );
		when( binder.createPropertyBinder( keyDescriptor ) ).thenReturn( keyOne ).thenReturn( keyTwo );

		val valueOne = mock( AbstractEntityPropertyBinder.class );
		val valueTwo = mock( AbstractEntityPropertyBinder.class );
		when( binder.createPropertyBinder( valueDescriptor ) ).thenReturn( valueOne ).thenReturn( valueTwo );

		Map<String, Integer> values = new LinkedHashMap<>();
		values.put( "two", 2 );
		values.put( "three", 3 );
		property.setValue( values );

		when( keyOne.getValue() ).thenReturn( "two" );
		when( valueOne.getValue() ).thenReturn( 2 );
		when( keyTwo.getValue() ).thenReturn( "three" );
		when( valueTwo.getValue() ).thenReturn( 3 );

		assertThat( property.getOriginalValue() ).isEqualTo( ORIGINAL_VALUE );
		assertThat( property.getValue() ).isEqualTo( values );
	}

	@Test
	public void itemCanBeAddedByKeyAndInitialKeyIsTheEntryKey() {
		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );

		val key = mock( AbstractEntityPropertyBinder.class );
		when( binder.createPropertyBinder( keyDescriptor ) ).thenReturn( key );

		val value = mock( AbstractEntityPropertyBinder.class );
		when( binder.createPropertyBinder( valueDescriptor ) ).thenReturn( value );

		property.getEntries().clear();
		property.getEntries().get( "random" ).getKey().setValue( "test" );
		property.getEntries().get( "random" ).getValue().setValue( 33 );

		InOrder inOrder = inOrder( key, value );
		inOrder.verify( key ).setValueInternal( "random" );
		inOrder.verify( key ).setValue( "test" );
		inOrder.verify( value ).setValue( 33 );

		when( key.getValue() ).thenReturn( "test" );
		when( value.getValue() ).thenReturn( 33 );

		assertThat( property.getValue() ).isEqualTo( Collections.singletonMap( "test", 33 ) );
	}

	@Test
	public void valueIsInitializedWithOriginalValueWithIncrementalBinding() {
		property.enableBinding( true );
		property.setUpdateEntriesOnBinding( true );

		assertThat( property.getEntries() ).hasSize( 1 );
		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );
	}

	@Test
	public void deletedItemsArePresentInTheItemsButNotInTheValueWhileBindingIsBusy() {
		property.getEntries().get( "0" ).setDeleted( true );

		assertThat( property.getEntries() ).hasSize( 1 );
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
	}

	@Test
	public void deletedItemsAreRemovedWhenBindingIsDisabled() {
		property.getEntries().get( "0" ).setDeleted( true );
		assertThat( property.getEntries() ).hasSize( 1 );
		property.enableBinding( false );

		assertThat( property.getEntries() ).isEmpty();
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
	}

	@Test
	public void valueSortsTheItems() {
		val keyOne = mock( AbstractEntityPropertyBinder.class );
		val keyTwo = mock( AbstractEntityPropertyBinder.class );
		when( binder.createPropertyBinder( keyDescriptor ) ).thenReturn( keyOne ).thenReturn( keyTwo );

		val valueOne = mock( AbstractEntityPropertyBinder.class );
		val valueTwo = mock( AbstractEntityPropertyBinder.class );
		when( binder.createPropertyBinder( valueDescriptor ) ).thenReturn( valueOne ).thenReturn( valueTwo );

		Map<String, Integer> values = new LinkedHashMap<>();
		values.put( "two", 2 );
		values.put( "three", 3 );
		property.setValue( values );

		when( keyOne.getValue() ).thenReturn( "two" );
		when( valueOne.getValue() ).thenReturn( 2 );
		when( keyTwo.getValue() ).thenReturn( "three" );
		when( valueTwo.getValue() ).thenReturn( 3 );

		property.getEntries().get( "0" ).setSortIndex( -1 );
		property.getEntries().get( "1" ).setSortIndex( 1 );
		assertThat( ( (Map) property.getValue() ).values() ).containsExactly( 2, 3 );

		property.getEntries().get( "0" ).setSortIndex( 2 );
		assertThat( ( (Map) property.getValue() ).values() ).containsExactly( 3, 2 );
	}

	@Test
	public void itemsGetRecreatedIfValueHasChanged() {
		assertThat( property.getEntries() ).hasSize( 1 );

		val item = property.getEntries().get( "0" );

		property.setValue( Collections.singletonMap( "two", 2 ) );
		assertThat( property.getEntries().get( "0" ) ).isNotSameAs( item );
		assertThat( property.getEntries() ).hasSize( 1 );

		property.setValue( null );
		assertThat( property.getEntries() ).isEmpty();
	}

	@Test
	public void updatingTheValueDoesNotUseTheController() {
		property.setValue( Collections.singletonMap( "two", 2 ) );
		verify( collectionController ).fetchValue( bindingContext );
		verifyNoInteractions( collectionController );
	}

	@Test
	public void modifiedIfTheNewValueCollectionIsDifferentFromTheOriginal() {
		assertThat( property.isModified() ).isFalse();

		assertThat( property.getOriginalValue() ).isEqualTo( ORIGINAL_VALUE );
		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );
		assertThat( property.isModified() ).isFalse();

		when( binder.convertIfNecessary( any(), eq( COLLECTION ), eq( "properties[collection].entries" ) ) ).thenReturn( Collections.emptyMap() );
		assertThat( property.isModified() ).isTrue();
	}

	@Test
	public void boundButNotSetValueModifies() {
		property.enableBinding( true );
		property.setBound( true );
		assertThat( property.isModified() ).isTrue();
		assertThat( property.isDirty() ).isTrue();

		property.enableBinding( false );
		assertThat( property.isModified() ).isTrue();
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
	}

	@Test
	public void boundButItemsFetchedDoesNotModify() {
		property.setBound( true );
		assertThat( property.isDirty() ).isTrue();
		assertThat( property.getEntries() ).hasSize( 1 );
		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );
		assertThat( property.isModified() ).isFalse();
	}

	@Test
	public void boundDuringBindingButValueNotSetIsEmptyButNotDeleted() {
		property.enableBinding( true );
		property.setBound( true );
		assertThat( property.isDeleted() ).isFalse();
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
		assertThat( property.getEntries() ).isEmpty();
	}

	@Test
	public void boundDuringBindingButUpdatingDoesNotClearItems() {
		property.enableBinding( true );
		property.setBound( true );
		property.setUpdateEntriesOnBinding( true );
		assertThat( property.isDeleted() ).isFalse();
		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );
		assertThat( property.getEntries() ).hasSize( 1 );
	}

	@Test
	public void explicitlyDeletedDoesNotClearTheItemsImmediately() {
		property.setDeleted( true );
		assertThat( property.isDeleted() ).isTrue();
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
		assertThat( property.getEntries() ).hasSize( 1 );
	}

	@Test
	public void itemsAreClearedOnDeletedPropertyWhenBindingDisabled() {
		property.setDeleted( true );
		property.enableBinding( false );
		assertThat( property.isDeleted() ).isTrue();
		assertThat( property.getValue() ).isEqualTo( Collections.emptyMap() );
		assertThat( property.getEntries() ).isEmpty();
	}

	@Test
	public void boundButValueAssignedIsNotDeleted() {
		property.enableBinding( true );
		property.setBound( true );
		property.setValue( ORIGINAL_VALUE );
		assertThat( property.isDeleted() ).isFalse();
		assertThat( property.isModified() ).isFalse();
		assertThat( property.getEntries() ).hasSize( 1 );
	}

	@Test
	public void settingNullValueIsNotSameAsDeleted() {
		property.setValue( null );
		assertThat( property.isDeleted() ).isFalse();
	}

	@Test
	public void settingEmptyCollectionValueIsNotSameAsDeleted() {
		property.setValue( Collections.emptyMap() );
		assertThat( property.isDeleted() ).isFalse();
	}

	@Test
	public void createNewValueCreatesNewValueButDoesNotUpdatePropertyItself() {
		when( binder.createValue( collectionController ) ).thenReturn( Collections.singletonMap( "two", 2 ) );
		Object value = property.createNewValue();
		assertThat( value ).isEqualTo( Collections.singletonMap( "two", 2 ) );

		assertThat( property.getValue() ).isEqualTo( ORIGINAL_VALUE );
	}

	@Test
	public void getInitializedValueDoesNotModifyCurrentValue() {
		assertThat( property.getInitializedValue() ).isEqualTo( ORIGINAL_VALUE );
		verify( binder, never() ).createValue( any() );
	}

	@Test
	public void getInitializedValueInitializesNewValueIfNullFetched() {
		reset( collectionController );
		when( binder.createValue( collectionController ) ).thenReturn( Collections.singletonMap( "two", 2 ) );

		when( key.getValue() ).thenReturn( "two" );
		when( value.getValue() ).thenReturn( 2 );
		assertThat( property.getInitializedValue() ).isEqualTo( Collections.singletonMap( "two", 2 ) );
		verify( binder ).createValue( collectionController );
		verify( key ).setValue( "two" );
		verify( value ).setValue( 2 );

		assertThat( property.getValue() ).isEqualTo( Collections.singletonMap( "two", 2 ) );
		assertThat( property.getOriginalValue() ).isNull();
	}

	@Test
	public void applyValueFlushesToTheControllerWithTheOriginalValue() {
		when( collectionController.applyValue( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, ORIGINAL_VALUE, false ) ) ).thenReturn( true );
		assertThat( property.applyValue() ).isTrue();

		when( value.getValue() ).thenReturn( 3 );
		assertThat( property.applyValue() ).isFalse();
		verify( collectionController ).applyValue( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.singletonMap( "one", 3 ), false ) );
	}

	@Test
	public void applyValueFlagsDelete() {
		when( collectionController.applyValue( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.emptyMap(), true ) ) )
				.thenReturn( true );
		property.setDeleted( true );
		assertThat( property.applyValue() ).isTrue();
		verify( collectionController ).applyValue( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.emptyMap(), true ) );
	}

	@Test
	public void saveFlushesToTheControllerWithTheOriginalValue() {
		when( collectionController.save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, ORIGINAL_VALUE, false ) ) ).thenReturn( true );
		assertThat( property.save() ).isTrue();

		when( value.getValue() ).thenReturn( 3 );
		assertThat( property.save() ).isFalse();
		verify( collectionController ).save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.singletonMap( "one", 3 ), false ) );
	}

	@Test
	public void saveWithNullIfDeleted() {
		when( collectionController.save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.emptyMap(), true ) ) )
				.thenReturn( true );
		property.setDeleted( true );
		assertThat( property.save() ).isTrue();
		verify( collectionController ).save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.emptyMap(), true ) );
	}

	@Test
	public void saveSavesIndividualEntriesFirstIfNotDeleted() {
		when( key.save() ).thenReturn( true );
		when( value.save() ).thenReturn( false );
		when( collectionController.save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, ORIGINAL_VALUE, false ) ) ).thenReturn( false );
		assertThat( property.save() ).isTrue();

		InOrder inOrder = inOrder( key, value, collectionController );
		inOrder.verify( key ).save();
		inOrder.verify( value ).save();
		inOrder.verify( collectionController ).save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, ORIGINAL_VALUE, false ) );
	}

	@Test
	public void saveIgnoresIndividualEntriesIfDeleted() {
		when( collectionController.save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.emptyMap(), true ) ) )
				.thenReturn( true );
		property.setDeleted( true );
		assertThat( property.save() ).isTrue();
		verify( collectionController ).save( bindingContext, new EntityPropertyValue<>( ORIGINAL_VALUE, Collections.emptyMap(), true ) );

		verifyNoInteractions( key, value );
	}

	@Test
	public void validate() {
		Errors errors = new BeanPropertyBindingResult( binder, "" );
		errors.pushNestedPath( "properties[prop]" );

		assertThat( property.validate( errors, "hint" ) ).isTrue();
		assertThat( errors.getErrorCount() ).isEqualTo( 0 );

		property.getEntries().get( "0" ).getKey().setValue( "do nothing" );

		doAnswer( invocation -> {
			Errors err = invocation.getArgument( 0 );
			err.rejectValue( "value", "bad-key" );

			return null;
		} )
				.when( key )
				.validate( errors, "hint" );

		doAnswer( invocation -> {
			Errors err = invocation.getArgument( 0 );
			err.rejectValue( "value", "bad-value" );

			return null;
		} )
				.when( value )
				.validate( errors, "hint" );

		doAnswer( invocation -> {
			Errors err = invocation.getArgument( 2 );
			err.rejectValue( "", "bad" );

			return null;
		} )
				.when( collectionController )
				.validate( any(), any(), eq( errors ), eq( "hint" ) );

		assertThat( property.validate( errors, "hint" ) ).isFalse();

		errors.popNestedPath();

		assertThat( errors.getErrorCount() ).isEqualTo( 3 );
		assertThat( errors.getFieldError( "properties[prop].entries[0].key.value" ) )
				.isNotNull()
				.satisfies( fe -> {
					assertThat( fe.isBindingFailure() ).isFalse();
					assertThat( fe.getField() ).isEqualTo( "properties[prop].entries[0].key.value" );
					assertThat( fe.getCode() ).isEqualTo( "bad-key" );
				} );
		assertThat( errors.getFieldError( "properties[prop].entries[0].value.value" ) )
				.isNotNull()
				.satisfies( fe -> {
					assertThat( fe.isBindingFailure() ).isFalse();
					assertThat( fe.getField() ).isEqualTo( "properties[prop].entries[0].value.value" );
					assertThat( fe.getCode() ).isEqualTo( "bad-value" );
				} );
		assertThat( errors.getFieldError( "properties[prop].value" ) )
				.isNotNull()
				.satisfies( fe -> {
					assertThat( fe.isBindingFailure() ).isFalse();
					assertThat( fe.getField() ).isEqualTo( "properties[prop].value" );
					assertThat( fe.getCode() ).isEqualTo( "bad" );
				} );
	}

	@Test
	public void resolveNonExistingChildProperty() {
		assertThat( property.resolvePropertyBinder( mock( EntityPropertyDescriptor.class ) ) ).isNull();
	}

	@Test
	public void resolveSelfProperty() {
		assertThat( property.resolvePropertyBinder( collectionDescriptor ) ).isSameAs( property );
	}
}
