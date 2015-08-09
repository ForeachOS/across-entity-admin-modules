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
package com.foreach.across.modules.bootstrapui.config;

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactoryImpl;
import com.foreach.across.modules.bootstrapui.resource.BootstrapUiWebResourcePackage;
import com.foreach.across.modules.bootstrapui.resource.DateTimePickerWebResourcePackage;
import com.foreach.across.modules.bootstrapui.resource.JQueryWebResourcePackage;
import com.foreach.across.modules.web.resource.WebResourcePackageManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * @author Arne Vandamme
 */
@Configuration
public class BootstrapUiConfiguration
{
	@RefreshableCollection
	private Collection<WebResourcePackageManager> webResourcePackageManagers;

	@PostRefresh
	protected void registerWebResourcePackages() {
		for ( WebResourcePackageManager packageManager : webResourcePackageManagers ) {
			packageManager.register( DateTimePickerWebResourcePackage.NAME, new DateTimePickerWebResourcePackage() );
			packageManager.register( JQueryWebResourcePackage.NAME, new JQueryWebResourcePackage( true ) );
			packageManager.register( BootstrapUiWebResourcePackage.NAME, new BootstrapUiWebResourcePackage() );
		}
	}

	@Bean
	public BootstrapUiFactory bootstrapUiFactory() {
		return new BootstrapUiFactoryImpl();
	}
}
