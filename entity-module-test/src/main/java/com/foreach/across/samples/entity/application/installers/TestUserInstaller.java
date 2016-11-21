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
import com.foreach.across.samples.entity.application.business.Group;
import com.foreach.across.samples.entity.application.business.User;
import com.foreach.across.samples.entity.application.repositories.GroupRepository;
import com.foreach.across.samples.entity.application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.stream.Stream;

import static java.util.Calendar.*;

/**
 * @author Arne Vandamme
 * @since 2.0.0
 */
@Order(2)
@Installer(description = "Installs a number of test users for filtering", phase = InstallerPhase.AfterModuleBootstrap)
public class TestUserInstaller
{
	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private UserRepository userRepository;

	private transient Calendar cal;

	@InstallerMethod
	public void installTestUsersInGroup() {
		cal = Calendar.getInstance();
		cal.set( YEAR, 2016 );
		cal.set( MONTH, JANUARY );
		cal.set( DAY_OF_MONTH, 1 );
		cal.set( HOUR_OF_DAY, 10 );

		Group group = groupRepository.findOne( -1L );

		Stream.of( "john", "joey", "jane", "paul" )
		      .forEach( s -> {
			      for ( int i = 0; i < 15; i++ ) {
				      createUserInGroup( s + " " + i, group );
			      }
		      } );

		// verify installed groups
		Assert.isTrue( userRepository.findByGroup( group, new PageRequest( 0, 30 ) ).getTotalElements() == 60 );
		Assert.isTrue( userRepository.findByGroupAndNameContaining( group, "j", new PageRequest( 0, 30 ) )
		                             .getTotalElements() == 45 );
	}

	private void createUserInGroup( String name, Group group ) {
		User user = new User();
		user.setName( name );
		user.setGroup( group );
		user.setRegistrationDate( cal.getTime() );

		cal.add( DAY_OF_MONTH, 5 );
		cal.add( HOUR_OF_DAY, 1 );
		cal.add( MINUTE, 15);

		userRepository.save( user );
	}
}
