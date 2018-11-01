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

package com.foreach.across.modules.entity.registry.properties.binding;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyBindingContext;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyController;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for an {@link EntityPropertyBindingContext} which implements
 * resolving of child property binding contexts.
 *
 * @author Arne Vandamme
 * @see SimpleEntityPropertyBindingContext
 * @since 3.2.0
 */
abstract class AbstractEntityPropertyBindingContext implements EntityPropertyBindingContext
{
	private final Map<String, EntityPropertyBindingContext> propertyContextMap = new HashMap<>();

	@Override
	public final EntityPropertyBindingContext resolvePropertyBindingContext( @NonNull String propertyName, EntityPropertyController controller ) {
		return propertyContextMap.computeIfAbsent( propertyName, key ->
				controller != null ? new ChildEntityPropertyBindingContext( this, controller ) : null
		);
	}
}
