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

package com.foreach.across.modules.entity.views.request;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an entity view command object.  In case of a form view will usually hold the DTO of the
 * entity being created or updated.  Additionally extension objects can be registered that
 * will be bound and validated as well.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class EntityViewCommand
{
	private String entityName;

	@Valid
	private Object entity;

	@Valid
	private Map<String, Object> extensions = new HashMap<>();

	public EntityViewCommand() {
	}

	@Deprecated
	public String getEntityName() {
		return entityName;
	}

	public void setEntityName( String entityName ) {
		this.entityName = entityName;
	}

	/**
	 * @return The entity (dto) instance for the command.
	 */
	public Object getEntity() {
		return entity;
	}

	/**
	 * @return the entity if of the expected type, null otherwise
	 */
	public <V> V getEntity( Class<V> entityType ) {
		return entityType.isInstance( entity ) ? (V) entity : null;
	}

	public void setEntity( Object entity ) {
		this.entity = entity;
	}

	/**
	 * @return Map of possible command extensions that have been registered.
	 */
	public Map<String, Object> getExtensions() {
		return extensions;
	}

	public void addExtensions( String name, Object extension ) {
		extensions.put( name, extension );
	}

	public <Y> Y getExtension( String extensionName, Class<Y> extensionType ) {
		return extensionType.cast( extensions.get( extensionName ) );
	}
}
