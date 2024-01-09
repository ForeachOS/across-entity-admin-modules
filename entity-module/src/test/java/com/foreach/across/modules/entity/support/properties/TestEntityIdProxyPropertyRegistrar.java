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

package com.foreach.across.modules.entity.support.properties;

import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.builders.EntityPropertyRegistryBuilder;
import com.foreach.across.modules.entity.query.EntityQueryCondition;
import com.foreach.across.modules.entity.query.EntityQueryConditionTranslator;
import com.foreach.across.modules.entity.query.EntityQueryOps;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityModel;
import com.foreach.across.modules.entity.registry.EntityRegistry;
import com.foreach.across.modules.entity.registry.properties.*;
import lombok.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.validation.Errors;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
@SuppressWarnings("unchecked")
public class TestEntityIdProxyPropertyRegistrar
{
	private DefaultConversionService conversionService = new DefaultConversionService();
	private EntityRegistry entityRegistry = mock( EntityRegistry.class );

	private EntityConfiguration entityConfiguration = mock( EntityConfiguration.class );
	private EntityModel entityModel = mock( EntityModel.class );

	private EntityIdProxyPropertyRegistrar registrar = new EntityIdProxyPropertyRegistrar( entityRegistry, conversionService );

	private MutableEntityPropertyRegistry propertyRegistry = DefaultEntityPropertyRegistryProvider.INSTANCE.create( RefOwner.class );

	@BeforeEach
	public void before() {
		when( entityRegistry.getEntityConfiguration( Ref.class ) ).thenReturn( entityConfiguration );
		when( entityRegistry.getEntityConfiguration( "ref" ) ).thenReturn( entityConfiguration );

		when( entityModel.getIdType() ).thenReturn( Integer.class );
		when( entityConfiguration.getEntityModel() ).thenReturn( entityModel );
		when( entityConfiguration.getEntityType() ).thenReturn( Ref.class );

		doAnswer( invocationOnMock -> ( (Ref) invocationOnMock.getArgument( 0 ) ).getId() ).when( entityModel ).getId( any() );
	}

