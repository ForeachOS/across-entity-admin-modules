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

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.*;

/**
 * @author Arne Vandamme
 */
public abstract class EntityPropertyRegistrySupport implements MutableEntityPropertyRegistry
{
	private final Map<String, EntityPropertyDescriptor> descriptorMap = new HashMap<>();

	private final EntityPropertyRegistryProvider registryProvider;
	private final EntityPropertySelectorExecutor selectorExecutor;

	private EntityPropertyFilter defaultFilter;
	private Comparator<EntityPropertyDescriptor> defaultOrder = null;

	protected EntityPropertyRegistrySupport( EntityPropertyRegistryProvider registryProvider ) {
		this.registryProvider = registryProvider;
		selectorExecutor = new EntityPropertySelectorExecutor( this, registryProvider );
	}

	protected EntityPropertyRegistryProvider getRegistryProvider() {
		return registryProvider;
	}

	@Override
	public EntityPropertyFilter getDefaultFilter() {
		return defaultFilter;
	}

	@Override
	public void setDefaultFilter( EntityPropertyFilter filter ) {
		defaultFilter = filter;
	}

	@Override
	public void setDefaultOrder( String... propertyNames ) {
		setDefaultOrder( new EntityPropertyComparators.Ordered( propertyNames ) );
	}

	@Override
	public void setDefaultOrder( Comparator<EntityPropertyDescriptor> defaultOrder ) {
		this.defaultOrder = defaultOrder;
	}

	@Override
	public Comparator<EntityPropertyDescriptor> getDefaultOrder() {
		return defaultOrder;
	}

	@Override
	public List<EntityPropertyDescriptor> getProperties() {
		return getProperties( getDefaultFilter() );
	}

	@Override
	public List<EntityPropertyDescriptor> getProperties( EntityPropertyFilter filter ) {
		return fetchProperties( filter, getDefaultOrder() );
	}

	@Deprecated
	@Override
	public List<EntityPropertyDescriptor> getProperties( EntityPropertyFilter filter,
	                                                     Comparator<EntityPropertyDescriptor> comparator ) {
		Assert.notNull( filter );
		Assert.notNull( comparator );

		return fetchProperties( filter, comparator.thenComparing( getDefaultOrder() ) );
	}

	private List<EntityPropertyDescriptor> fetchProperties( EntityPropertyFilter filter,
	                                                        Comparator<EntityPropertyDescriptor> comparator ) {
		EntityPropertyFilter filterToUse = filter != null ? filter : getDefaultFilter();

		if ( filterToUse == null ) {
			filterToUse = EntityPropertyFilters.NOOP;
		}

		List<EntityPropertyDescriptor> filtered = new ArrayList<>();

		if ( filterToUse instanceof EntityPropertyFilter.Inclusive ) {
			for ( String propertyName : ( (EntityPropertyFilter.Inclusive) filterToUse ) ) {
				EntityPropertyDescriptor descriptor = getProperty( propertyName );
				if ( descriptor != null ) {
					filtered.add( descriptor );
				}
			}
		}
		else {
			for ( EntityPropertyDescriptor candidate : getRegisteredDescriptors() ) {
				if ( !isNestedProperty( candidate.getName() ) && filterToUse.shouldInclude( candidate ) ) {
					filtered.add( candidate );
				}
			}
		}

		if ( comparator != null ) {
			Collections.sort( filtered, comparator );
		}

		return filtered;
	}

	@Override
	public List<EntityPropertyDescriptor> select( EntityPropertySelector selector ) {
		return selectorExecutor.select( selector );
	}

	private boolean isNestedProperty( String name ) {
		return StringUtils.contains( name, "." );
	}

	/**
	 * @return The unfiltered list of all registered EntityPropertyDescriptors.
	 */
	@Override
	public Collection<EntityPropertyDescriptor> getRegisteredDescriptors() {
		return descriptorMap.values();
	}

	@Override
	public MutableEntityPropertyDescriptor getProperty( String propertyName ) {
		EntityPropertyDescriptor descriptor = descriptorMap.get( propertyName );

		return descriptor instanceof MutableEntityPropertyDescriptor ? (MutableEntityPropertyDescriptor) descriptor : null;
	}

	@Override
	public boolean contains( String propertyName ) {
		return descriptorMap.containsKey( propertyName ) || getProperty( propertyName ) != null;
	}

	@Override
	public void register( MutableEntityPropertyDescriptor descriptor ) {
		if ( descriptor.getPropertyRegistry() != null && descriptor.getPropertyRegistry() != this ) {
			throw new IllegalArgumentException( "Descriptor already has a different property registry attached." );
		}

		descriptor.setPropertyRegistry( this );
		descriptorMap.put( descriptor.getName(), descriptor );
	}
}
