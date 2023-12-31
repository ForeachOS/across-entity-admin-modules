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

package com.foreach.across.samples.entity.application.installers;

import com.foreach.across.core.annotations.Installer;
import com.foreach.across.core.annotations.InstallerMethod;
import com.foreach.across.core.installers.InstallerPhase;
import com.foreach.across.samples.entity.application.business.Note;
import com.foreach.across.samples.entity.application.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Steven Gentens
 * @since 2.2.0
 */
@Installer(description = "Installs some test notes", phase = InstallerPhase.AfterModuleBootstrap)
@RequiredArgsConstructor
public class TestNoteInstaller
{
	private final NoteRepository noteRepository;

	@InstallerMethod
	public void installNotes() {
		String[] words = "Lorem ipsum dolor sit amet consectetur adipiscing elit Integer nec orci quis purus efficitur tempus".split( " " );
		Random random = new Random();

		for ( int i = 0; i < 3; i++ ) {
			Arrays.stream( words ).forEach(
					word -> {
						Note note = new Note();
						note.setName( word );
						String content = words[random.nextInt( words.length )] + " " + words[random.nextInt( words.length )] + " "
								+ words[random.nextInt( words.length )];
						note.setContent( content );
						noteRepository.save( note );
					}
			);
		}
	}
}
