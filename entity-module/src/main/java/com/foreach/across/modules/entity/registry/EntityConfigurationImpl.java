package com.foreach.across.modules.entity.registry;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.registry.support.AttributeSupport;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The base configuration for an Entity type.  Provides access to the
 * {@link com.foreach.across.modules.entity.registry.EntityModel},
 * {@link com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry}
 * along with the registered views and attributes.
 */
public class EntityConfigurationImpl<T> extends AttributeSupport implements MutableEntityConfiguration<T>
{
	private final String name;
	private final Class<T> entityType;
	private final Map<String, EntityViewFactory> registeredViews = new HashMap<>();
	private final Map<Class, EntityAssociation> entityAssociations = new HashMap<>();

	private EntityMessageCodeResolver entityMessageCodeResolver;

	private String displayName;

	private EntityModel<T, ? extends Serializable> entityModel;
	private EntityPropertyRegistry propertyRegistry;

	public EntityConfigurationImpl( Class<T> entityType ) {
		this( StringUtils.uncapitalize( entityType.getSimpleName() ), entityType );
	}

	public EntityConfigurationImpl( String name, Class<T> entityType ) {
		this.name = name;
		this.entityType = entityType;

		this.displayName = EntityUtils.generateDisplayName( name );
	}

	@Override
	public EntityModel<T, ? extends Serializable> getEntityModel() {
		return entityModel;
	}

	public void setEntityModel( EntityModel<T, ? extends Serializable> entityModel ) {
		this.entityModel = entityModel;
	}

	@Override
	public Class<T> getEntityType() {
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
	public boolean hasView( String viewName ) {
		return registeredViews.containsKey( viewName );
	}

	@Override
	public void registerView( String viewName, EntityViewFactory viewFactory ) {
		Assert.notNull( viewName );
		Assert.notNull( viewFactory );
		registeredViews.put( viewName, viewFactory );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <Y extends EntityViewFactory> Y getViewFactory( String viewName ) {
		return (Y) registeredViews.get( viewName );
	}

	@Override
	public EntityPropertyRegistry getPropertyRegistry() {
		return propertyRegistry;
	}

	@Override
	public void setPropertyRegistry( EntityPropertyRegistry propertyRegistry ) {
		Assert.notNull( propertyRegistry );
		this.propertyRegistry = propertyRegistry;
	}

	@Override
	public EntityMessageCodeResolver getEntityMessageCodeResolver() {
		return entityMessageCodeResolver;
	}

	@Override
	public void setEntityMessageCodeResolver( EntityMessageCodeResolver entityMessageCodeResolver ) {
		Assert.notNull( entityMessageCodeResolver );
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
	public <V> MutableEntityAssociation<V> association( EntityConfiguration<V> associatedEntityConfiguration ) {
		return association( associatedEntityConfiguration.getEntityType() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V> MutableEntityAssociation<V> association( Class<V> entityType ) {
		return (MutableEntityAssociation<V>) entityAssociations.get( entityType );
	}

	@Override
	public Collection<EntityAssociation> getAssociations() {
		return entityAssociations.values();
	}

	@Override
	public <V> MutableEntityAssociation<V> createAssociation(
			MutableEntityConfiguration<V> associatedEntityConfiguration
	) {
		Class<V> associatedType = associatedEntityConfiguration.getEntityType();

		if ( !entityAssociations.containsKey( associatedType ) ) {
			entityAssociations.put( associatedType, new EntityAssociationImpl<>( this,
			                                                                     associatedEntityConfiguration ) );
		}

		return association( associatedType );
	}
}
