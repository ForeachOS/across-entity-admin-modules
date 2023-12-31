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
package com.foreach.across.modules.entity;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.annotations.AcrossDepends;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.bootstrapui.BootstrapUiModule;
import com.foreach.across.modules.web.AcrossWebModule;

import java.util.Set;

@AcrossDepends(
		required = { AcrossWebModule.NAME },
		optional = { AdminWebModule.NAME, BootstrapUiModule.NAME }
)
public class EntityModule extends AcrossModule
{
	public static final String VALIDATOR = "entityValidator";

	public static final String NAME = "EntityModule";
	public static final String RESOURCES = "entity";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Provide entity management functionality.";
	}

	@Override
	public String getResourcesKey() {
		return RESOURCES;
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( ComponentScanConfigurer.forAcrossModule( EntityModule.class ) );
	}
}
