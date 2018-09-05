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

import com.foreach.across.modules.entity.views.support.ContextualValidator;
import lombok.NonNull;
import lombok.val;
import org.springframework.validation.Validator;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Arne Vandamme
 * @since 3.2.0
 */
public class NestedEntityPropertyController implements EntityPropertyController, ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object>
{
	private final String contextName;
	private final EntityPropertyController parent;
	private final EntityPropertyController child;

	public NestedEntityPropertyController( @NonNull String requiredChildContextName,
	                                       @NonNull EntityPropertyController parent,
	                                       @NonNull EntityPropertyController child ) {
		this.contextName = requiredChildContextName;
		this.parent = parent;
		this.child = new GenericEntityPropertyController( child );
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> order( int order ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> valueFetcher( Function<EntityPropertyBindingContext, Object> valueFetcher ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> createValueSupplier( Supplier<Object> supplier ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> createValueFunction( Function<EntityPropertyBindingContext, Object> function ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> applyValueConsumer( BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> valueWriter ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> applyValueFunction( BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> valueWriter ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> saveConsumer( BiConsumer<EntityPropertyBindingContext, EntityPropertyValue<Object>> saveFunction ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> saveFunction( BiFunction<EntityPropertyBindingContext, EntityPropertyValue<Object>, Boolean> saveFunction ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> addValidator( ContextualValidator<EntityPropertyBindingContext, Object> contextualValidator ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> addValidator( Validator validator ) {
		return null;
	}

	@Override
	public ConfigurableEntityPropertyController<EntityPropertyBindingContext, Object> addValidators( Validator... validators ) {
		return null;
	}

	@Override
	public <X, V> ConfigurableEntityPropertyController<X, V> withEntity( Class<X> entityType, Class<V> propertyType ) {
		return null;
	}

	@Override
	public <X, V> ConfigurableEntityPropertyController<X, V> withTarget( Class<X> targetType, Class<V> propertyType ) {
		return null;
	}

	@Override
	public <X, W, V> ConfigurableEntityPropertyController<EntityPropertyBindingContext<X, W>, V> withBindingContext( Class<X> entityType,
	                                                                                                                 Class<W> targetType,
	                                                                                                                 Class<V> propertyType ) {
		return null;
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
	public boolean applyValue( EntityPropertyBindingContext context, EntityPropertyValue propertyValue ) {
		return child.applyValue( childContext( context ), propertyValue );
	}

	@Override
	public boolean save( EntityPropertyBindingContext context, EntityPropertyValue propertyValue ) {
		return child.save( childContext( context ), propertyValue );
	}

	private EntityPropertyBindingContext childContext( EntityPropertyBindingContext context ) {
		return context.retrieveNamedChildContext( contextName, c -> childContextFactory( (EntityPropertyBindingContext) c ) );
	}

	@Override
	public int getOrder() {
		return 0;
	}

	private EntityPropertyBindingContext childContextFactory( EntityPropertyBindingContext parentContext ) {
		Object parentValue = parent.fetchValue( parentContext );

		val builder = EntityPropertyBindingContext.builder()
		                                          .controller( parent )
		                                          .entity( parentValue )
		                                          .target( parentValue )
				//.target( parent.createBinderTarget( parentContext, parentValue ) )
				;

		// todo: customize the binding context builder

		return builder.build();
	}
}
