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
package com.foreach.across.modules.entity.config.builders.configuration;

import com.foreach.across.modules.entity.config.builders.AbstractEntityPropertyDescriptorBuilder;
import com.foreach.across.modules.entity.config.builders.AbstractSimpleEntityViewBuilder;
import com.foreach.across.modules.entity.config.builders.EntityConfigurationBuilder;
import com.foreach.across.modules.entity.newviews.ViewElementMode;
import com.foreach.across.modules.entity.views.ConfigurablePropertiesEntityViewFactorySupport;
import com.foreach.across.modules.entity.views.support.ValueFetcher;
import com.foreach.across.modules.web.ui.ViewElementBuilder;

public class ViewBuilder extends AbstractSimpleEntityViewBuilder<ConfigurablePropertiesEntityViewFactorySupport, ViewBuilder>
{
	@SuppressWarnings("unchecked")
	public class PropertyRegistryBuilder
			extends EntityViewPropertyRegistryBuilder<PropertyRegistryBuilder>
	{
		public class PropertyDescriptorBuilder extends AbstractEntityPropertyDescriptorBuilder<PropertyDescriptorBuilder>
		{
			@Override
			public PropertyDescriptorBuilder attribute( String name, Object value ) {
				return super.attribute( name, value );
			}

			@Override
			public <S> PropertyDescriptorBuilder attribute( Class<S> type, S value ) {
				return super.attribute( type, value );
			}

			@Override
			public PropertyDescriptorBuilder displayName( String displayName ) {
				return super.displayName( displayName );
			}

			@Override
			public PropertyDescriptorBuilder spelValueFetcher( String expression ) {
				return super.spelValueFetcher( expression );
			}

			@Override
			public PropertyDescriptorBuilder valueFetcher( ValueFetcher valueFetcher ) {
				return super.valueFetcher( valueFetcher );
			}

			@Override
			public PropertyDescriptorBuilder order( int order ) {
				return super.order( order );
			}

			@Override
			public PropertyDescriptorBuilder writable( boolean writable ) {
				return super.writable( writable );
			}

			@Override
			public PropertyDescriptorBuilder readable( boolean readable ) {
				return super.readable( readable );
			}

			@Override
			public PropertyDescriptorBuilder hidden( boolean hidden ) {
				return super.hidden( hidden );
			}

			@Override
			public PropertyDescriptorBuilder viewElementModeCaching( ViewElementMode mode,
			                                                         boolean cacheable ) {
				return super.viewElementModeCaching( mode, cacheable );
			}

			@Override
			public PropertyDescriptorBuilder viewElementType( ViewElementMode mode,
			                                                  String viewElementType ) {
				return super.viewElementType( mode, viewElementType );
			}

			@Override
			public PropertyDescriptorBuilder viewElementBuilder( ViewElementMode mode,
			                                                     ViewElementBuilder viewElementBuilder ) {
				return super.viewElementBuilder( mode, viewElementBuilder );
			}

			@Override
			public PropertyRegistryBuilder and() {
				return propertyRegistryBuilder;
			}
		}

		private final PropertyRegistryBuilder propertyRegistryBuilder;
		private final ViewBuilder parent;

		public PropertyRegistryBuilder( ViewBuilder parent ) {
			this.propertyRegistryBuilder = this;
			this.parent = parent;
		}

		@Override
		public synchronized PropertyDescriptorBuilder property( String name ) {
			return (PropertyDescriptorBuilder) super.property( name );
		}

		@Override
		protected PropertyDescriptorBuilder createDescriptorBuilder( String name ) {
			return new PropertyDescriptorBuilder();
		}

		@Override
		public ViewBuilder and() {
			return parent;
		}
	}

	@Override
	public PropertyRegistryBuilder properties() {
		return (PropertyRegistryBuilder) super.properties();
	}

	@Override
	public PropertyRegistryBuilder properties( String... propertyNames ) {
		return (PropertyRegistryBuilder) super.properties( propertyNames );
	}

	@Override
	protected PropertyRegistryBuilder createPropertiesBuilder() {
		return new PropertyRegistryBuilder( this );
	}

	@Override
	public EntityConfigurationBuilder and() {
		return (EntityConfigurationBuilder) parent;
	}
}
