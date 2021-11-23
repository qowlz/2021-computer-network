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
	public boolean Send(byte[] input) {
	
		IP_HEADER UpperHeader = ByteToObj(input, IP_HEADER.class);
		
		SendHeader.ip_dst = Arrays.copyOf(UpperHeader.ip_dst, 4);
		SendHeader.opcode = 0x01; // request Ÿ��
		SendHeader.ip_src = Arrays.copyOf(ipAddress,4);
		SendHeader.mac_src = Arrays.copyOf(macAddress,6); // �۽��� �������� ����
		SendHeader.mac_dst = new byte[] {-1,-1,-1,-1,-1,-1}; // ������ ��ε�ĳ����
		// ������ ip�� arpapp layer���� ����
		
		if(getCache(SendHeader.ip_dst) == null && Arrays.equals(SendHeader.ip_src, SendHeader.ip_dst) != true) { // ����� ������ �ּҰ� ���ų� ���ο��� �����°� ����
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
		if(Arrays.equals(RecvHeader.ip_src, ipAddress)) return false; // �۽����� �����̸� ����

		if(tempARP == null) { // ĳ�����̺� ������
			ARP_CACHE arpCache = new ARP_CACHE(RecvHeader.ip_src, RecvHeader.mac_src, true);
			addCacheTable(arpCache); // ���� ���� �߰�
		}else {  // ĳ�����̺� ����
			tempARP.status = true;
			tempARP.mac = Arrays.copyOf(RecvHeader.mac_src, 6); // ���Ź��� �������� �۽����� �����
		}
		updateCacheTable();

		switch (RecvHeader.opcode) { // arp ��Ŷ ���ڵ�� �з�
		case 0x01: // request

			if(Arrays.equals(RecvHeader.ip_dst, ipAddress)) {	// �������� �����̸�
				SendHeader.opcode = 0x02; // reply
				SendHeader.ip_dst = Arrays.copyOf(RecvHeader.ip_src,4); // �������� ��������
				SendHeader.mac_dst = Arrays.copyOf(RecvHeader.mac_src,6); // �������� ��������
				SendHeader.mac_src = macAddress; // �۽��� �ٽ�����
				SendHeader.ip_src = ipAddress; // �۽��� ip ���� ����
				GetUnderLayer(0).Send(ObjToByte(SendHeader));
			}
			break;
		case 0x02: // reply
			System.out.println("ARP ���� ����");
			// ���� �Ϸ�
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