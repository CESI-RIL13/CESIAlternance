package fr.cesi.alternance.training;

public class Training {
	
	private int id;
	private String name;
	private String alias;
	private int duration;
	
	public Training(int id, String name, String alias, int duration){
		this.id = id;
		this.name = name;
		this.alias = alias;
		this.duration = duration;
	}
	
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public void setName(String title) {
		this.name = name;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
}

