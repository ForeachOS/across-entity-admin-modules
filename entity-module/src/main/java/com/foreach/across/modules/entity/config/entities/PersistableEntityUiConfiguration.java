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
package com.foreach.across.modules.entity.config.entities;

import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.hibernate.business.SettableIdBasedEntity;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Persistable;

/**
 * Configures default properties for {@link org.springframework.data.domain.Persistable} entities
 * and known child classes.
 *
 * @author Arne Vandamme
 */
@Configuration
@OrderInModule(1)
public class PersistableEntityUiConfiguration implements EntityConfigurer
{
	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.assignableTo( Persistable.class )
		        .properties(
				        props -> props.property( "new" ).readable( false ).hidden( true )
		        );

		entities.assignableTo( SettableIdBasedEntity.class )
		        .properties( props -> props.property( "newEntityId" ).readable( false ).hidden( true ) );
	}
}
