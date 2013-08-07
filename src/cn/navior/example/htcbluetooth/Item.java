package cn.navior.example.htcbluetooth;

/**
 * item for RSSI measuring table
 * @author wangxiayang
 *
 */
public class Item {
	private String mac;	// MAC address for Bluetooth device
	private int searchIndex;	// index for the searching
	private int rssi;	// RSSI value for the the device in this measurement
	private String name;	// device name
	
	public Item( String mac ){
		this.mac = mac;
	}
	
	public String getMac(){
		return mac;
	}

	public int getSearchIndex() {
		return searchIndex;
	}

	public void setSearchIndex(int searchIndex) {
		this.searchIndex = searchIndex;
	}

	public int getRssi() {
		return rssi;
	}

	public void setRssi(int rssi) {
		this.rssi = rssi;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
