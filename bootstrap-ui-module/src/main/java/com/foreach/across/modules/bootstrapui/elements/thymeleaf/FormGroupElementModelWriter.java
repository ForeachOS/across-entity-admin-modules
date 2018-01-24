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
package com.foreach.across.modules.bootstrapui.elements.thymeleaf;

import com.foreach.across.modules.bootstrapui.elements.*;
import com.foreach.across.modules.bootstrapui.utils.BootstrapElementUtils;
import com.foreach.across.modules.web.thymeleaf.ThymeleafModelBuilder;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.thymeleaf.AbstractHtmlViewElementModelWriter;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.*;
import org.thymeleaf.spring4.expression.Fields;
import org.thymeleaf.spring4.expression.SpringStandardExpressionObjectFactory;
import org.thymeleaf.spring4.naming.SpringContextVariableNames;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import static com.foreach.across.modules.bootstrapui.elements.thymeleaf.FormViewElementModelWriter.VAR_CURRENT_BOOTSTRAP_FORM;

/**
 * The FormGroupElementModelWriter is core to default Bootstrap based form rendering.
 * It supports a combination of control, label and optional help text.
 * It will automatically set attributes based on the presence of the different elements and
 * an optional form layout.  For most cases this will work perfectly but if you want total control
 * of the generated markup, you might want to create the form group markup manually.
 *
 * @author Arne Vandamme
 * @since 1.0.0
 */
public class FormGroupElementModelWriter extends AbstractHtmlViewElementModelWriter<FormGroupElement>
{
	private final Collection<String> CONTROL_ELEMENTS = Arrays.asList( "input", "textarea", "select" );

	@Override
	public void writeModel( FormGroupElement group, ThymeleafModelBuilder model ) {
		super.writeOpenElement( group, model );

		model.addAttributeValue( "class", "form-group" );

		if ( group.isRequired() ) {
			model.addAttributeValue( "class", "required" );
		}

		if ( isCheckboxGroup( group ) ) {
			model.addAttributeValue( "class", "checkbox" );
		}
		else if ( isRadioGroup( group ) ) {
			model.addAttributeValue( "class", "radio" );
		}

		FormLayout layout = determineFormLayout( group, model );

		Consumer<ThymeleafModelBuilder> errorBuilder = null;

		ViewElement control = group.getControl();
		FormControlElement formControl = BootstrapElementUtils.getFormControl( group );
		IModel controlModel = control != null ? model.createViewElementModel( group.getControl() ) : null;

		ViewElement helpBlock = group.getHelpBlock();
		IModel helpBlockModel = helpBlock != null ? model.createViewElementModel( helpBlock ) : null;

		ViewElement label = group.getLabel();
		IModel labelModel = label != null ? model.createViewElementModel( group.getLabel() ) : null;

		if ( labelModel != null && group.isRequired() ) {
			addRequiredIndicatorToLabel( model.getModelFactory(), labelModel );
		}

		if ( formControl != null ) {
			String controlId = model.retrieveHtmlId( formControl );

			if ( helpBlock != null && !formControl.hasAttribute( "aria-describedby" ) ) {
				String helpId = setOrRetrieveHelpBlockId( model.getModelFactory(), helpBlockModel, controlId );
				setDescribedByAttributeOnControl( model.getModelFactory(), controlModel, helpId );
			}

			if ( layout.getType() == FormLayout.Type.INLINE && !layout.isShowLabels() ) {
				String labelText = label instanceof LabelFormElement ? ( (LabelFormElement) label ).getText() : null;

				if ( labelText != null ) {
					setPlaceholderAttributeOnControl( model.getModelFactory(), controlModel, labelText );
				}
			}

			errorBuilder = group.isDetectFieldErrors()
					? createFieldErrorsBuilder( formControl.getControlName(), model.getTemplateContext() )
					: null;
		}

		if ( helpBlockModel != null && layout.getType() == FormLayout.Type.INLINE ) {
			makeHelpBlockScreenReaderOnly( model.getModelFactory(), helpBlockModel );
		}

		if ( errorBuilder != null ) {
			model.addAttributeValue( "class", "has-error" );
		}

		if ( labelModel != null ) {
			addFormLayoutToLabel( model.getModelFactory(), labelModel, layout );
			model.addModel( labelModel );
		}

		// open wrapper
		if ( layout.getType() == FormLayout.Type.HORIZONTAL ) {
			model.addOpenElement( "div" );
			model.addAttributeValue( "class", layout.getGrid().get( 1 ).toString() );

			if ( labelModel == null ) {
				model.addAttributeValue( "class", layout.getGrid().get( 0 ).asOffset().toString() );
			}
		}

		if ( helpBlockModel != null && group.isRenderHelpBlockBeforeControl() ) {
			model.addModel( helpBlockModel );
		}

		if ( controlModel != null ) {
			model.addModel( controlModel );
		}

		if ( helpBlockModel != null && !group.isRenderHelpBlockBeforeControl() ) {
			model.addModel( helpBlockModel );
		}

		writeChildren( group, model );

		if ( errorBuilder != null ) {
			errorBuilder.accept( model );
		}

		// close wrapper
		if ( layout.getType() == FormLayout.Type.HORIZONTAL ) {
			model.addCloseElement();
		}

		writeCloseElement( group, model );
	}

