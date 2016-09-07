package org.mule.modules.metrics.model;

public class MetricsValueGeneral implements MetricsValue {

	private String name_id;
	private long timeStamp;
	private String type;

	public MetricsValueGeneral(String name_id, long timeStamp) {
		super();
		this.name_id = name_id;
		this.timeStamp = timeStamp;
		this.type = "General";

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

	@Override
	public String toString() {
		return "MetricsValueCounter [name_id=" + name_id + ", timeStamp="
				+ timeStamp + "]";
	}

}
