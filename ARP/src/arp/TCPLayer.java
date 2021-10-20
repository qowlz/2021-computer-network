package arp;

import java.util.Arrays;

public class TCPLayer extends BaseLayer {

	TCP_HEADER SendHeader = new TCP_HEADER();
	TCP_HEADER RecvHeader = new TCP_HEADER();
	
	public TCPLayer(String pName) {
		pLayerName = pName;
	}
	
	public boolean Send(byte[] input) {
		SendHeader.data = Arrays.copyOf(input, input.length);

		byte[] b = ObjToByte(SendHeader);
		GetUnderLayer(0).Send(Arrays.copyOf(b, b.length));
		return true;
	}

	@Override
	public boolean Receive(byte[] input) {		
		
		RecvHeader = ByteToObj(input, TCP_HEADER.class);
		switch (RecvHeader.port_dst) {
		case 0x2090:
			System.out.println("채팅 패킷");
			GetUpperLayer(0).Receive(RecvHeader.data);
			return true;
			
		case 0x2091:
			System.out.println("파일 패킷");
			GetUpperLayer(1).Receive(RecvHeader.data);
			return true;
		}
		return false;
	}
	
}