	private FormLayout determineFormLayout( FormGroupElement group, ThymeleafModelBuilder model ) {
		FormLayout layout = group.getFormLayout();

		if ( layout == null ) {
			FormViewElement form = (FormViewElement) model.getTemplateContext().getVariable( VAR_CURRENT_BOOTSTRAP_FORM );

			if ( form != null ) {
				layout = form.getFormLayout();
			}

			if ( layout == null ) {
				layout = FormLayout.normal();
			}
		}

		if ( layout.getType() == FormLayout.Type.HORIZONTAL ) {
			if ( layout.getGrid().size() != 2 ) {
				throw new IllegalStateException( "Horizontal form requires a grid layout of 2 positions." );
			}
		}
		return layout;
	}

	/**
	 * First element will get sr-only class added.
	 */
	private void makeHelpBlockScreenReaderOnly( IModelFactory modelFactory, IModel helpBlockModel ) {
		for ( int i = 0; i < helpBlockModel.size(); i++ ) {
			ITemplateEvent event = helpBlockModel.get( i );
			if ( event instanceof IOpenElementTag ) {
				IOpenElementTag elementTag = (IOpenElementTag) event;

				String currentClass = StringUtils.defaultString( elementTag.getAttributeValue( "class" ) );
				String newClass = StringUtils.strip( currentClass + " sr-only" );
				helpBlockModel.replace( i, modelFactory.setAttribute( elementTag, "class", newClass ) );

				return;
			}
		}
	}

	/**
	 * Add layout class to first element of the label model.
	 */
	private void addFormLayoutToLabel( IModelFactory modelFactory, IModel labelModel, FormLayout layout ) {
		for ( int i = 0; i < labelModel.size(); i++ ) {
			ITemplateEvent event = labelModel.get( i );
			if ( event instanceof IOpenElementTag ) {
				IOpenElementTag elementTag = (IOpenElementTag) event;
				if ( layout.getType() == FormLayout.Type.HORIZONTAL ) {
					String currentClass = StringUtils.defaultString( elementTag.getAttributeValue( "class" ) );
					String newClass = StringUtils.strip( currentClass + " " + layout.getGrid().get( 0 ).toString() );
					labelModel.replace( i, modelFactory.setAttribute( elementTag, "class", newClass ) );
				}
				else if ( layout.getType() == FormLayout.Type.INLINE && !layout.isShowLabels() ) {
					String currentClass = StringUtils.defaultString( elementTag.getAttributeValue( "class" ) );
					String newClass = StringUtils.strip( currentClass + " sr-only" );
					labelModel.replace( i, modelFactory.setAttribute( elementTag, "class", newClass ) );
				}
				return;
			}
		}
	}

