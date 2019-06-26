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
package com.foreach.across.modules.bootstrapui.resource;

import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourcePackage;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import lombok.RequiredArgsConstructor;

import static com.foreach.across.modules.web.resource.WebResource.css;
import static com.foreach.across.modules.web.resource.WebResource.javascript;
import static com.foreach.across.modules.web.resource.WebResourceRule.add;
import static com.foreach.across.modules.web.resource.WebResourceRule.addPackage;

/**
 * Responsible for adding the basic bootstrap css and javascript classes.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@RequiredArgsConstructor
public class BootstrapUiWebResources implements WebResourcePackage
{
	public static final String NAME = "bootstrap";

	private static final String BOOTSTRAP_VERSION = "3.3.7";

	private final boolean minified;

	@Override
	public void install( WebResourceRegistry registry ) {
		registry.apply(
				addPackage( JQueryWebResources.NAME ),

				// Bootstrap CSS & Javascript
				add( css( "@webjars:/bootstrap/" + BOOTSTRAP_VERSION + "/css/bootstrap" + ( minified ? ".min" : "" ) + ".css" ) )
						.withKey( NAME )
						.toBucket( WebResource.CSS ),
				add( javascript( "@webjars:/bootstrap/" + BOOTSTRAP_VERSION + "/js/bootstrap" + ( minified ? ".min" : "" ) + ".js" ) )
						.withKey( NAME )
						.toBucket( WebResource.JAVASCRIPT_PAGE_END ),

				// BootstrapUiModule main javascript
				add( javascript( "@static:/" + BootstrapUiModule.NAME + "/js/bootstrapui.js" ) )
						.withKey( BootstrapUiModule.NAME )
						.toBucket( WebResource.JAVASCRIPT_PAGE_END )
		);
	}
}
