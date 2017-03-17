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

import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.entity.controllers.EntityControllerAttributes;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BindingResultUtils;

/**
 * @author Arne Vandamme
 */
@Deprecated
public class EntityFormView extends EntityView
{
	// todo: remove
	public static final String VIEW_TEMPLATE = PageContentStructure.TEMPLATE;

	// Will contain the original (unmodified) entity for which the form is being rendered
	public static final String ATTRIBUTE_ORIGINAL_ENTITY = "originalEntity";

	public EntityFormView( ModelMap model ) {
		super( model );
	}

	/**
	 * @return The original entity in case of an update form.
	 */
	public Object getOriginalEntity() {
		return getAttribute( ATTRIBUTE_ORIGINAL_ENTITY );
	}

	public void setOriginalEntity( Object entity ) {
		addAttribute( ATTRIBUTE_ORIGINAL_ENTITY, entity );
	}

	public boolean isUpdate() {
		Object original = getOriginalEntity();
		return original != null;
	}

	/**
	 * @return the binding result for the view request
	 */
	public BindingResult getBindingResult() {
		return BindingResultUtils.getBindingResult( asMap(), EntityControllerAttributes.VIEW_REQUEST );
	}
}
