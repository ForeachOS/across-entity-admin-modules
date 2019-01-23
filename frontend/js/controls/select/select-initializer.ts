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

/**
 * Find and activate all <a href="https://silviomoreto.github.io/bootstrap-select/">bootstrap-select</a> elements.
 */
function selectInitializer( node: any ): void {
    $( '[data-bootstrapui-select]', node ).each( function () {
        const configuration = $( this ).data( 'bootstrapui-select' );
        $( this ).selectpicker( configuration );
    } );
}

export default selectInitializer;
