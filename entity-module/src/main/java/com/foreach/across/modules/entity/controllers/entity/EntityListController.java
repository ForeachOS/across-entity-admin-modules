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
package com.foreach.across.modules.entity.controllers.entity;

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.entity.controllers.EntityOverviewController;
import com.foreach.across.modules.entity.controllers.EntityViewRequest;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.views.EntityListView;
import com.foreach.across.modules.entity.views.EntityView;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Lists all entities of a particular type.
 */
@Deprecated
@AdminWebController
@RequestMapping("/old" + EntityListController.PATH)
@SuppressWarnings("unchecked")
public class EntityListController extends EntityControllerSupport
{
	public static final String PATH = EntityOverviewController.PATH + PATH_ENTITY;

	@ModelAttribute(VIEW_REQUEST)
	public Object buildViewRequest(
			@PathVariable(VAR_ENTITY) EntityConfiguration entityConfiguration,
			NativeWebRequest request,
			ModelMap model ) {
		return super.buildViewRequest( entityConfiguration, false, false, null, request, model );
	}

	@Override
	protected void preparePageContent( PageContentStructure page ) {
		page.setRenderAsTabs( false );
	}

	@Override
	protected String getDefaultViewName() {
		return EntityView.LIST_VIEW_NAME;
	}

	@RequestMapping
	public String listAllEntities(
			@ModelAttribute(VIEW_REQUEST) EntityViewRequest viewRequest,
			ModelMap model,
			Pageable pageable
	) {
		model.addAttribute( EntityListView.ATTRIBUTE_PAGEABLE, pageable );

		return viewRequest.createView( model ).getTemplate();
	}

}
