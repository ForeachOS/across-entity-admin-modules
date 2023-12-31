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

export function createCookie( name, value, days ) {
  let expires;
  if ( days ) {
    const date = new Date();
    date.setTime( date.getTime() + (days * 24 * 60 * 60 * 1000) );
    expires = `expires=${date.toGMTString()}`;
  }
  else {
    expires = "";
  }
  document.cookie = `${name}=${value}${expires}; path=/`;
}

export function readCookie( name ) {
  const nameEQ = `${name}=`;
  const ca = document.cookie.split( ";" );
  for ( let i = 0; i < ca.length; i += 1 ) {
    let c = ca[i];
    while ( c.charAt( 0 ) === " " ) {
      c = c.substring( 1, c.length );
    }
    if ( c.indexOf( nameEQ ) === 0 ) {
      return c.substring( nameEQ.length, c.length );
    }
  }
  return null;
}

export function eraseCookie( name ) {
  createCookie( name, "", -1 );
}
