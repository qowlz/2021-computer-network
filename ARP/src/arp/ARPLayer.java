package arp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

// ARP 占쎈땾占쎈뻬

public class ARPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	ARPHeader m_sHeader = new ARPHeader();
	
	// 筌�癒��뻻占쎈�믭옙�뵠�뇡占� 占쎈씜占쎈쑓占쎌뵠占쎈뱜�몴占� 占쎌맄占쎈립 占쎌쟿占쎌뵠占쎈선 占쎄퐬占쎌젟
	public static ArpAppLayer appLayer;
	public void setArpAppLayer(ArpAppLayer Layer) {
		appLayer = Layer;
	}
	
	// ARP Cache Table 占쎄문占쎄쉐
	public static ArrayList<ARPCache> cache_table = new ArrayList<ARPCache>();
	public static ArrayList<Proxy> proxyEntry = new ArrayList<Proxy>();
	
	public ARPLayer(String pName) {
		pLayerName = pName;
	}
	
	//arp 占쎈엘占쎈쐭
	private static class ARPHeader{
		byte[] hardType = new byte[2];
		byte[] protType = new byte[2];
		byte hardSize;
		byte protSize;
		byte[] op = new byte[2];
		byte[] srcMac = new byte[6];
		byte[] srcIp = new byte[4];
		byte[] dstMac = new byte[6];
		byte[] dstIp = new byte[4];

		public ARPHeader() {
			hardType[1] = (byte)0x01;
			protType[0] = (byte)0x08;
			hardSize = 6;
			protSize = 4;
		}
		
		public void setOp(byte[] op) {
			this.op = op;
		}
		public void setSrcMac(byte[] mac) {
			this.srcMac = mac;
		}
		public void setSrcIp(byte[] ip) {
			this.srcIp = ip;
		}
		public void setDstMac(byte[] mac) {
			this.dstMac = mac;
		}
		public void setDstIp(byte[] ip) {
			this.dstIp = ip;
		}
	}
	
	//arp header占쎈퓠 ip占쏙옙 mac雅뚯눘�꺖 占쎄쉭占쎈샒
	public void setSrcIp(byte[] ip) {
		m_sHeader.srcIp = ip;
	}
	public void setSrcMac(byte[] mac) {
		m_sHeader.srcMac = mac;
	}
	public void setDstIp(byte[] ip) {
		m_sHeader.dstIp = ip;
	}
	public void setDstMac(byte[] mac) {
		m_sHeader.dstMac = mac;
	}
	
	//占쎈쑓占쎌뵠占쎄숲占쎈퓠 header �겫�늿�뵠疫뀐옙
	public byte[] ObjToByte(ARPHeader Header, byte[] input, int length) {
		//28byte占쎌벥 arp占쎈엘占쎈쐭 �겫�늿�뵠疫뀐옙
		byte[] buf = new byte[length+28];
		
		for(int i = 0; i < 28; i++) {
			if(i == 0 || i == 1) {
				buf[i] = Header.hardType[i];
			}
			else if (i==2 || i==3) {
				buf[i] = Header.protType[i-2];
			}
			else if (i == 4) {
				buf[i] = Header.hardSize;
			}
			else if (i == 5) {
				buf[i] = Header.protSize;
			}
			else if (i == 6 || i == 7) {
				buf[i] = Header.op[i-6];
			}
			else if (8 <= i && i <= 13) {
				buf[i] = Header.srcMac[i-8];
			}
			else if (14 <= i && i <= 17) {
				buf[i] = Header.srcIp[i-14];
			}
			else if (18 <= i && i <= 23) {
				buf[i] = Header.dstMac[i-18];
			}
			else if (24 <= i && i <= 27) {
				buf[i] = Header.dstIp[i-24];
			}
		}
		System.arraycopy(input, 0, buf, 28, length);
		return buf;
	}
	
	//send
	public boolean Send(byte[] input, int length) {
		m_sHeader.op[0] = (byte)0x00;
		m_sHeader.op[1] = (byte)0x01;
		
		//ARP Cache List占쎈퓠 占쎄맒占쏙옙獄쏉옙 ip�몴占� �빊遺쏙옙, mac占쏙옙 0x00000000嚥∽옙, status占쎈뮉 false嚥∽옙 占쎈ご占쎈뻻
		//cache 占쎈�믭옙�뵠�뇡遺용퓠 �빊遺쏙옙
		if(getCache(m_sHeader.dstIp) == null && !Arrays.equals(m_sHeader.srcIp, m_sHeader.dstIp)) {
			byte[] tempMac = new byte[6];
			ARPCache arpcache = new ARPCache(m_sHeader.dstIp, tempMac, false);
			addCacheTable(arpcache);
			
			// AppLayer占쎈퓠占쎄퐣 筌�癒��뻻占쎈�믭옙�뵠�뇡占� 占쎈씜占쎈쑓占쎌뵠占쎈뱜
			updateCacheTable();
		}
		
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		GetUnderLayer().Send(bytes, bytes.length);
		return true;
	}
	
	//receive
	public boolean Receive(byte[] input) {
		//input[7]占쏙옙 op�굜遺얜굡占쎌벥 占쎈�� 占쎌쁽�뵳占�
		
		//op揶쏉옙 0x01占쎌뵠筌롳옙 arp request
		if(input[7] == 0x01) {
			//srcIp, srcMac占쏙옙 癰귣�沅�占쎄텢占쎌뿺占쎌벥 Ip占쏙옙 Mac, dstIp占쎈뮉 獄쏆룇占쏙옙沅쀯옙�뿺占쎌벥 Ip
			byte[] srcIp = new byte[4];
			byte[] srcMac = new byte[6];
			byte[] dstIp = new byte[4];
			
			System.arraycopy(input, 8, srcMac, 0, 6);
			System.arraycopy(input, 14, srcIp, 0, 4);
			System.arraycopy(input, 24, dstIp, 0, 4);
			
			if(Arrays.equals(srcIp, m_sHeader.srcIp)) {
				return false;
			}
			
			ARPCache tempARP = getCache(srcIp);
			if(tempARP == null) {
				ARPCache arpCache = new ARPCache(srcIp, srcMac, true);
				addCacheTable(arpCache);
			}
			else {
				if(tempARP.status == false) {
					tempARP.status = true;
					tempARP.mac = srcMac;
				}
				else if(!Arrays.equals(tempARP.mac, srcMac)) {
					tempARP.mac = srcMac;
				}
			}
			updateCacheTable();

			//占쎄돌占쎈퓠野껓옙 占쎌궔 野껉퍔�뵠占쎌뵬筌롳옙 src, dst�몴占� 占쎈뮞占쎌넁占쎈릭�⑨옙 op�몴占� 0x02嚥∽옙 獄쏅떽�뵒 占쎌뜎 占쎌삺占쎌읈占쎈꽊
			if(Arrays.equals(dstIp, m_sHeader.srcIp)) {
				// �뇡�슢以덌옙諭띰㏄癒��뮞占쎈뱜揶쏉옙 占쎈툡占쎈빒 占쎈뱟占쎌젟 筌뤴뫗�읅筌욑옙嚥∽옙 揶쏉옙占쎈튊占쎈맙
				//System.arraycopy(input, 15, m_sHeader.srcMac, 0, 6);
				input[7] = (byte)0x02;
				src_dst_swap(input);
				System.arraycopy(m_sHeader.srcMac, 0, input, 8, 6);
				System.arraycopy(m_sHeader.srcIp, 0, input, 14, 4);		
						
				GetUnderLayer().Send(input, input.length);
			}
			//Proxy ARP 占쎈�믭옙�뵠�뇡遺얜즲 占쎌넇占쎌뵥
			else {
				Iterator<Proxy> iter = proxyEntry.iterator();
				
				while(iter.hasNext()) {
					Proxy proxy = iter.next();
					if(Arrays.equals(dstIp,  proxy.ip)) {
						
						//System.arraycopy(input,  15,  m_sHeader.srcMac,  0, 6);
						input[7] = (byte)0x02;
						src_dst_swap(input);
						System.arraycopy(m_sHeader.srcMac, 0, input, 8, 6);	
						
						GetUnderLayer().Send(input, input.length);
						break;
					}
				}
				updateProxyEntry();
			}
		}
		
		//op揶쏉옙 0x02占쎌뵠筌롳옙 arp reply
		else if(input[7] == 0x02) {
			byte[] srcIp = new byte[4];
			byte[] srcMac = new byte[6];
			
			System.arraycopy(input, 8, srcMac, 0, 6);
			System.arraycopy(input, 14, srcIp, 0, 4);
			
			if(Arrays.equals(srcIp, m_sHeader.srcIp)) {
				return false;
			}
			
			ARPCache tempARP = getCache(srcIp);
			if(tempARP != null) {
				tempARP.status = true;
				tempARP.mac = srcMac;
			}
			else if(tempARP == null) {
				ARPCache addARP = new ARPCache(srcIp, srcMac, true);
				addCacheTable(addARP);
			}
			
			// AppLayer占쎈퓠占쎄퐣 筌�癒��뻻占쎈�믭옙�뵠�뇡占� 占쎈씜占쎈쑓占쎌뵠占쎈뱜
			updateCacheTable();
		}
		
		return true;
	}
	
	
	@Override
    public String GetLayerName() {
        // TODO Auto-generated method stub
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        // TODO Auto-generated method stub
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        // TODO Auto-generated method stub
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        // TODO Auto-generated method stub
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        // TODO Auto-generated method stub
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }

    public byte[] src_dst_swap(byte[] input) {
    	byte[] src = new byte[10];
    	byte[] dst = new byte[10];
    	
    	System.arraycopy(input, 8, src, 0, 10);
    	System.arraycopy(input, 18, dst, 0, 10);

    	System.arraycopy(dst, 0, input, 8, 10);
    	System.arraycopy(src, 0, input, 18, 10);
    	
    	return input;
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
