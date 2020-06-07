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

package com.foreach.across.modules.bootstrapui.styles.utilities;

import com.foreach.across.modules.bootstrapui.styles.BootstrapStyleRule;

import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyleRule.of;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class RoundedStyleRule implements BootstrapStyleRule
{
	public final BootstrapStyleRule top = of( "rounded-top" );
	public final BootstrapStyleRule right = of( "rounded-right" );
	public final BootstrapStyleRule bottom = of( "rounded-bottom" );
	public final BootstrapStyleRule left = of( "rounded-left" );
	public final BootstrapStyleRule circle = of( "rounded-circle" );
	public final BootstrapStyleRule pill = of( "rounded-pill" );
	public final BootstrapStyleRule none = of( "rounded-0" );
	public final BootstrapStyleRule small = of( "rounded-sm" );
	public final BootstrapStyleRule large = of( "rounded-lg" );

	@Override
	public String[] toCssClasses() {
		return new String[] { "rounded" };
	}
}
