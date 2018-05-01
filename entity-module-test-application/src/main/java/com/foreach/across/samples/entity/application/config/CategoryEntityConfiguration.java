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

package com.foreach.across.samples.entity.application.config;

import com.foreach.across.modules.bootstrapui.elements.TextboxFormElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.config.builders.EntityPropertyRegistryBuilder;
import com.foreach.across.modules.entity.registry.EntityFactory;
import com.foreach.across.modules.entity.validators.EntityValidatorSupport;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.across.samples.entity.EntityModuleTestApplication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.*;
import java.util.function.Consumer;

/**
 * Configures a dummy <strong>category</strong> entity.
 * This entity is completely fake and has no Spring data repository.  It is represented by a {@link Map} containing
 * all its properties. The entire entity is manually configured: configuration, properties, entity model and views.
 * <p>
 * <p/>
 * This is a test case for manual configuration of an entity, probably not much of a real life use case however.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Configuration
@EnableAcrossJpaRepositories(basePackageClasses = EntityModuleTestApplication.class)
public class CategoryEntityConfiguration implements EntityConfigurer
{
	private final List<Map<String, Object>> categoryRepository = new ArrayList<>();

	/**
	 * Builds the initial category repository.
	 */
	public CategoryEntityConfiguration() {
		Map<String, Object> tv = new HashMap<>();
		tv.put( "id", "tv" );
		tv.put( "name", "Televisions" );

		categoryRepository.add( tv );
	}

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.create()
		        .as( Map.class )
		        .name( "category" )
		        .entityType( Map.class, false )
		        .displayName( "Category" )
		        .attribute( Validator.class, categoryValidator() )
		        .properties(
				        props -> props
						        .property( "id" )
						        .displayName( "Id" )
						        .propertyType( String.class )
						        .attribute( EntityAttributes.CONTROL_NAME, "entity[id]" )
						        .attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT )
						        .writable( true )
						        .spelValueFetcher( "get('id')" )
						        .order( 1 )
						        .and()
						        .property( "name" )
						        .displayName( "Name" )
						        .propertyType( String.class )
						        .attribute( EntityAttributes.CONTROL_NAME, "entity[name]" )
						        .attribute( TextboxFormElement.Type.class, TextboxFormElement.Type.TEXT )
						        .writable( true )
						        .<Map>valueFetcher( map -> map.get( "name" ) )
						        .order( 2 )
						        .and( registerStockCountProperty() )
						        .and( registerGenerateIdProperty() )
		        )
		        .entityModel(
				        model -> model
						        .entityFactory( new CategoryEntityFactory() )
						        .entityInformation( new CategoryEntityInformation() )
						        .labelPrinter( ( o, locale ) -> (String) o.get( "name" ) )
						        .findOneMethod( id -> categoryRepository.stream()
						                                                .filter( m -> id.equals(
								                                                m.get( "id" ) ) )
						                                                .findFirst().orElse( null ) )
						        .saveMethod(
								        category -> {
									        Optional<Map<String, Object>> existing = categoryRepository
											        .stream()
											        .filter( m -> m.get( "id" ).equals( category.get( "id" ) ) )
											        .findFirst();

									        if ( existing.isPresent() ) {
										        existing.ifPresent( e -> e.putAll( category ) );
									        }
									        else {
										        categoryRepository.add( category );
									        }

									        return category;
								        }
						        )
						        .deleteMethod( categoryRepository::remove )
		        )
		        .listView( lvb -> lvb.pageFetcher( pageable -> new PageImpl<>( categoryRepository ) ) )
		        .createFormView( fvb -> fvb.showProperties( "id", "name", "stockCount" ) )
		        .updateFormView( fvb -> fvb.showProperties( "name", "stockCount" ) )
		        .deleteFormView( dvb -> dvb.showProperties( "." ) )
		        .show();
	}

	/**
	 * Add a custom integer property: the stock count.
	 */
	private Consumer<EntityPropertyRegistryBuilder> registerStockCountProperty() {
		return props -> {
			props.property( "stockCount" )
			     .displayName( "Stock count" )
			     .propertyType( Integer.class )
			     .readable( true )
			     .writable( true )
			     .hidden( false )
			     .spelValueFetcher( "0" );
		};
	}

	/**
	 * Add a custom checkbox, that when checked will generate an id based on the name.
	 * If checked it will first validate that name is not empty but id is empty,
	 * and when the setValue method is called, will lowercase the name and replace all whitespace.
	 * <p>
	 * If there is a validation error and the checkbox is checked, it should still be checked on the re-render.
	 */
	private Consumer<EntityPropertyRegistryBuilder> registerGenerateIdProperty() {
		return props -> {
		};
	}

	/**
	 * Add a custom Manager property, a simple object with name and email representing the manager of a category.
	 * This is a single embedded entity that should only be saved after the category itself is saved.
	 * A reference using the unique category id is inserted in the categoryManagers map.
	 */
	private Consumer<EntityPropertyRegistryBuilder> registerManagerProperty() {
		return props -> {
		};
	}

	/**
	 * Add a custom brands property: a list of Brand entities for a category.
	 * A single brand has a name and a code.
	 * When the entity is saved, an entry will be added to the categoryBrands map.
	 */
	private Consumer<EntityPropertyRegistryBuilder> registerBrandsProperty() {
		return props -> {
		};
	}

	@Bean
	protected CategoryValidator categoryValidator() {
		return new CategoryValidator();
	}

	private static class CategoryValidator extends EntityValidatorSupport<Map<String, Object>>
	{
		@Override
		public boolean supports( Class<?> aClass ) {
			return Map.class.equals( aClass );
		}

		@Override
		protected void postValidation( Map<String, Object> entity, Errors errors ) {
			String prefix = StringUtils.removeEnd( errors.getNestedPath(), "." );
			errors.setNestedPath( "" );

			if ( StringUtils.defaultString( Objects.toString( entity.get( "id" ) ) ).length() == 0 ) {
				errors.rejectValue( prefix + "[id]", "NotBlank" );
			}
			if ( StringUtils.defaultString( Objects.toString( entity.get( "name" ) ) ).length() == 0 ) {
				errors.rejectValue( prefix + "[name]", "NotBlank" );
			}

			errors.pushNestedPath( "entity" );
		}
	}

	private static class CategoryEntityFactory implements EntityFactory<Map>
	{
		@Override
		public Map createNew( Object... args ) {
			return new HashMap<>();
		}

		@Override
		public Map createDto( Map entity ) {
			return new HashMap<>( (Map<?, ?>) entity );
		}
	}

	private static class CategoryEntityInformation implements EntityInformation<Map, String>
	{
		@Override
		public boolean isNew( Map map ) {
			return map.containsKey( "id" );
		}

		@Override
		public String getId( Map map ) {
			return (String) map.get( "id" );
		}

		@Override
		public Class<String> getIdType() {
			return String.class;
		}

		@Override
		public Class<Map> getJavaType() {
			return Map.class;
		}
	}
}
