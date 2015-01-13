package com.foreach.across.modules.entity.business;

import com.foreach.across.modules.entity.views.helpers.ValueFetcher;

public interface EntityPropertyDescriptor
{
	/**
	 * @return Property name.
	 */
	String getName();

	String getDisplayName();

	boolean isReadable();

	boolean isWritable();

	boolean isHidden();

	Class<?> getPropertyType();

	/**
	 * @return Associated instance that can fetch the property value from an instance.
	 */
	ValueFetcher getValueFetcher();

	/**
	 * Creates a new instance that is the result of merging the other descriptor into this one:
	 * properties set on the other descriptor will override this one.
	 *
	 * @param other EntityPropertyDescriptor to be merged into this one.
	 * @return New descriptor representing the merged instance.
	 */
	EntityPropertyDescriptor merge( EntityPropertyDescriptor other );
}
