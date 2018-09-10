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

package admin.application.config;

import com.foreach.across.modules.adminweb.events.UserContextAdminMenuGroup;
import com.foreach.across.modules.adminweb.menu.AdminMenuEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class UserContextConfiguration
{
	@EventListener
	public void registerDefaultUserContextAdminMenuItem( UserContextAdminMenuGroup userContextAdminMenuGroup ) {
		if ( "admin".equals( userContextAdminMenuGroup.getDisplayName() ) ) {
			userContextAdminMenuGroup.setDisplayName( "Administrator" );
			userContextAdminMenuGroup.setThumbnailUrl( "http://www.gravatar.com/avatar/73543542128f5a067ffc34305eefe48a" );
		}
	}

	@EventListener
	public void buildUserContextMenu( AdminMenuEvent menu ) {
		menu.item( UserContextAdminMenuGroup.MENU_PATH + "/profile" )
		    .title( "Your profile" )
		    .url( "@adminWeb:/user/profile" );
	}
}
