package common;

public class Stock {
	private String tick = "Tick";
	private String fullName = "Name";
	private Integer owned = 0;
	private Integer value = 0;
	private String lastCheck = "Date";

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
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	
	public Integer getTotalValue() {
		return value * owned;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return tick;
	}
}
