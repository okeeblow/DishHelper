package org.cooltrainer.dishhelper;

/*
 * Transponders are represented in XML like so:
 * 
 * <transponder frequency="3762000" symbol_rate="22000000" polarization="1" fec_inner="0" system="1" modulation="1"/>
 * 
 * Frequency is in hertz.
 * Symbol rate is in symbols per second instead of the commonly-used ksps.
 * For polarization, 0 is Horizontal/LHC and 1 is Vertical/RHC.
 * FEC: I have no idea.
 * System: Nope.
 * Modulation: Unsure what these correspond to.
 * 
 */


public class Transponder {
	private int frequency, symbol, polarization, fec, modulation;
	
	public Transponder()
	{
		this.frequency = 0;
		this.symbol = 0;
		this.polarization = 0;
		this.fec = 0;
		this.modulation = 0;
	}
	public Transponder(int frequency, int symbol, int polarization, int fec, int modulation)
	{
		this.frequency = frequency;
		this.symbol = symbol;
		this.polarization = polarization;
		this.fec = fec;
		this.modulation = modulation;
	}
	public int getFrequency()
	{
		return this.frequency;
	}
	public String getPrettyFrequency()
	{
		return ((double)frequency / 1000 / 1000) + " GHz";
	}
	public int getSymbolRate()
	{
		return this.symbol;
	}
	public Double getPrettySymbolRate()
	{
		return Double.parseDouble(Integer.toString(this.symbol)) / 1000;
	}
	public int getPolarization()
	{
		return this.polarization;
	}
	public String getPrettyPolarization()
	{
		return (this.polarization == 0) ? "H/LHC" : "V/RHC";
	}
	public int getFec()
	{
		return this.fec;
	}
	public int getModulation()
	{
		return this.modulation;
	}
}
