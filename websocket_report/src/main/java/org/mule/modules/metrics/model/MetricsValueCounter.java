package org.mule.modules.metrics.model;

public class MetricsValueCounter implements MetricsValue {

	private String name_id;
	private long timeStamp;
	private String count;
	private String type;

	public MetricsValueCounter(String name_id, long timeStamp, String count) {
		super();
		this.name_id = name_id;
		this.timeStamp = timeStamp;
		this.count = count;
		this.type = "Counter";
	}

	public String getType() {
		return type;
	}

	public String getName_id() {
		return name_id;
	}

	public void setName_id(String name_id) {
		this.name_id = name_id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

	@Override
	public String toString() {
		return "MetricsValueCounter [name_id=" + name_id + ", timeStamp="
				+ timeStamp + ", count=" + count + "]";
	}

}
