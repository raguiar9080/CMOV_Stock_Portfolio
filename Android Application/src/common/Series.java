package common;

import java.io.Serializable;
import java.util.ArrayList;

public class Series implements Serializable{

	private static final long serialVersionUID = -2847859096410742061L;
	private String first;
	private ArrayList<Double> second;
	
	public Series(String name)
	{
		first = name;
		second = new ArrayList<Double>();
	}
	
	public void add(Double value)
	{
		second.add(value);
	}
	
	public String getFirst()
	{
		return first;
	}
	
	public  ArrayList<Double> getSecond()
	{
		return second;
	}
}
