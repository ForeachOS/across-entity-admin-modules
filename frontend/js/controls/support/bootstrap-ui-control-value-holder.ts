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

export function createControlValueHolder( label: string, value: any, context: any ): BootstrapUiControlValueHolder {
    return {label, value, context}
}

export default interface BootstrapUiControlValueHolder
{
    /**
     * Displayed representation of the value
     */
    readonly label: string;

    /**
     * Actual value of the displayed element
     */
    readonly value: any;

    /**
     * Context of the value, e.g. which element defined this value.
     */
    readonly context: any;
}