	@Test
	public void defaultConfigurationForSingleValue() {
		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "ref" ).targetPropertyName( "refId" ).entityType( Ref.class ) ).and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor refId = propertyRegistry.getProperty( "refId" );
		assertThat( refId ).isNotNull();
		assertThat( refId.isReadable() ).isTrue();
		assertThat( refId.isWritable() ).isTrue();
		assertThat( refId.isHidden() ).isTrue();

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "ref" );
		assertThat( ref ).isNotNull();
		assertThat( ref.getDisplayName() ).isEqualTo( "Ref" );
		assertThat( ref.getPropertyType() ).isEqualTo( Ref.class );
		assertThat( EntityAttributes.isRequired( ref ) ).isFalse();

		RefOwner owner = new RefOwner();
		assertThat( owner.getRefId() ).isNull();

		assertThat( ref.getController().fetchValue( EntityPropertyBindingContext.forReading( owner ) ) ).isNull();
		verify( entityModel, never() ).findOne( any() );

		ref.getController().applyValue( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( new Ref( 20 ) ) );
		assertThat( owner.getRefId() ).isEqualTo( 20 );

		Ref other = new Ref();
		when( entityModel.findOne( 33 ) ).thenReturn( other );
		owner.setRefId( 33 );
		assertThat( ref.getController().fetchValue( EntityPropertyBindingContext.forReading( owner ) ) ).isSameAs( other );

		Errors errors = mock( Errors.class );
		ref.getController().validate( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( null ), errors );
		verifyNoInteractions( errors );

		EntityQueryConditionTranslator conditionTranslator = ref.getAttribute( EntityQueryConditionTranslator.class );
		assertThat( conditionTranslator ).isNotNull();

		assertThat( conditionTranslator.translate( new EntityQueryCondition( "ref", EntityQueryOps.CONTAINS, new Ref( 13 ), new Ref( 14 ) ) ) )
				.isEqualTo( new EntityQueryCondition( "refId", EntityQueryOps.CONTAINS, 13, 14 ) );
	}

	@Test
	public void singleValueWithDifferentTypes() {
		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "ref" ).targetPropertyName( "wrappedRefId" ).entityType( Ref.class ) ).and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "ref" );
		assertThat( ref ).isNotNull();
		assertThat( ref.getDisplayName() ).isEqualTo( "Ref" );
		assertThat( ref.getPropertyType() ).isEqualTo( Ref.class );
		assertThat( EntityAttributes.isRequired( ref ) ).isFalse();

		RefOwner owner = new RefOwner();
		assertThat( owner.getRefId() ).isNull();

		ref.getController().applyValue( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( new Ref( 20 ) ) );
		assertThat( owner.getWrappedRefId() ).isEqualTo( RefId.from( 20 ) );

		Ref other = new Ref();
		when( entityModel.findOne( 33 ) ).thenReturn( other );
		owner.setWrappedRefId( RefId.from( 33 ) );
		assertThat( ref.getController().fetchValue( EntityPropertyBindingContext.forReading( owner ) ) ).isSameAs( other );

		EntityQueryConditionTranslator conditionTranslator = ref.getAttribute( EntityQueryConditionTranslator.class );
		assertThat( conditionTranslator ).isNotNull();

		assertThat( conditionTranslator.translate( new EntityQueryCondition( "ref", EntityQueryOps.CONTAINS, new Ref( 13 ), new Ref( 14 ) ) ) )
				.isEqualTo( new EntityQueryCondition( "wrappedRefId", EntityQueryOps.CONTAINS, RefId.from( 13 ), RefId.from( 14 ) ) );
	}

	@Test
	public void defaultConfigurationForMultiValue() {
		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "refs" ).targetPropertyName( "refIds" ).entityType( Ref.class ) ).and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor refId = propertyRegistry.getProperty( "refIds" );
		assertThat( refId ).isNotNull();
		assertThat( refId.isReadable() ).isTrue();
		assertThat( refId.isWritable() ).isTrue();
		assertThat( refId.isHidden() ).isTrue();

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "refs" );
		assertThat( ref ).isNotNull();
		assertThat( ref.getDisplayName() ).isEqualTo( "Refs" );
		assertThat( ref.getPropertyTypeDescriptor() ).isEqualTo( TypeDescriptor.collection( List.class, TypeDescriptor.valueOf( Ref.class ) ) );
		assertThat( EntityAttributes.isRequired( ref ) ).isFalse();

		RefOwner owner = new RefOwner();
		assertThat( owner.getRefIds() ).isEmpty();

		EntityPropertyBindingContext ctx = EntityPropertyBindingContext.forUpdating( owner, owner );

		ref.getController().applyValue( ctx, EntityPropertyValue.of( Arrays.asList( new Ref( 20 ), new Ref( 30 ) ) ) );
		assertThat( owner.getRefIds() ).containsExactly( 20, 30 );

		Ref other = new Ref();
		Ref extra = new Ref();
		when( entityModel.findOne( 33 ) ).thenReturn( other );
		when( entityModel.findOne( 22 ) ).thenReturn( extra );
		owner.setRefIds( Arrays.asList( 33, 22 ) );
		assertThat( ref.getController().fetchValue( EntityPropertyBindingContext.forReading( owner ) ) )
				.isEqualTo( Arrays.asList( other, extra ) );

		Errors errors = mock( Errors.class );
		ref.getController().validate( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( null ), errors );
		ref.getController().validate( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( Collections.emptyList() ), errors );
		verifyNoInteractions( errors );

		EntityQueryConditionTranslator conditionTranslator = ref.getAttribute( EntityQueryConditionTranslator.class );
		assertThat( conditionTranslator ).isNotNull();

		assertThat( conditionTranslator.translate( new EntityQueryCondition( "refs", EntityQueryOps.CONTAINS, new Ref( 13 ), new Ref( 14 ) ) ) )
				.isEqualTo( new EntityQueryCondition( "refIds", EntityQueryOps.CONTAINS, 13, 14 ) );
	}

	@Test
	public void multiValueConfigurationWithDifferentTypes() {
		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "refs" ).targetPropertyName( "wrappedRefIds" ).entityType( Ref.class ) ).and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "refs" );
		assertThat( ref ).isNotNull();
		assertThat( ref.getDisplayName() ).isEqualTo( "Refs" );
		assertThat( ref.getPropertyTypeDescriptor() ).isEqualTo( TypeDescriptor.array( TypeDescriptor.valueOf( Ref.class ) ) );
		assertThat( EntityAttributes.isRequired( ref ) ).isFalse();

		RefOwner owner = new RefOwner();
		assertThat( owner.getWrappedRefIds() ).isEmpty();

		EntityPropertyBindingContext ctx = EntityPropertyBindingContext.forUpdating( owner, owner );

		ref.getController().applyValue( ctx, EntityPropertyValue.of( new Ref[] { new Ref( 20 ), new Ref( 30 ) } ) );
		assertThat( owner.getWrappedRefIds() ).containsExactly( RefId.from( 20 ), RefId.from( 30 ) );

		Ref other = new Ref();
		Ref extra = new Ref();
		when( entityModel.findOne( 33 ) ).thenReturn( other );
		when( entityModel.findOne( 22 ) ).thenReturn( extra );
		owner.setWrappedRefIds( new RefId[] { RefId.from( 33 ), RefId.from( 22 ) } );
		assertThat( ref.getController().fetchValue( EntityPropertyBindingContext.forReading( owner ) ) )
				.isEqualTo( new Ref[] { other, extra } );

		EntityQueryConditionTranslator conditionTranslator = ref.getAttribute( EntityQueryConditionTranslator.class );
		assertThat( conditionTranslator ).isNotNull();

		assertThat( conditionTranslator.translate( new EntityQueryCondition( "refs", EntityQueryOps.CONTAINS, new Ref( 13 ), new Ref( 14 ) ) ) )
				.isEqualTo( new EntityQueryCondition( "wrappedRefIds", EntityQueryOps.CONTAINS, RefId.from( 13 ), RefId.from( 14 ) ) );
	}

	@Test
	public void proxyIsRequiredIfOriginalIsRequired() {
		propertyRegistry.getProperty( "refId" ).setAttribute( EntityAttributes.PROPERTY_REQUIRED, true );

		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "ref" ).targetPropertyName( "refId" ).entityType( Ref.class ) ).and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "ref" );
		assertThat( EntityAttributes.isRequired( ref ) ).isTrue();
	}

	@Test
	public void nonNullValidationIsPerformedIfRequired() {
		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "ref" ).targetPropertyName( "refId" ).entityType( Ref.class ) )
				.attribute( EntityAttributes.PROPERTY_REQUIRED, true )
				.and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "ref" );

		RefOwner owner = new RefOwner();

		Errors errors = mock( Errors.class );
		ref.getController().validate( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( null ), errors );
		verify( errors ).rejectValue( "", "NotNull" );
	}

	@Test
	public void notEmptyValidationIsPerformedIfRequired() {
		new EntityPropertyRegistryBuilder()
				.property( registrar.propertyName( "refs" ).targetPropertyName( "refIds" ).entityType( Ref.class ) )
				.attribute( EntityAttributes.PROPERTY_REQUIRED, true )
				.and()
				.apply( propertyRegistry );

		MutableEntityPropertyDescriptor ref = propertyRegistry.getProperty( "refs" );

		RefOwner owner = new RefOwner();

		Errors errors = mock( Errors.class );
		ref.getController().validate( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( null ), errors );
		ref.getController().validate( EntityPropertyBindingContext.forUpdating( owner, owner ), EntityPropertyValue.of( Collections.emptyList() ), errors );
		verify( errors, times( 2 ) ).rejectValue( "", "NotEmpty" );
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class Ref
	{
		private Integer id;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class RefOwner
	{
		private Integer refId;

		private RefId wrappedRefId;

		private List<Integer> refIds = Collections.emptyList();

		private RefId[] wrappedRefIds = new RefId[0];
	}

	@Getter
	@EqualsAndHashCode
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class RefId implements Serializable
	{
		private static final long serialVersionUID = 42L;

		private final Integer id;

		public static RefId from( Integer id ) {
			return new RefId( id );
		}

		@SuppressWarnings("unused")
		public int toInteger() {
			return id;
		}
	}
}
