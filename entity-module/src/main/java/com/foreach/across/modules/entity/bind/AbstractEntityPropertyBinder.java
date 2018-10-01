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

package com.foreach.across.modules.entity.bind;

import com.foreach.across.modules.entity.registry.properties.EntityPropertyController;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyValue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.core.Ordered;

import java.util.Optional;

/**
 * Base class for {@link EntityPropertyBinder} implementations, holds the common "main controller"
 * related methods and simple properties.
 *
 * @author Arne Vandamme
 * @since 3.2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class AbstractEntityPropertyBinder implements EntityPropertyBinder<Object>
{
	private final EntityPropertiesBinder binder;
	private final EntityPropertyDescriptor descriptor;
	private final EntityPropertyController<Object, Object> controller;

	@Getter
	@Setter
	private boolean bound;

	@Getter
	@Setter
	private boolean deleted;

	/**
	 * The original value that was fetched when the property was initialized.
	 * If {@code null} the original value has never been fetched.
	 */
	@SuppressWarnings("all")
	private Optional<Object> originalValue;

	@Getter
	@Setter
	private int sortIndex;

	@Override
	public Object getOriginalValue() {
		return loadOriginalValue();
	}

	final Object loadOriginalValue() {
		if ( originalValue == null ) {
			originalValue = Optional.ofNullable( fetchOriginalValue() );
		}
		return originalValue.orElse( null );
	}

	Object fetchOriginalValue() {
		return controller.fetchValue( binder.getBindingContext() );
	}

	/**
	 * Pre-set the original value, this will avoid that {@link #loadOriginalValue()} will  be called.
	 *
	 * @param value to use instead
	 */
	protected void setOriginalValue( Object value ) {
		originalValue = Optional.ofNullable( value );
	}

	@Override
	public Object createNewValue() {
		return binder.createValue( controller, descriptor.getPropertyTypeDescriptor() );
	}

	@Override
	public boolean applyValue() {
		if ( controller != null ) {
			return controller.applyValue( binder.getBindingContext(), new EntityPropertyValue<>( loadOriginalValue(), getValue(), isDeleted() ) );
		}
		return false;
	}

	@Override
	public boolean save() {
		if ( controller != null ) {
			return controller.save( binder.getBindingContext(), new EntityPropertyValue<>( loadOriginalValue(), getValue(), isDeleted() ) );
		}
		return false;
	}

	@Override
	public int getControllerOrder() {
		return controller != null ? controller.getOrder() : Ordered.LOWEST_PRECEDENCE;
	}
}
