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

package com.foreach.across.samples.entity.application.config;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiElements;
import com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements;
import com.foreach.across.modules.entity.autosuggest.AutoSuggestDataAttributeRegistrar;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.registry.EntityAssociation;
import com.foreach.across.modules.entity.support.EntityPropertyRegistrationHelper;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.entity.web.links.EntityViewLinks;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import com.foreach.across.samples.entity.application.business.Friend;
import com.foreach.across.samples.entity.application.business.Hobby;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FriendConfiguration implements EntityConfigurer
{
	private final AutoSuggestDataAttributeRegistrar autoSuggestData;
	private final EntityPropertyRegistrationHelper proxyPropertyRegistrar;
	private final EntityViewLinks entityViewLinks;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( Friend.class )
		        .properties(
				        props -> props.property( proxyPropertyRegistrar.entityIdProxy( "hobby" )
				                                                       .entityType( Hobby.class )
				                                                       .targetPropertyName( "hobbyId" ) )
				                      .and()
				                      .property( "hobbyId" )
				                      .hidden( true )
				                      .and()
				                      .property( "users" )
				                      .viewElementType( ViewElementMode.CONTROL, BootstrapUiElements.AUTOSUGGEST )
				                      .attribute( autoSuggestData.entityQuery( "name ilike '%{0}%'" ) )
		        )
		        .listView(
				        lvb -> lvb.showProperties( ".", "~hobby" )
		        );

		entities.withType( Hobby.class )
		        .properties(
				        props -> props.property( "name" )
				                      .attribute( "xyz", "some attribute" )
		        )
		        .listView( "customListView",
		                   lvb -> lvb.properties(
				                   props -> props.property( "name" )
				                                 .viewElementBuilder( ViewElementMode.LIST_VALUE, (ViewElementBuilder) ctx -> {
					                                 Object instance = EntityViewElementUtils.currentEntity( ctx );
					                                 String currentValue = EntityViewElementUtils.currentPropertyValue( ctx, String.class );
					                                 return BootstrapViewElements.bootstrap.builders.link()
					                                                                                .url( entityViewLinks.linkTo( instance ).toUriString() )
					                                                                                .text( currentValue )
					                                                                                .build( ctx );
				                                 } )
		                   )
		        )
		        .association(
				        ab -> ab.name( "users" )
				                .associationType( EntityAssociation.Type.EMBEDDED )
				                .targetEntityType( Friend.class )
				                .targetProperty( "hobby" )
				                .listView()
				                .detailView()
				                .updateFormView()
				                .createFormView()
				                .show()
		        );
	}
}
