package com.foreach.across.module.applicationinfo.business;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static org.junit.Assert.*;

public class TestAcrossApplicationInfo
{
	private Logger logger = LoggerFactory.getLogger( TestAcrossApplicationInfo.class );

	private AcrossApplicationInfoImpl applicationInfo;

	@Before
	public void reset() {
		applicationInfo = new AcrossApplicationInfoImpl();
	}

	@Test
	public void uptimeCalculation() throws InterruptedException {
		applicationInfo.setBootstrapEndDate( new Date() );

		long first = applicationInfo.getUptime();
		logger.debug( "initial uptime: " + first );
		assertTrue( first >= 0 );

		Thread.sleep( 15 );
		logger.debug( "uptime after thread sleep: " + applicationInfo.getUptime() );
		assertTrue( applicationInfo.getUptime() > first );
//		assertTrue( applicationInfo.getUptime() >= 5 );
	}

	@Test
	public void uptimeIsZeroIfBootstrapNotFinished() throws InterruptedException {
		assertEquals( 0, applicationInfo.getUptime() );
		Thread.sleep( 15 );
		assertEquals( 0, applicationInfo.getUptime() );
	}

	@Test
	public void bootstrapDurationIsZeroIfBootstrapNotFinished() {
		assertEquals( 0, applicationInfo.getBootstrapDuration() );

		applicationInfo.setBootstrapStartDate( new Date() );
		assertEquals( 0, applicationInfo.getBootstrapDuration() );

		applicationInfo.setBootstrapStartDate( null );
		applicationInfo.setBootstrapEndDate( new Date() );
		assertEquals( 0, applicationInfo.getBootstrapDuration() );
	}

	@Test
	public void bootstrapDuration() {
		Date start = new Date( 50000 );
		applicationInfo.setBootstrapStartDate( start );

		Date end = new Date( 55000 );
		applicationInfo.setBootstrapEndDate( end );
		assertEquals( 5000, applicationInfo.getBootstrapDuration() );

		Date nextEnd = new Date( 75000 );
		applicationInfo.setBootstrapEndDate( nextEnd );
		assertEquals( 25000, applicationInfo.getBootstrapDuration() );
	}

	@Test
	public void isRunningIn() {
		assertFalse( applicationInfo.isRunningIn( "test" ) );

		applicationInfo.setEnvironmentId( "production" );
		assertFalse( applicationInfo.isRunningIn( "test" ) );
		assertTrue( applicationInfo.isRunningIn( "PRODUCTION" ) );
		assertTrue( applicationInfo.isRunningIn( "production" ) );

		applicationInfo.setEnvironmentName( "test" );
		assertFalse( applicationInfo.isRunningIn( "test" ) );
		assertTrue( applicationInfo.isRunningIn( "PRODUCTION" ) );
		assertTrue( applicationInfo.isRunningIn( "production" ) );

		applicationInfo.setEnvironmentId( "TEST" );
		assertTrue( applicationInfo.isRunningIn( "test" ) );
		assertFalse( applicationInfo.isRunningIn( "PRODUCTION" ) );
		assertFalse( applicationInfo.isRunningIn( "production" ) );
	}
}
