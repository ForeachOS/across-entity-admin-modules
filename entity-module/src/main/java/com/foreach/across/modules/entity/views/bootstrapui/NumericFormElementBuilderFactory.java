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

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.elements.NumericFormElementConfiguration;
import com.foreach.across.modules.bootstrapui.elements.NumericFormElementConfiguration.Format;
import com.foreach.across.modules.bootstrapui.elements.builder.NumericFormElementBuilder;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactoryHelper;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderFactorySupport;
import com.foreach.across.modules.entity.views.EntityViewElementBuilderService;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.ConversionServiceValueTextPostProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.NumericValueTextPostProcessor;
import com.foreach.across.modules.entity.views.bootstrapui.processors.element.PropertyPlaceholderTextPostProcessor;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElementPostProcessor;
import com.foreach.across.modules.web.ui.elements.builder.TextViewElementBuilder;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Currency;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * @author Arne Vandamme
 */
@ConditionalOnBootstrapUI
@Component
public class NumericFormElementBuilderFactory extends EntityViewElementBuilderFactorySupport<ViewElementBuilder>
{
	private final ControlBuilderFactory controlBuilderFactory = new ControlBuilderFactory();
	private final ValueBuilderFactory valueBuilderFactory = new ValueBuilderFactory();

	private EntityViewElementBuilderService viewElementBuilderService;
	private EntityViewElementBuilderFactoryHelper builderFactoryHelpers;

	private boolean defaultForceWhitespaceAroundSign;

	/**
	 * Should signs be surrounded by whitespace when creating a default configuration. Default is {@code false}
	 * meaning it will depend on the output locale.
	 *
	 * @param defaultForceWhitespaceAroundSign true to always force whitespace around the sign
	 */
	public void setDefaultForceWhitespaceAroundSign( boolean defaultForceWhitespaceAroundSign ) {
		this.defaultForceWhitespaceAroundSign = defaultForceWhitespaceAroundSign;
	}

	@Override
	public boolean supports( String viewElementType ) {
		return BootstrapUiElements.NUMERIC.equals( viewElementType );
	}

