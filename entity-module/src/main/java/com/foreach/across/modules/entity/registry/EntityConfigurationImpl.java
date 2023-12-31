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

import com.foreach.across.core.support.AttributeSupport;
import com.foreach.across.modules.entity.actions.EntityConfigurationAllowableActionsBuilder;
import com.foreach.across.modules.entity.actions.FixedEntityAllowableActionsBuilder;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyRegistry;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import com.foreach.across.modules.spring.security.actions.AllowableActions;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The base configuration for an Entity type.  Provides access to the
 * {@link com.foreach.across.modules.entity.registry.EntityModel},
 * {@link com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry}
 * along with the registered views and attributes.
 *
 * @author Arne Vandamme
 * @since 1.0.0
 */
public class EntityConfigurationImpl<T> extends AttributeSupport implements MutableEntityConfiguration<T>
{
	private final String name;
	private final Class<? extends T> entityType;
	private final Map<String, EntityViewFactory> registeredViews = new HashMap<>();
	private final Map<String, EntityAssociation> entityAssociations = new HashMap<>();

	private EntityMessageCodeResolver entityMessageCodeResolver;
	private EntityConfigurationAllowableActionsBuilder allowableActionsBuilder
			= new FixedEntityAllowableActionsBuilder( FixedEntityAllowableActionsBuilder.DEFAULT_ALLOWABLE_ACTIONS );

	private boolean hidden;
	private String displayName;

	private EntityModel<T, Serializable> entityModel;
	private MutableEntityPropertyRegistry propertyRegistry;

	public EntityConfigurationImpl( Class<T> entityType ) {
		this( EntityUtils.generateEntityName( entityType ), entityType );
	}

	public EntityConfigurationImpl( @NonNull String name, @NonNull Class<? extends T> entityType ) {
		this.name = name;
		this.entityType = entityType;

		this.displayName = EntityUtils.generateDisplayName( name );
	}

	@Override
	public EntityModel<T, Serializable> getEntityModel() {
		return entityModel;
	}

	@Override
	public boolean hasEntityModel() {
		return entityModel != null;
	}

	public void setEntityModel( EntityModel<T, Serializable> entityModel ) {
		this.entityModel = entityModel;
	}

	@Override
	public Class<? extends T> getEntityType() {
		return entityType;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName( String displayName ) {
		this.displayName = displayName;
	}

	@Override
	public boolean isHidden() {
		return hidden;
	}

	@Override
	public void setHidden( boolean hidden ) {
		this.hidden = hidden;
	}

	@Override
	public boolean hasView( String viewName ) {
		return registeredViews.containsKey( viewName );
	}

	@Override
	public void registerView( @NonNull String viewName, @NonNull EntityViewFactory viewFactory ) {
		registeredViews.put( viewName, viewFactory );
	}

	@Override
	public void removeView( String viewName ) {
		registeredViews.remove( viewName );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y extends EntityViewFactory> Y getViewFactory( String viewName ) {
		return (Y) registeredViews.get( viewName );
	}

	@Override
	public String[] getViewNames() {
		String[] viewNames = registeredViews.keySet().toArray( new String[registeredViews.size()] );
		Arrays.sort( viewNames );
		return viewNames;
	}

	@Override
	public MutableEntityPropertyRegistry getPropertyRegistry() {
		return propertyRegistry;
	}

	@Override
	public void setPropertyRegistry( @NonNull MutableEntityPropertyRegistry propertyRegistry ) {
		this.propertyRegistry = propertyRegistry;
	}

	@Override
	public EntityMessageCodeResolver getEntityMessageCodeResolver() {
		return entityMessageCodeResolver;
	}

	@Override
	public void setEntityMessageCodeResolver( @NonNull EntityMessageCodeResolver entityMessageCodeResolver ) {
		this.entityMessageCodeResolver = entityMessageCodeResolver;
	}

	@Override
	public boolean isNew( T entity ) {
		return entityModel.isNew( entity );
	}

	@Override
	public Class<?> getIdType() {
		return entityModel.getIdType();
	}

	@Override
	public Serializable getId( T entity ) {
		return entityModel.getId( entity );
	}

	@Override
	public String getLabel( T entity ) {
		return entityModel.getLabel( entity );
	}

	@Override
	public Collection<EntityAssociation> getAssociations() {
		return entityAssociations.values();
	}

	@Override
	public MutableEntityAssociation createAssociation( String name ) {
		if ( !entityAssociations.containsKey( name ) ) {
			entityAssociations.put( name, new EntityAssociationImpl( name, this ) );
		}

		return association( name );
	}

	@Override
	public MutableEntityAssociation association( String name ) {
		return (MutableEntityAssociation) entityAssociations.get( name );
	}

	@Override
	public EntityConfigurationAllowableActionsBuilder getAllowableActionsBuilder() {
		return allowableActionsBuilder;
	}

	@Override
	public void setAllowableActionsBuilder( @NonNull EntityConfigurationAllowableActionsBuilder allowableActionsBuilder ) {
		this.allowableActionsBuilder = allowableActionsBuilder;
	}

	@Override
	public AllowableActions getAllowableActions() {
		return allowableActionsBuilder.getAllowableActions( this );
	}

	@Override
	public AllowableActions getAllowableActions( T entity ) {
		return allowableActionsBuilder.getAllowableActions( this, entity );
	}

	@Override
	public String toString() {
		return "EntityConfigurationImpl{" +
				"name='" + name + '\'' +
				", entityType=" + entityType +
				'}';
	}
}
