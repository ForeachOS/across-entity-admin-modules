/*
 * Copyright 2019 the original author or authors
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

package com.foreach.across.modules.bootstrapui.styles;

import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyleRule.of;

/**
 * https://getbootstrap.com/docs/4.3/utilities/screen-readers/
 *
 * @author Arne Vandamme
 * @since 2.3.0
 */
@SuppressWarnings("WeakerAccess")
public class ScreenReaderOnlyStyleRule implements BootstrapStyleRule
{
	public final BootstrapStyleRule focusable = of( "sr-only-focusable" );

	@Override
	public String[] toCssClasses() {
		return new String[] { "sr-only" };
	}
}
