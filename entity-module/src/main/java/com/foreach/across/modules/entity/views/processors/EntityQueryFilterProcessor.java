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

package com.foreach.across.modules.entity.views.processors;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.bootstrapui.elements.BootstrapUiFactory;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.entity.query.*;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.WebDataBinder;

import java.util.Optional;

import static com.foreach.across.modules.entity.views.DefaultEntityViewFactory.ATTRIBUTE_CONTAINER_ELEMENT;
import static com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils.find;

/**
 * Processor and page executor for {@link com.foreach.across.modules.entity.query.EntityQuery} based approach.
 * For this filter to work, the class must have a valid {@link com.foreach.across.modules.entity.query.EntityQueryExecutor}
 * and {@link com.foreach.across.modules.entity.query.EntityQueryParser}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Component
@Exposed
@Scope("prototype")
public class EntityQueryFilterProcessor extends AbstractEntityFetchingViewProcessor
{
	/**
	 * Can hold an optional EQL statement that should be applied to the query being executed.
	 */
	public static final String EQL_PREDICATE_ATTRIBUTE_NAME = "_entityQueryPredicate";

	private static final String PARAM = "eqFilter";

	private BootstrapUiFactory bootstrapUi;

	/**
	 * The basic filter that should always be applied.
	 * These conditions can never be removed and will always be appended.
	 */
	@Setter
	private String baseEqlPredicate;

	@Override
	public void initializeCommandObject( com.foreach.across.modules.entity.views.request.EntityViewRequest entityViewRequest,
	                                     com.foreach.across.modules.entity.views.request.EntityViewCommand command,
	                                     WebDataBinder dataBinder ) {
		command.addExtension( PARAM, "" );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Iterable fetchItems( EntityViewRequest entityViewRequest,
	                               EntityView entityView,
	                               Pageable pageable ) {
		EntityViewContext viewContext = entityViewRequest.getEntityViewContext();
		String filter = entityViewRequest.getCommand().getExtension( PARAM, String.class );

		try {
			EntityConfiguration entityConfiguration = viewContext.getEntityConfiguration();
			EntityQueryParser parser = entityConfiguration.getAttribute( EntityQueryParser.class );
			EntityQuery query = parser.parse( filter );

			if ( baseEqlPredicate != null ) {
				query = EntityQuery.and( query, parser.parse( baseEqlPredicate ) );
			}

			String additionalPredicate = entityView.getAttribute( EQL_PREDICATE_ATTRIBUTE_NAME, String.class );
			if ( additionalPredicate != null ) {
				query = EntityQuery.and( query, parser.parse( additionalPredicate ) );
			}

			EntityQueryExecutor executor = entityConfiguration.getAttribute( EntityQueryExecutor.class );

			if ( viewContext.isForAssociation() ) {
				EntityAssociation association = viewContext.getEntityAssociation();
				AssociatedEntityQueryExecutor associatedExecutor = new AssociatedEntityQueryExecutor<>( association.getTargetProperty(), executor );
				return associatedExecutor.findAll( viewContext.getParentContext().getEntity( Object.class ), query, pageable );
			}
			else {
				return executor.findAll( query, pageable );
			}
		}
		catch ( EntityQueryParsingException pe ) {
			String message = pe.getMessage();

			if ( pe.hasErrorExpressionPosition() ) {
				message += " ; position " + pe.getErrorExpressionPosition();
			}

			entityView.addAttribute( "filterError", message );

		}
		catch ( Exception e ) {
			entityView.addAttribute( "filterError", ExceptionUtils.getRootCauseMessage( e ) );
		}

		// Explicitly return null to avoid "0 users found" along with an exception
		return null;
	}

	@Override
	public void postRender( com.foreach.across.modules.entity.views.request.EntityViewRequest entityViewRequest, EntityView entityView ) {
		String filter = entityViewRequest.getCommand().getExtension( PARAM, String.class );

		ContainerViewElement container = entityView.getAttribute( ATTRIBUTE_CONTAINER_ELEMENT, ContainerViewElement.class );
		ViewElementBuilderContext builderContext = EntityViewProcessorAdapter.retrieveBuilderContext();

		// move the original actions
		Optional<ContainerViewElement> header = find( container, ListFormViewProcessor.DEFAULT_FORM_NAME + "-header", ContainerViewElement.class );
		header.ifPresent( h -> {
			h.addFirstChild(
					bootstrapUi
							.inputGroup(
									bootstrapUi.textbox()
									           .controlName( "extensions[" + PARAM + "]" )
									           .text( filter )
							)
							.addonAfter(
									bootstrapUi.button()
									           .submit()
									           .iconOnly( new GlyphIcon( GlyphIcon.SEARCH ) )
							)
							.build( builderContext )
			);

			String errorMessage = entityView.getAttribute( "filterError", String.class );

			if ( !StringUtils.isBlank( errorMessage ) ) {
				container.addChild(
						bootstrapUi
								.alert()
								.danger()
								.add( bootstrapUi.text( errorMessage ) )
								.build( builderContext )
				);
			}
		} );
	}

	@Autowired
	void setBootstrapUiFactory( BootstrapUiFactory bootstrapUiFactory ) {
		this.bootstrapUi = bootstrapUiFactory;
	}
}
