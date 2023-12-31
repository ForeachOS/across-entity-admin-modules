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
import com.foreach.across.modules.adminweb.ui.PageContentStructure;
import com.foreach.across.modules.bootstrapui.elements.ButtonViewElement;
import com.foreach.across.modules.bootstrapui.elements.Style;
import com.foreach.across.modules.entity.query.AssociatedEntityQueryExecutor;
import com.foreach.across.modules.entity.query.EntityQuery;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.registry.EntityModel;
import com.foreach.across.modules.entity.views.EntityView;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import com.foreach.across.modules.entity.views.events.BuildEntityDeleteViewEvent;
import com.foreach.across.modules.entity.views.processors.support.EntityFormStateCompleted;
import com.foreach.across.modules.entity.views.processors.support.EntityViewPageHelper;
import com.foreach.across.modules.entity.views.processors.support.ViewElementBuilderMap;
import com.foreach.across.modules.entity.views.request.EntityViewCommand;
import com.foreach.across.modules.entity.views.request.EntityViewRequest;
import com.foreach.across.modules.entity.views.support.EntityMessages;
import com.foreach.across.modules.entity.web.EntityLinkBuilder;
import com.foreach.across.modules.web.ui.elements.ContainerViewElement;
import com.foreach.across.modules.web.ui.elements.builder.ContainerViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.support.ContainerViewElementUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.function.Consumer;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.entity.views.processors.support.EntityFormStateCompleted.ENTITY_DELETED;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * Responsible for rendering the actual delete entity page, and performing the delete action if necessary.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Slf4j
@Component
@Exposed
@Scope("prototype")
public class DeleteEntityViewProcessor extends EntityViewProcessorAdapter
{
	static final String DELETE_CONFIGURATION = "deleteEntityConfiguration";

	private ApplicationEventPublisher eventPublisher;
	private EntityViewPageHelper entityViewPageHelper;

	/**
	 * Should the form state be published as an event for modification (defaults to {@code true}).
	 */
	@Setter
	@Getter
	private boolean publishFormState;

	/**
	 * Set a consumer to apply to the form state after it has been built and published as an event.
	 */
	@Setter
	@Getter
	private Consumer<EntityFormStateCompleted<?>> formStateConsumer;

