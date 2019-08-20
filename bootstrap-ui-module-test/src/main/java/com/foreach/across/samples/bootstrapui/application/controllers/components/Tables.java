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

package com.foreach.across.samples.bootstrapui.application.controllers.components;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.TableViewElement;
import com.foreach.across.modules.web.menu.PathBasedMenuBuilder;
import com.foreach.across.samples.bootstrapui.application.controllers.ExampleController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/components/table")
class Tables extends ExampleController
{
	@Override
	protected void menuItems( PathBasedMenuBuilder menu ) {
		menu.item( "/components/table", "Tables" );
	}

	@RequestMapping(method = RequestMethod.GET)
	String render() {
		return render( panel( "Full table", simpleTableElement() ) );
	}

	private TableViewElement simpleTableElement() {
		return BootstrapUiBuilders
				.table()
				.header(
						BootstrapUiBuilders.tableHeader().add(
								BootstrapUiBuilders.tableHeaderCell().text( "Name" )
						).add(
								BootstrapUiBuilders.tableHeaderCell().text( "Value" )
						)
				)
				.body(
						BootstrapUiBuilders.tableBody().add(
								BootstrapUiBuilders.tableRow().add(
										BootstrapUiBuilders.tableCell().text( "Height" )
								).add(
										BootstrapUiBuilders.tableCell().text( "128" )
								)

						)
				)
				.footer(
						BootstrapUiBuilders.tableFooter().add(
								BootstrapUiBuilders.tableRow().add(
										BootstrapUiBuilders.tableCell()
										                   .columnSpan( 2 )
										                   .text( "Table footer" )
										                   .heading( true )
								)
						)
				)
				.build();
	}
}
