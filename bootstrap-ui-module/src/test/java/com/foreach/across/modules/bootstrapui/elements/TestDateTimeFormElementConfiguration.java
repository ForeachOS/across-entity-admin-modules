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
package com.foreach.across.modules.bootstrapui.elements;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.foreach.across.modules.bootstrapui.elements.DateTimeFormElementConfiguration.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestDateTimeFormElementConfiguration
{
	private static final Date PRINT_DATE;

	static {
		try {
			PRINT_DATE = DateUtils.parseDate( "2015-08-07 10:31:22", "yyyy-MM-dd HH:mm:ss" );
		}
		catch ( ParseException pe ) {
			throw new RuntimeException( pe );
		}
	}

	public static final String REQUIRED_LC_ENV = "en_US.UTF-8";

	@BeforeAll
	static void beforeAll() {
		String lcTime = System.getenv( "LC_TIME" );
		assertTrue( lcTime == null || lcTime.equals( REQUIRED_LC_ENV ), "Use LC_TIME=" + REQUIRED_LC_ENV );
	}

	@Test
	public void newConfiguration() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		assertEquals( Format.DATETIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "nl-BE", localized.get( "locale" ) );

		assertEquals( "07-Aug-2015 10:31", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "7-aug-2015 10:31", localized.createDateFormat().format( PRINT_DATE ) );

		configuration.setLocalizePatterns( false );
		assertEquals( "en-GB", configuration.localize( Locale.forLanguageTag( "nl-BE" ) ).get( "locale" ) );
	}

	@Test
	public void datetimeFull() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.DATETIME_FULL
		);
		assertEquals( Format.DATETIME_FULL, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME_FULL, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_PATTERN_DATETIME,
				               FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "07 August 2015 10:31:22", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "7 augustus 2015 10:31:22", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void date() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.DATE );
		assertEquals( Format.DATE, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATE, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATE, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "07-Aug-2015", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "7-aug-2015", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void dateFull() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.DATE_FULL );
		assertEquals( Format.DATE_FULL, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATE_FULL, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATE, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "Friday, 7 August 2015", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "vrijdag 7 augustus 2015", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void time() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration(
				Format.TIME );
		assertEquals( Format.TIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_TIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_TIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME },
				(String[]) configuration.get( "extraFormats" )
		);

		DateTimeFormElementConfiguration localized = configuration.localize( Locale.forLanguageTag( "nl-BE" ) );
		assertEquals( "10:31", configuration.createDateFormat().format( PRINT_DATE ) );
		assertEquals( "10:31", localized.createDateFormat().format( PRINT_DATE ) );
	}

	@Test
	public void nullValuesDoNotBreak() {
		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();

		configuration.setDefaultDate( (LocalDateTime) null );
		assertNull( configuration.get( "defaultDate" ) );

		configuration.setDefaultDate( (LocalDate) null );
		assertNull( configuration.get( "defaultDate" ) );

		configuration.setDefaultDate( (LocalTime) null );
		assertNull( configuration.get( "defaultDate" ) );

		configuration.setDefaultDate( (Date) null );
		assertNull( configuration.get( "defaultDate" ) );
	}

	@Test
	public void customAttributes() throws ParseException {
		Date start = DateUtils.parseDate( "2015-08-07 10:31", "yyyy-MM-dd HH:mm" );
		Date end = DateUtils.parseDate( "2015-08-08 10:31", "yyyy-MM-dd HH:mm" );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		configuration.setMinDate( start );
		configuration.setMaxDate( end );
		configuration.setShowClearButton( true );

		assertEquals( Format.DATETIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		assertEquals( "2015-08-07 10:31", configuration.get( "minDate" ) );
		assertEquals( "2015-08-08 10:31", configuration.get( "maxDate" ) );
		Map<String, Boolean> buttons = (Map<String, Boolean>) configuration.get( "buttons" );
		assertEquals( true, buttons.get( "showClear" ) );
	}

	@Test
	public void enabledDates() throws ParseException {
		Date start = DateUtils.parseDate( "2015-08-07 10:31", "yyyy-MM-dd HH:mm" );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		configuration.setEnabledDates( new Date[]{start});

		assertEquals( Format.DATETIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		Object[] enabledDates = (Object[]) configuration.get( "enabledDates" );
		assertEquals( 1, enabledDates.length );
		assertEquals( "2015-08-07 10:31", enabledDates[0] );
	}

	@Test
	public void disabledDates() throws ParseException {
		Date disabledDate = DateUtils.parseDate( "2015-08-07 10:31", "yyyy-MM-dd HH:mm" );
		Date disabledDate2 = DateUtils.parseDate( "2015-08-08 10:31", "yyyy-MM-dd HH:mm" );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		configuration.setDisabledDates( new Date[]{disabledDate, disabledDate2});

		assertEquals( Format.DATETIME, configuration.getFormat() );
		assertEquals( FMT_PATTERN_DATETIME, configuration.get( "format" ) );
		assertEquals( "en-GB", configuration.get( "locale" ) );
		assertEquals( FMT_EXPORT_MOMENT_DATETIME, configuration.get( "exportFormat" ) );
		assertArrayEquals(
				new String[] { FMT_EXPORT_MOMENT_DATETIME, FMT_PATTERN_DATE, FMT_EXTRA_PATTERN_DATE },
				(String[]) configuration.get( "extraFormats" )
		);

		Object[] disabledDates = (Object[]) configuration.get( "disabledDates" );
		assertEquals( 2, disabledDates.length );
		assertEquals( "2015-08-07 10:31", disabledDates[0] );
		assertEquals( "2015-08-08 10:31", disabledDates[1] );
	}

	@Test
	public void dateToLocalDateConversion() throws Exception {
		Date date = DateUtils.parseDate( "2015-08-07 10:31", "yyyy-MM-dd HH:mm" );
		LocalDateTime localDateTime = LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() );

		assertEquals(
				"2015-08-07 10:31",
				DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ).format( localDateTime )
		);

		assertEquals( date, Date.from( localDateTime.atZone( ZoneId.systemDefault() ).toInstant() ) );
	}

	@Test
	public void sqlDateAndDateAreSuccessfullyConverted() throws ParseException {
		Date date = DateUtils.parseDate( "2015-08-07 00:00", "yyyy-MM-dd HH:mm" );
		DateTimeFormElementConfiguration dateConfiguration = new DateTimeFormElementConfiguration();
		dateConfiguration.setDateAttribute( "test", date );

		java.sql.Date sqlDate = new java.sql.Date( date.getTime() );
		DateTimeFormElementConfiguration sqlDateConfiguration = new DateTimeFormElementConfiguration();
		sqlDateConfiguration.setDateAttribute( "test", sqlDate );

		assertEquals( dateConfiguration.get( "test" ), sqlDateConfiguration.get( "test" ) );
	}

	@Test
	public void customAttributesForJava8Times() throws ParseException {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" );
		LocalDateTime start = LocalDateTime.parse( "2015-08-07 10:31", formatter );
		LocalDateTime end = LocalDateTime.parse( "2015-08-08 10:31", formatter );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration();
		configuration.setMinDate( start );
		configuration.setMaxDate( end );
		configuration.setShowClearButton( true );

		assertEquals( "2015-08-07 10:31", configuration.get( "minDate" ) );
		assertEquals( "2015-08-08 10:31", configuration.get( "maxDate" ) );

		configuration = new DateTimeFormElementConfiguration();
		configuration.setMinDate( start.toLocalDate() );
		configuration.setMaxDate( end.toLocalDate() );

		assertEquals( "2015-08-07 00:00", configuration.get( "minDate" ) );
		assertEquals( "2015-08-08 00:00", configuration.get( "maxDate" ) );

		configuration = new DateTimeFormElementConfiguration();
		configuration.setMinDate( start.toLocalTime() );
		configuration.setMaxDate( end.toLocalTime() );
		configuration.setShowClearButton( true );

		String localDateAsString = LocalDate.now().format( DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) );

		assertEquals( localDateAsString + " 10:31", configuration.get( "minDate" ) );
		assertEquals( localDateAsString + " 10:31", configuration.get( "maxDate" ) );

		DateTimeFormElementConfiguration configurationWithDate = new DateTimeFormElementConfiguration();
		Date date = DateUtils.parseDate( "2015-08-07 10:31", "yyyy-MM-dd HH:mm" );
		configuration.setDefaultDate( date );

		DateTimeFormElementConfiguration configurationWithJava8Date = new DateTimeFormElementConfiguration();
		configuration.setDefaultDate( LocalDateTime.ofInstant( date.toInstant(), configuration.getZoneId() ) );

		assertEquals( configurationWithDate.get( "defaultDate" ), configurationWithJava8Date.get( "defaultDate" ) );

	}

	@Test
	public void dateTimeFormatterIsEqualToDateFormat() {
		LocalDateTime localDateTime = LocalDateTime.ofInstant( PRINT_DATE.toInstant(), ZoneId.systemDefault() );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration( Format.DATE );
		assertEquals( configuration.createDateFormat().format( PRINT_DATE ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.DATE_FULL );
		assertEquals( configuration.createDateFormat().format( PRINT_DATE ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.TIME );
		assertEquals( configuration.createDateFormat().format( PRINT_DATE ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.DATETIME );
		assertEquals( configuration.createDateFormat().format( PRINT_DATE ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.DATETIME_FULL );
		assertEquals( configuration.createDateFormat().format( PRINT_DATE ), configuration.createDateTimeFormatter().format( localDateTime ) );
	}

	@Test
	public void localizedDateTimeFormatterIsEqualToDateFormat() {
		Date d1 = Date.from( Instant.EPOCH );
		Date d2 = Date.from( Instant.now() );

		List<Date> randomDates = new ArrayList<>();
		randomDates.add( PRINT_DATE );
		for ( int i = 0; i < 2000; i += 1 ) {
			randomDates.add( getRandomDateBetween( d1, d2 ) );
		}

		//TODO: might have to rethink this or correct this if  we want to support all 700'ish locales from JDK11
		List<String> supportedLocales = Arrays.asList( "", "ar_AE", "ar_JO", "ar_SY", "hr_HR", "fr_BE", "es_PA", "mt_MT", "es_VE", "bg", "zh_TW", "it", "ko",
		                                               "uk", "lv",
		                                               "da_DK", "es_PR", "vi_VN", "en_US", "sr_ME", "sv_SE", "es_BO", "en_SG", "ar_BH", "pt", "ar_SA", "sk",
		                                               "ar_YE",
		                                               "ga", "en_MT", "et", "sv", "cs", "sr_BA_#Latn", "el", "uk_UA", "hu", "fr_CH", "in",
		                                               "es_AR",
		                                               "ar_EG", "ja_JP_JP_#u-ca-japanese", "es_SV", "pt_BR", "be", "is_IS", "cs_CZ", "es", "pl_PL", "tr",
		                                               "ca_ES",
		                                               "sr_CS", "ms_MY", "hr", "lt", "es_ES", "es_CO", "bg_BG", "sq", "fr", "ja", "sr_BA", "is", "es_PY", "de",
		                                               "es_EC",
		                                               "es_US", "ar_SD", "en", "ro_RO", "en_PH", "ca", "ar_TN", "sr_ME_#Latn", "es_GT", "sl", "ko_KR", "el_CY",
		                                               "es_MX",
		                                               "ru_RU", "es_HN", "zh_HK", "no_NO_NY", "hu_HU", "ar_IQ", "es_CL", "ar_MA", "ga_IE", "mk",
		                                               "tr_TR",
		                                               "et_EE", "ar_QA", "sr__#Latn", "pt_PT", "fr_LU", "ar_OM", "sq_AL", "es_DO", "es_CU", "ar", "ru",
		                                               "en_NZ",
		                                               "sr_RS", "de_CH", "es_UY", "ms", "el_GR", "iw_IL", "en_ZA", "fr_FR",
		                                               "de_AT", "nl",
		                                               "no_NO", "en_AU", "vi", "nl_NL", "fr_CA", "lv_LV", "de_LU", "es_CR", "ar_KW", "sr", "ar_LY", "mt",
		                                               "it_CH", "da",
		                                               "de_DE", "ar_DZ", "sk_SK", "lt_LT", "it_IT", "en_IE", "zh_SG", "ro", "en_CA", "nl_BE", "no", "pl",
		                                               "zh_CN",
		                                               "ja_JP", "de_GR", "sr_RS_#Latn", "iw", "en_IN", "ar_LB", "es_NI", "zh", "mk_MK", "be_BY", "sl_SI",
		                                               "es_PE",
		                                               "in_ID", "en_GB" );
		for ( Date date : randomDates ) {
			supportedLocales.stream().map( Locale::forLanguageTag ).forEach( locale -> {
				assertLocalizedFormattersReturnEqualValues( date, locale );
			} );
		}
	}

	private void assertLocalizedFormattersReturnEqualValues( Date date, Locale locale ) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant( date.toInstant(), ZoneId.systemDefault() );

		DateTimeFormElementConfiguration configuration = new DateTimeFormElementConfiguration( Format.DATE );
		configuration.setLocale( locale );
		assertEquals( configuration.createDateFormat().format( date ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.DATE_FULL );
		configuration.setLocale( locale );
		assertEquals( configuration.createDateFormat().format( date ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.TIME );
		configuration.setLocale( locale );
		assertEquals( configuration.createDateFormat().format( date ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.DATETIME );
		configuration.setLocale( locale );
		assertEquals( configuration.createDateFormat().format( date ), configuration.createDateTimeFormatter().format( localDateTime ) );

		configuration = new DateTimeFormElementConfiguration( Format.DATETIME_FULL );
		configuration.setLocale( locale );
		assertEquals( configuration.createDateFormat().format( date ), configuration.createDateTimeFormatter().format( localDateTime ) );
	}

	private Date getRandomDateBetween( Date start, Date end ) {
		long diff = end.getTime() - start.getTime() + 1;
		return new Date( start.getTime() + (long) ( Math.random() * diff ) );
	}
}
