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

package com.foreach.across.modules.entity.views.context;

import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityModel;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyRegistry;
import com.foreach.across.modules.entity.support.EntityMessageCodeResolver;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.web.links.EntityViewLinkBuilder;
import com.foreach.across.modules.spring.security.actions.AllowableActions;

/**
 * Extends {@link EntityViewContext} with settable properties.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public interface ConfigurableEntityViewContext extends EntityViewContext
{
	void setEntity( Object entity );

	void setEntityConfiguration( EntityConfiguration entityConfiguration );

	void setEntityModel( EntityModel entityModel );

	void setLinkBuilder( EntityViewLinkBuilder linkBuilder );

	void setMessageCodeResolver( EntityMessageCodeResolver codeResolver );

	void setEntityMessages( EntityMessages entityMessages );

	void setEntityAssociation( EntityAssociation association );

	void setPropertyRegistry( EntityPropertyRegistry propertyRegistry );

	void setParentContext( EntityViewContext parentContext );

	void setAllowableActions( AllowableActions allowableAcations );
}
