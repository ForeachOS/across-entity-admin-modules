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
package com.foreach.across.modules.entity.newviews.bootstrapui.options;

import com.foreach.across.modules.bootstrapui.elements.builder.OptionsFormElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.OptionsFormElementBuilder.Option;
import com.foreach.across.modules.entity.newviews.util.EntityViewElementUtils;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.util.EntityUtils;
import com.foreach.across.modules.entity.views.support.ValueFetcher;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generates {@link OptionsFormElementBuilder.Option} options for an enum.
 * Requires an {@link com.foreach.across.modules.entity.support.EntityMessageCodeResolver} attribute to be
 * present when building.  A message code of the form <em>enums.ENUM_NAME.ENUM_VALUE</em> will be looked up.
 *
 * @author Arne Vandamme
 */
public class EnumOptionIterableBuilder implements OptionIterableBuilder
{
	protected final Class<? extends Enum> enumType;
	protected final ValueFetcher<Object> valueFetcher;

	@SuppressWarnings("unchecked")
	public EnumOptionIterableBuilder( Class<? extends Enum> enumType,
	                                  ValueFetcher<?> valueFetcher ) {
		this.enumType = enumType;
		this.valueFetcher = (ValueFetcher<Object>) valueFetcher;
	}

	@Override
	public Iterable<Option> buildOptions( ViewElementBuilderContext builderContext ) {
		EntityMessageCodeResolver codeResolver = builderContext.getAttribute( EntityMessageCodeResolver.class );
		Collection selected = retrieveSelected( EntityViewElementUtils.currentEntity( builderContext ) );

		Enum[] enumValues = enumType.getEnumConstants();
		List<Option> options = new ArrayList<>( enumValues.length );

		for ( Enum enumValue : enumValues ) {
			Option option = new OptionsFormElementBuilder.Option();

			String messageCode = "enums." + enumType.getSimpleName() + "." + enumValue.name();
			String defaultLabel = EntityUtils.generateDisplayName( enumValue.name() );

			option.label( codeResolver.getMessageWithFallback( messageCode, defaultLabel ) );
			option.value( enumValue.name() );

			option.selected( selected.contains( enumValue ) );

			options.add( option );
		}

		return options;
	}

	protected Collection retrieveSelected( Object entity ) {
		if ( entity != null ) {
			Object selected = valueFetcher.getValue( entity );

			if ( selected != null ) {
				if ( selected instanceof Collection ) {
					return (Collection) selected;
				}
				else if ( selected.getClass().isArray() ) {
					return CollectionUtils.arrayToList( selected );
				}
				else {
					return Collections.singleton( selected );
				}
			}
		}

		return Collections.emptyList();
	}
}