	@Override
	protected ViewElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
	                                                   ViewElementMode viewElementMode, String viewElementType ) {
		if ( ViewElementMode.isControl( viewElementMode ) && propertyDescriptor.isWritable() ) {
			return controlBuilderFactory.createBuilder( propertyDescriptor, viewElementMode, viewElementType );
		}

		return valueBuilderFactory.createBuilder( propertyDescriptor, viewElementMode, viewElementType );
	}

	@Autowired
	public void setViewElementBuilderService( EntityViewElementBuilderService viewElementBuilderService ) {
		this.viewElementBuilderService = viewElementBuilderService;
	}

	@Autowired
	public void setBuilderFactoryHelpers( EntityViewElementBuilderFactoryHelper builderFactoryHelpers ) {
		this.builderFactoryHelpers = builderFactoryHelpers;
	}

	/**
	 * Responsible for creating the value element that also supports the {@link NumericFormElementConfiguration}
	 * that was specified on the control.
	 */
	private class ValueBuilderFactory extends EntityViewElementBuilderFactorySupport<TextViewElementBuilder>
	{
		@Override
		public boolean supports( String viewElementType ) {
			return true;
		}

		@Override
		protected TextViewElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
		                                                       ViewElementMode viewElementMode, String viewElementType ) {
			ViewElementPostProcessor valueTextPostProcessor
					= builderFactoryHelpers.createDefaultValueTextPostProcessor( propertyDescriptor );

			if ( valueTextPostProcessor instanceof ConversionServiceValueTextPostProcessor ) {
				NumericFormElementConfiguration config = null;

				if ( propertyDescriptor.isWritable() ) {
					ViewElementBuilder control = viewElementBuilderService.getElementBuilder(
							propertyDescriptor, ViewElementMode.CONTROL
					);

					if ( control instanceof NumericFormElementBuilder ) {
						config = ( (NumericFormElementBuilder) control ).getConfiguration();
					}
				}

				if ( config == null ) {
					config = controlBuilderFactory.determineBaseConfiguration( propertyDescriptor );
				}

				if ( config != null ) {
					valueTextPostProcessor = new NumericValueTextPostProcessor<>( propertyDescriptor, config );
				}
			}

			return html.builders.text( "" ).postProcessor( valueTextPostProcessor );
		}
	}

	/**
	 * Responsible for creating the control.
	 */
	private class ControlBuilderFactory extends EntityViewElementBuilderFactorySupport<NumericFormElementBuilder>
	{
		@Override
		public boolean supports( String viewElementType ) {
			return true;
		}

		@Override
		protected NumericFormElementBuilder createInitialBuilder( EntityPropertyDescriptor propertyDescriptor,
		                                                          ViewElementMode viewElementMode,
		                                                          String viewElementType ) {
			return bootstrap.builders
					.numeric()
					.name( propertyDescriptor.getName() )
					.controlName( propertyDescriptor.getName() )
					.required( EntityAttributes.isRequired( propertyDescriptor ) )
					.configuration( determineBaseConfiguration( propertyDescriptor ) )
					.postProcessor( EntityViewElementUtils.controlNamePostProcessor( propertyDescriptor ) )
					.postProcessor( new PropertyPlaceholderTextPostProcessor<>() )
					.postProcessor(
							( builderContext, numericFormElement ) ->
							{
								Number propertyValue = EntityViewElementUtils.currentPropertyValue( builderContext, Number.class );

								if ( propertyValue != null ) {
									numericFormElement.setValue( propertyValue );
								}
							}
					);
		}

		public NumericFormElementConfiguration determineBaseConfiguration( EntityPropertyDescriptor descriptor ) {
			NumericFormElementConfiguration configuration = null;

			if ( descriptor.hasAttribute( NumericFormElementConfiguration.class ) ) {
				NumericFormElementConfiguration base = descriptor.getAttribute(
						NumericFormElementConfiguration.class );
				configuration = new NumericFormElementConfiguration( base );
			}
			else if ( descriptor.hasAttribute( Currency.class ) ) {
				Currency currency = descriptor.getAttribute( Currency.class );
				configuration = new NumericFormElementConfiguration( currency );
				configuration.setForceWhitespaceAroundSign( defaultForceWhitespaceAroundSign );
			}
			else if ( descriptor.hasAttribute( Format.class ) ) {
				Format format = descriptor.getAttribute( Format.class );
				configuration = new NumericFormElementConfiguration( format );
				configuration.setForceWhitespaceAroundSign( defaultForceWhitespaceAroundSign );
			}
			else {
				TypeDescriptor typeDescriptor = descriptor.getPropertyTypeDescriptor();

				if ( typeDescriptor != null && typeDescriptor.hasAnnotation( NumberFormat.class ) ) {
					NumberFormat numberFormat = typeDescriptor.getAnnotation( NumberFormat.class );

					if ( StringUtils.isEmpty( numberFormat.pattern() ) ) {
						if ( numberFormat.style() == NumberFormat.Style.CURRENCY ) {
							configuration = new NumericFormElementConfiguration( Format.CURRENCY );
							configuration.setForceWhitespaceAroundSign( defaultForceWhitespaceAroundSign );
						}
						else if ( numberFormat.style() == NumberFormat.Style.PERCENT ) {
							configuration = new NumericFormElementConfiguration( Format.PERCENT );
							configuration.setMultiplier( 100 );
							configuration.setForceWhitespaceAroundSign( defaultForceWhitespaceAroundSign );
						}
					}
				}
			}

			if ( configuration != null ) {
				Class<?> type = ClassUtils.primitiveToWrapper( descriptor.getPropertyType() );

				if ( Integer.class.equals( type ) || Long.class.equals( type ) || Short.class.equals( type )
						|| Byte.class.equals( type ) || AtomicInteger.class.equals( type )
						|| AtomicLong.class.equals( type ) || BigInteger.class.equals( type ) ) {
					configuration.setDecimalPositions( 0 );
				}
			}

			return configuration;
		}
	}
}
