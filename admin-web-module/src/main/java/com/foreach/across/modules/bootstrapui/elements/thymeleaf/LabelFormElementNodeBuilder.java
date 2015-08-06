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

import com.foreach.across.modules.bootstrapui.elements.LabelFormElement;
import com.foreach.across.modules.bootstrapui.utils.BootstrapElementUtils;
import com.foreach.across.modules.web.thymeleaf.HtmlIdStore;
import com.foreach.across.modules.web.thymeleaf.ViewElementNodeFactory;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.elements.thymeleaf.HtmlViewElementThymeleafSupport;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Element;

/**
 * @author Arne Vandamme
 */
public class LabelFormElementNodeBuilder extends HtmlViewElementThymeleafSupport<LabelFormElement>
{
	@Override
	protected Element createNode( LabelFormElement control,
	                              Arguments arguments,
	                              ViewElementNodeFactory viewElementNodeFactory ) {
		Element element = new Element( "label" );
		element.setAttribute( "class", "control-label" );

		attribute( element, "for", determineTargetId( control, arguments ) );
		text( element, control.getText() );

		return element;
	}

	private String determineTargetId( LabelFormElement label, Arguments arguments ) {
		if ( label.hasTarget() ) {
			if ( label.isTargetId() ) {
				return label.getTargetAsId();
			}

			ViewElement target = BootstrapElementUtils.getFormControl( label.getTargetAsElement() );

			if ( target == null ) {
				target = label.getTargetAsElement();
			}

			return HtmlIdStore.fetch( arguments ).retrieveHtmlId( target );
		}

		return null;
	}
}
