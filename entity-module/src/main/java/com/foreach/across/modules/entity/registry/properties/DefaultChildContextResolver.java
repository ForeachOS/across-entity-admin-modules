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

import com.foreach.across.modules.entity.bind.EntityPropertiesBinder;
import com.foreach.across.modules.entity.bind.EntityPropertyBinder;
import lombok.RequiredArgsConstructor;

/**
 * @author Steven Gentens
 * @since 3.2.0
 */
@RequiredArgsConstructor
public class DefaultChildContextResolver implements ChildContextResolver
{
	private final EntityPropertiesBinder propertiesBinder;

	@Override
	public EntityPropertyBindingContext resolve( String name ) {
		if ( propertiesBinder.containsKey( name ) ) {
			EntityPropertyBinder entityPropertyBinder = propertiesBinder.get( name );

			return EntityPropertyBindingContext.builder()
			                                   .entity( entityPropertyBinder.getOriginalValue() )
			                                   .target( entityPropertyBinder.getValue() )
			                                   .readonly( true )
			                                   .build();
		}
		return null;
	}
}
