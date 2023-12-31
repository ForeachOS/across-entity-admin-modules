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
package com.foreach.across.modules.bootstrapui.elements;

import com.foreach.across.modules.web.ui.elements.HtmlViewElement;

/**
 * Basic support for a typical form input element.  A form control with limited
 * attributes like control name and disabled.
 *
 * @author Arne Vandamme
 */
public interface FormInputElement extends HtmlViewElement
{
	String getControlName();

	FormInputElement setControlName( String controlName );

	boolean isDisabled();

	FormInputElement setDisabled( boolean disabled );
}
