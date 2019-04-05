package de.securedimensions.geoxacml.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
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
		String log4JPropertyFile = "src/test/resources/log4j.properties";
		Properties p = new Properties();

		try {
		    p.load(new FileInputStream(log4JPropertyFile));
		    PropertyConfigurator.configure(p);
		    LOGGER.info("Configured!");
		} catch (IOException e) {
		   e.printStackTrace();

		}
		LOGGER.debug("Beginning Tests");

	}

	@AfterClass
	public static void tearDownClass()
	{
		LOGGER.debug("Finishing Tests");
	}

}
