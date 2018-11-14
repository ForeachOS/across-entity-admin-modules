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

package com.foreach.across.samples.bootstrapui.application.controllers;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.FileUploadFormElement;
import com.foreach.across.modules.bootstrapui.resource.BootstrapUiFormElementsWebResources;
import com.foreach.across.modules.web.events.BuildMenuEvent;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generates a fileUpload control using a {@link FileUploadFormElement}.
 *
 * @author Vanhoof Stijn
 * @since 1.0.0
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/file")
public class FileUploadFormController
{
	@EventListener(condition = "#navMenu.menuName=='navMenu'")
	public void registerMenuItems( BuildMenuEvent navMenu ) {
		navMenu.builder()
		       .item( "/test/form-elements/file", "File upload", "/file" ).order( 6 );
	}

	@RequestMapping(method = RequestMethod.GET)
	public String renderFileUpload( Model model, ViewElementBuilderContext builderContext, WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.addPackage( BootstrapUiFormElementsWebResources.NAME );

		Map<String, ViewElement> generatedElements = new LinkedHashMap<>();
		generatedElements.put( "Singe file upload", singeFIle() );
		generatedElements.put( "MultiFileUpload", multiFile() );
		generatedElements.put( "Only pdf files allowed ", pdfUploadOnly() );

		model.addAttribute( "generatedElements", generatedElements );

		return "th/bootstrapUiTest/elementsRendering";
	}

	private FileUploadFormElement singeFIle() {
		return BootstrapUiBuilders.file()
		                          .controlName( "singleFile" )
		                          .build();
	}

	private FileUploadFormElement multiFile() {
		return BootstrapUiBuilders.file()
		                          .controlName( "multiFIle" )
		                          .multiple( true )
		                          .build();
	}

	private FileUploadFormElement pdfUploadOnly() {
		return BootstrapUiBuilders.file()
		                          .controlName( "singleFile" )
		                          .accept( ".pdf" )
		                          .build();
	}
}
