package pack0;

public class Cache {
	private int timeToLive;
	private String ipAddress;
	private String name;
	private byte[] data;
	public Cache(int timeToLive, String ipAddress, String name) {
		super();
		this.timeToLive = timeToLive;
		this.ipAddress = ipAddress;
		this.name = name;
	}
	public int getTimeToLive() {
		return timeToLive;
	}
	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public Cache(int timeToLive, String ipAddress, String name, byte[] data) {
		super();
		this.timeToLive = timeToLive;
		this.ipAddress = ipAddress;
		this.name = name;
		this.data = data;
	}
	public String getDetails() {
		return "Server Name: " + name +"\n"
				+"ipAddress: " +ipAddress+"\n"
				+"Time To Live: "+timeToLive+"\n";
	}
	
	
}
