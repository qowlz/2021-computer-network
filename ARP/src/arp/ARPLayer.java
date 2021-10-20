package arp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JOptionPane;

import arp.BaseLayer.TCP_HEADER;

public class ARPLayer extends BaseLayer{

	private static ArrayList<ARP_CACHE> cache_table = new ArrayList<ARP_CACHE>();
	private static ArrayList<Proxy> proxyEntry = new ArrayList<Proxy>();
	
	private static ArpAppLayer appLayer;
	
	ARP_HEADER SendHeader = new ARP_HEADER();
	ARP_HEADER RecvHeader = new ARP_HEADER();
	
	public void setArpAppLayer(ArpAppLayer Layer) {
		appLayer = Layer;
	}
	
	public ARPLayer(String pName) {
		pLayerName = pName;	
	}
	
	@Override
	public boolean Send(byte[] input) {
	
		IP_HEADER UpperHeader = ByteToObj(input, IP_HEADER.class);
		
		SendHeader.ip_dst = Arrays.copyOf(UpperHeader.ip_dst, 4);
		SendHeader.opcode = 0x01; // request 타입
		SendHeader.ip_src = Arrays.copyOf(ipAddress,4);
		SendHeader.mac_src = Arrays.copyOf(macAddress,6); // 송신지 본인으로 설정
		SendHeader.mac_dst = new byte[] {-1,-1,-1,-1,-1,-1}; // 수신지 브로드캐스팅
		// 목적지 ip는 arpapp layer에서 설정
		
		if(getCache(SendHeader.ip_dst) == null && Arrays.equals(SendHeader.ip_src, SendHeader.ip_dst) != true) { // 헤더에 목적지 주소가 없거나 본인에게 보내는거 제외
			byte[] tempMac = new byte[6];
			ARP_CACHE arpcache = new ARP_CACHE(SendHeader.ip_dst, tempMac, false);
			addCacheTable(arpcache);
			
			updateCacheTable();
		}
		
		GetUnderLayer(0).Send(ObjToByte(SendHeader));
		return true;
	}
	//receive
	@Override
	public boolean Receive(byte[] input) {

		RecvHeader = ByteToObj(input, ARP_HEADER.class);
		
		ARP_CACHE tempARP = getCache(RecvHeader.ip_src);
		if(Arrays.equals(RecvHeader.ip_src, ipAddress)) return false; // 송신지가 본인이면 종료

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

			if(Arrays.equals(RecvHeader.ip_dst, ipAddress)) {	// 수신지가 본인이면
				SendHeader.opcode = 0x02; // reply
				SendHeader.ip_dst = Arrays.copyOf(RecvHeader.ip_src,4); // 수신지를 목적지로
				SendHeader.mac_dst = Arrays.copyOf(RecvHeader.mac_src,6); // 수신지를 목적지로
				SendHeader.mac_src = macAddress; // 송신지 다시지정
				SendHeader.ip_src = ipAddress; // 송신지 ip 나로 지정
				GetUnderLayer(0).Send(ObjToByte(SendHeader));

			}else{ // 목적지가 자신이 아닌경우 프록시 찾기
				Iterator<Proxy> iter = proxyEntry.iterator();

				while(iter.hasNext()) {
					Proxy proxy = iter.next();
					if(Arrays.equals(RecvHeader.ip_dst,  proxy.ip)) { // 프록시에 존재하면
						SendHeader.opcode = 0x02; // reply
						SendHeader.ip_dst = Arrays.copyOf(RecvHeader.ip_src,4);
						SendHeader.mac_dst = Arrays.copyOf(RecvHeader.mac_src,6);
						SendHeader.mac_src = macAddress; // 맥은 본인 pc로
						SendHeader.ip_src = Arrays.copyOf(proxy.ip, 4); // 송신지 ip 프록시 ip로 지정
						GetUnderLayer(0).Send(ObjToByte(SendHeader));
						break;
					}
				}
				updateProxyEntry();
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
    
    public void proxyRemove(byte[] ip) {
    	Iterator<Proxy> iter = proxyEntry.iterator();
    	
    	while(iter.hasNext()) {
    		Proxy proxy = iter.next();
    		if(Arrays.equals(ip, proxy.ip)) {
    			iter.remove();
    		}
    	}
    	updateProxyEntry();
    }
    
    public void addProxy(byte[] ip, byte[] mac) {
    	Proxy proxy = new Proxy(ip, mac);
    	proxyEntry.add(proxy);
    	updateProxyEntry();
    }
    
    
    public void updateCacheTable() {
    	appLayer.updateARPCacheTable(cache_table);
    }
    
    public void updateProxyEntry() {
    	appLayer.updateProxyEntry(proxyEntry);
    }
}
