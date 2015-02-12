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

import com.foreach.across.modules.entity.registry.support.ReadableAttributes;
import com.foreach.across.modules.entity.views.EntityViewFactory;

/**
 * Filtered view on an association between two {@link com.foreach.across.modules.entity.registry.EntityConfiguration}s.
 *
 * @author Arne Vandamme
 */
public interface EntityAssociation<ET> extends ReadableAttributes
{
	String getName();

	Class<ET> getEntityType();

	EntityConfiguration<ET> getAssociatedEntityConfiguration();

	boolean hasView( String name );

	<Y extends EntityViewFactory> Y getViewFactory( String viewName );
}
