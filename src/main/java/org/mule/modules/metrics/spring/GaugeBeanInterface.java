package org.mule.modules.metrics.spring;

import org.mule.api.MuleContext;
import org.mule.api.context.MuleContextAware;

public interface GaugeBeanInterface extends MuleContextAware {

	public void setMuleContext(MuleContext context);

	public String getName();

	public String getCategory();

	public void setCategory(String category);

	public void setName(String name);

	public Integer returnValue();

	public MuleContext getMuleContext();

}
