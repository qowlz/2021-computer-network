package router;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JOptionPane;

public class ARPLayer extends BaseLayer{

	private static ArrayList<ARP_CACHE> cache_table = new ArrayList<ARP_CACHE>();
	
	private static ApplicationLayer appLayer;
	
	ARP_HEADER SendHeader = new ARP_HEADER();
	ARP_HEADER RecvHeader = new ARP_HEADER();
	
	public void setArpAppLayer(ApplicationLayer Layer) {
		appLayer = Layer;
	}
	
	public ARPLayer(String pName) {
		pLayerName = pName;	
	}
	
	@Override
	public boolean Send(byte[] input, int interfaceID) {
	
		IP_HEADER UpperHeader = ByteToObj(input, IP_HEADER.class);
		EthernetLayer UnderLayer = (EthernetLayer) GetUnderLayer(0);
		
		SendHeader.ip_dst = UpperHeader.ip_dst;
		SendHeader.opcode = 0x01; // request 타입
		SendHeader.ip_src = UpperHeader.ip_src;
		SendHeader.mac_dst = new byte[] {-1,-1,-1,-1,-1,-1}; // 수신지 브로드캐스팅
		SendHeader.mac_src = NILayer.getMacAddress(interfaceID); 
		
		if (UpperHeader.data.length > 1) { // arp req가 아닌 데이터의 경우 이더넷으로 패스
			//System.out.println("pass to ether");
			UnderLayer.SendHeader.mac_src = NILayer.getMacAddress(interfaceID);
			UnderLayer.SendHeader.data = UpperHeader.data;
			UnderLayer.SendHeader.frame_type = 0x0800;
			UnderLayer.Send(ObjToByte(SendHeader), interfaceID);
			return true;
		}
			
		// 목적지 ip는 arpapp layer에서 설정
		
		if(getCache(SendHeader.ip_dst) == null && Arrays.equals(SendHeader.ip_src, SendHeader.ip_dst) != true) { // 헤더에 목적지 주소가 없거나 본인에게 보내는거 제외
			
			byte[] tempMac = new byte[6];
			ARP_CACHE arpcache = new ARP_CACHE(SendHeader.ip_dst, tempMac, false);
			addCacheTable(arpcache);
			
			updateCacheTable();
		}
		UnderLayer.SendHeader.mac_src = NILayer.getMacAddress(interfaceID);
		UnderLayer.Send(ObjToByte(SendHeader), interfaceID);
		return true;
	}
	//receive

	public boolean Receive(byte[] input, int interfaceID) {
	
		RecvHeader = ByteToObj(input, ARP_HEADER.class);
		
		ARP_CACHE tempARP = getCache(RecvHeader.ip_src);
		
		if(Arrays.equals(RecvHeader.ip_src, NILayer.getIpAddress(interfaceID))) return false; // 송신지가 본인이면 종료

		//System.out.println(MacToStr(RecvHeader.mac_src) + " -arp> " + MacToStr(RecvHeader.mac_dst));
		
		if(tempARP == null) { // 캐시테이블 미적중
			ARP_CACHE arpCache = new ARP_CACHE(RecvHeader.ip_src, RecvHeader.mac_src, true);
			addCacheTable(arpCache); // 새로 만들어서 추가
		}else {  // 캐시테이블 적중
			tempARP.status = true;
			tempARP.mac = Arrays.copyOf(RecvHeader.mac_src, 6); // 수신받은 데이터의 송신지로 덮어쓰기
		}
		updateCacheTable();

		switch (RecvHeader.opcode) { // arp 패킷 옵코드로 분류
		case 0x01: // request
			//System.out.println("ARP 요청 수신");
			if(Arrays.equals(RecvHeader.ip_dst, NILayer.getIpAddress(interfaceID))) {	// 수신지가 본인이면
				//System.out.println("ARP 응답 송신");
				ARP_HEADER NewSendHeader = new ARP_HEADER();
				NewSendHeader.opcode = 0x02; // reply
				NewSendHeader.ip_dst = Arrays.copyOf(RecvHeader.ip_src,4); // 수신지를 목적지로
				NewSendHeader.mac_dst = Arrays.copyOf(RecvHeader.mac_src,6); // 수신지를 목적지로
				NewSendHeader.mac_src = NILayer.getMacAddress(interfaceID); // 송신지 다시지정
				NewSendHeader.ip_src = NILayer.getIpAddress(interfaceID); // 송신지 ip 나로 지정
				GetUnderLayer(0).Send(ObjToByte(NewSendHeader), interfaceID);
			}
			break;
		case 0x02: // reply
			System.out.println("ARP 응답 수신");
			// 수신 완료
			break;
		}
		return true;
	}


    public void addCacheTable(ARP_CACHE cache) {
    	cache_table.add(cache);
    	updateCacheTable();
    }
    public void cacheRemoveAll() {
        cache_table.clear();
        updateCacheTable();
    }
    public void cacheRemove(byte[] ip) {
    	Iterator<ARP_CACHE> iter = cache_table.iterator();
    	
    	while(iter.hasNext()) {
    		ARP_CACHE cache = iter.next();
    		if(Arrays.equals(ip, cache.ip)) {
    			iter.remove();
    		}
    	}
    	updateCacheTable();
    }
    public ARP_CACHE getCache(byte[] ip) {
    	Iterator<ARP_CACHE> iter = cache_table.iterator();
    	while(iter.hasNext()) {
    		ARP_CACHE cache = iter.next();
    		if(Arrays.equals(ip, cache.ip)) {
    			return cache;
    		}
    	}
    	return null;
    }
    public void updateCacheTable() {
    	appLayer.updateARPCacheTable(cache_table);
    }
}