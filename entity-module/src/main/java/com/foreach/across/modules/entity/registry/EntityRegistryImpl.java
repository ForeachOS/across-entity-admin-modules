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

package com.foreach.across.modules.entity.registry;

import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.*;

/**
 * Contains the registered entity definitions that are manageable.
 * Every registered {@link com.foreach.across.modules.entity.registry.MutableEntityConfiguration} must have
 * a unique name ({@link EntityConfiguration#getName()}) and entity type ({@link EntityConfiguration#getEntityType()}).
 * <p/>
 * WARNING: Although in most cases not an actual issue, EntityRegistry currently does not support registering multiple
 * classes with the same name from different class loaders.
 */
@Service
public class EntityRegistryImpl implements MutableEntityRegistry
{
	private static final Comparator<EntityConfiguration> DISPLAYNAME_COMPARATOR = new Comparator<EntityConfiguration>()
	{
		@Override
		public int compare( EntityConfiguration left, EntityConfiguration right ) {
			return ObjectUtils.compare( left.getDisplayName(), right.getDisplayName() );
		}
	};

	private static final Logger LOG = LoggerFactory.getLogger( EntityRegistryImpl.class );

	private final List<EntityConfiguration> entityConfigurations = new ArrayList<>();

	@Override
	public Collection<EntityConfiguration> getEntities() {
		return Collections.unmodifiableList( entityConfigurations );
	}

	@Override
	public void register( @NonNull MutableEntityConfiguration<?> entityConfiguration ) {
		Assert.notNull( entityConfiguration.getEntityType(), "entityType of entityConfiguration cannot be null" );
		Assert.notNull( entityConfiguration.getName(), "name of entityConfiguration cannot be null" );

		EntityConfiguration existingByName = getEntityConfiguration( entityConfiguration.getName() );

		if ( existingByName != null && existingByName != entityConfiguration ) {
			throw new IllegalArgumentException( "There is another EntityConfiguration for " + existingByName
					.getEntityType() + " with name " + existingByName.getName() );
		}

		EntityConfiguration existingByType = getEntityConfiguration( entityConfiguration.getEntityType() );

		if ( existingByType != null && existingByType != entityConfiguration ) {
			throw new IllegalArgumentException( "There is another EntityConfiguration for " + existingByType
					.getEntityType() + " with name " + existingByType.getName() );
		}

		if ( existingByName == entityConfiguration || existingByType == entityConfiguration ) {
			LOG.trace( "Attempt to re-register EntityConfiguration for " + entityConfiguration
					.getEntityType() + " with name " + entityConfiguration.getName() );
			return;
		}

		if ( entityConfiguration.getEntityModel() == null ) {
			LOG.warn( "Registering entity type {} without entity model - functionality will be limited",
			          entityConfiguration.getEntityType() );
		}

		entityConfigurations.add( entityConfiguration );
		entityConfigurations.sort( DISPLAYNAME_COMPARATOR );
	}

	@Override
	public boolean contains( Class<?> entityType ) {
		return getEntityConfiguration( entityType ) != null;
	}

	@Override
	public boolean contains( String entityName ) {
		return getEntityConfiguration( entityName ) != null;
	}

	@Override
	public <T> MutableEntityConfiguration<T> remove( String entityName ) {
		MutableEntityConfiguration<T> registered = getEntityConfiguration( entityName );

		if ( registered != null ) {
			entityConfigurations.remove( registered );
		}

		return registered;
	}

	@Override
	public <T> MutableEntityConfiguration<T> remove( Class<T> entityType ) {
		MutableEntityConfiguration<T> registered = getEntityConfiguration( entityType );

		if ( registered != null ) {
			entityConfigurations.remove( registered );
		}

		return registered;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> EntityConfiguration<T> getEntityConfiguration( T entity ) {
		return entity != null ? getEntityConfiguration( (Class<T>) ClassUtils.getUserClass( entity ) ) : null;
	}

	@Override
	@SuppressWarnings({ "unchecked", "squid:S1872" })
	public <T> MutableEntityConfiguration<T> getEntityConfiguration( @NonNull Class<T> entityType ) {
		for ( EntityConfiguration configuration : entityConfigurations ) {
			// Consider 2 classes the same if they have the same name - workaround some issues with spring boot devtools classloader
			// squid:S1872
			if ( configuration.getEntityType().getName().equals( entityType.getName() ) ) {
				return (MutableEntityConfiguration<T>) configuration;
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> MutableEntityConfiguration<T> getEntityConfiguration( @NonNull String entityName ) {
		for ( EntityConfiguration configuration : entityConfigurations ) {
			if ( StringUtils.equals( configuration.getName(), entityName ) ) {
				return (MutableEntityConfiguration<T>) configuration;
			}
		}

		return null;
	}
}
