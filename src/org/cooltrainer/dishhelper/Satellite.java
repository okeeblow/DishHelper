package org.cooltrainer.dishhelper;

import java.util.ArrayList;

public class Satellite
{
	private String name;
	private Double position;
	
	private ArrayList<Transponder> transponders;
	
	public Satellite(String name, double position)
	{
		this.name = name;
		this.position = position / 10.0;
		this.transponders = new ArrayList<Transponder>();
	}
	
	public Satellite()
	{
		this.name = "Unknown";
		this.position = 0.0;
		this.transponders = new ArrayList<Transponder>();
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public Double getPosition()
	{
		return this.position;
	}
	
	public void setPosition(double position)
	{
		this.position = position;
	}
	
	public void addTransponder(Transponder transponder)
	{
		this.transponders.add(transponder);
	}
	
	public ArrayList<Transponder> getTransponders()
	{
		return this.transponders;
	}
}
