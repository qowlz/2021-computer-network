package arp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JOptionPane;

public class ARPLayer extends BaseLayer{

	public static ArrayList<ARPCache> cache_table = new ArrayList<ARPCache>();
	public static ArrayList<Proxy> proxyEntry = new ArrayList<Proxy>();
	
	public static ArpAppLayer appLayer;
	
	ARP_HEADER Header = new ARP_HEADER();
	
	public void setArpAppLayer(ArpAppLayer Layer) {
		appLayer = Layer;
	}
	
	public ARPLayer(String pName) {
		pLayerName = pName;	
	}
	
	@Override
	public boolean Send(byte[] input) {
	
		Header.opcode = 0x01; // request 타입
		Header.ip_src = Arrays.copyOf(ipAddress,4);
		Header.mac_src = Arrays.copyOf(macAddress,6); // 송신지 본인으로 설정
		Header.mac_dst = new byte[] {-1,-1,-1,-1,-1,-1}; // 수신지 브로드캐스팅
		// 목적지 ip는 arpapp layer에서 설정
		
		if(getCache(Header.ip_dst) == null && Arrays.equals(Header.ip_src, Header.ip_dst) != true) { // 헤더에 목적지 주소가 없거나 본인에게 보내는거 제외
			byte[] tempMac = new byte[6];
			ARPCache arpcache = new ARPCache(Header.ip_dst, tempMac, false);
			addCacheTable(arpcache);
			
			updateCacheTable();
		}
		
		GetUnderLayer().Send(ObjToByte(Header));
		return true;
	}
	public String ipByteToString(byte[] stringIP) {
		String result = "";
		for(byte raw : stringIP){
			result += raw & 0xFF;
			result += ".";
		}
		return result.substring(0, result.length()-1);		
	}
	//receive
	@Override
	public boolean Receive(byte[] input) {

		Header = ByteToObj(input, ARP_HEADER.class);
		
		System.out.println(ipByteToString(Header.ip_src) + " -> " + ipByteToString(Header.ip_dst));
		
		ARPCache tempARP = getCache(Header.ip_src);

		switch (Header.opcode) { // arp 패킷 옵코드로 분류
		case 0x01: // request
			
			if(Arrays.equals(Header.ip_src, ipAddress)) return false; // 송신지가 본인이면 종료
			
			System.out.println("ARP reqst ");
			
			// 요청이 오면 일단 저장
			if(tempARP == null) { // 캐시테이블 미적중
				ARPCache arpCache = new ARPCache(Header.ip_src, Header.mac_src, true);
				addCacheTable(arpCache);
			}else {  // 캐시테이블 적중
				tempARP.status = true;
				tempARP.mac = Arrays.copyOf(Header.mac_src, 6);
			}
			updateCacheTable();

			if(Arrays.equals(Header.ip_dst, ipAddress)) {	// 수신지가 본인이면
				Header.opcode = 0x02; // reply
				Header.ip_dst = Arrays.copyOf(Header.ip_src,4); // 수신지를 목적지로
				Header.mac_dst = Arrays.copyOf(Header.mac_src,6); // 수신지를 목적지로
				Header.mac_src = Arrays.copyOf(macAddress,6); // 송신지 다시지정
				Header.ip_src = Arrays.copyOf(ipAddress,4); // 송신지 ip 나로 지정
				System.out.println("arp 응답");
				GetUnderLayer().Send(ObjToByte(Header));

			}else{ // 목적지가 자신이 아닌경우 프록시 찾기
				Iterator<Proxy> iter = proxyEntry.iterator();
				
				while(iter.hasNext()) {
					Proxy proxy = iter.next();
					if(Arrays.equals(Header.ip_dst,  proxy.ip)) { // 프록시에 존재하면
						Header.opcode = 0x02; // reply
						Header.ip_dst = Arrays.copyOf(Header.ip_src,4);
						Header.mac_dst = Arrays.copyOf(Header.mac_src,6);
						Header.mac_src = Arrays.copyOf(macAddress,6); // 맥은 본인 pc로
						Header.ip_src = Arrays.copyOf(proxy.ip,4); // 송신지 ip 프록시 ip로 지정
						GetUnderLayer().Send(ObjToByte(Header)); 
						break;
					}
				}
				updateProxyEntry();
			}
			break;
		case 0x02: // reply
			
			if(Arrays.equals(Header.ip_src, ipAddress)) return false; // 송신지가 본인이면 종료
			
			System.out.println(MacToStr(Header.mac_src) + " -> " + MacToStr(Header.mac_dst));
			System.out.println("ARP reply ");
			new JOptionPane().showMessageDialog(null,"응답이 왔어요");
			// 수신
			if(tempARP == null) { // 캐시테이블 미적중
				ARPCache arpCache = new ARPCache(Header.ip_src, Header.mac_src, true);
				addCacheTable(arpCache);
			}else {  // 캐시테이블 적중
				tempARP.status = true;
				tempARP.mac = Arrays.copyOf(Header.mac_src, 6);
			}
			
			updateCacheTable();
			break;
		}
			
		return true;
	}


    public class ARPCache{
    	// ip雅뚯눘�꺖, mac雅뚯눘�꺖, status
    	public byte[] ip = new byte[4];
    	public byte[] mac = new byte[6];
    	public boolean status;
    	
    	public ARPCache(byte[] ipAddress, byte[] macAddress, boolean status) {
    		this.ip = ipAddress;
    		this.mac = macAddress;
    		this.status = status;
    	}
    }
    
    public void addCacheTable(ARPCache cache) {
    	cache_table.add(cache);
    	updateCacheTable();
    }
    public void cacheRemoveAll() {
        cache_table.clear();
        updateCacheTable();
    }
    public void cacheRemove(byte[] ip) {
    	Iterator<ARPCache> iter = cache_table.iterator();
    	
    	while(iter.hasNext()) {
    		ARPCache cache = iter.next();
    		if(Arrays.equals(ip, cache.ip)) {
    			iter.remove();
    		}
    	}
    	updateCacheTable();
    }
    public ARPCache getCache(byte[] ip) {
    	Iterator<ARPCache> iter = cache_table.iterator();
    	while(iter.hasNext()) {
    		ARPCache cache = iter.next();
    		if(Arrays.equals(ip, cache.ip)) {
    			return cache;
    		}
    	}
    	return null;
    }
    
    
    public class Proxy{
    	public byte[] ip = new byte[4];
    	public byte[] mac = new byte[6];
    	
    	public Proxy(byte[] ip, byte[] mac) {
    		this.ip = ip;
    		this.mac = mac;
    	}
    }
    
    public Proxy getProxy(byte[] ip) {
    	Iterator<Proxy> iter = proxyEntry.iterator();
    	while(iter.hasNext()) {
    		Proxy proxy = iter.next();
    		if(Arrays.equals(ip, proxy.ip)) {
    			return proxy;
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
