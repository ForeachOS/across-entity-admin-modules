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

package com.foreach.across.modules.bootstrapui.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.HashMap;
import java.util.Locale;

/**
 * Configuration class for a {@link AutoSuggestFormElementBuilder} based on
 * <a href="https://github.com/twitter/typeahead.js">typeahead.js</a>.
 *
 * @author Sander Van Loock
 */
public class AutosuggestFormElementConfiguration extends HashMap<String, Object>
{
	public static final String END_POINT_KEY = "endPoint";

	@JsonIgnore
	private Locale locale = LocaleContextHolder.getLocale();

	public AutosuggestFormElementConfiguration() {
		setEndPoint( "/autosuggest" );
	}

	public AutosuggestFormElementConfiguration( String endPoint ) {
		this();
		setEndPoint( endPoint );
	}

	public AutosuggestFormElementConfiguration( AutosuggestFormElementConfiguration configuration ) {
		putAll( configuration );
	}

	/**
	 * Sets the remote url endpoint where to fetch suggestions.  Defaults to /endpoint
	 *
	 * @param endPoint
	 */
	public void setEndPoint( String endPoint ) {
		put( "endPoint", endPoint );
	}

	public String getEndPoint() {
		return (String) getOrDefault( END_POINT_KEY, "/autosuggest" );
	}

	public AutosuggestFormElementConfiguration localize( Locale locale ) {
		AutosuggestFormElementConfiguration clone = new AutosuggestFormElementConfiguration( this );
		clone.locale = locale;

		return clone;
	}
}
