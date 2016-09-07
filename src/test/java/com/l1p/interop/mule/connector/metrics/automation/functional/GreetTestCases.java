package com.l1p.interop.mule.connector.metrics.automation.functional;

import com.l1p.interop.mule.connector.metrics.MetricsConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.devkit.ctf.junit.AbstractTestCase;

public class GreetTestCases extends
		AbstractTestCase<MetricsConnector> {

	public GreetTestCases() {
		super(MetricsConnector.class);
	}

	@Before
	public void setup() {
		// TODO
	}

	@After
	public void tearDown() {
		// TODO
	}

	@Test
	public void verify() {
		java.lang.String expected = null;
		java.lang.String friend = null;
//		assertEquals(getConnector().count(friend), expected);
	}

}