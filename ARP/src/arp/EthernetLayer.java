package arp;

import java.util.Arrays;

import arp.BaseLayer.ARP_CACHE;

public class EthernetLayer extends BaseLayer {

	ETHERNET_HEADER Header = new ETHERNET_HEADER();
	
	public EthernetLayer(String pName) {
		pLayerName = pName;	
	}
	
	@Override
	public boolean Send(byte[] input) {	
		System.out.println("Ether");
		
		Header.data = input; // 데이터 넣기 
		Header.mac_src = Arrays.copyOf(macAddress,6); // 송신지 설정	
		
		
		if (Header.frame_type == 0x0800) { // ip에서 날라온거
			IP_HEADER UpperHeader = ByteToObj(input, IP_HEADER.class);
			System.out.println("IP Send Packet");
			System.out.println(Header.frame_type);
			System.out.println(MacToStr(Header.mac_src) + " -> " + MacToStr(Header.mac_dst));
			
			this.GetUnderLayer(0).Send(ObjToByte(Header));
			return true;
			
		}else {
			ARP_HEADER UpperHeader = ByteToObj(input, ARP_HEADER.class);

			Header.frame_type = 0x0806; // arp 타입

			switch(UpperHeader.opcode) { // arp 헤더의 옵코드
			case 1: // ARP request
				Header.mac_dst = new byte[] {-1, -1, -1, -1, -1, -1}; // 브로드 캐스팅
				break;
			case 2: // ARP reply
				Header.mac_dst = Arrays.copyOf(UpperHeader.mac_dst,6); // 수신지 맥주소는 인계받음
				break;
			}
			System.out.println("ARP Send Packet");
			System.out.println(Header.frame_type);
			System.out.println(MacToStr(Header.mac_src) + " -> " + MacToStr(Header.mac_dst));
			
			this.GetUnderLayer(0).Send(ObjToByte(Header));
			return true;
		}

	}
	
	public synchronized boolean Receive(byte[] input) {


		Header = ByteToObj(input, ETHERNET_HEADER.class);
		
		if (Arrays.equals(Header.mac_src, macAddress))
			return false;
		
		System.out.println(MacToStr(Header.mac_src) + " -> " + MacToStr(Header.mac_dst));
		
		if( (isBroadcast(Header.mac_dst) || Arrays.equals(Header.mac_dst, macAddress))) { // 수신지가 브로드캐스팅 이거나 나 일경우 수신
			System.out.println(Header.frame_type);
			if(Header.frame_type == 0x0806){ // arp 타입이면
				System.out.println("ARP로 전달용");
				this.GetUpperLayer(0).Receive(Header.data);
				return true;
			}else if(Header.frame_type == 0x0800) { // IP 타입이면
				this.GetUpperLayer(1).Receive(Header.data);
				return true;
			}
		}
		
		return false;
	}
	
}
