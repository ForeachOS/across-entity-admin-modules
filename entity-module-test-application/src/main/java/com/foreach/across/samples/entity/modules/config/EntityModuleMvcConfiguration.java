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

package com.foreach.across.samples.entity.modules.config;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.web.config.support.PrefixingHandlerMappingConfiguration;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EntityModuleMvcConfiguration extends PrefixingHandlerMappingConfiguration
{
	@Qualifier("adminWebHandlerMapping")
	@Autowired
	PrefixingRequestMappingHandlerMapping x;

	private final ClassFilter entityViewControllerClassFilter = clazz -> new AnnotationClassFilter( EntityViewController.class, true ).matches(
			clazz ) && !new AnnotationClassFilter( AdminWebController.class, true ).matches( clazz );

	@Override
	protected String getPrefixPath() {
		return "";
	}

	@Override
	protected ClassFilter getHandlerMatcher() {
		return entityViewControllerClassFilter;
	}

	@Bean(name = "entityHandlerMapping")
	@Exposed
	@Override
	public PrefixingRequestMappingHandlerMapping controllerHandlerMapping() {
		return super.controllerHandlerMapping();
	}
}
