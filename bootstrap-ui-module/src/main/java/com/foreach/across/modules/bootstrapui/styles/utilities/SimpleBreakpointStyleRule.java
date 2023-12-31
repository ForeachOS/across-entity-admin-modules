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
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Arne Vandamme
 * @since 3.0.0
 */
public class SimpleBreakpointStyleRule implements BreakpointStyleRule
{
	private final String prefix;
	private final String suffix;
	private final String[] additionalCss;

	public SimpleBreakpointStyleRule( String prefix, String suffix, String... additionalCss ) {
		this.prefix = prefix;
		this.suffix = suffix;
		this.additionalCss = additionalCss;
	}

	@Override
	public BootstrapStyleRule on( String breakpoint ) {
		return BootstrapStyleRule.appendOnSet( BootstrapStyleRule.of( additionalCss ), prefix + "-" + breakpoint + ( suffix != null ? "-" + suffix : "" ) );
	}

	@Override
	public String[] toCssClasses() {
		return ArrayUtils.add( additionalCss, prefix + ( suffix != null ? "-" + suffix : "" ) );
	}

	@Override
	public void removeFrom( HtmlViewElement target ) {
		target.removeCssClass( prefix + ( suffix != null ? "-" + suffix : "" ) );
	}
}
