package org.mule.modules.metrics.model;

public class MetricsValueGauge implements MetricsValue {

	private String name_id;
	private long timeStamp;
	private String value;
	private String type;

	public MetricsValueGauge(String name_id, long timeStamp, String value) {
		super();
		this.name_id = name_id;
		this.timeStamp = timeStamp;
		this.value = value;
		this.type = "Gauge";
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MetricsValueCounter [name_id=" + name_id + ", timeStamp="
				+ timeStamp + ", value=" + value + "]";
	}

}
