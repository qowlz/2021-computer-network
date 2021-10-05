package arp;

import java.util.ArrayList;


public class IPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	IPHeader m_sHeader = new IPHeader();
	
	public IPLayer(String pName) {
		pLayerName = pName;
	}
	
	private static class IPHeader{
		byte ip_verlen;
		byte ip_tos;
		byte[] ip_len = new byte[2];
		byte[] ip_id = new byte[2];
		byte[] ip_fragoff = new byte[2];
		byte ip_ttl;
		byte ip_proto;
		byte[] ip_cksum = new byte[2];
		byte[] ip_src = new byte[4];
		byte[] ip_dst = new byte[4];
		
		public IPHeader() {
			
		}
		
		public void setSrcIp(byte[] ip) {
			this.ip_src = ip;
		}
		public void setDstIp(byte[] ip) {
			this.ip_dst = ip;
		}
	}
	
	public void setSrcIp(byte[] ip) {
		m_sHeader.ip_src = ip;
	}
	public void setDstIp(byte[] ip) {
		m_sHeader.ip_dst = ip;
	}
	
	public byte[] ObjToByte(IPHeader Header, byte[] input, int length) {
		//28byte의 arp헤더 붙이기
		byte[] buf = new byte[length+20];
		
		for(int i = 0; i < 20; i++) {
			if(i == 0) {
				buf[i] = Header.ip_verlen;
			}
			else if (i==1) {
				buf[i] = Header.ip_tos;
			}
			else if (2 <= i && i <= 3) {
				buf[i] = Header.ip_len[i-2];
			}
			else if (4 <= i && i <= 5) {
				buf[i] = Header.ip_id[i-4];
			}
			else if (i == 6 || i == 7) {
				buf[i] = Header.ip_fragoff[i-6];
			}
			else if (i == 8) {
				buf[i] = Header.ip_ttl;
			}
			else if (i == 9) {
				buf[i] = Header.ip_proto;
			}
			else if (10 <= i && i <= 11) {
				buf[i] = Header.ip_cksum[i-10];
			}
			else if (12 <= i && i <= 15) {
				buf[i] = Header.ip_src[i-12];
			}
			else if (16 <= i && i <= 19) {
				buf[i] = Header.ip_dst[i-16];
			}
		}
		System.arraycopy(input, 0, buf, 20, length);
		return buf;
	}
	
	public boolean Send(byte[] input, int length) {
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		GetUnderLayer().Send(bytes, bytes.length);
		return true;
	}
	
	public boolean Receive(byte[] input) {
		
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
}
