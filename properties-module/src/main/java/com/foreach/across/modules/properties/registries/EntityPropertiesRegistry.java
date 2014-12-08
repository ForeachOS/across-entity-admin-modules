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
package com.foreach.across.modules.properties.registries;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.info.AcrossModuleInfo;
import com.foreach.across.modules.properties.config.EntityPropertiesDescriptor;
import com.foreach.across.modules.properties.repositories.PropertyTrackingRepository;
import com.foreach.common.spring.properties.PropertyTypeRegistry;
import com.foreach.common.spring.properties.support.SingletonPropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Allows modules to register extension properties.  An EntityPropertiesRegistry requires
 * the {@link com.foreach.across.modules.properties.repositories.PropertyTrackingRepository} to be active
 * and exposed.  The latter is automatically the case if the PropertiesModule is configured correctly.
 *
 * @author Arne Vandamme
 */
public abstract class EntityPropertiesRegistry
{
	private final Logger LOG = LoggerFactory.getLogger( getClass() );

	private final PropertyTypeRegistry<String> propertyTypeRegistry;

	private final EntityPropertiesDescriptor descriptor;
	private final PropertyTrackingRepository propertyTrackingRepository;

	protected EntityPropertiesRegistry( EntityPropertiesDescriptor descriptor ) {
		this( descriptor, null );
	}

	protected EntityPropertiesRegistry( EntityPropertiesDescriptor descriptor, Class classForUnknownProperties ) {
		this.descriptor = descriptor;
		this.propertyTypeRegistry = classForUnknownProperties != null
				? new PropertyTypeRegistry<String>( classForUnknownProperties, descriptor.conversionService() )
				: new PropertyTypeRegistry<String>( descriptor.conversionService() );
		this.propertyTrackingRepository = descriptor.trackingRepository();

		Assert.notNull( descriptor.conversionService(), "EntityPropertiesRegistry requires a valid ConversionService" );
	}

	public PropertyTypeRegistry<String> getPropertyTypeRegistry() {
		return propertyTypeRegistry;
	}

	public ConversionService getDefaultConversionService() {
		return propertyTypeRegistry.getDefaultConversionService();
	}

	public void register( AcrossModuleInfo acrossModule, String propertyKey, Class propertyClass ) {
		register( acrossModule.getName(), propertyKey, propertyClass );
	}

	public void register( AcrossModule acrossModule, String propertyKey, Class propertyClass ) {
		register( acrossModule.getName(), propertyKey, propertyClass );
	}

	private void register( String owner, String propertyKey, Class propertyClass ) {
		Assert.notNull( owner, "Only AcrossModules can register properties." );

		trackProperty( owner, propertyKey );

		propertyTypeRegistry.register( propertyKey, propertyClass );
	}

	public <A> void register( AcrossModuleInfo acrossModule,
	                          String propertyKey,
	                          Class<A> propertyClass,
	                          A propertyValue ) {
		register( acrossModule.getName(), propertyKey, propertyClass, propertyValue );
	}

	public <A> void register( AcrossModule acrossModule, String propertyKey, Class<A> propertyClass, A propertyValue ) {
		register( acrossModule.getName(), propertyKey, propertyClass, propertyValue );
	}

	private <A> void register( String owner, String propertyKey, Class<A> propertyClass, A propertyValue ) {
		Assert.notNull( owner, "Only AcrossModules can register properties." );

		trackProperty( owner, propertyKey );

		propertyTypeRegistry.register( propertyKey, propertyClass,
		                               SingletonPropertyFactory.<String, A>forValue( propertyValue ) );
	}

	private void trackProperty( String owner, String propertyKey ) {
		if ( propertyTrackingRepository != null ) {
			try {
				propertyTrackingRepository.register( owner, descriptor, propertyKey );
			}
			catch ( Exception e ) {
				LOG.warn( "Tracking property registration failed", e );
			}
		}
	}

	public void unregister( String propertyKey ) {
		propertyTypeRegistry.unregister( propertyKey );
	}

	public Class getClassForProperty( String propertyKey ) {
		return propertyTypeRegistry.getClassForProperty( propertyKey );
	}

	public Object getDefaultValueForProperty( String propertyKey ) {
		return propertyTypeRegistry.getDefaultValueForProperty( propertyKey );
	}

	public Class getClassForUnknownProperties() {
		return propertyTypeRegistry.getClassForUnknownProperties();
	}

	public void setClassForUnknownProperties( Class classForUnknownProperties ) {
		propertyTypeRegistry.setClassForUnknownProperties( classForUnknownProperties );
	}

	public boolean isRegistered( String propertyKey ) {
		return propertyTypeRegistry.isRegistered( propertyKey );
	}

	public Collection<String> getRegisteredProperties() {
		return propertyTypeRegistry.getRegisteredProperties();
	}

	public boolean isEmpty() {
		return propertyTypeRegistry.isEmpty();
	}

	public void clear() {
		propertyTypeRegistry.clear();
	}
}
