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

package com.foreach.across.modules.entity.query.support;

import com.foreach.across.modules.entity.query.EQString;
import com.foreach.across.modules.entity.query.EQType;
import com.foreach.across.modules.entity.query.EQTypeConverter;
import com.foreach.across.modules.entity.query.EntityQueryFunctionHandler;
import com.foreach.across.modules.entity.util.StringToDurationWithPeriodConverter;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.convert.TypeDescriptor;

import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.Locale;

/**
 * Provides date related functions to be used in entity queries.
 * <ul>
 * <li>now(): returns the current time</li>
 * <li>today(): returns the date of today</li>
 * </ul>
 * Supported property types are {@link Date} and {@link Long}.
 *
 * @author Arne Vandamme
 * @since 2.0.0
 */
public class EntityQueryDateFunctions implements EntityQueryFunctionHandler
{
	public static final String NOW = "now";
	public static final String TODAY = "today";

	public static final String MONDAY = "monday";
	public static final String TUESDAY = "tuesday";
	public static final String WEDNESDAY = "wednesday";
	public static final String THURSDAY = "thursday";
	public static final String FRIDAY = "friday";
	public static final String SATURDAY = "saturday";
	public static final String SUNDAY = "sunday";
	public static final String DAY = "day";
	public static final String WEEK = "week";
	public static final String WEEK_NUMBER = "weekNumber";
	public static final String MONTH = "month";
	public static final String YEAR = "year";

	private static final String[] FUNCTION_NAMES = new String[] { NOW, TODAY };

	@Override
	public boolean accepts( String functionName, TypeDescriptor desiredType ) {
		return ArrayUtils.contains( FUNCTION_NAMES, functionName ) && (
				Date.class.equals( desiredType.getObjectType() )
						|| LocalDateTime.class.equals( desiredType.getObjectType() )
						|| Long.class.equals( desiredType.getObjectType() )
		);
	}

	@Override
	public Object apply( String functionName,
	                     EQType[] arguments,
	                     TypeDescriptor desiredType,
	                     EQTypeConverter argumentConverter ) {
		LocalDateTime calculatedDateTime = calculateDate( functionName );
		calculatedDateTime = addDateTimeModifiers( calculatedDateTime, arguments );

		return convertToDesiredType( calculatedDateTime, desiredType.getObjectType() );
	}

	/**
	 * Calculate the right localDateTime that is represented by the functionName
	 *
	 * @param functionName The name of the function
	 * @return The resulting {@link LocalDateTime}
	 */
	private LocalDateTime calculateDate( String functionName ) {
		LocalDate today = LocalDate.now();

		switch ( functionName ) {
			case TODAY:
				return today.atStartOfDay();
			case DAY:
				return today.atStartOfDay();
			case MONDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY) ).atStartOfDay();
			case TUESDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.TUESDAY) ).atStartOfDay();
			case WEDNESDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY) ).atStartOfDay();
			case THURSDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY) ).atStartOfDay();
			case FRIDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY) ).atStartOfDay();
			case SATURDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY) ).atStartOfDay();
			case SUNDAY:
				return today.with( TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY) ).atStartOfDay();
			case WEEK:
				DayOfWeek firstDayOfWeek = WeekFields.of( Locale.getDefault() ).getFirstDayOfWeek();

				return today.with( firstDayOfWeek ).atStartOfDay();
			case WEEK_NUMBER:
				break;
			case MONTH:
				return today.withDayOfMonth( 1 ).atStartOfDay();
			case YEAR:
				return today.withDayOfYear( 1 ).atStartOfDay();
		}

		return LocalDateTime.now();
	}

	/**
	 * Parse all string arguments to {@link Duration} objects and modify the date
	 *
	 * @param dateTime  The calculated dateTime
	 * @param arguments An array of modifiers represented by strings y|M|w|d|h|m e.g. +1d, -1y, ...
	 * @return the modified calculated dateTime
	 */
	private LocalDateTime addDateTimeModifiers( LocalDateTime dateTime, EQType[] arguments ) {
		for ( EQType argument : arguments ) {
			if ( EQString.class.isAssignableFrom( argument.getClass() ) ) {
				DurationWithPeriod durationWithPeriod = StringToDurationWithPeriodConverter.of( ( (EQString) argument ).getValue() );
				dateTime = dateTime.plus( durationWithPeriod.getPeriod() );
				dateTime = dateTime.plus( durationWithPeriod.getDuration() );
			}
		}

		return dateTime;
	}

	/**
	 * Convert the calculated dateTime to the desired object type
	 *
	 * @param dateTime    The calculated dateTime
	 * @param desiredType The type that is expected as output
	 * @return An instance of the desiredType
	 */
	private Object convertToDesiredType( LocalDateTime dateTime, Class<?> desiredType ) {
		if ( Long.class.equals( desiredType ) ) {
			return Date.from( dateTime.atZone( ZoneId.systemDefault() ).toInstant() ).getTime();
		}

		if ( Date.class.equals( desiredType ) ) {
			return Date.from( dateTime.atZone( ZoneId.systemDefault() ).toInstant() );
		}

		return dateTime;
	}
}