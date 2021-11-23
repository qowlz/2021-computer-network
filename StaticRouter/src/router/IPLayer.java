package router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class IPLayer extends BaseLayer {
	IP_HEADER SendHeader = new IP_HEADER();
	IP_HEADER RecvHeader = new IP_HEADER();

	public ArrayList<byte[]> packet_queue = new ArrayList<byte[]>();
	
	public static ArrayList<ROUTING_ENRTY> RoutingTable = new ArrayList<ROUTING_ENRTY>();

    public IPLayer(String pName) {
        pLayerName = pName;
    }

    @Override
    public boolean Receive(byte[] input) {
    	
        RecvHeader = ByteToObj(input, IP_HEADER.class);
        
		Iterator<byte[]> packet = packet_queue.iterator(); // 밀린패킷 우선처리
		while (packet.hasNext()) {
			byte[] recv_data = packet.next();
			packet.remove();
			Receive(recv_data);
		}
		
        ARPLayer ARP = (ARPLayer) GetUnderLayer(0);
        EthernetLayer Ethernet = (EthernetLayer) ARP.GetUnderLayer(0);
        
        ROUTING_ENRTY entry = getEntry(RecvHeader.ip_dst); // entry 검색
        
        if (entry != null && !entry.flag.contains("U")) { // 적중
        	
    		if(Arrays.equals(getMaskedIP(RecvHeader.ip_dst, StrToIp(entry.mask)), StrToIp(entry.dst))) { // 동일 LAN
    			
    			ARP_CACHE cache = ARP.getCache(RecvHeader.ip_dst);
    			
				SendHeader.ip_dst = RecvHeader.ip_dst;
				SendHeader.ip_src = srcIpAddress;
				ARP.SendHeader.mac_src = srcMacAddress;
    			
    			if (cache != null) { // arp 적중
    				SendHeader.data = input;
    				ARP.Send(ObjToByte(SendHeader)); // 패킷 패스
    				return true;
    			}else { // arp 미적중
    					
    				SendHeader.data = new byte[0];
    				ARP.Send(ObjToByte(SendHeader)); // arp req 전송
    	            System.out.println("arp req 전송");
    	            
    				packet_queue.add(input);
    				System.out.println("packet queue에 추가됨");
    				return false;
    			}
    			
    		}else if (entry.flag.contains("G")) { // 게이트웨이로 전달

        	}
        	
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
    		ROUTING_ENRTY entry = iter.next();
    		if(Arrays.equals(ip, StrToIp(entry.dst))) {
    			iter.remove();
    		}
    	}
    	updateCache();
    }
    public ROUTING_ENRTY getEntry(byte[] ip) {
    	Iterator<ROUTING_ENRTY> iter = RoutingTable.iterator();
    	
    	ROUTING_ENRTY longest_entry = null;
    	int longest_prefix = 0;

    	while(iter.hasNext()) {
    		ROUTING_ENRTY entry = iter.next();
    		if(Arrays.equals(getMaskedIP(ip,StrToIp(entry.mask)), StrToIp(entry.dst))) {
    			if (longest_prefix < entry.gateway.split("255").length-1) {
    				longest_entry = entry;
    				longest_prefix = entry.gateway.split("255").length-1;
    			}
    		}
    	}

		return longest_entry;
    }
    public void updateCache() {
    	((ApplicationLayer)layerManager.GetLayer(Constants.AppLayerName)).updateRoutingTable(RoutingTable);
    }
	    
}