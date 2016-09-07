package org.mule.modules.metrics.model;

public class MetricsValueMetered implements MetricsValue {

	private String name_id;
	private long timeStamp;
	private String count;
	private String m1_rate;
	private String m5_rate;
	private String m15_rate;
	private String mean_rate;
	private String type;

	public MetricsValueMetered(String name_id, long timeStamp) {
		super();
		this.name_id = name_id;
		this.timeStamp = timeStamp;
		this.type = "Meter";

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

	public String getM1_rate() {
		return m1_rate;
	}

	public void setM1_rate(String m1_rate) {
		this.m1_rate = m1_rate;
	}

	public String getM5_rate() {
		return m5_rate;
	}

	public void setM5_rate(String m5_rate) {
		this.m5_rate = m5_rate;
	}

	public String getM15_rate() {
		return m15_rate;
	}

	public void setM15_rate(String m15_rate) {
		this.m15_rate = m15_rate;
	}

	public String getMean_rate() {
		return mean_rate;
	}

	public void setMean_rate(String mean_rate) {
		this.mean_rate = mean_rate;
	}

	@Override
	public String toString() {
		return "MetricsValueMetered [name_id=" + name_id + ", timeStamp="
				+ timeStamp + ", count=" + count + ", m1_rate=" + m1_rate
				+ ", m5_rate=" + m5_rate + ", m15_rate=" + m15_rate
				+ ", mean_rate=" + mean_rate + "]";
	}

}
