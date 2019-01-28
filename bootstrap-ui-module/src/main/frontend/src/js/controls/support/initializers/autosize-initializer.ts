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

declare const autosize: any;

/**
 * Finds and activates autogrow textarea elements and disables resizing on textareas that do not allow line breaks.
 */
function autosizeInitializer( node: any ): void {
    autosize( $( '.js-autosize', node ) );
    $( '.js-disable-line-breaks.js-autosize' ).css( 'resize', 'none' );
}

export default autosizeInitializer;
