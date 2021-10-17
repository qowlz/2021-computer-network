package arp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import arp.BaseLayer.IP_HEADER;

public class IPLayer extends BaseLayer {

	IP_HEADER Header = new IP_HEADER();
	ArrayList<IP_HEADER> packet_queue = new ArrayList<IP_HEADER>();
	
	public IPLayer(String pName) {
		pLayerName = pName;
		queueTimer(1000);
	}
	
    public void queueTimer(long time) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	ARPLayer ARP = (ARPLayer)GetUnderLayer(0);
            	for (IP_HEADER Packet : packet_queue) {
            		ARP_CACHE cache = ARP.getCache(Packet.ip_dst);
            		
            		if (cache != null) {
            			EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(0);
            			Ethernet.Header.mac_dst = Arrays.copyOf(cache.mac,cache.mac.length); // 맥주소 지정하고
            			Ethernet.Header.frame_type = 0x0800; // type 지정하고
            			byte[] b = ObjToByte(Packet);
            			Ethernet.Send(Arrays.copyOf(b, b.length)); // Ethernet으로 보냄
            		}
            	}
            	
        		
            }
        };
        timer.schedule(task, time, time); 
    }
	
    
	@Override
	public boolean Send(byte[] input) {
		
		Header.ip_src = ipAddress;
		
		TCP_HEADER UpperHeader = ByteToObj(input, TCP_HEADER.class);
		ARPLayer ARP = (ARPLayer)GetUnderLayer(0);
		ARP_CACHE cache = ARP.getCache(Header.ip_dst);
	
		if (cache == null || Arrays.equals(Header.ip_dst, ipAddress) || UpperHeader.data.length == 0) { // 목적지 ip의 맥주소가 없거나 도착지가 나일경우 (gratuitous 일 경우)
			ARP.Header.ip_dst = Arrays.copyOf(Header.ip_dst, 4); // ARP의 대상 ip 변경
			EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(1);
			Ethernet.Header.frame_type = 0x0806; // type 지정하
				
			if (UpperHeader.data.length != 0) { // 데이터가 있는데 미적중 했다는뜻
				System.out.println("패킷 불발");
				IP_HEADER Pacekt = new IP_HEADER();
				Pacekt = ByteToObj(ObjToByte(Header),IP_HEADER.class);
				packet_queue.add(Pacekt); // 불발된 패킷 큐에 넣기			
				Header.data = new byte[0]; // data 삭제 -> 헤더만 보내기
			}

			byte[] b = ObjToByte(Header);
			ARP.Send(Arrays.copyOf(b, b.length)); // ARP로 보냄
		}else { // 목적지 ip의 mac주소가 있으면
			System.out.println(IpToStr(cache.ip));
			EthernetLayer Ethernet = (EthernetLayer)GetUnderLayer(1);
			Ethernet.Header.mac_dst = Arrays.copyOf(cache.mac,cache.mac.length); // 맥주소 지정하고
			Ethernet.Header.frame_type = 0x0800; // type 지정하고
			byte[] b = ObjToByte(Header);
			Ethernet.Send(Arrays.copyOf(b, b.length)); // Ethernet으로 보냄
		}
		
		
		// ARP 요청이면 그냥 보내고
		
		// ARP 요청이 아닌데
		// ARP 테이블에 dst ip가 없으면 요청보내고 
		// 데이터는 queue로 보냄 -> 응답이 왔을때 ip가 있으면 보내기
		
		return true;
	}
	
	@Override
	public boolean Receive(byte[] input) {		
		Header = ByteToObj(input, IP_HEADER.class);		
		
		if (Arrays.equals(Header.ip_dst, ipAddress)) { // 자기 ip면 전달
			GetUpperLayer(0).Send(Header.data);
			return true;
		}
		return false;
	}
	
}
