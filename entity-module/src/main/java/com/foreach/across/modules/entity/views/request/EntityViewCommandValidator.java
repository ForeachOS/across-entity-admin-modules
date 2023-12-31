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

package com.foreach.across.modules.entity.views.request;

import com.foreach.across.core.annotations.Exposed;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.bind.EntityPropertiesBinder;
import com.foreach.across.modules.entity.registry.EntityConfiguration;
import com.foreach.across.modules.entity.views.context.EntityViewContext;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;
import org.springframework.validation.Validator;

/**
 * Default validator for an {@link EntityViewCommand} that performs validation on both
 * the {@link EntityViewCommand#getEntity()} and individual {@link EntityViewCommand#getExtensions()} properties.
 * <p/>
 * This validator takes a single {@link com.foreach.across.modules.entity.views.context.EntityViewContext} and
 * a fallback {@link Validator}.  The entity attached to the {@link EntityViewCommand} is assumed
 * to be of {@link EntityViewContext#getEntityConfiguration()}.  If no custom validator is defined on the
 * {@link com.foreach.across.modules.entity.registry.EntityConfiguration}, the fallback validator will be used.
 * The same fallback validator will be used for validating the extensions.
 * <p/>
 * This implementation supports validation hints by extending {@link SmartValidator}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Component
@Exposed
public class EntityViewCommandValidator implements SmartValidator
{
	private final EntityViewContext entityViewContext;
	private final Validator fallbackValidator;

	@Autowired
	public EntityViewCommandValidator( @NonNull EntityViewContext entityViewContext,
	                                   @NonNull @Qualifier(EntityModule.VALIDATOR) Validator fallbackValidator ) {
		this.entityViewContext = entityViewContext;
		this.fallbackValidator = fallbackValidator;
	}

	@Override
	public boolean supports( Class<?> clazz ) {
		return EntityViewCommand.class.isAssignableFrom( clazz );
	}

	@Override
	public void validate( Object target, Errors errors ) {
		validate( target, errors, new Object[0] );
	}

	@Override
	public void validate( Object target, Errors errors, Object... validationHints ) {
		EntityViewCommand command = (EntityViewCommand) target;
		Object entity = command.getEntity();

		Runnable validation = () -> {
			if ( entity != null ) {
				errors.pushNestedPath( "entity" );
				try {
					validate( retrieveEntityValidator(), entity, errors, validationHints );
				}
				finally {
					errors.popNestedPath();
				}
			}
		};

		EntityPropertiesBinder properties = command.getProperties();

		if ( properties != null && !properties.isReadonly() ) {
			properties.createController()
			          .addEntityValidationCallback( validation )
			          .applyValuesAndValidate( errors, validationHints );
		}
		else {
			validation.run();
		}

		command.getExtensions()
		       .forEach( ( key, value ) -> {
			       errors.pushNestedPath( "extensions[" + key + "]" );
			       try {
				       validate( fallbackValidator, value, errors, validationHints );

				       command.getExtensionValidators( key )
				              .forEach( validator -> validate( validator, value, errors, validationHints ) );
			       }
			       finally {
				       errors.popNestedPath();
			       }
		       } );
	}

	private void validate( Validator validator, Object target, Errors errors, Object... validationHints ) {
		if ( validator instanceof SmartValidator ) {
			( (SmartValidator) validator ).validate( target, errors, validationHints );
		}
		else {
			validator.validate( target, errors );
		}
	}

	private Validator retrieveEntityValidator() {
		Validator validator = null;

		EntityConfiguration entityConfiguration = entityViewContext.getEntityConfiguration();

		if ( entityConfiguration != null ) {
			validator = entityConfiguration.getAttribute( Validator.class );
		}

		return validator != null ? validator : fallbackValidator;
	}
}
