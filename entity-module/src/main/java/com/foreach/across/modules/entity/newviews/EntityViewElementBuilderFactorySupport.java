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
package com.foreach.across.modules.entity.newviews;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Arne Vandamme
 */
public abstract class EntityViewElementBuilderFactorySupport<T extends ViewElementBuilder>
		implements EntityViewElementBuilderFactory<T>
{
	private List<EntityViewElementBuilderProcessor<T>> processors = new ArrayList<>();

	public void addProcessor( EntityViewElementBuilderProcessor<T> processor ) {
		processors.add( processor );
	}

	@Override
	@SuppressWarnings("unchecked")
	public T createBuilder( EntityPropertyDescriptor propertyDescriptor,
	                        EntityPropertyRegistry entityPropertyRegistry,
	                        EntityConfiguration entityConfiguration ) {
		T builder = createInitialBuilder( propertyDescriptor, entityPropertyRegistry, entityConfiguration );

		for ( EntityViewElementBuilderProcessor<T> processor : processors ) {
			processor.process( propertyDescriptor, entityPropertyRegistry, entityConfiguration, builder );
		}

		return builder;
	}

	protected abstract T createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
	                                           EntityPropertyRegistry entityPropertyRegistry,
	                                           EntityConfiguration entityConfiguration );
}
