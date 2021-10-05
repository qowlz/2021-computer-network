package arp;

import java.util.ArrayList;

public class TCPLayer implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	TCPHeader m_sHeader = new TCPHeader();
	
	public TCPLayer(String pName) {
		pLayerName = pName;
	}
	
	private static class TCPHeader{
		byte[] tcp_sport = new byte[2];
		byte[] tcp_dport = new byte[2];
		byte[] tcp_seq = new byte[4];
		byte[] tcp_ack = new byte[4];
		byte tcp_offset;
		byte tcp_flag;
		byte[] tcp_window = new byte[2];
		byte[] tcp_cksum = new byte[2];
		byte[] tcp_urgptr = new byte[2];
		byte[] padding = new byte[4];
		
		public TCPHeader() {
			
		}
	}
	
	public byte[] ObjToByte(TCPHeader Header, byte[] input, int length) {
		byte[] buf = new byte[length+24];
		
		for(int i = 0; i < 24; i++) {
			if(i == 0 || i == 1) {
				buf[i] = Header.tcp_sport[i];
			}
			else if (i==2 || i==3) {
				buf[i] = Header.tcp_dport[i-2];
			}
			else if (4 <= i && i <= 7) {
				buf[i] = Header.tcp_seq[i-4];
			}
			else if (8 <= i && i <= 11) {
				buf[i] = Header.tcp_ack[i-8];
			}
			else if (i == 12) {
				buf[i] = Header.tcp_offset;
			}
			else if (i == 13) {
				buf[i] = Header.tcp_flag;
			}
			else if (14 <= i && i <= 15) {
				buf[i] = Header.tcp_window[i-14];
			}
			else if (16 <= i && i <= 17) {
				buf[i] = Header.tcp_cksum[i-16];
			}
			else if (18 <= i && i <= 19) {
				buf[i] = Header.tcp_urgptr[i-18];
			}
			else if (20 <= i && i <= 23) {
				buf[i] = Header.padding[i-20];
			}
		}
		System.arraycopy(input, 0, buf, 24, length);
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
