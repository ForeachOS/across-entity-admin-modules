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

/**
 * https://getbootstrap.com/docs/4.3/utilities/flex/#justify-content
 *
 * @author Arne Vandamme
 * @since 2.3.0
 */
@SuppressWarnings("WeakerAccess")
public class JustifyContentStyleRule
{
	public final BreakpointStyleRule start = new SimpleBreakpointStyleRule( "justify-content", "start" );
	public final BreakpointStyleRule end = new SimpleBreakpointStyleRule( "justify-content", "end" );
	public final BreakpointStyleRule center = new SimpleBreakpointStyleRule( "justify-content", "center" );
	public final BreakpointStyleRule between = new SimpleBreakpointStyleRule( "justify-content", "between" );
	public final BreakpointStyleRule around = new SimpleBreakpointStyleRule( "justify-content", "around" );
}
