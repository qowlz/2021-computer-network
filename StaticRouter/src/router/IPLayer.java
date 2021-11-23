package router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class IPLayer extends BaseLayer {
	IP_HEADER SendHeader = new IP_HEADER();
	IP_HEADER RecvHeader = new IP_HEADER();

	public static ArrayList<ROUTING_ENRTY> RoutingTable = new ArrayList<ROUTING_ENRTY>();

	private static ApplicationLayer appLayer;
	
	public void setIpAppLayer(ApplicationLayer Layer) {
		this.appLayer = Layer;
	}
	
    public IPLayer(String pName) {
        pLayerName = pName;
    }

    @Override
    public boolean Send(byte[] input) {

        SendHeader.ip_src = ipAddress;
        SendHeader.data = input;

        ARPLayer ARP = (ARPLayer) GetUnderLayer(0);
        ARP_CACHE cache = ARP.getCache(SendHeader.ip_dst); // Cache Table에 목적지 주소의 IP가 있는지 확인 -> 없으면 null

        // Cache Table에 목적지 IP 주소가 없거나 / 2. 목적지 IP 주소가 본인 IP 주소인 경우 (GARP) / 3. 데이터가 없는 경우
        if (cache == null || Arrays.equals(SendHeader.ip_dst, ipAddress) || UpperHeader.data.length == 0) {
            ARP.SendHeader.ip_dst = Arrays.copyOf(SendHeader.ip_dst, 4); // ARP 목적지 IP 주소 변경
            EthernetLayer Ethernet = (EthernetLayer) GetUnderLayer(1);
            Ethernet.SendHeader.frame_type = 0x0806; // Type 지정

            if (UpperHeader.data.length != 0) { // 데이터는 있지만 목적지에 도착하지 않은 경우 (NOT GARP)
                IP_HEADER Packet = new IP_HEADER(); // Packet 객체 생성
                Packet = ByteToObj(ObjToByte(SendHeader), IP_HEADER.class);
                packet_queue.add(Packet); // 불발된 패킷 큐에 넣기
                SendHeader.data = new byte[0]; // Data 부분은 제거하고 헤더만 전달
            }

            byte[] b = ObjToByte(SendHeader);
            ARP.Send(Arrays.copyOf(b, b.length)); // ARP로 전달
        } else {
            EthernetLayer Ethernet = (EthernetLayer) GetUnderLayer(1);
            Ethernet.SendHeader.mac_dst = Arrays.copyOf(cache.mac, cache.mac.length); // Cache Table에 저장된 MAC 주소를 가져와 저장
            Ethernet.SendHeader.frame_type = 0x0800; // Type 지정

            SendHeader.ip_dst = cache.ip; // 목적지는 Cache Table에 저장된 IP
            SendHeader.ip_version = 4; // IPv4

            byte[] b = ObjToByte(SendHeader);

            Ethernet.Send(Arrays.copyOf(b, b.length)); // Ethernet Layer로 전달
        }

        return true;
    }

    @Override
    public boolean Receive(byte[] input) {
        RecvHeader = ByteToObj(input, IP_HEADER.class);
        ARPLayer ARP = (ARPLayer) GetUnderLayer(0);

        if (Arrays.equals(RecvHeader.ip_dst, ipAddress)) { // 자신의 IP 주소인 경우
            ARP_CACHE cache = ARP.getCache(RecvHeader.ip_src); // 출발지 IP 주소가 Cache Table에 있는지 확인 -> 없으면 Null
            if (cache == null) { //  자신의 IP 주소가 상대방 Arp Cache Table에만 있는 경우
                EthernetLayer Ethernet = (EthernetLayer) GetUnderLayer(1);
                cache = new ARP_CACHE(RecvHeader.ip_src, Ethernet.RecvHeader.mac_src, true); // 자신의 Cache Table에 상대방의 IP 주소, MAC 주소를 등록
                ARP.addCacheTable(cache);
            }
            System.out.println(IpToStr(RecvHeader.ip_src) + "-IP_RECV>" + IpToStr(RecvHeader.ip_dst));
            GetUpperLayer(0).Receive(RecvHeader.data);
            return true;
        }
        return false;
    }
    
	public void addEntry(ROUTING_ENRTY cache) {
		RoutingTable.add(cache);
	 	updateCache();
    }
    public void clsCache() {
    	RoutingTable.clear();
    	updateCache();
    }
    public void removeEntry(byte[] ip) {
    	Iterator<ROUTING_ENRTY> iter = RoutingTable.iterator();
    	
    	while(iter.hasNext()) {
    		ROUTING_ENRTY cache = iter.next();
    		if(Arrays.equals(ip, cache.ip)) {
    			iter.remove();
    		}
    	}
    	updateCache();
    }
    public ROUTING_ENRTY getEntry(byte[] ip) {
    	Iterator<ROUTING_ENRTY> iter = RoutingTable.iterator();
    	while(iter.hasNext()) {
    		ROUTING_ENRTY cache = iter.next();
    		if(Arrays.equals(ip, cache.ip)) {
    			return cache;
    		}
    	}
    	return null;
    }
	       
    public void updateCache() {
    	appLayer.updateRoutingTable(RoutingTable);
    }
	    
}