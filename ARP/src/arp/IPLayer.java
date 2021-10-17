package arp;

import java.util.Arrays;

public class IPLayer extends BaseLayer {

	IP_HEADER Header = new IP_HEADER();
	
	public IPLayer(String pName) {
		pLayerName = pName;
	}
	
	@Override
	public boolean Send(byte[] input) {
		
		Header.ip_src = ipAddress;
		
		byte[] b = ObjToByte(Header);
				
		GetUnderLayer().Send(Arrays.copyOf(b, b.length));
		
		return true;
	}
	
	@Override
	public boolean Receive(byte[] input) {		

		Header = ByteToObj(input, IP_HEADER.class);		
		GetUpperLayer(0).Send(Header.data);
		return true;
	}
	
}
