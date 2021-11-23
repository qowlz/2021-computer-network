package router;

import java.util.Arrays;

import router.BaseLayer.IP_HEADER;

public class EthernetLayer extends BaseLayer {

	ETHERNET_HEADER SendHeader = new ETHERNET_HEADER();
	ETHERNET_HEADER RecvHeader = new ETHERNET_HEADER();
	
	public EthernetLayer(String pName) {
		pLayerName = pName;	
	}
	
	@Override
	public boolean Send(byte[] input) {
		// log("sending packet to under layer");
		
		if (SendHeader.frame_type == 0x0800) { // ip에서 날라온거
			System.out.println("IP Send Packet");
			System.out.println(SendHeader.frame_type);
			System.out.println(MacToStr(SendHeader.mac_src) + " -> " + MacToStr(SendHeader.mac_dst));
			this.GetUnderLayer(0).Send(ObjToByte(SendHeader));
			return true;
			
		}else {
			ARP_HEADER UpperHeader = ByteToObj(input, ARP_HEADER.class);

			SendHeader.frame_type = 0x0806; // arp 타입

			switch(UpperHeader.opcode) { // arp 헤더의 옵코드
				case 1: // ARP request
					SendHeader.mac_dst = new byte[] {-1, -1, -1, -1, -1, -1}; // 브로드 캐스팅
					break;
				case 2: // ARP reply
					SendHeader.mac_dst = Arrays.copyOf(UpperHeader.mac_dst,6); // 수신지 맥주소는 인계받음
					break;
			}
			System.out.println("ARP Send Packet");
			System.out.println(SendHeader.frame_type);
			System.out.println(MacToStr(SendHeader.mac_src) + " -> " + MacToStr(SendHeader.mac_dst));
			
			this.GetUnderLayer(0).Send(ObjToByte(SendHeader));
			return true;
		}
	}
	
	public synchronized boolean Receive(byte[] input) {

		RecvHeader = ByteToObj(input, ETHERNET_HEADER.class);
		
		if (NILayer.srcMacAddress == null && !isBroadcast(RecvHeader.mac_dst)) // 브로드캐스트 주소가 아닌데 매칭되는 nilayer가 없을때 종료
			return false;
		
		if (Arrays.equals(RecvHeader.mac_src, NILayer.srcMacAddress)) // 본인이 수신지 일경우 종료
			return false;
		
		//System.out.println(MacToStr(RecvHeader.mac_src) + " -> " + MacToStr(RecvHeader.mac_dst));
		
		if( (Arrays.equals(RecvHeader.mac_dst, NILayer.srcMacAddress)) ) { // 수신지가 브로드캐스팅 이거나 나 일경우 수신

			if(RecvHeader.frame_type == 0x0806){ // arp 타입이면
				System.out.println("To ARP Layer");
				System.out.println(MacToStr(RecvHeader.mac_src) + " -> " + MacToStr(RecvHeader.mac_dst));
				((ARPLayer)GetUpperLayer(0)).Receive(RecvHeader.data);
				return true;
			}else if(RecvHeader.frame_type == 0x0800) { // IP 타입이면
				
				this.GetUpperLayer(1).Receive(RecvHeader.data);
				return true;
			}
		}
		
		return false;
	}
	
}