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

package com.foreach.across.modules.entity.controllers.admin;

import com.foreach.across.modules.adminweb.annotations.AdminWebController;
import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.entity.controllers.EntityControllerAttributes;
import com.foreach.across.modules.entity.views.EntityFormView;
import com.foreach.across.modules.entity.views.EntityListView;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.EntityViewFactory;
import com.foreach.across.modules.entity.views.context.ConfigurableEntityViewContext;
import com.foreach.across.modules.entity.views.context.EntityViewContextLoader;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewCommandValidator;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.context.WebAppPathResolver;
import com.foreach.across.modules.web.template.WebTemplateInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.Optional;

import static com.foreach.across.modules.entity.controllers.EntityControllerAttributes.PATH_ENTITY;

/**
 * Generic controller for building entity views of non-associated entities.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Controller
@AdminWebController
@RequestMapping("/new-entities" + PATH_ENTITY)
public class GenericEntityViewController implements EntityControllerAttributes
{
	private ConfigurableEntityViewContext entityViewContext;
	private EntityViewRequest entityViewRequest;
	private PageContentStructure pageContentStructure;
	private EntityViewContextLoader entityViewContextLoader;
	private EntityViewCommandValidator entityViewCommandValidator;
	private WebAppPathResolver webAppPathResolver;

	/**
	 * Responsible for building the initial {@link com.foreach.across.modules.entity.views.context.EntityViewContext}
	 * and {@link EntityViewRequest}.
	 */
	@ModelAttribute
	public void createEntityViewRequest(
			@PathVariable(VAR_ENTITY) String entityName,
			@PathVariable(value = VAR_ENTITY_ID, required = false) Serializable entityId,
			@PathVariable(value = "action", required = false) String action,
			HttpMethod httpMethod,
			NativeWebRequest webRequest,
			ModelMap model,
			RedirectAttributes redirectAttributes
	) {
		buildEntityViewContext( entityName, entityId );

		// add the basic properties
		String viewName = resolveViewName( webRequest, entityId, action );

		entityViewRequest.setModel( model );
		entityViewRequest.setRedirectAttributes( redirectAttributes );
		entityViewRequest.setPageContentStructure( pageContentStructure );
		entityViewRequest.setEntityViewContext( entityViewContext );
		entityViewRequest.setWebRequest( webRequest );
		entityViewRequest.setHttpMethod( httpMethod );
		entityViewRequest.setViewName( viewName );
		entityViewRequest.setCommand( new EntityViewCommand() );
		entityViewRequest.setPartialFragment(
				StringUtils.defaultIfBlank( webRequest.getParameter( WebTemplateInterceptor.PARTIAL_PARAMETER ), null )
		);

		// retrieve and set the view factory
		EntityViewFactory viewFactory = entityViewContext.getEntityConfiguration()
		                                                 .getViewFactory( viewName );
		if ( viewFactory == null ) {
			throw new IllegalStateException( "No registered EntityViewFactory with name: " + viewName );
		}

		entityViewRequest.setViewFactory( viewFactory );

		viewFactory.prepareEntityViewContext( entityViewContext );

		viewFactory.validateRequest( entityViewRequest );

		model.addAttribute( "entityViewRequest", entityViewRequest );
		model.addAttribute( "entityViewCommand", entityViewRequest.getCommand() );
		model.addAttribute( "entityViewContext", entityViewContext );
	}

	@InitBinder("entityViewCommand")
	public void initViewCommandBinder( WebDataBinder dataBinder, HttpMethod httpMethod ) {
		dataBinder.setMessageCodesResolver( entityViewContext.getMessageCodeResolver() );

		// by default register command validation for post and put requests
		if ( httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT ) {
			dataBinder.setValidator( entityViewCommandValidator );
		}

		EntityViewFactory viewFactory = entityViewRequest.getViewFactory();
		viewFactory.initializeCommandObject( entityViewRequest, entityViewRequest.getCommand(), dataBinder );
	}

	@RequestMapping(value = { "", PATH_ENTITY_ID, PATH_ENTITY_ID + "/{action:delete|update}" })
	public Object executeView(
			@PathVariable(value = VAR_ENTITY_ID, required = false) Serializable entityId,
			@PathVariable(value = "action", required = false) String action,
			@ModelAttribute("entityViewCommand") @Valid EntityViewCommand command,
			BindingResult bindingResult
	) {
		entityViewRequest.setBindingResult( bindingResult );

		EntityView entityView = entityViewRequest.getViewFactory().createView( entityViewRequest );

		if ( entityView.isRedirect() ) {
			return webAppPathResolver.redirect( entityView.getRedirectUrl() );
		}
		else if ( entityView.isCustomView() ) {
			return entityView.getCustomView();
		}

		return StringUtils.defaultString( entityView.getTemplate(), PageContentStructure.TEMPLATE );
	}

	@SuppressWarnings("unchecked")
	private void buildEntityViewContext( @PathVariable(VAR_ENTITY) String entityName,
	                                     @PathVariable(value = VAR_ENTITY_ID, required = false) Serializable entityId ) {
		entityViewContextLoader.loadForEntityConfiguration( entityViewContext, entityName );

		if ( isPossibleEntityId( entityId ) ) {
			entityViewContext.setEntity( entityViewContext.getEntityModel().findOne( entityId ) );
		}
	}

	private boolean isPossibleEntityId( Serializable candidate ) {
		return candidate != null && !"create".equals( candidate );
	}

	private String resolveViewName( NativeWebRequest webRequest, Serializable entityId, String action ) {
		return Optional
				.ofNullable( webRequest.getParameter( "view" ) )
				.orElseGet( () -> {
					if ( entityViewContext.holdsEntity() ) {
						if ( "delete".equals( action ) ) {
							return EntityFormView.DELETE_VIEW_NAME;
						}
						else if ( "update".equals( action ) ) {
							return EntityFormView.UPDATE_VIEW_NAME;
						}

						return "view";
					}

					return "create".equals( entityId )
							? EntityFormView.CREATE_VIEW_NAME : EntityListView.VIEW_NAME;
				} );
	}

	@Autowired
	void setEntityViewContextLoader( EntityViewContextLoader entityViewContextLoader ) {
		this.entityViewContextLoader = entityViewContextLoader;
	}

	@Autowired
	void setEntityViewContext( ConfigurableEntityViewContext entityViewContext ) {
		this.entityViewContext = entityViewContext;
	}

	@Autowired
	void setEntityViewRequest( EntityViewRequest entityViewRequest ) {
		this.entityViewRequest = entityViewRequest;
	}

	@Autowired
	void setPageContentStructure( PageContentStructure pageContentStructure ) {
		this.pageContentStructure = pageContentStructure;
	}

	@Autowired
	void setEntityViewCommandValidator( EntityViewCommandValidator entityViewCommandValidator ) {
		this.entityViewCommandValidator = entityViewCommandValidator;
	}

	@Autowired
	void setWebAppPathResolver( WebAppPathResolver webAppPathResolver ) {
		this.webAppPathResolver = webAppPathResolver;
	}
}
