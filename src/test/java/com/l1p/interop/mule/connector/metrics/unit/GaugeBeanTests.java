package com.l1p.interop.mule.connector.metrics.unit;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.component.SimpleCallableJavaComponentTestCase;

import com.l1p.interop.mule.connector.metrics.spring.GaugeBean;

public class GaugeBeanTests extends SimpleCallableJavaComponentTestCase {

	@Test
	public void testConstructor() {
		GaugeBean bean = new GaugeBean();
		assertTrue(bean != null);
	}
	
	@Test
	public void testSetMuleContext() {
		try {
			MuleContext context = super.createMuleContext();
			GaugeBean bean = new GaugeBean();
			bean.setMuleContext(context);
			MuleContext contextTest = bean.getMuleContext();
			assertEquals(contextTest, context);
		} catch (Exception e) {
			e.printStackTrace();
			fail("failed.  Error setting Mule Context with error: " + e.getMessage());
		}
	}
	
	@Test
	public void testNameSetters() {
		String name = "John";
		GaugeBean bean = new GaugeBean();
		bean.setName(name);
		
		String nameTest = bean.getName();
		assertEquals(nameTest, name);
	}
	
	@Test
	public void testCategorySetters() {
		String category = "316";
		GaugeBean bean = new GaugeBean();
		
		bean.setCategory(category);
		String categoryTestResponse = bean.getCategory();
		
		assertEquals(categoryTestResponse, category);
	}
	
	@Test
	public void testReturnValue() {
		GaugeBean bean = new GaugeBean();
		Integer returnTest = bean.returnValue();
	}

}
