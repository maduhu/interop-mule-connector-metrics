package com.l1p.interop.mule.connector.metrics.spring;

import org.mule.api.MuleContext;
import org.mule.api.annotations.expressions.Lookup;

public class GaugeBean implements GaugeBeanInterface {

	private String category;
	private String name;

	@Lookup
	private MuleContext muleContext;

	@Override
	public void setMuleContext(MuleContext context) {
		this.muleContext = context;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public MuleContext getMuleContext() {
		return muleContext;
	}

	@Override
	public Integer returnValue() {

		return new Integer(0);
	}

}
