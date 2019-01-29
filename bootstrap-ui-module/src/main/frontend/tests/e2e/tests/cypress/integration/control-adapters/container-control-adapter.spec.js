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

describe( 'ControlAdapter - Container', function () {

    before( function () {
        cy.visit( "/control-adapters" );
    } );

    it( "element exists", function () {
        cy.get( "#options-ca-multi-checkbox" )
                .then( element => {
                    expect( element.data( 'bootstrapui-adapter-type' ) ).to.be.eq( "container" );
                    expect( element.data( 'bootstrapui-adapter' ) ).to.not.be.undefined;
                } );
    } );

    it( "has underlying control adapters", function () {
        cy.get( "#options-ca-multi-checkbox" )
                .then( ( wrapper ) => {
                    expect( wrapper.find( "[data-bootstrapui-adapter-type]" ).length ).to.eq( 3 );
                } );
    } );

    it( "modifying value", function () {
        cy.get( "#options-ca-multi-checkbox" )
                .then( ( wrapper ) => {
                    const adapter = wrapper.data( "bootstrapui-adapter" );
                    expect( () => adapter.selectValue( "anything" ) ).to.throw( 'Selecting values is currently not support on ContainerControlAdapters.' );
                } );
    } );

    it( "bootstrapui.change event is fired if a child element is fired", function () {
        cy.get( "#options-ca-multi-checkbox" )
                .then( ( wrapper ) => {
                    const adapter = wrapper.data( "bootstrapui-adapter" );

                    const obj = {
                        handle( controlAdapter ) {
                            return controlAdapter;
                        }
                    };
                    const spy = cy.spy( obj, 'handle' );

                    wrapper.on( "bootstrapui.change", function ( event, controlAdapter ) {
                        if ( event.target === adapter.getTarget() ) {
                            obj.handle( controlAdapter );
                        }
                    } );

                    wrapper.find( "[type=checkbox]" ).first().trigger( 'change' );

                    expect( spy ).to.have.callCount( 1 );
                    expect( spy ).to.have.returned( adapter );
                } );
    } );

    it( "reset applies reset on underlying control adapters", function () {
        cy.get( '#options-ca-multi-checkbox' )
                .find( '[type=checkbox]' )
                .each( ( cb ) => {
                    cb.prop( 'checked', true );
                    expect( cb.is( ':checked' ) ).to.be.true;
                } )
                .closest( '[data-bootstrapui-adapter-type="container"]' )
                .then( ( container ) => {
                    const adapter = container.data( 'bootstrapui-adapter' );
                    expect( adapter.getValue() ).to.have.length( 3 );

                    adapter.reset();
                    expect( adapter.getValue() ).to.have.length( 0 );
                    expect( Cypress.$( ':checked', container ) ).to.have.property( 'length', 0 );
                } );
    } );

    it( "value is defined by values of underlying control adapters", function () {
        cy.get( '#options-ca-multi-checkbox' )
                .find( '[type=checkbox]' )
                .each( ( cb, idx ) => {
                    if ( idx % 2 === 0 ) {
                        cb.prop( 'checked', true );
                        expect( cb.is( ':checked' ) ).to.be.true;
                    }
                } )
                .closest( '[data-bootstrapui-adapter-type="container"]' )
                .then( ( container ) => {
                    const adapter = container.data( 'bootstrapui-adapter' );
                    const currentValues = adapter.getValue();
                    expect( currentValues ).to.have.length( 2 );

                    function checkValue( valueholder, label, value ) {
                        expect( valueholder ).to.have.property( 'label', label );
                        expect( valueholder ).to.have.property( 'value', value );
                    }

                    checkValue( currentValues[0], 'One', '1' );
                    checkValue( currentValues[1], '3', 'Three' );
                } );
    } )

} );