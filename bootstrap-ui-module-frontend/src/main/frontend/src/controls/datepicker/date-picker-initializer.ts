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
 * Find and activate all date time pickers as <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Eonasdan Bootstrap datepicker</a> elements.
 *
 * @author Steven Gentens
 * @since 2.2.0
 */

import $ from 'jquery';
import moment from 'moment';

function initializeDateTimePickers( node: any ): void
{
    $.fn.datetimepicker.Constructor.Default = $.extend( {}, $.fn.datetimepicker.Constructor.Default, {
        icons: {
            time: 'far fa-clock',
            date: 'far fa-calendar',
            up: 'fas fa-arrow-up',
            down: 'fas fa-arrow-down',
            previous: 'fas fa-chevron-left',
            next: 'fas fa-chevron-right',
            today: 'far fa-calendar-check',
            clear: 'fas fa-trash',
            close: 'fas fa-times',
        },
    } );

    $( '[data-bootstrapui-datetimepicker]', node ).each( function () {
        const configuration = $.extend( true, {}, $( this ).data( 'bootstrapui-datetimepicker' ) );
        const exportFormat = configuration.exportFormat;

        delete configuration.exportFormat;

        $( this ).datetimepicker( configuration );
        $( this ).on( 'change.datetimepicker', ( e: any ) => {
            const exchangeValue = e.date ? moment( e.date ).format( exportFormat ) : '';
            $( 'input[type=hidden]', $( this ) ).attr( 'value', exchangeValue );
        } );
        $( this ).find( 'input[type="text"]' ).on( 'blur focusout', () => {
            $( this ).data( 'datetimepicker' ).hide();
        } );
    } );
}

export default initializeDateTimePickers;
