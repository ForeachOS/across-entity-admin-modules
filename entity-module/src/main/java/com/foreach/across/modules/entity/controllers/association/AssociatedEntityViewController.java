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
package com.foreach.across.modules.entity.controllers.association;

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.entity.controllers.EntityViewRequest;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

import java.io.Serializable;

/**
 * Renders a view of an entity.
 *
 * @author Arne Vandamme
 */
@AdminWebController
@RequestMapping(AssociatedEntityViewController.PATH)
public class AssociatedEntityViewController extends AssociatedEntityControllerSupport
{
	public static final String PATH = AssociatedEntityListController.PATH + PATH_ASSOCIATION_ID;

	@ModelAttribute(VIEW_REQUEST)
	public Object buildViewRequest(
			@PathVariable(VAR_ENTITY) EntityConfiguration entityConfiguration,
			@PathVariable(VAR_ENTITY_ID) Serializable entityId,
			@PathVariable(VAR_ASSOCIATION) String associationName,
			@PathVariable(VAR_ASSOCIATION_ID) Serializable associatedEntityId,
			NativeWebRequest request,
			ModelMap model ) {
		return super.buildViewRequest(
				entityConfiguration, entityId, associationName, true, false, associatedEntityId, request,
				model
		);
	}

	@Override
	protected String getDefaultViewName() {
		// todo: default entity view
		return "entityView";
	}

	@RequestMapping
	public String renderEntityView(
			@ModelAttribute(VIEW_REQUEST) EntityViewRequest viewRequest,
			ModelMap model
	) {
		return viewRequest.createView( model ).getTemplate();
	}
}
