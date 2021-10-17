package arp;

import java.util.Arrays;

public class TCPLayer extends BaseLayer {

	TCP_HEADER Header = new TCP_HEADER();
	
	public TCPLayer(String pName) {
		pLayerName = pName;
	}
	
	public boolean Send(byte[] input) {
		Header.data = Arrays.copyOf(input, input.length);
		byte[] b = ObjToByte(Header);
		System.out.println("TCP send length" + Header.data.length);
		GetUnderLayer(0).Send(Arrays.copyOf(b, b.length));
		return true;
	}
	
	@Override
	public boolean Receive(byte[] input) {		
		
		Header = ByteToObj(input, TCP_HEADER.class);
		System.out.println(Header.port_dst);
		switch (Header.port_dst) {
		case 0x2090:
			System.out.println("채팅 받음");
			GetUpperLayer(0).Receive(Header.data);
			return true;
			
		case 0x2091:
			GetUpperLayer(1).Receive(Header.data);
			return true;
		}
		return false;
	}
	
}
