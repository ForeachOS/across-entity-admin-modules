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
package com.foreach.across.modules.entity.views;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @author Arne Vandamme
 */
@Service
public class EntityViewElementBuilderServiceImpl implements EntityViewElementBuilderService
{
	private static final Logger LOG = LoggerFactory.getLogger( EntityViewElementBuilderServiceImpl.class );

	private Collection<ViewElementTypeLookupStrategy> elementTypeLookupStrategies;
	private Collection<EntityViewElementBuilderFactory> builderFactories;

	@SuppressWarnings("unchecked")
	@Override
	public ViewElementBuilder getElementBuilder( EntityPropertyDescriptor descriptor, ViewElementMode mode ) {
		ViewElementLookupRegistry lookupRegistry = descriptor.getAttribute( ViewElementLookupRegistry.class );

		if ( lookupRegistry != null ) {
			ViewElementBuilder builder = lookupRegistry.getViewElementBuilder( mode );

			if ( builder != null ) {
				return builder;
			}
			else {
				builder = createElementBuilder( descriptor, mode );
				Collection<ViewElementPostProcessor<?>> postProcessors
						= lookupRegistry.getViewElementPostProcessors( mode );

				if ( !postProcessors.isEmpty() ) {
					if ( builder instanceof ViewElementBuilderSupport ) {
						postProcessors.forEach( ( (ViewElementBuilderSupport) builder )::postProcessor );
					}
					else {
						LOG.warn(
								"ViewElementPostProcessors registered for {} but the builder is not of type ViewElementBuilderSupport",
								descriptor.getName() );
					}
				}

				if ( builder != null && lookupRegistry.isCacheable( mode ) ) {
					lookupRegistry.cacheViewElementBuilder( mode, builder );
				}

				return builder;
			}
		}

		return createElementBuilder( descriptor, mode );
	}

	@Override
	public ViewElementBuilder createElementBuilder( EntityPropertyDescriptor descriptor,
	                                                ViewElementMode mode ) {
		String elementType = getElementType( descriptor, mode );

		return createElementBuilder( descriptor, mode, elementType );
	}

	@Override
	public ViewElementBuilder createElementBuilder( EntityPropertyDescriptor descriptor,
	                                                ViewElementMode mode,
	                                                String elementType ) {
		for ( EntityViewElementBuilderFactory builderFactory : builderFactories ) {
			if ( builderFactory.supports( elementType ) ) {
				return builderFactory.createBuilder( descriptor, mode, elementType );
			}
		}

		return null;
	}

	@Override
	public String getElementType( EntityPropertyDescriptor descriptor, ViewElementMode mode ) {
		ViewElementLookupRegistry lookupRegistry = descriptor.getAttribute( ViewElementLookupRegistry.class );

		String elementType = lookupRegistry != null ? lookupRegistry.getViewElementType( mode ) : null;

		if ( elementType == null ) {
			// if not, fetch one
			for ( ViewElementTypeLookupStrategy lookupStrategy : elementTypeLookupStrategies ) {
				elementType = lookupStrategy.findElementType( descriptor, mode );

				if ( elementType != null ) {
					break;
				}
			}
		}

		if ( elementType != null && lookupRegistry != null ) {
			lookupRegistry.setViewElementType( mode, elementType );
		}

		return elementType;
	}

	@Autowired
	void setElementTypeLookupStrategies( @RefreshableCollection(incremental = true, includeModuleInternals = true) Collection<ViewElementTypeLookupStrategy> elementTypeLookupStrategies ) {
		this.elementTypeLookupStrategies = elementTypeLookupStrategies;
	}

	@Autowired
	void setBuilderFactories( @RefreshableCollection(incremental = true, includeModuleInternals = true) Collection<EntityViewElementBuilderFactory> builderFactories ) {
		this.builderFactories = builderFactories;
	}
}
