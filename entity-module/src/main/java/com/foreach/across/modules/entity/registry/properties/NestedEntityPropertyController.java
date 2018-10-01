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

import lombok.NonNull;
import org.springframework.validation.Errors;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
// todo: implement
public class NestedEntityPropertyController implements EntityPropertyController, ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object>
{
	private final String contextName;
	private final EntityPropertyController parent;
	private final GenericEntityPropertyController child;

	public NestedEntityPropertyController( @NonNull String requiredChildContextName,
	                                       @NonNull EntityPropertyController parent,
	                                       @NonNull EntityPropertyController child ) {
		this.contextName = requiredChildContextName;
		this.parent = parent;
		this.child = new GenericEntityPropertyController( child );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> order( int order ) {
		return child.order( order );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> valueFetcher( Function<EntityPropertyBindingContext, Object> valueFetcher ) {
		return child.createValueFunction( valueFetcher );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> createValueSupplier( Supplier<Object> supplier ) {
		return child.createValueSupplier( supplier );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> createValueFunction( Function<EntityPropertyBindingContext, Object> function ) {
		return child.createValueFunction( function );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> applyValueConsumer( BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> valueWriter ) {
		return child.applyValueConsumer( valueWriter );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> applyValueFunction( BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> valueWriter ) {
		return child.applyValueFunction( valueWriter );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> saveConsumer( BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> saveFunction ) {
		return child.saveConsumer( saveFunction );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> saveFunction( BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> saveFunction ) {
		return child.saveFunction( saveFunction );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> validator( EntityPropertyValidator propertyValidator ) {
		return child.validator( propertyValidator );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> contextualValidator( ContextualValidator<EntityPropertyBindingContext, Object> contextualValidator ) {
		return child.contextualValidator( contextualValidator );
	}

	@Override
	public <X, V> ConfigurableEntityPropertyController<X, V> withEntity( Class<X> entityType, Class<V> propertyType ) {
		return child.withEntity( entityType, propertyType );
	}

	@Override
	public <X, V> ConfigurableEntityPropertyController<X, V> withTarget( Class<X> targetType, Class<V> propertyType ) {
		return child.withTarget( targetType, propertyType );
	}

	@Override
	public <X, W, V> ConfigurableEntityPropertyController<EntityPropertyBindingContext, V> withBindingContext( Class<X> entityType,
	                                                                                                           Class<W> targetType,
	                                                                                                           Class<V> propertyType ) {
		return child.withBindingContext( entityType, targetType, propertyType );
	}

	@Override
	public Object fetchValue( EntityPropertyBindingContext context ) {
		return child.fetchValue( childContext( context ) );
	}

	@Override
	public Object createValue( EntityPropertyBindingContext context ) {
		return child.createValue( childContext( context ) );
	}

	@Override
	public void validate( EntityPropertyBindingContext context, EntityPropertyValue propertyValue, Errors errors, Object... validationHints ) {
		child.validate( childContext( context ), propertyValue, errors, validationHints );
	}

	@Override
	public boolean applyValue( EntityPropertyBindingContext context, EntityPropertyValue propertyValue ) {
		return child.applyValue( childContext( context ), propertyValue );
	}

	@Override
	public boolean save( EntityPropertyBindingContext context, EntityPropertyValue propertyValue ) {
		return child.save( childContext( context ), propertyValue );
	}

	private EntityPropertyBindingContext childContext( EntityPropertyBindingContext context ) {
		return context.getOrCreateChildContext( contextName, ( parentContext, builder ) -> {
			Object parentValue = parent.fetchValue( parentContext );

			builder.controller( parent ).entity( parentValue ).target( parentValue );
		} );
	}

	@Override
	public int getOrder() {
		return child.getOrder();
	}
}
