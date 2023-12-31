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

package com.foreach.across.modules.entity.registrars.repository;

import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.registry.properties.DefaultEntityPropertyRegistryProvider;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.MutableEntityPropertyRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.EmbeddedId;
import javax.persistence.GeneratedValue;

/**
 * Adds {@link org.springframework.data.mapping.PersistentProperty} and related attributes
 * to entity properties, based on the configured {@link MappingContext}s.
 *
 * @author Arne Vandamme
 */
@Component
@OrderInModule(2)
@RequiredArgsConstructor
public class PersistenceMetadataPropertiesRegistrar implements DefaultEntityPropertyRegistryProvider.PropertiesRegistrar
{
	private final MappingContextRegistry mappingContextRegistry;

	@Override
	public void accept( Class<?> entityType, MutableEntityPropertyRegistry registry ) {
		mappingContextRegistry
				.getPersistentEntity( entityType )
				.ifPresent( entity -> {
					            for ( EntityPropertyDescriptor descriptor : registry.getRegisteredDescriptors() ) {
						            PersistentProperty persistentProperty = entity.getPersistentProperty( descriptor.getName() );

						            if ( persistentProperty != null ) {
							            MutableEntityPropertyDescriptor mutable = registry.getProperty( descriptor.getName() );

							            if ( mutable != null ) {
								            registerPropertyPersistenceMetadata( persistentProperty, mutable );
								            registerSortableMetadata( persistentProperty, mutable );
							            }
						            }
					            }
				            }
				);

	}

	private void registerSortableMetadata( PersistentProperty persistentProperty,
	                                       MutableEntityPropertyDescriptor mutable ) {
		if ( !persistentProperty.isTransient() && !persistentProperty.isCollectionLike() ) {
			mutable.setAttribute( Sort.Order.class, Sort.Order.by( persistentProperty.getName() ) );
		}
	}

	private void registerPropertyPersistenceMetadata( PersistentProperty persistentProperty,
	                                                  MutableEntityPropertyDescriptor mutable ) {
		mutable.setAttribute( PersistentProperty.class, persistentProperty );

		boolean embedded = isEmbedded( persistentProperty );

		if ( embedded ) {
			// only explicitly set value if embedded
			mutable.setAttribute( EntityAttributes.IS_EMBEDDED_OBJECT, true );
		}

		if ( mutable.isHidden() && mutable.isReadable() && embedded ) {
			mutable.setHidden( false );
		}

		if ( persistentProperty.isAnnotationPresent( GeneratedValue.class ) ) {
			mutable.setHidden( true );
			mutable.setWritable( false );
		}
	}

	private boolean isEmbedded( PersistentProperty persistentProperty ) {
		boolean hasAnnotation = persistentProperty.isAnnotationPresent( Embedded.class )
				|| persistentProperty.isAnnotationPresent( EmbeddedId.class )
				|| persistentProperty.isAnnotationPresent( ElementCollection.class );

		return hasAnnotation && !isBaseType( persistentProperty.getActualType() );
	}

	private boolean isBaseType( Class<?> clazz ) {
		return String.class.equals( clazz ) || ClassUtils.isPrimitiveOrWrapper( clazz );
	}
}
