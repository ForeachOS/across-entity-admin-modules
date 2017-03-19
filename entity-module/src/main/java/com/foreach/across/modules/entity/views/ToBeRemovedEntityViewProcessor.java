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

package com.foreach.across.modules.entity.views;

import com.foreach.across.modules.entity.controllers.EntityViewCommand;
import com.foreach.across.modules.entity.web.WebViewCreationContext;
import org.springframework.ui.ModelMap;
import org.springframework.validation.DataBinder;

/**
 * Temporary base class for during refactoring.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Deprecated
public abstract class ToBeRemovedEntityViewProcessor implements EntityViewProcessor
{
	@Override
	public void prepareModelAndCommand( String viewName,
	                                    WebViewCreationContext creationContext,
	                                    EntityViewCommand command,
	                                    ModelMap model ) {

	}

	@Override
	public void prepareDataBinder( String viewName,
	                               WebViewCreationContext creationContext,
	                               EntityViewCommand command,
	                               DataBinder dataBinder ) {

	}

	@Override
	public void preProcess( WebViewCreationContext creationContext, EntityView view ) {

	}

	@Override
	public void postProcess( WebViewCreationContext creationContext, EntityView view ) {

	}
}
