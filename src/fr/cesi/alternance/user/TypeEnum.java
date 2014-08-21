package fr.cesi.alternance.user;

import java.util.ArrayList;
import java.util.List;

public enum TypeEnum {
	VIADEO("Viadeo"),
	LINKEDIN("LinkedIn"),
	TWITTER("Twitter"),
	FACEBOOK("Facebook"),
	VIACESI("Viacesi"),
	OTHER("Autre");
	
	private final String value;
	
	TypeEnum(String value)
	{
		this.value=value;
	}
	
	public String value()
	{
		return value;
	}
	
	public static TypeEnum fromString(String value){
		if (value != null) {
			for (TypeEnum b : TypeEnum.values()) {
				if (value.equalsIgnoreCase(b.value)) {
					return b;
				}
			}
		}
		return null;
	}
	
	public static String[] list(){
		List<String> l=new ArrayList<String>();
		for (TypeEnum b : TypeEnum.values()) {
			l.add(b.value);
		}
		String[] list=new String[l.size()];
		list=l.toArray(list);
		return list;
	}
}
