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
package com.foreach.across.modules.it.properties.definingmodule;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.core.context.configurer.ApplicationContextConfigurer;
import com.foreach.across.core.context.configurer.ComponentScanConfigurer;
import com.foreach.across.modules.it.properties.definingmodule.installers.RevisionPropertiesInstaller;
import com.foreach.across.modules.it.properties.definingmodule.installers.UserPropertiesInstaller;

import java.util.Set;

/**
 * @author Arne Vandamme
 */
public class DefiningModule extends AcrossModule
{
	@Override
	public String getName() {
		return "DefiningModule";
	}

	@Override
	public String getDescription() {
		return "Defines a two custom property sets: one revision based, one not.";
	}

	@Override
	protected void registerDefaultApplicationContextConfigurers( Set<ApplicationContextConfigurer> contextConfigurers ) {
		contextConfigurers.add( new ComponentScanConfigurer( getClass().getPackage().getName() ) );
	}

	@Override
	public Object[] getInstallers() {
		return new Object[] {
				UserPropertiesInstaller.class,
				RevisionPropertiesInstaller.class
		};
	}
}
