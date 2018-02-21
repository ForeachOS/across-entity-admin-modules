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
package com.foreach.across.modules.entity.views.bootstrapui;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.FieldsetFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.FieldsetFormElementBuilder;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.registry.properties.EntityPropertySelector;
import com.foreach.across.modules.entity.registry.properties.meta.PropertyPersistenceMetadata;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderService;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.TextCodeResolverPostProcessor;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Builds a {@link com.foreach.across.modules.bootstrapui.elements.builder.FieldsetFormElementBuilder}
 * for a property.  The property can have a {@link EntityAttributes#FIELDSET_PROPERTY_SELECTOR} attribute
 * specifying the selector that should be used to fetch the members of the fieldset.
 * If none is available and the property is embedded, a default will be created for all properties of the embedded type.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.entity.EntityAttributes#FIELDSET_PROPERTY_SELECTOR
 */
@Component
public class FieldsetFormElementBuilderFactory extends EntityViewElementBuilderFactorySupport<FieldsetFormElementBuilder>
{
	private EntityViewElementBuilderService entityViewElementBuilderService;

	@Override
	public boolean supports( String viewElementType ) {
		return BootstrapUiElements.FIELDSET.equals( viewElementType );
	}

	@Override
	protected FieldsetFormElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
	                                                           ViewElementMode viewElementMode,
	                                                           String viewElementType ) {
		FieldsetFormElementBuilder fieldset
				= BootstrapUiBuilders.fieldset()
				                     .name( propertyDescriptor.getName() )
				                     .legend()
				                     .text( propertyDescriptor.getDisplayName() )
				                     .postProcessor(
						                     new TextCodeResolverPostProcessor<>( "properties." + propertyDescriptor.getName() )
				                     )
				                     .and()
				                     .postProcessor( new DescriptionTextPostProcessor( propertyDescriptor ) );

		EntityPropertySelector selector = retrieveMembersSelector( propertyDescriptor );
		EntityPropertyRegistry propertyRegistry = propertyDescriptor.getPropertyRegistry();

		if ( selector != null && propertyRegistry != null ) {
			for ( EntityPropertyDescriptor member : propertyRegistry.select( selector ) ) {
				ViewElementBuilder memberBuilder = entityViewElementBuilderService.getElementBuilder(
						member, viewElementMode
				);

				if ( memberBuilder != null ) {
					fieldset.add( memberBuilder );
				}
			}
		}

		return fieldset;
	}

	private EntityPropertySelector retrieveMembersSelector( EntityPropertyDescriptor descriptor ) {
		EntityPropertySelector selector
				= descriptor.getAttribute( EntityAttributes.FIELDSET_PROPERTY_SELECTOR, EntityPropertySelector.class );

		if ( selector == null && PropertyPersistenceMetadata.isEmbeddedProperty( descriptor ) ) {
			selector = new EntityPropertySelector( descriptor.getName() + ".*" );
		}

		return selector;
	}

	@Autowired
	public void setEntityViewElementBuilderService( EntityViewElementBuilderService entityViewElementBuilderService ) {
		this.entityViewElementBuilderService = entityViewElementBuilderService;
	}

	/**
	 * Attempts to resolve a property description (help block).
	 */
	public static class DescriptionTextPostProcessor implements ViewElementPostProcessor<FieldsetFormElement>
	{
		private final EntityPropertyDescriptor propertyDescriptor;
		private EntityMessageCodeResolver defaultMessageCodeResolver;

		public DescriptionTextPostProcessor( EntityPropertyDescriptor propertyDescriptor ) {
			this.propertyDescriptor = propertyDescriptor;
		}

		public void setDefaultMessageCodeResolver( EntityMessageCodeResolver defaultMessageCodeResolver ) {
			this.defaultMessageCodeResolver = defaultMessageCodeResolver;
		}

		@Override
		public void postProcess( ViewElementBuilderContext builderContext, FieldsetFormElement element ) {
			EntityMessageCodeResolver codeResolver = builderContext.getAttribute( EntityMessageCodeResolver.class );

			if ( codeResolver == null ) {
				codeResolver = defaultMessageCodeResolver;
			}

			if ( codeResolver != null ) {
				String description = codeResolver.getMessageWithFallback(
						"properties." + propertyDescriptor.getName() + "[description]", ""
				);

				if ( !StringUtils.isBlank( description ) ) {
					element.addFirstChild( BootstrapUiBuilders.helpBlock( description ).build( builderContext ) );
				}
			}
		}
	}
}
