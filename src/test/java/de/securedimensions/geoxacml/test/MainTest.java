package de.securedimensions.geoxacml.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.securedimensions.geoxacml.test.datatype.GeometryAttributeTest;

/**
 * 
 * Main GeoXACML3 core implementation test suite.
 * 
 */
@RunWith(Suite.class)
@SuiteClasses(value = { GeometryAttributeTest.class })
public class MainTest
{
	/**
	 * the logger we'll use for all messages
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MainTest.class);
	
	@BeforeClass
	public static void setUpClass()
	{
		LOGGER.info("Beginning Tests");
	}

	@AfterClass
	public static void tearDownClass()
	{
		LOGGER.info("Finishing Tests");
	}

}
