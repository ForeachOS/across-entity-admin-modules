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

package com.foreach.across.modules.entity.registry.properties;

import lombok.Getter;
import lombok.NonNull;
import org.springframework.validation.Errors;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class GenericEntityPropertyController implements EntityPropertyController, ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object>
{
	private final EntityPropertyController original;

	/**
	 * Processing order for this controller.
	 * Manually created properties will default to execute after the entity, native properties
	 * or other properties changing the original entity should usually execute before changes on the entity itself.
	 */
	private Integer order = AFTER_ENTITY;

	@Override
	public int getOrder() {
		if ( order == null && original != null ) {
			return original.getOrder();
		}

		return order == null ? AFTER_ENTITY : order;
	}

	@Getter
	private Function<EntityPropertyBindingContext, Object> valueFetcher;

	@Getter
	private Function<Collection<EntityPropertyBindingContext>, Map<EntityPropertyBindingContext, Object>> bulkValueFetcher;

	private Boolean optimizedForBulkValueFetching;

	@Getter
	private Function<EntityPropertyBindingContext, Object> createValueFunction;

	@Getter
	private BiFunction<EntityPropertyBindingContext, Object, Object> createDtoFunction;

	@Getter
	private BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> applyValueFunction;

	@Getter
	private BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> saveFunction;

	@Getter
	private EntityPropertyValidator validator;

	public GenericEntityPropertyController() {
		this.original = null;
	}

	public GenericEntityPropertyController( EntityPropertyController original ) {
		this.original = original;
		this.order = null;
	}

	@Override
	public boolean isOptimizedForBulkValueFetching() {
		if ( optimizedForBulkValueFetching != null ) {
			return optimizedForBulkValueFetching;
		}

		return original != null && original.isOptimizedForBulkValueFetching();
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> bulkValueFetcher( Function<Collection<EntityPropertyBindingContext>, Map<EntityPropertyBindingContext, Object>> valueFetcher ) {
		bulkValueFetcher = valueFetcher;
		optimizedForBulkValueFetching( bulkValueFetcher != null );
		return this;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> optimizedForBulkValueFetching( boolean optimized ) {
		optimizedForBulkValueFetching = optimized;
		return this;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<EntityPropertyBindingContext, Object> fetchValues( Collection collection ) {
		if ( bulkValueFetcher != null ) {
			return bulkValueFetcher.apply( collection );
		}

		return original != null ? original.fetchValues( collection ) : EntityPropertyController.super.fetchValues( collection );
	}

	@Override
	public GenericEntityPropertyController order( int order ) {
		this.order = order;
		return this;
	}

	@Override
	public GenericEntityPropertyController valueFetcher( Function<EntityPropertyBindingContext, Object> valueFetcher ) {
		this.valueFetcher = valueFetcher;
		return this;
	}

	@Override
	public GenericEntityPropertyController createValueSupplier( Supplier<Object> supplier ) {
		return createValueFunction( e -> supplier.get() );
	}

	@Override
	public GenericEntityPropertyController createValueFunction( Function<EntityPropertyBindingContext, Object> function ) {
		this.createValueFunction = function;
		return this;
	}

	@Override
	public GenericEntityPropertyController createDtoFunction( Function<Object, Object> function ) {
		this.createDtoFunction = ( bindingContext, o ) -> function.apply( o );
		return this;
	}

	@Override
	public GenericEntityPropertyController createDtoFunction( BiFunction<EntityPropertyBindingContext, Object, Object> function ) {
		this.createDtoFunction = function;
		return this;
	}

	@Override
	public GenericEntityPropertyController applyValueConsumer( BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> valueWriter ) {
		if ( valueWriter == null ) {
			this.applyValueFunction = null;
		}
		else {
			this.applyValueFunction = ( context, value ) -> {
				valueWriter.accept( context, value );
				return true;
			};
		}
		return this;
	}

	@Override
	public GenericEntityPropertyController applyValueFunction( BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> valueWriter ) {
		this.applyValueFunction = valueWriter;
		return this;
	}

	@Override
	public GenericEntityPropertyController saveConsumer( BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> saveFunction ) {
		if ( saveFunction == null ) {
			this.saveFunction = null;
		}
		else {
			this.saveFunction = ( context, value ) -> {
				saveFunction.accept( context, value );
				return true;
			};
		}
		return this;
	}

	@Override
	public GenericEntityPropertyController saveFunction( BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> saveFunction ) {
		this.saveFunction = saveFunction;
		return this;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> validator( EntityPropertyValidator validator ) {
		this.validator = validator;
		return this;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> contextualValidator( ContextualValidator<EntityPropertyBindingContext, Object> validator ) {
		if ( validator != null ) {
			this.validator = ( bindingContext, propertyValue, errors, validationHints )
					-> validator.validate( bindingContext, propertyValue.getNewValue(), errors, validationHints );
		}
		else {
			this.validator = null;
		}
		return this;
	}

	@Override
	public Object fetchValue( EntityPropertyBindingContext context ) {
		if ( valueFetcher != null ) {
			return valueFetcher.apply( context );
		}
		return original != null ? original.fetchValue( context ) : null;
	}

	@Override
	public Object createValue( EntityPropertyBindingContext context ) {
		if ( createValueFunction != null ) {
			return createValueFunction.apply( context );
		}
		return original != null ? original.createValue( context ) : null;
	}

	@Override
	public Object createDto( EntityPropertyBindingContext context, Object value ) {
		if ( createDtoFunction != null ) {
			return createDtoFunction.apply( context, value );
		}
		return original != null ? original.createDto( context, value ) : ( value != null ? value : createValue( context ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean applyValue( @NonNull EntityPropertyBindingContext context, @NonNull EntityPropertyValue propertyValue ) {
		if ( applyValueFunction != null ) {
			return applyValueFunction.apply( context, propertyValue );
		}

		return original != null && original.applyValue( context, propertyValue );
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean save( EntityPropertyBindingContext context, EntityPropertyValue propertyValue ) {
		if ( saveFunction != null ) {
			return saveFunction.apply( context, propertyValue );
		}

		return original != null && original.save( context, propertyValue );
	}

	@Override
	@SuppressWarnings("unchecked")
	public void validate( EntityPropertyBindingContext context, EntityPropertyValue propertyValue, Errors errors, Object... validationHints ) {
		if ( validator != null ) {
			validator.validate( context, propertyValue, errors, validationHints );
		}
		else if ( original != null ) {
			original.validate( context, propertyValue, errors, validationHints );
		}
	}

	@Override
	public <X, V> ConfigurableEntityPropertyController<X, V> withEntity( Class<? super X> entityType, Class<? super V> propertyType ) {
		return new ScopedConfigurableEntityPropertyController<>( this, EntityPropertyBindingContext::getEntity );
	}

	@Override
	public <X, V> ConfigurableEntityPropertyController<X, V> withTarget( Class<? super X> entityType, Class<? super V> propertyType ) {
		return new ScopedConfigurableEntityPropertyController<>( this, EntityPropertyBindingContext::getTarget );
	}

	@Override
	public <V> ConfigurableEntityPropertyController<EntityPropertyBindingContext, V> withBindingContext( Class<? super V> propertyType ) {
		return new ScopedConfigurableEntityPropertyController<>( this, ctx -> ctx );
	}
}
