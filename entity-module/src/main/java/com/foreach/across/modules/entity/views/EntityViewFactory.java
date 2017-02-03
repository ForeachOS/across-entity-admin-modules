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
import com.foreach.across.modules.entity.views.context.ConfigurableEntityViewContext;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import org.springframework.ui.ModelMap;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;

/**
 * Central API for building custom entity views using the generic controllers.
 *
 * @author Arne Vandamme
 */
public interface EntityViewFactory<T extends ViewCreationContext>
{
	@Deprecated
	void prepareModelAndCommand( String viewName,
	                             T creationContext,
	                             EntityViewCommand command,
	                             ModelMap model );

	@Deprecated
	void prepareDataBinder( String viewName, T creationContext, EntityViewCommand viewRequest, DataBinder dataBinder );

	@Deprecated
	EntityView create( String viewName, T creationContext, ModelMap model );

	/**
	 * Apply possible factory modifications to the {@link ConfigurableEntityViewContext}.
	 * Call this method before creating an {@link com.foreach.across.modules.entity.views.request.EntityViewRequest}
	 * that uses the context.
	 * <p/>
	 * This method is called first, before {@link #validateRequest(EntityViewRequest)} in the default view rendering.
	 *
	 * @param entityViewContext to modify
	 */
	void prepareEntityViewContext( ConfigurableEntityViewContext entityViewContext );

	/**
	 * Verify the {@link EntityViewRequest} is valid for this factory.
	 * This usually means things like performing security checks and checking all individual properties are valid.
	 * <p/>
	 * Any invalid requests are expected to throw the most relevant exceptions.
	 * <p/>
	 * Called after {@link #prepareEntityViewContext(ConfigurableEntityViewContext)} but before initializing the
	 * command object.
	 *
	 * @param entityViewRequest request to validate
	 */
	void validateRequest( EntityViewRequest entityViewRequest );

	/**
	 * Initialize the {@link com.foreach.across.modules.entity.views.request.EntityViewCommand} for the given request.
	 * Optionally configure the {@link WebDataBinder} that will be used to bind the command object.
	 *
	 * @param entityViewRequest request
	 * @param command           object
	 * @param dataBinder        to bind the web request to the command
	 */
	void initializeCommandObject( EntityViewRequest entityViewRequest,
	                              com.foreach.across.modules.entity.views.request.EntityViewCommand command,
	                              WebDataBinder dataBinder );

	/**
	 * Create the {@link EntityView} for the given request.
	 * This will execute the controller logic and will build all view elements required.
	 * <p/>
	 * This method requires a valid request and a fully initialized/bound command object.
	 *
	 * @param entityViewRequest request
	 * @return view created
	 */
	EntityView createView( EntityViewRequest entityViewRequest );
}
