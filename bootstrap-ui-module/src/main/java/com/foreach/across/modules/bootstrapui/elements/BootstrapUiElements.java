/*
 * Copyright 2019 the original author or authors
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

import com.foreach.across.modules.bootstrapui.styles.BootstrapStyles;
import com.foreach.across.modules.web.ui.StandardViewElements;
import com.foreach.across.modules.web.ui.ViewElement;

/**
 * @author Arne Vandamme
 * @see com.foreach.across.modules.bootstrapui.attributes.BootstrapAttributes
 * @see BootstrapStyles
 *
 */
public interface BootstrapUiElements extends StandardViewElements
{
	String HIDDEN = "bootstrapHidden";

	@Deprecated
	String ICON = "bootstrapIcon";

	String BUTTON = "bootstrapButton";
	String TEXTBOX = "bootstrapTextbox";
	String TEXTAREA = "bootstrapTextarea";
	String FORM = "bootstrapForm";
	String CHECKBOX = "bootstrapCheckbox";
	String RADIO = "bootstrapRadio";
	String TOGGLE = "bootstrapToggleSwitch";
	String SELECT = "bootstrapSelect";
	String MULTI_CHECKBOX = "bootstrapMultiCheckbox";
	String STATIC_CONTROL = "bootstrapStaticControl";
	String LABEL = "bootstrapLabel";
	String FORM_GROUP = "bootstrapFormGroup";
	String TABLE = "bootstrapTable";
	String COLUMN = "bootstrapColumn";
	String FIELDSET = "bootstrapFieldset";
	String DATETIME = "bootstrapDateTime";
	String NUMERIC = "bootstrapNumeric";
	String LINK = "bootstrapLink";
	String FILE_UPLOAD = "bootstrapFileUpload";
	String AUTOSUGGEST = "bootstrapAutoSuggest";
	String GENERIC_FORM_CONTROL = "bootstrapGenericFormControl";
}
