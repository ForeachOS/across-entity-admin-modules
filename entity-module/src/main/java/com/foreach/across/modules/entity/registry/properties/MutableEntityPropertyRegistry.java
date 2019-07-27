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

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * @author Arne Vandamme
 */
public interface MutableEntityPropertyRegistry extends EntityPropertyRegistry
{
	/**
	 * Set the id for this property registry.
	 *
	 * @param id of this registry
	 */
	void setId( String id );

	/**
	 * Attach a descriptor to this registry.  A descriptor can only be attached to a
	 * single registry, so the {@link EntityPropertyDescriptor#getPropertyRegistry()} should return {@code null}
	 * on the descriptor you try to register.
	 *
	 * @param descriptor instance
	 */
	void register( MutableEntityPropertyDescriptor descriptor );

	@Override
	MutableEntityPropertyDescriptor getProperty( String propertyName );

	void setDefaultOrder( String... propertyNames );

	void setDefaultOrder( Comparator<EntityPropertyDescriptor> defaultOrder );

	void setDefaultFilter( Predicate<EntityPropertyDescriptor> filter );
}
