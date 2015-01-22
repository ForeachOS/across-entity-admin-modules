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

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyFilter;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyFilters;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.properties.ConversionServicePrintablePropertyView;
import com.foreach.across.modules.entity.views.properties.PrintablePropertyView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Base support class for entity view factories that supports the configuration of the properties
 * to display for a a given entity type.
 *
 * @author Arne Vandamme
 */
public abstract class ConfigurablePropertiesEntityViewFactorySupport<T extends EntityView>
		extends SimpleEntityViewFactorySupport<T>
{
	private ConversionService conversionService;
	private EntityPropertyRegistry propertyRegistry;
	private EntityPropertyFilter propertyFilter;
	private Comparator<EntityPropertyDescriptor> propertyComparator;

	public EntityPropertyRegistry getPropertyRegistry() {
		return propertyRegistry;
	}

	public void setPropertyRegistry( EntityPropertyRegistry propertyRegistry ) {
		this.propertyRegistry = propertyRegistry;
	}

	public EntityPropertyFilter getPropertyFilter() {
		return propertyFilter;
	}

	public void setPropertyFilter( EntityPropertyFilter propertyFilter ) {
		this.propertyFilter = propertyFilter;
	}

	public Comparator<EntityPropertyDescriptor> getPropertyComparator() {
		return propertyComparator;
	}

	public void setPropertyComparator( Comparator<EntityPropertyDescriptor> propertyComparator ) {
		this.propertyComparator = propertyComparator;
	}

	/**
	 * @param conversionService The conversion service to attach to the property views.
	 */
	@Autowired(required = false)
	public void setConversionService( ConversionService conversionService ) {
		this.conversionService = conversionService;
	}

	@Override
	protected void buildViewModel( EntityConfiguration entityConfiguration,
	                               EntityMessageCodeResolver messageCodeResolver,
	                               T view ) {
		view.setEntityProperties( getEntityProperties( messageCodeResolver ) );

		extendViewModel( entityConfiguration, view );
	}

	private List<PrintablePropertyView> getEntityProperties( EntityMessageCodeResolver messageCodeResolver ) {
		EntityPropertyFilter filter = getPropertyFilter() != null ? getPropertyFilter() : EntityPropertyFilters.NoOp;

		List<EntityPropertyDescriptor> descriptors;

		if ( getPropertyComparator() != null ) {
			descriptors = getPropertyRegistry().getProperties( filter, getPropertyComparator() );
		}
		else {
			descriptors = getPropertyRegistry().getProperties( filter );
		}

		List<PrintablePropertyView> propertyViews = new ArrayList<>( descriptors.size() );

		for ( EntityPropertyDescriptor descriptor : descriptors ) {
			propertyViews.add( createPropertyView( descriptor, messageCodeResolver ) );
		}

		return propertyViews;
	}

	protected PrintablePropertyView createPropertyView( EntityPropertyDescriptor descriptor,
	                                                    EntityMessageCodeResolver messageCodeResolver ) {
		return new ConversionServicePrintablePropertyView( messageCodeResolver, conversionService, descriptor );
	}

	protected abstract void extendViewModel( EntityConfiguration entityConfiguration, T view );
}
