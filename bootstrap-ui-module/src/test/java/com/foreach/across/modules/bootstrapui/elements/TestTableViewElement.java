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
package com.foreach.across.modules.bootstrapui.elements;

import com.foreach.across.modules.web.ui.elements.TemplateViewElement;
import com.foreach.across.modules.web.ui.elements.TextViewElement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.foreach.across.modules.bootstrapui.styles.BootstrapStyles.css;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 */
public class TestTableViewElement extends AbstractBootstrapViewElementTest
{
	@Test
	public void simple() {
		TableViewElement table = new TableViewElement();
		assertThat( table.matches( css.table ) ).isTrue();

		renderAndExpect( table, "<table class='table' />" );

		table.remove( css.table );
		renderAndExpect( table, "<table />" );
	}

	@Test
	public void customTemplate() {
		TableViewElement table = new TableViewElement();
		table.setCustomTemplate( CUSTOM_TEMPLATE );

		renderAndExpect( table, CUSTOM_TEMPLATE_OUTPUT );
	}

	@Test
	public void customTemplateChild() {
		TableViewElement table = new TableViewElement();
		table.addChild( new TemplateViewElement( CUSTOM_TEMPLATE ) );

		renderAndExpect( table, "<table class='table'>" + CUSTOM_TEMPLATE_OUTPUT + "</table>" );
	}

	@Test
	public void responsive() {
		TableViewElement table = new TableViewElement();
		table.setAttribute( "data-test", "test" );
		table.setResponsive( true );

		renderAndExpect(
				table,
				"<div class='table-responsive'><table class='table' data-test='test'></table></div>"
		);
	}

	@Test
	public void styles() {
		TableViewElement table = new TableViewElement();
		table.addStyle( Style.Table.CONDENSED );
		assertThat( table.matches( css.table.small ) ).isTrue();

		renderAndExpect(
				table,
				"<table class='table table-sm' />"
		);

		table.addStyle( Style.Table.HOVER );
		renderAndExpect(
				table,
				"<table class='table-sm table table-hover' />"
		);
		assertThat( table.matches( css.table.small.and( css.table.hover ) ) ).isTrue();

		table.clearStyles();
		table.setStyles( Collections.singleton( Style.Table.STRIPED ) );
		renderAndExpect(
				table,
				"<table class='table table-striped' />"
		);
		assertThat( table.matches( css.table.striped.and( css.table.hover.negate() ) ) ).isTrue();
	}

	@Test
	public void styleRuleToStyles() {
		TableViewElement table = new TableViewElement();
		table.set( css.table.small );
		assertThat( table.getStyles() ).containsExactly( Style.Table.CONDENSED );

		table.set( css.table.hover );
		assertThat( table.getStyles() ).containsExactlyInAnyOrder( Style.Table.CONDENSED, Style.Table.HOVER );

		table.set( css.table.striped );
		assertThat( table.getStyles() ).containsExactlyInAnyOrder( Style.Table.CONDENSED, Style.Table.HOVER, Style.Table.STRIPED );

		table.remove( css.table.striped, css.table.small, css.table.hover );
		table.set( css.table.bordered );
		assertThat( table.getStyles() ).containsExactly( Style.Table.BORDERED );
	}

	@Test
	public void rowsAsChildrenOfTable() {
		TableViewElement table = new TableViewElement();

		TableViewElement.Row headerRow = new TableViewElement.Row();
		headerRow.addChild( new TableViewElement.Cell() );

		table.addChild( row( heading( "heading 1" ), heading( "heading 2" ) ) );
		table.addChild( row( cell( "one" ), cell( "two" ) ) );

		TableViewElement.Cell warning = cell( "three" );
		warning.setStyle( Style.TableCell.WARNING );
		assertThat( warning.matches( css.table.warning ) ).isTrue();

		table.addChild( row( warning, cell( "four" ) ) );

		TableViewElement.Cell doubleCell = cell( "five" );
		doubleCell.setColumnSpan( 2 );

		TableViewElement.Row activeRow = row( doubleCell );
		activeRow.setStyle( Style.ACTIVE );
		assertThat( activeRow.matches( css.table.active ) ).isTrue();

		table.addChild( activeRow );

		renderAndExpect(
				table,
				"<table class='table'>" +
						"<tr><th>heading 1</th><th>heading 2</th></tr>" +
						"<tr><td>one</td><td>two</td></tr>" +
						"<tr><td class='table-warning'>three</td><td>four</td></tr>" +
						"<tr class='table-active'><td colspan='2'>five</td></tr>" +
						"</table>"
		);

		warning.set( css.table.danger );
		activeRow.set( css.table.success );
		renderAndExpect(
				table,
				"<table class='table'>" +
						"<tr><th>heading 1</th><th>heading 2</th></tr>" +
						"<tr><td>one</td><td>two</td></tr>" +
						"<tr><td class='table-danger'>three</td><td>four</td></tr>" +
						"<tr class='table-success'><td colspan='2'>five</td></tr>" +
						"</table>"
		);

		warning.remove( css.table.danger );
		activeRow.remove( css.table.success );
		renderAndExpect(
				table,
				"<table class='table'>" +
						"<tr><th>heading 1</th><th>heading 2</th></tr>" +
						"<tr><td>one</td><td>two</td></tr>" +
						"<tr><td>three</td><td>four</td></tr>" +
						"<tr><td colspan='2'>five</td></tr>" +
						"</table>"
		);
	}

