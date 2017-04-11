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

import org.junit.Test;

/**
 * @author Arne Vandamme
 * @since 1.0.0
 */
public class TestFileUploadFormElement extends AbstractBootstrapViewElementTest
{
	@Test
	public void simpleElement() {
		FileUploadFormElement upload = new FileUploadFormElement();
		upload.setControlName( "myfile" );

		renderAndExpect(
				upload,
				"<input type='file' id='myfile' name='myfile' />"
		);
	}

	@Test
	public void accept() {
		FileUploadFormElement upload = new FileUploadFormElement();
		upload.setAccept( "image/*" );
		renderAndExpect(
				upload,
				"<input type='file' accept='image/*' />"
		);

		upload.setAccept( null );
		renderAndExpect(
				upload,
				"<input type='file' />"
		);
	}

	@Test
	public void multiple() {
		FileUploadFormElement upload = new FileUploadFormElement();

		upload.setMultiple( true );
		renderAndExpect(
				upload,
				"<input type='file' multiple='true' />"
		);

		upload.setMultiple( false );
		renderAndExpect(
				upload,
				"<input type='file' />"
		);
	}

	@Test
	public void value() {
		FileUploadFormElement upload = new FileUploadFormElement();

		upload.setValue( "c:/myfile" );
		renderAndExpect(
				upload,
				"<input type='file' value='c:/myfile' />"
		);

		upload.setValue( null );
		renderAndExpect(
				upload,
				"<input type='file' />"
		);
	}
}
