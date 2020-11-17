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

package com.foreach.across.samples.bootstrapui.application.controllers.components;

import com.foreach.across.modules.bootstrapui.elements.FormLayout;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;

@Controller
@RequestMapping("/components/forms/formHorizontal")
public class HorizontalForms extends Forms
{
	@Override
	protected void menuItems( PathBasedMenuBuilder menu ) {
		menu.item( "/components/forms/formHorizontal", "Horizontal form" );
	}

	@GetMapping
	String renderForm( @ModelAttribute FormDto formDto, Model model ) {
		return render( panel( "Simple form", buildForm( formDto ).formLayout( FormLayout.horizontal( 3 ) ).with( css.form.horizontal.label.small ) ) );
	}

}