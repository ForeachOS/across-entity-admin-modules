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
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.MethodInvocationException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Optional;

/**
 * Wrapper for binding values to custom properties. Much like a {@link org.springframework.beans.BeanWrapper}
 * except it uses an {@link EntityPropertyRegistry} to determine which properties exist and what their type is.
 * Each existing descriptor should have a {@link com.foreach.across.modules.entity.registry.properties.EntityPropertyController}
 * with a value reader/writer for correct use.
 * <p/>
 * Implemented as a {@code Map} for data binder purposes, but expects every key to correspond with a registered property
 * in the {@link EntityPropertyRegistry}. When fetched, will create an intermediate target
 * for that property and apply type conversion based on the registered property type.
 * Should not be used as a regular {@link java.util.Map} implementation.
 * <p/>
 * When a {@link ConversionService} is specified, type conversion will occur when setting a property value.
 * <p/>
 * WARNING: How properties are accessed can be relevant in how they are treated.
 * You can access for example {@code properties[user].value.name} or {@code properties[user.name].value} and both might
 * have different access semantics because the latter uses an entirely separate {@link EntityPropertyDescriptor}
 * whereas in the former {@code name} is a direct bean path.
 *
 * @author Arne Vandamme
 * @see EntityPropertiesBinderController
 * @see EntityPropertyControlName
 * @since 3.2.0
 */
@RequiredArgsConstructor
public class EntityPropertiesBinder extends HashMap<String, EntityPropertyBinder> implements EntityPropertyValues
{
	@NonNull
	private final EntityPropertyRegistry propertyRegistry;

	private EntityPropertyDescriptor parentProperty;

	/**
	 * Prefix when the map is being used for data binding.
	 * If set and an exception occurs when setting a property value, it will be
	 * rethrown as a {@link ConversionNotSupportedException} with {@link PropertyChangeEvent} information.
	 * <p/>
	 * The binder prefix would usually be the name of a property where this {@code EntityPropertiesBinder}
	 * is available on the {@link org.springframework.validation.DataBinder} target.
	 * When using
	 */
	@Setter
	@Getter
	private String binderPrefix = "";

	/**
	 * Optionally set a {@link ConversionService} that should be used to convert the input
	 * value to the required field type. It no {@code ConversionService} is set, the actual value
	 * must match the expected field type or a {@link ClassCastException} will occur.
	 * <p/>
	 * If a {@code ConversionService} is set, type conversion will be attempted and exceptions
	 * will only be thrown if conversion failes.
	 */
	@Setter
	private ConversionService conversionService;

	@Getter
	private boolean bindingEnabled;

	/**
	 * Returns self so that this binder could be used as direct {@link org.springframework.validation.DataBinder} target.
	 * The {@link #setBinderPrefix(String)} is usually set to {@code properties} in this case.
	 *
	 * @return self
	 */
	public final EntityPropertiesBinder getProperties() {
		return this;
	}

	/**
	 * Set the binding context for this binder.
	 */
	@Setter
	@Getter
	private EntityPropertyBindingContext bindingContext;

	@Override
	public EntityPropertyBinder getOrDefault( Object key, EntityPropertyBinder defaultValue ) {
		return get( key );
	}

	/**
	 * Get the property with the given name.
	 * Will fetch the property descriptor and create the value holder with the current value.
	 * If there is no descriptor for that property, an {@link IllegalArgumentException} will be thrown.
	 *
	 * @param key property name
	 * @return value holder
	 */
	@Override
	public EntityPropertyBinder get( Object key ) {
		EntityPropertyBinder valueHolder = super.get( key );
		String propertyName = (String) key;

		if ( valueHolder == null ) {
			try {
				String fqPropertyName = parentProperty != null ? parentProperty.getName() + "." + propertyName : propertyName;
				val descriptor = propertyRegistry.getProperty( fqPropertyName );
				if ( descriptor == null ) {
					throw new IllegalArgumentException( "No such property descriptor: '" + fqPropertyName + "'" );
				}

				AbstractEntityPropertyBinder binder = createPropertyBinder( descriptor );
				binder.setBinderPath( getPropertyBinderPath( propertyName ) );
				binder.enableBinding( bindingEnabled );

				// if there is a child binding context with the same name, assume it represents the same property and
				// use the pre-loaded binding context for property values
				Optional.ofNullable( bindingContext.getChildContexts().get( descriptor.getName() ) )
				        .ifPresent( bindingContext -> {
					        binder.setOriginalValue( bindingContext.getEntity() );
					        binder.setValue( bindingContext.getTarget() );
				        } );

				valueHolder = binder;
				put( propertyName, valueHolder );
			}
			catch ( IllegalArgumentException iae ) {
				if ( !StringUtils.isEmpty( binderPrefix ) ) {
					PropertyChangeEvent pce = new PropertyChangeEvent( this, binderPrefix + "[" + key + "]", null, null );
					throw new MethodInvocationException( pce, iae );
				}
				throw iae;
			}
		}

		return valueHolder;
	}

	/**
	 * Signal that actual binding is enabled, this allows individual property binders to determine how they
	 * should interpret the current value. For example: if binding is enabled and a list value is requested,
	 * the list will only return the items that have been bound before, disregarding the possible currently
	 * stored value.
	 * <p/>
	 * In readonly mode this property will usually be kept {@code false}, simply returning the property values.
	 *
	 * @param bindingEnabled true to signal binding is enabled
	 */
	public void setBindingEnabled( boolean bindingEnabled ) {
		this.bindingEnabled = bindingEnabled;

		values().forEach( b -> b.enableBinding( bindingEnabled ) );
	}

