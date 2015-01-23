package com.foreach.across.modules.entity.views.form;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import org.springframework.core.convert.ConversionService;

public class TextFormElement extends FormElementSupport
{
	public TextFormElement( EntityMessageCodeResolver messageCodeResolver,
	                        ConversionService conversionService,
	                        EntityPropertyDescriptor descriptor ) {
		super( messageCodeResolver, conversionService, descriptor, "text" );
	}

	public String getLabel() {
		return getDisplayName();
	}
}
