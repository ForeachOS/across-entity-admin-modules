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
package com.foreach.across.modules.entity.web;

import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityModel;
import org.apache.commons.lang3.ClassUtils;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Creates links to entity views and standard controllers.
 * By default crud and list views are included.
 */
public class EntityLinkBuilder
{
	private final String rootPath;
	private final EntityConfiguration entityConfiguration;

	private String overviewPath = "{0}/{1}";
	private String createPath = "{0}/{1}/create";
	private String viewPath = "{0}/{1}/{2,number,#}";
	private String updatePath = "{0}/{1}/{2,number,#}/update";
	private String deletePath = "{0}/{1}/{2,number,#}/delete";
	private String associationsPath = "{0}/{1}/{2,number,#}/associations";

	public EntityLinkBuilder( String rootPath, EntityConfiguration entityConfiguration ) {
		this.rootPath = rootPath;
		this.entityConfiguration = entityConfiguration;

		if ( !isNumberIdType( entityConfiguration ) ) {
			viewPath = "{0}/{1}/{2}";
			updatePath = "{0}/{1}/{2}/update";
			deletePath = "{0}/{1}/{2}/delete";
			associationsPath = "{0}/{1}/{2}/associations";
		}
	}

	private boolean isNumberIdType( EntityConfiguration entityConfiguration ) {
		EntityModel entityModel = entityConfiguration.getEntityModel();

		if ( entityModel != null ) {
			return ClassUtils.isAssignable( entityModel.getIdType(), Number.class );
		}

		return false;
	}

	protected String getOverviewPath() {
		return overviewPath;
	}

	protected String getCreatePath() {
		return createPath;
	}

	protected String getViewPath() {
		return viewPath;
	}

	protected String getUpdatePath() {
		return updatePath;
	}

	protected String getDeletePath() {
		return deletePath;
	}

	protected String getAssociationsPath() {
		return associationsPath;
	}

	public void setOverviewPath( String overviewPath ) {
		this.overviewPath = overviewPath;
	}

	public void setCreatePath( String createPath ) {
		this.createPath = createPath;
	}

	public void setViewPath( String viewPath ) {
		this.viewPath = viewPath;
	}

	public void setUpdatePath( String updatePath ) {
		this.updatePath = updatePath;
	}

	public void setDeletePath( String deletePath ) {
		this.deletePath = deletePath;
	}

	public void setAssociationsPath( String associationsPath ) {
		this.associationsPath = associationsPath;
	}

	public String overview() {
		return format( overviewPath );
	}

	public String create() {
		return format( createPath );
	}

	public String update( Object entity ) {
		return format( updatePath, entity );
	}

	public String delete( Object entity ) {
		return format( deletePath, entity );
	}

	public String view( Object entity ) {
		return format( viewPath, entity );
	}

	public String associations( Object entity ) {
		return format( associationsPath, entity );
	}

	private String format( String pattern ) {
		return MessageFormat.format( pattern, rootPath, entityConfiguration.getName(), null );
	}

	@SuppressWarnings("unchecked")
	private String format( String pattern, Object entity ) {
		Serializable id = entityConfiguration.getEntityModel().getId( entity );
		return MessageFormat.format( pattern, rootPath, entityConfiguration.getName(), id );
	}

	/**
	 * Creates a new link builder that represents the current linkbuilder as an association to
	 * a parent entity.
	 */
	public EntityLinkBuilder asAssociationFor( EntityLinkBuilder parent, Object parentEntity ) {
		String associationRootPath = parent.associations( parentEntity );

		EntityLinkBuilder linkBuilder = new EntityLinkBuilder( associationRootPath, entityConfiguration );
		linkBuilder.viewPath = this.viewPath;
		linkBuilder.overviewPath = this.overviewPath;
		linkBuilder.associationsPath = this.associationsPath;
		linkBuilder.createPath = this.createPath;
		linkBuilder.updatePath = this.updatePath;
		linkBuilder.deletePath = this.deletePath;

		return linkBuilder;
	}
}
