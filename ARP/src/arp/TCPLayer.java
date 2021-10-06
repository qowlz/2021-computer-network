package arp;

import java.util.Arrays;

public class TCPLayer extends BaseLayer {

	TCP_HEADER Header = new TCP_HEADER();
	
	public TCPLayer(String pName) {
		pLayerName = pName;
	}
	
	@Override
	public boolean Send(byte[] input) {
		
		byte[] b = ObjToByte(Header);

		GetUnderLayer().Send(Arrays.copyOf(b, b.length));
		return true;
	}
	
	@Override
	public boolean Receive(byte[] input) {		
		
		Header = ByteToObj(input, TCP_HEADER.class);
		GetUpperLayer(0).Send(Header.data);
		return true;
	}
	
}
