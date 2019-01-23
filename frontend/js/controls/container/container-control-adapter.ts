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
 * @author Steven Gentens
 * @since 2.2.0
 */
import BaseControlAdapter from '../support/base-control-adapter';
import BootstrapUiControlValueHolder from '../support/bootstrap-ui-control-value-holder';
import BootstrapUiAttributes from '../support/bootstrap-ui-attributes';
import BootstrapUiControlAdapter from '../support/bootstrap-ui-control-adapter';
import {ControlAdapterFactory} from '../support/control-adapter-factory';

/**
 * {@link BootstrapUiControlAdapter} for elements whose state depends on the state of its children.
 */
export default class ContainerControlAdapter extends BaseControlAdapter
{
    private readonly adapters: any[];

    constructor( target: any ) {
        super( target );
        const controlElements: BootstrapUiControlAdapter[] = [];
        $( target ).find( `[data-${BootstrapUiAttributes.CONTROL_ADAPTER_TYPE}]` )
            .each( ( index, element ) => {
                const adapter = $( element ).data( BootstrapUiAttributes.CONTROL_ADAPTER );
                controlElements.push( adapter );

                $( adapter.getTarget() ).on( 'bootstrapui.change', ( event ) => {
                    this.triggerChange();
                } );
                $( adapter.getTarget() ).on( 'bootstrapui.submit', event => this.triggerSubmit() );
            } );
        this.adapters = controlElements;
    }

    getValue(): BootstrapUiControlValueHolder[] {
        return [].concat( [].concat( ...this.adapters.map( adapter => adapter.getValue() ) ) );
        // return [createControlValueHolder( target.parent().text(), target.attr( 'value' ), this.getTarget() )];
    }

    reset(): void {
        this.adapters.forEach( adapter => adapter.reset() );
    }

    selectValue( select: boolean ): void {
        throw new Error( 'Selecting values is currently not support on ContainerControlAdapters.' );
    }
}

/**
 * Initializes a {@link ContainerControlAdapter} for a given node.
 * Before initializing the container node, potentially underlying nodes will be initialized as well.
 *
 * @param node to initialize
 */
export function createContainerControlAdapter( node: any ): BootstrapUiControlAdapter {
    ControlAdapterFactory.initializeNode( node );
    return new ContainerControlAdapter( node );
}