	@Override
	protected void doControl( EntityViewRequest entityViewRequest,
	                          EntityView entityView,
	                          EntityViewCommand command,
	                          BindingResult bindingResult,
	                          HttpMethod httpMethod ) {
		BuildEntityDeleteViewEvent viewConfiguration = buildDeleteViewConfiguration( entityViewRequest.getEntityViewContext() );
		entityView.addAttribute( DELETE_CONFIGURATION, viewConfiguration );
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost( EntityViewRequest entityViewRequest, EntityView entityView, EntityViewCommand command, BindingResult bindingResult ) {
		BuildEntityDeleteViewEvent deleteConfiguration = entityView.getAttribute( DELETE_CONFIGURATION, BuildEntityDeleteViewEvent.class );

		if ( !deleteConfiguration.isDeleteDisabled() && !bindingResult.hasErrors() ) {
			try {
				EntityViewContext entityViewContext = entityViewRequest.getEntityViewContext();
				EntityModel entityModel = entityViewContext.getEntityModel();
				Object entityToDelete = entityViewContext.getEntity( Object.class );
				entityModel.delete( entityToDelete );

				publishAndApplyEntityFormState( entityViewRequest, entityView, entityToDelete );
			}
			catch ( RuntimeException e ) {
				entityViewPageHelper.throwOrAddExceptionFeedback( entityViewRequest, "feedback.entityDeleteFailed", e );
			}
		}
	}

	private void publishAndApplyEntityFormState( EntityViewRequest entityViewRequest, EntityView entityView, Object deletedEntity ) {
		EntityViewContext entityViewContext = entityViewRequest.getEntityViewContext();
		EntityFormStateCompleted<?> entityState = new EntityFormStateCompleted<>( ENTITY_DELETED, deletedEntity, entityViewRequest, entityView );

		entityState.addFeedbackMessage(
				EntityFormStateCompleted
						.feedback( Style.SUCCESS )
						.messageCode( "feedback.entityDeleted" )
						.message( entityViewContext.getEntityMessages().withNameSingular( "feedback.entityDeleted", entityViewContext.getEntityLabel() ) )
						.build()
		);

		entityState.setRedirectUrl( entityViewContext.getLinkBuilder().listView().toUriString() );

		// view specific handling
		if ( formStateConsumer != null ) {
			formStateConsumer.accept( entityState );
		}

		// event based public modifications
		if ( publishFormState ) {
			eventPublisher.publishEvent( entityState );
		}

		// apply the form state values
		boolean isRedirect = entityView.isRedirect();

		entityState.getFeedbackMessages()
		           .stream()
		           .filter( fm -> !isRedirect || !fm.isOnlyForRedirect() )
		           .forEach( fm -> {
			           if ( isRedirect ) {
				           entityViewPageHelper.addGlobalFeedbackMessageAfterRedirect( entityViewRequest, fm.getStyle(), fm.getMessageOrCode() );
			           }
			           else {
				           // todo: centralize this
				           PageContentStructure page = entityViewRequest.getPageContentStructure();
				           page.addToFeedback( bootstrap.builders.alert()
				                                                 .style( fm.getStyle() )
				                                                 .dismissible()
				                                                 .text( fm.getMessageOrCode() )
				                                                 .build() );
			           }
		           } );
	}

	@Override
	protected void render( EntityViewRequest entityViewRequest,
	                       EntityView entityView,
	                       ContainerViewElementBuilderSupport<?, ?> containerBuilder,
	                       ViewElementBuilderMap builderMap,
	                       com.foreach.across.modules.web.ui.ViewElementBuilderContext builderContext ) {
		EntityMessages messages = entityViewRequest.getEntityViewContext().getEntityMessages();
		BuildEntityDeleteViewEvent deleteConfiguration = entityView.getAttribute( DELETE_CONFIGURATION, BuildEntityDeleteViewEvent.class );

		String confirmationMessage = messages.withNameSingular( "delete.confirmation" );
		if ( deleteConfiguration.isDeleteDisabled() ) {
			confirmationMessage = messages.withNameSingular( "delete.deleteDisabled" );
		}

		builderMap.get( "entityForm-column-0", ContainerViewElementBuilderSupport.class )
		          .add( deleteConfiguration.messages() )
		          .add(
				          html.builders
						          .p()
						          .css( deleteConfiguration.isDeleteDisabled() ? Style.DANGER.forPrefix( "text" ) : "" )
						          .add( html.builders.text( confirmationMessage ) )
		          );
	}

	@Override
	protected void postRender( EntityViewRequest entityViewRequest,
	                           EntityView entityView,
	                           ContainerViewElement container,
	                           com.foreach.across.modules.web.ui.ViewElementBuilderContext builderContext ) {
		BuildEntityDeleteViewEvent deleteConfiguration = entityView.removeAttribute( DELETE_CONFIGURATION, BuildEntityDeleteViewEvent.class );
		EntityMessages messages = entityViewRequest.getEntityViewContext().getEntityMessages();

		// modify the buttons to match the allowed configuration
		ContainerViewElementUtils
				.find( container, "buttons", ContainerViewElement.class )
				.ifPresent( buttons -> {
					ContainerViewElementUtils.remove( buttons, "btn-save" );
					if ( !deleteConfiguration.isDeleteDisabled() ) {
						buttons.addFirstChild(
								bootstrap.builders.button()
								                  .name( "btn-delete" )
								                  .data( "em-button-role", "delete" )
								                  .style( Style.DANGER )
								                  .submit()
								                  .text( messages.messageWithFallback( "buttons.delete" ) )
								                  .build( builderContext )
						);
					}
					else {
						ContainerViewElementUtils.find( buttons, "btn-cancel", ButtonViewElement.class )
						                         .ifPresent( btn -> btn.setStyle( Style.PRIMARY ) );
					}
				} );
	}

	private BuildEntityDeleteViewEvent buildDeleteViewConfiguration( EntityViewContext entityViewContext ) {
		com.foreach.across.modules.web.ui.ViewElementBuilderContext builderContext = EntityViewProcessorAdapter.retrieveBuilderContext();

		BuildEntityDeleteViewEvent<?> event
				= new BuildEntityDeleteViewEvent<>( entityViewContext.getEntity(), builderContext );
		event.setDeleteDisabled( false );

		ContainerViewElement associations = html.builders.ul().build( builderContext );

		event.setAssociations( associations );
		EntityMessages entityMessages = entityViewContext.getEntityMessages();
		event.setMessages(
				html.builders
						.container()
						.add(
								html.builders
										.container()
										.name( "associations" )
										.add(
												html.builders
														.p()
														.add( html.builders.text( entityMessages.withNameSingular( "delete.associations" ) ) )
										)
										.add( associations )
						)
						.build( builderContext )
		);

		buildAssociations( entityViewContext, event );

		eventPublisher.publishEvent( event );

		// Remove the associations block if no associations were added
		if ( !event.associations().hasChildren() ) {
			ContainerViewElementUtils.remove( event.messages(), "associations" );
		}

		return event;
	}

	private void buildAssociations( EntityViewContext entityViewContext,
	                                BuildEntityDeleteViewEvent viewConfiguration ) {
		Object parent = entityViewContext.getEntity( Object.class );
		EntityLinkBuilder parentLinkBuilder = entityViewContext.getLinkBuilder();
		EntityConfiguration<?> entityConfiguration = entityViewContext.getEntityConfiguration();

		LOG.trace( "Fetching associated items and disabling delete if parent delete mode is SUPPRESS" );

		entityConfiguration.getAssociations().forEach(
				association -> {
					int count = countAssociatedItems( association, parent );

					if ( count > 0 ) {
						if ( EntityAssociation.ParentDeleteMode.SUPPRESS == association.getParentDeleteMode() ) {
							LOG.trace( "Suppressing delete action because association {} has {} items",
							           association.getName(), count );
							viewConfiguration.setDeleteDisabled( true );
						}

						if ( !association.isHidden() ) {
							addAssociationInfo( viewConfiguration, parent, parentLinkBuilder, association, count );
						}
					}
				}
		);

		LOG.trace( "Delete disabled after association check: {}", viewConfiguration.isDeleteDisabled() );
	}

	private void addAssociationInfo( BuildEntityDeleteViewEvent viewConfiguration,
	                                 Object parent,
	                                 EntityLinkBuilder parentLinkBuilder,
	                                 EntityAssociation association,
	                                 int itemCount ) {
		EntityMessages messages = new EntityMessages( association.getTargetEntityConfiguration().getEntityMessageCodeResolver() );
		EntityLinkBuilder linkBuilder = association.getAttribute( EntityLinkBuilder.class ).asAssociationFor( parentLinkBuilder, parent );

		String title = messages.withNamePlural( "delete.associatedResults", itemCount );

		viewConfiguration.associations().addChild(
				html.builders.li()
				             .name( association.getName() )
				             .add( bootstrap.builders
						                   .link()
						                   .url( linkBuilder.overview() )
						                   .text( title ) )
				             .build( viewConfiguration.getBuilderContext() )
		);
	}

	private int countAssociatedItems( EntityAssociation association, Object parent ) {
		if ( EntityAssociation.ParentDeleteMode.IGNORE != association.getParentDeleteMode() ) {
			AssociatedEntityQueryExecutor executor = association.getAttribute( AssociatedEntityQueryExecutor.class );

			if ( executor != null ) {
				return executor.findAll( parent, EntityQuery.all() ).size();
			}
		}

		return 0;
	}

	@Autowired
	void setEventPublisher( ApplicationEventPublisher eventPublisher ) {
		this.eventPublisher = eventPublisher;
	}

	@Autowired
	void setEntityViewPageHelper( EntityViewPageHelper entityViewPageHelper ) {
		this.entityViewPageHelper = entityViewPageHelper;
	}
}