	@Test
	public void manualCaptionHeaderBodyFooterAndColGroup() {
		TableViewElement table = new TableViewElement();
		table.addChild( caption( "table caption" ) );
		table.addChild( header( row( heading( "heading 1" ), heading( "heading 2" ) ) ) );
		table.addChild( body( row( cell( "one" ), cell( "two" ) ) ) );
		table.addChild( footer() );
		table.addChild( colgroup() );

		renderAndExpect(
				table,
				"<table class='table'>" +
						"<caption>table caption</caption>" +
						"<thead>" +
						"<tr><th>heading 1</th><th>heading 2</th></tr>" +
						"</thead>" +
						"<tbody>" +
						"<tr><td>one</td><td>two</td></tr>" +
						"</tbody>" +
						"<tfoot/>" +
						"<colgroup><col span='2' class='column-class'/></colgroup>" +
						"</table>"
		);
	}

	@Test
	public void captionHeaderBodyFooterAndColGroupAsProperties() {
		TableViewElement table = new TableViewElement();
		table.setCaption( caption( "table caption" ) );
		table.setHeader( header( row( heading( "heading 1" ), heading( "heading 2" ) ) ) );
		table.setBody( body( row( cell( "one" ), cell( "two" ) ) ) );
		table.setFooter( footer( row( cell( "three" ), cell( "four" ) ) ) );
		table.setColumnGroup( colgroup() );

		renderAndExpect(
				table,
				"<table class='table'>" +
						"<caption>table caption</caption>" +
						"<colgroup><col span='2' class='column-class'/></colgroup>" +
						"<thead>" +
						"<tr><th>heading 1</th><th>heading 2</th></tr>" +
						"</thead>" +
						"<tbody>" +
						"<tr><td>one</td><td>two</td></tr>" +
						"</tbody>" +
						"<tfoot>" +
						"<tr><td>three</td><td>four</td></tr>" +
						"</tfoot>" +
						"</table>"
		);

	}

	private TableViewElement.ColumnGroup colgroup() {
		TableViewElement.ColumnGroup columnGroup = new TableViewElement.ColumnGroup();
		TableViewElement.ColumnGroup.Column column = new TableViewElement.ColumnGroup.Column();
		column.setSpan( 2 );
		column.setAttribute( "class", "column-class" );

		columnGroup.addChild( column );

		return columnGroup;
	}

	private TableViewElement.Caption caption( String text ) {
		TableViewElement.Caption caption = new TableViewElement.Caption();
		caption.setText( text );

		return caption;
	}

	private TableViewElement.Header header( TableViewElement.Row... rows ) {
		TableViewElement.Header header = new TableViewElement.Header();

		for ( TableViewElement.Row row : rows ) {
			header.addChild( row );
		}

		return header;
	}

	private TableViewElement.Footer footer( TableViewElement.Row... rows ) {
		TableViewElement.Footer footer = new TableViewElement.Footer();

		for ( TableViewElement.Row row : rows ) {
			footer.addChild( row );
		}

		return footer;
	}

	private TableViewElement.Body body( TableViewElement.Row... rows ) {
		TableViewElement.Body body = new TableViewElement.Body();

		for ( TableViewElement.Row row : rows ) {
			body.addChild( row );
		}

		return body;
	}

	private TableViewElement.Row row( TableViewElement.Cell... cells ) {
		TableViewElement.Row row = new TableViewElement.Row();

		for ( TableViewElement.Cell cell : cells ) {
			row.addChild( cell );
		}

		return row;
	}

	private TableViewElement.Cell heading( String text ) {
		TableViewElement.Cell cell = new TableViewElement.Cell();
		cell.setHeading( true );
		cell.setText( text );

		return cell;
	}

	private TableViewElement.Cell cell( String text ) {
		TableViewElement.Cell cell = new TableViewElement.Cell();
		cell.addChild( new TextViewElement( text ) );

		return cell;
	}
}
