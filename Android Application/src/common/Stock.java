package common;

import java.io.Serializable;

public class Stock implements Serializable {
	private static final long serialVersionUID = 1262807410200907660L;
	private String tick = "Tick";
	private String fullName = "Name";
	private Integer owned = 0;
	private Double value = 0.0;
	private String lastCheck = "Date";
	private Integer exchanges = 0;

	public Stock(String tick, String fullName, Integer owned)
	{
		this.tick = tick;
		this.fullName = fullName;
		this.owned = owned;
	}
	
	public String getTick() {
		return tick;
	}
	public void setTick(String tick) {
		this.tick = tick;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getLastCheck() {
		return lastCheck;
	}
	public void setLastCheck(String lastCheck) {
		this.lastCheck = lastCheck;
	}
		
	public Integer getOwned() {
		return owned;
	}
	
	public void setOwned(Integer owned) {
		this.owned = owned;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}
	
	public Double getTotalValue() {
		return value * owned;
	}
	
	public Integer getExchanges() {
		return exchanges;
	}

	public void setExchanges(Integer exchanges) {
		this.exchanges = exchanges;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return tick;
	}
}