	/**
	 * We add the indicator before the first &lt;/label&gt; tag.
	 */
	private void addRequiredIndicatorToLabel( IModelFactory modelFactory, IModel labelModel ) {
		for ( int i = 0; i < labelModel.size(); i++ ) {
			ITemplateEvent event = labelModel.get( i );
			if ( event instanceof ICloseElementTag ) {
				ICloseElementTag closeElementTag = (ICloseElementTag) event;

				if ( "label".equalsIgnoreCase( closeElementTag.getElementCompleteName() ) ) {
					labelModel.insert( i, modelFactory.createOpenElementTag( "sup", "class", "required" ) );
					labelModel.insert( i + 1, modelFactory.createText( "*" ) );
					labelModel.insert( i + 2, modelFactory.createCloseElementTag( "sup" ) );
					return;
				}
			}
		}
	}

	/**
	 * Find the very first control (input, select or textarea) and set the aria-describedby attribute.
	 */
	private void setDescribedByAttributeOnControl( IModelFactory modelFactory, IModel controlModel, String helpId ) {
		for ( int i = 0; i < controlModel.size(); i++ ) {
			ITemplateEvent event = controlModel.get( i );
			if ( event instanceof IOpenElementTag ) {
				IOpenElementTag elementTag = (IOpenElementTag) event;

				if ( CONTROL_ELEMENTS.contains( StringUtils.lowerCase( elementTag.getElementCompleteName() ) ) ) {
					controlModel.replace( i, modelFactory.setAttribute( elementTag, "aria-describedby", helpId ) );
					return;
				}
			}
		}
	}

	private void setPlaceholderAttributeOnControl( IModelFactory modelFactory,
	                                               IModel controlModel,
	                                               String placeholder ) {
		for ( int i = 0; i < controlModel.size(); i++ ) {
			ITemplateEvent event = controlModel.get( i );
			if ( event instanceof IOpenElementTag ) {
				IOpenElementTag elementTag = (IOpenElementTag) event;

				if ( CONTROL_ELEMENTS.contains( StringUtils.lowerCase( elementTag.getElementCompleteName() ) ) ) {
					if ( !"select".equals( StringUtils.lowerCase( elementTag.getElementCompleteName() ) )
							&& !elementTag.hasAttribute( "placeholder" ) ) {
						controlModel.replace( i, modelFactory.setAttribute( elementTag, "placeholder", placeholder ) );
					}

					return;
				}
			}
		}
	}

	/**
	 * We assume the first open element to be the help block.  If an id is set on the first open element,
	 * we use that one.  Else we set one on.
	 */
	private String setOrRetrieveHelpBlockId( IModelFactory modelFactory, IModel helpBlockModel, String controlId ) {
		for ( int i = 0; i < helpBlockModel.size(); i++ ) {
			ITemplateEvent event = helpBlockModel.get( i );
			if ( event instanceof IOpenElementTag ) {
				IOpenElementTag elementTag = (IOpenElementTag) event;

				if ( elementTag.hasAttribute( "id" ) ) {
					return elementTag.getAttributeValue( "id" );
				}
				else {
					String helpId = controlId + ".help";
					helpBlockModel.replace( i, modelFactory.setAttribute( elementTag, "id", helpId ) );
					return helpId;
				}
			}
		}

		return null;
	}

	private Consumer<ThymeleafModelBuilder> createFieldErrorsBuilder( String controlName,
	                                                                  ITemplateContext templateContext ) {
		if ( controlName != null
				&& templateContext.containsVariable( SpringContextVariableNames.SPRING_BOUND_OBJECT_EXPRESSION ) ) {
			Fields fields = (Fields) templateContext.getExpressionObjects().getObject(
					SpringStandardExpressionObjectFactory.FIELDS_EXPRESSION_OBJECT_NAME
			);

			String propertyName = StringUtils.startsWith( controlName, "_" )
					? StringUtils.substring( controlName, 1 )
					: controlName;

			if ( fields != null && fields.hasErrors( propertyName ) ) {
				return model -> {
					model.addOpenElement( "div" );
					model.addAttributeValue( "class", "small", "text-danger" );
					model.addHtml( "" + StringUtils.join( fields.errors( propertyName ), " " ) );
					model.addCloseElement();
				};
			}
		}

		return null;
	}

	private boolean isCheckboxGroup( FormGroupElement group ) {
		return group.getControl() instanceof CheckboxFormElement;
	}

	private boolean isRadioGroup( FormGroupElement group ) {
		return group.getControl() instanceof RadioFormElement;
	}
}
