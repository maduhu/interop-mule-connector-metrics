package org.mule.modules.metrics.model;


public class MetricsPortlet {

	private String action;
	private String id;
	private String category = "";
	private String name = "";
	private String type = "";
	private MetricsValue value;

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public MetricsValue getValue() {
		return value;
	}

	public void setValue(MetricsValue value) {
		this.value = value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String generateId() {
		return this.getCategory().replace(".", "_").concat("_")
				.concat(this.getName());
	}

	public String generateId(String category, String name) {
		return category.replace(".", "_").concat("_").concat(name);
	}

	public void setCategoryAndName(String name) {

		int p = name.lastIndexOf(".");

		this.setCategory(name.substring(0, p));
		this.setName(name.substring(p + 1));

	}

}
