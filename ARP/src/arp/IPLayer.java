package arp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class IPLayer extends BaseLayer {

	IP_HEADER Header = new IP_HEADER();
	public ArrayList<IP_HEADER> packet_queue = new ArrayList<IP_HEADER>();
	
	
	public IPLayer(String pName) {
		pLayerName = pName;
		queueTimer(1000);
	}
	
    public void queueTimer(long time) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	int len = packet_queue.size();
            	//try {
            		ARPLayer ARP = (ARPLayer)GetUnderLayer(0);
            		
                	//for (IP_HEADER Packet : packet_queue) {
            		for (int idx=0 ; idx<len ; idx++) {
            			IP_HEADER Packet = packet_queue.get(idx);
                		ARP_CACHE cache = ARP.getCache(Packet.ip_dst);
                		
                		if (cache != null) {
                			if (!Arrays.equals(cache.mac, new byte[] {0,0,0,0,0,0})) {
                    			EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(1);
                    			Ethernet.Header.mac_dst = Arrays.copyOf(cache.mac,cache.mac.length); // 맥주소 지정하고
                    			Ethernet.Header.frame_type = 0x0800; // type 지정하고
                    			Packet.ip_dst = cache.ip;
                    			Packet.ip_version = 4;
                    			Packet.ip_src = ipAddress;
                    			byte[] b = ObjToByte(Packet);
                    			Ethernet.Send(Arrays.copyOf(b, b.length)); // Ethernet으로 보냄
                    			
                    			packet_queue.remove(Packet);
                			}	
                		}
                	}	
            	//}catch(Exception e) {
            	//	System.out.println(e.toString());
            	//}
            	
            }
        };
        timer.schedule(task, time, time); 
    }
	
    
	@Override
	public boolean Send(byte[] input) {
		
		Header.ip_src = ipAddress;
		Header.data = input;
		
		TCP_HEADER UpperHeader = ByteToObj(input, TCP_HEADER.class);
		ARPLayer ARP = (ARPLayer)GetUnderLayer(0);
		ARP_CACHE cache = ARP.getCache(Header.ip_dst);
	
		if (cache == null || Arrays.equals(Header.ip_dst, ipAddress) || UpperHeader.data.length == 0) { // 목적지 ip의 맥주소가 없거나 도착지가 나일경우 (gratuitous 일 경우)
			ARP.Header.ip_dst = Arrays.copyOf(Header.ip_dst, 4); // ARP의 대상 ip 변경
			EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(1);
			Ethernet.Header.frame_type = 0x0806; // type 지정하
				
			if (UpperHeader.data.length != 0) { // 데이터가 있는데 미적중 했다는뜻

				IP_HEADER Pacekt = new IP_HEADER();
				Pacekt = ByteToObj(ObjToByte(Header),IP_HEADER.class);
				packet_queue.add(Pacekt); // 불발된 패킷 큐에 넣기			
				Header.data = new byte[0]; // data 삭제 -> 헤더만 보내기
			}
	
			byte[] b = ObjToByte(Header);
			ARP.Send(Arrays.copyOf(b, b.length)); // ARP로 보냄
		}else { // 목적지 ip의 mac주소가 있으면

			EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(1);
			Ethernet.Header.mac_dst = Arrays.copyOf(cache.mac,cache.mac.length); // 맥주소 지정하고
			Ethernet.Header.frame_type = 0x0800; // type 지정하고
			
			Header.ip_dst = cache.ip;
			Header.ip_version = 4;

			byte[] b = ObjToByte(Header);
			
			Ethernet.Send(Arrays.copyOf(b, b.length)); // Ethernet으로 보냄
		}
		
		return true;
	}
	
	@Override
	public boolean Receive(byte[] input) {		
		Header = ByteToObj(input, IP_HEADER.class);		
		ARPLayer ARP = (ARPLayer)GetUnderLayer(0);
		
		if (Arrays.equals(Header.ip_dst, ipAddress)) { // 자기 ip면 전달
			ARP_CACHE cache = ARP.getCache(Header.ip_src); // 송신지 ip 캐시에서 검색
			if (cache == null) { //  나의 ip가 상대쪽 arp 테이블에만 있는경우 	
				//TCPLayer TCP = (TCPLayer)GetUpperLayer(0);
				//if (TCP.Header.port_dst == 0x2090 || TCP.Header.port_dst == 0x2091) {
					EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(1);
					cache = new ARP_CACHE(Header.ip_src, Ethernet.Header.mac_src, true);
					ARP.addCacheTable(cache);
				//}
			}
			System.out.println(IpToStr(Header.ip_src) + "-IP_RECV>" + IpToStr(Header.ip_dst) );
			GetUpperLayer(0).Receive(Header.data);
			return true;
		}
		return false;
	}
	
}
