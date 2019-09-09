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

package com.foreach.across.modules.bootstrapui.elements.builder.entry.support;

import com.foreach.across.modules.bootstrapui.elements.builder.TableViewElementBuilder;
import com.foreach.across.modules.web.ui.ViewElement;

/**
 * Support class used by {@link com.foreach.across.modules.bootstrapui.elements.builder.entry.BootstrapViewElementBuilders}
 *
 * @author Stijn Vanhoof
 * @since 3.0.0
 */
public class BootstrapTableBuilderSupport
{
	public TableViewElementBuilder.Body body() {
		return new TableViewElementBuilder.Body();
	}

	public TableViewElementBuilder.Body body( ViewElement.WitherSetter... setters ) {
		return body().with( setters );
	}

	public TableViewElementBuilder.Caption caption() {
		return new TableViewElementBuilder.Caption();
	}

	public TableViewElementBuilder.Caption caption( ViewElement.WitherSetter... setters ) {
		return caption().with( setters );
	}

	public TableViewElementBuilder.Cell cell() {
		return new TableViewElementBuilder.Cell();
	}

	public TableViewElementBuilder.Cell cell( ViewElement.WitherSetter... setters ) {
		return cell().with( setters );
	}

	public TableViewElementBuilder.Footer footer() {
		return new TableViewElementBuilder.Footer();
	}

	public TableViewElementBuilder.Footer footer( ViewElement.WitherSetter... setters ) {
		return footer().with( setters );
	}

	public TableViewElementBuilder.Header header() {
		return new TableViewElementBuilder.Header();
	}

	public TableViewElementBuilder.Header header( ViewElement.WitherSetter... setters ) {
		return header().with( setters );
	}

	public TableViewElementBuilder.Row row() {
		return new TableViewElementBuilder.Row();
	}

	public TableViewElementBuilder.Row row( ViewElement.WitherSetter... setters ) {
		return row().with( setters );
	}
}
