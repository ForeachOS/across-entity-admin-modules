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

package com.foreach.across.modules.bootstrapui.styles.utilities.across;

import com.foreach.across.modules.bootstrapui.styles.utilities.BreakpointStyleRule;

/**
 * https://getbootstrap.com/docs/4.3/utilities/float/
 *
 * @author Steven Gentens
 * @since 3.0.0
 */
public class AcrossFloatStyleRule
{
	public final BreakpointStyleRule left = new AcrossSimpleBreakpointStyleRule( "float", "left" );
	public final BreakpointStyleRule right = new AcrossSimpleBreakpointStyleRule( "float", "right" );
	public final BreakpointStyleRule none = new AcrossSimpleBreakpointStyleRule( "float", "none" );
}
