package router;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;


public class IPLayer extends BaseLayer {
	IP_HEADER SendHeader = new IP_HEADER();
	IP_HEADER RecvHeader = new IP_HEADER();

    public static class PACKET_ITEM{
    	public byte[] data;
    	public int interfaceID;

    	public PACKET_ITEM(byte[] inp, int id) {
    		this.data = inp;
    		this.interfaceID = id;
    	}
    }
    
	public static ArrayList<PACKET_ITEM> packet_queue = new ArrayList<PACKET_ITEM>();
	
	public static ArrayList<ROUTING_ENRTY> RoutingTable = new ArrayList<ROUTING_ENRTY>();

    public IPLayer(String pName) {
        pLayerName = pName;
    }

    @Override
    public boolean Receive(byte[] input, int interfaceID) {
    	
        RecvHeader = ByteToObj(input, IP_HEADER.class);	
        ARPLayer ARP = (ARPLayer) GetUnderLayer(0);
        
		 int len = packet_queue.size();
		 if (len > 128) {
			 packet_queue.clear();
			 len = 0;
		 }
			 	 
         for (int idx = len-1; idx >= 0; idx--) {
        	 PACKET_ITEM recv_data = packet_queue.get(idx);
        	 
        	 ARP_CACHE cache = ARP.getCache(RecvHeader.ip_dst);
        	 if (cache != null) { // arp 적중
        		 if (cache.status == true) {
        			packet_queue.remove(idx);
     				SendHeader.ip_dst = RecvHeader.ip_dst;
    				SendHeader.ip_src = NILayer.getIpAddress(recv_data.interfaceID); 
     				SendHeader.data = recv_data.data;
     				ARP.SendHeader.mac_dst = cache.mac; // cache mac을 dst로 지정
     				ARP.Send(ObjToByte(SendHeader), recv_data.interfaceID); // 패킷 패스
        		 }
        	 }
        	 
        	 
         }
         
        ROUTING_ENRTY entry = getEntry(RecvHeader.ip_dst); // entry 검색
        
        
        if (entry != null && entry.flag.contains("U")) { // 적중
        	System.out.println("outport : " + entry.Interface);
        	System.out.println(IpToStr(RecvHeader.ip_src) + " -ip> " + IpToStr(RecvHeader.ip_dst));
    		if(Arrays.equals(getMaskedIP(RecvHeader.ip_dst, StrToIp(entry.mask)), StrToIp(entry.dst))) { // 동일 LAN
    			
    			ARP_CACHE cache = ARP.getCache(RecvHeader.ip_dst);
    			int portIndex = Integer.parseInt(entry.Interface);
    			
				SendHeader.ip_dst = RecvHeader.ip_dst;
				SendHeader.ip_src = NILayer.getIpAddress(portIndex); // 선택된 interface port로 ip 지정
				
    			if (cache != null) { // arp 적중
    				if (cache.status == true) {
    					System.out.println("arp ok");
        				SendHeader.data = input;
        				ARP.SendHeader.mac_dst = cache.mac; // cache mac을 dst로 지정
        				ARP.Send(ObjToByte(SendHeader), portIndex); // 패킷 패스
        				return true;
    				}
    			}else { // arp 미적중
    					
    				
    				SendHeader.data = new byte[0];
    				ARP.Send(ObjToByte(SendHeader), portIndex); // arp req 전송
    				
    				packet_queue.add(new PACKET_ITEM(input, Integer.parseInt(entry.Interface)));
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
    			if (longest_prefix < entry.mask.split("255").length-1) {
    				longest_entry = entry;
    				longest_prefix = entry.mask.split("255").length-1;
    				
    			}
    		}
    	}

		return longest_entry;
    }
    public void updateCache() {
    	((ApplicationLayer)layerManager.GetLayer(Constants.AppLayerName)).updateRoutingTable(RoutingTable);
    }
	    
}