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

import com.foreach.across.modules.bootstrapui.elements.RadioFormElement;
import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.ViewElement;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;
import org.thymeleaf.dom.Node;

import java.util.Collections;
import java.util.List;

/**
 * @author Arne Vandamme
 */
public class RadioFormElementThymeleafBuilder extends FormControlElementThymleafSupport<RadioFormElement>
{
	@Override
	public List<Node> buildNodes( RadioFormElement control,
	                              Arguments arguments,
	                              ViewElementNodeFactory viewElementNodeFactory ) {
		boolean showLabel = control.getText() != null || !control.isEmpty();

		Element label = createElement( "label" );
		Element radio = createElement( "input" );
		radio.setAttribute( "type", "radio" );

		String radioHtmlId = retrieveHtmlId( arguments, control );
		attribute( radio, "id", radioHtmlId );
		attribute( label, "for", radioHtmlId );
		attribute( radio, "value", control.getValue(), viewElementNodeFactory );
		attribute( radio, "checked", control.isChecked() );
		applyProperties( control, arguments, radio );

		if ( showLabel ) {
			label.addChild( radio );

			if ( control.getText() != null ) {
				text( label, control.getText() );
			}

			for ( ViewElement child : control ) {
				for ( Node childNode : viewElementNodeFactory.buildNodes( child, arguments ) ) {
					label.addChild( childNode );
				}
			}
		}

		if ( control.isWrapped() ) {
			Element wrapper = createElement( "div" );
			wrapper.setAttribute( "class", "radio" );

			if ( control.isDisabled() ) {
				wrapper.setAttribute( "class", wrapper.getAttributeValue( "class" ) + " disabled" );
			}

			if ( showLabel ) {
				wrapper.addChild( label );
			}
			else {
				wrapper.addChild( radio );
			}

			return Collections.singletonList( wrapper );
		}

		return Collections.singletonList( showLabel ? label : radio );
	}

	@Override
	protected Element createNode( RadioFormElement control,
	                              Arguments arguments,
	                              ViewElementNodeFactory viewElementNodeFactory ) {
		return null;
	}

}
