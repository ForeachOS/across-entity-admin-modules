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
package com.foreach.across.modules.entity.config.builders;

import com.foreach.across.modules.entity.registry.ConfigurableEntityViewRegistry;
import com.foreach.across.modules.entity.registry.EntityViewRegistry;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * @author Arne Vandamme
 */
public abstract class AbstractEntityViewBuilder<T extends EntityViewFactory, SELF>
{
	protected Object parent;
	private String name;
	private T factory;

	protected void setName( String name ) {
		this.name = name;
	}

	protected void setParent( Object parent ) {
		this.parent = parent;
	}

	@SuppressWarnings("unchecked")
	public SELF factory( T entityViewFactory ) {
		this.factory = entityViewFactory;
		return (SELF) this;
	}

	@SuppressWarnings("unchecked")
	protected void apply( ConfigurableEntityViewRegistry viewRegistry, AutowireCapableBeanFactory beanFactory ) {
		T configuredFactory = viewRegistry.getViewFactory( name );

		if ( configuredFactory == null ) {
			configuredFactory = factory != null ? factory : createFactoryInstance( beanFactory );
			viewRegistry.registerView( name, configuredFactory );
		}

		applyToViewFactory( beanFactory, viewRegistry, configuredFactory );
	}

	protected abstract T createFactoryInstance( AutowireCapableBeanFactory beanFactory );

	protected abstract void applyToViewFactory( AutowireCapableBeanFactory beanFactory,
	                                            EntityViewRegistry viewRegistry,
	                                            T factory );
}