	/**
	 * Create a new controller for this properties binder, allows for applying, validating and saving the configured binder properties.
	 *
	 * @return new controller instance
	 */
	public EntityPropertiesBinderController createController() {
		return new EntityPropertiesBinderController( this );
	}

	String getPropertyBinderPath( String propertyName ) {
		return StringUtils.defaultIfEmpty( binderPrefix, "properties" ) + "[" + propertyName + "]";
	}

	AbstractEntityPropertyBinder createPropertyBinder( EntityPropertyDescriptor descriptor ) {
		TypeDescriptor typeDescriptor = descriptor.getPropertyTypeDescriptor();
		EntityPropertyBindingType bindingType = EntityPropertyBindingType.forProperty( descriptor );

		switch ( bindingType ) {
			case MAP:
				val keyDescriptor = getOrCreateDescriptor( descriptor.getName() + EntityPropertyRegistry.MAP_KEY, typeDescriptor.getMapKeyTypeDescriptor() );
				val valueDescriptor = getOrCreateDescriptor( descriptor.getName() + EntityPropertyRegistry.MAP_VALUE,
				                                             typeDescriptor.getMapValueTypeDescriptor() );

				return new MapEntityPropertyBinder( this, descriptor, keyDescriptor, valueDescriptor );
			case COLLECTION:
				val memberDescriptor = getOrCreateDescriptor( descriptor.getName() + EntityPropertyRegistry.INDEXER,
				                                              typeDescriptor.getElementTypeDescriptor() );

				return new ListEntityPropertyBinder( this, descriptor, memberDescriptor );
			default:
				return new SingleEntityPropertyBinder( this, descriptor );
		}
	}

	private EntityPropertyDescriptor getOrCreateDescriptor( String name, TypeDescriptor expectedType ) {
		EntityPropertyDescriptor descriptor = propertyRegistry.getProperty( name );

		if ( descriptor == null ) {
			SimpleEntityPropertyDescriptor dummy = new SimpleEntityPropertyDescriptor( name );
			dummy.setPropertyTypeDescriptor( expectedType );
			dummy.setPropertyRegistry( propertyRegistry );
			descriptor = dummy;
		}

		return descriptor;
	}

	// todo: cleanup
	boolean shouldSetBinderPrefix() {
		return StringUtils.isNotEmpty( binderPrefix );
	}

	EntityPropertiesBinder createChildBinder( EntityPropertyDescriptor parent, EntityPropertyController controller, Object propertyValue ) {
		EntityPropertiesBinder childBinder = new EntityPropertiesBinder( propertyRegistry );
		childBinder.parentProperty = parent;
		childBinder.setConversionService( conversionService );
		childBinder.setBindingEnabled( bindingEnabled );

		if ( EntityPropertyRegistry.isMemberPropertyDescriptor( parent ) ) {
			childBinder.setBindingContext(
					EntityPropertyBindingContext.builder()
					                            .entity( propertyValue )
					                            .target( propertyValue )
					                            .readonly( bindingContext.isReadonly() )
					                            .build()
			);
		}
		else {
			// nested property will lookup the child binding context
			childBinder.setBindingContext( bindingContext );
			String childContextName = parent.getTargetPropertyName();
			if ( bindingContext.hasChildContext( childContextName ) ) {
				// todo: refactor this? currently child context should be reset if creating a new one with an initialized value
				EntityPropertyBindingContext existingChildContext = bindingContext.getOrCreateChildContext( childContextName, ( p, b ) -> {
				} );

				if ( existingChildContext.getTarget() == null && propertyValue != null ) {
					bindingContext.removeChildContext( childContextName );
				}
			}

			if ( !bindingContext.hasChildContext( childContextName ) ) {
				bindingContext.getOrCreateChildContext(
						childContextName,
						( p, b ) -> b.controller( controller ).entity( propertyValue ).target( propertyValue )
				);
			}

		}

		return childBinder;
	}

	Object createValue( EntityPropertyController controller ) {
		if ( controller != null ) {
			return controller.createValue( getBindingContext() );
		}

		return null;
	}

	Object convertIfNecessary( Object source, TypeDescriptor targetType, String path ) {
		return convertIfNecessary( source, targetType, targetType.getObjectType(), path );
	}

	Object convertIfNecessary( Object source, TypeDescriptor targetType, Class<?> typeToReport, String path ) {
		if ( source == null || source.getClass().equals( Object.class ) ) {
			return null;
		}

		try {
			ConversionService conversionService = this.conversionService != null ? this.conversionService : DefaultConversionService.getSharedInstance();
			if ( conversionService != null ) {
				return conversionService.convert( source, TypeDescriptor.forObject( source ), targetType );
			}

			return targetType.getObjectType().cast( source );
		}
		catch ( ClassCastException | ConversionFailedException | ConverterNotFoundException cce ) {
			if ( !StringUtils.isEmpty( binderPrefix ) ) {
				PropertyChangeEvent pce = new PropertyChangeEvent( this, path, null, source );
				throw new ConversionNotSupportedException( pce, typeToReport, cce );
			}
			throw cce;
		}
	}
}
