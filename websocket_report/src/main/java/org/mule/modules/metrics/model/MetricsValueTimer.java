package org.mule.modules.metrics.model;

public class MetricsValueTimer implements MetricsValue {

	private String name_id;
	private long timeStamp;
	private String max;
	private String mean;
	private String min;
	private String stddev;
	private String p50;
	private String p75;
	private String p95;
	private String p98;
	private String p99;
	private String p999;
	private String type;

	public MetricsValueTimer(String name_id, long timeStamp) {
		super();
		this.name_id = name_id;
		this.timeStamp = timeStamp;
		this.type = "Timer";

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

	public String getMax() {
		return max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public String getMean() {
		return mean;
	}

	public void setMean(String mean) {
		this.mean = mean;
	}

	public String getMin() {
		return min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getStddev() {
		return stddev;
	}

	public void setStddev(String stddev) {
		this.stddev = stddev;
	}

	public String getP50() {
		return p50;
	}

	public void setP50(String p50) {
		this.p50 = p50;
	}

	public String getP75() {
		return p75;
	}

	public void setP75(String p75) {
		this.p75 = p75;
	}

	public String getP95() {
		return p95;
	}

	public void setP95(String p95) {
		this.p95 = p95;
	}

	public String getP98() {
		return p98;
	}

	public void setP98(String p98) {
		this.p98 = p98;
	}

	public String getP99() {
		return p99;
	}

	public void setP99(String p99) {
		this.p99 = p99;
	}

	public String getP999() {
		return p999;
	}

	public void setP999(String p999) {
		this.p999 = p999;
	}

	@Override
	public String toString() {
		return "MetricsValueTimer [name_id=" + name_id + ", timeStamp="
				+ timeStamp + ",  max=" + max + ", mean=" + mean + ", min="
				+ min + ", stddev=" + stddev + ", p50=" + p50 + ", p75=" + p75
				+ ", p95=" + p95 + ", p98=" + p98 + ", p99=" + p99 + ", p999="
				+ p999 + "]";
	}

}
