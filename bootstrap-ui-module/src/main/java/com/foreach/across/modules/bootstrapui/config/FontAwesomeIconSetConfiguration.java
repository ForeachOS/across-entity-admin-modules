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

package com.foreach.across.modules.bootstrapui.config;

import com.foreach.across.modules.bootstrapui.elements.icons.IconSetRegistry;
import com.foreach.across.modules.bootstrapui.elements.icons.SimpleIconSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.foreach.across.modules.web.ui.elements.HtmlViewElement.Functions.css;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * Register the default Font Awesome {@link SimpleIconSet} that BootstrapUiModule uses.
 * For now the free solid and brand icons of Font Awesome 5 are implemented.
 *
 * @author Stijn Vanhoof
 * @since 3.0.0
 */
@Configuration
public class FontAwesomeIconSetConfiguration
{
	public static final String FONT_AWESOME_REGULAR_ICON_SET = "fontawesome-regular";
	public static final String FONT_AWESOME_SOLID_ICON_SET = "fontawesome-solid";
	public static final String FONT_AWESOME_BRANDS_ICON_SET = "fontawesome-brands";

	@Autowired
	public void createDefaultIconSets() {
		IconSetRegistry.addIconSet( FONT_AWESOME_REGULAR_ICON_SET, createFontAwesomeIconSet( "far") );
		IconSetRegistry.addIconSet( FONT_AWESOME_SOLID_ICON_SET, createFontAwesomeIconSet( "fas") );
		IconSetRegistry.addIconSet( FONT_AWESOME_BRANDS_ICON_SET, createFontAwesomeIconSet( "fab" ) );
	}

	private SimpleIconSet createFontAwesomeIconSet( String cssPrefix ) {
		SimpleIconSet simpleiconset = new SimpleIconSet();
		simpleiconset.setDefaultIconResolver( ( iconName ) -> html.i( css( cssPrefix + " fa-" + iconName ) ) );
		return simpleiconset;
	}
}