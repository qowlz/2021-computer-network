package arp;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	_ETHERNET_Frame m_sHeader;
	
	public EthernetLayer(String pName) {
		// super(pName);
		// TODO Auto-generated constructor stub
		pLayerName = pName;
		ResetHeader();
	}
	
	public void ResetHeader() {
		m_sHeader = new _ETHERNET_Frame();
	}
	
    private class _ETHERNET_ADDR {
        private byte[] addr = new byte[6];

        public _ETHERNET_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
            this.addr[4] = (byte) 0x00;
            this.addr[5] = (byte) 0x00;

        }
    }
    
    private class _ETHERNET_Frame {
        _ETHERNET_ADDR enet_dstaddr;
        _ETHERNET_ADDR enet_srcaddr;
        byte[] enet_type;
        byte[] enet_data;

        public _ETHERNET_Frame() {
            this.enet_dstaddr = new _ETHERNET_ADDR();
            this.enet_srcaddr = new _ETHERNET_ADDR();
            this.enet_type = new byte[2];
            this.enet_data = null;
        }
    }
    
    public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length) {//data�뿉 �뿤�뜑 遺숈뿬二쇨린
		byte[] buf = new byte[length + 14];
		for(int i = 0; i < 6; i++) {
			buf[i] = Header.enet_dstaddr.addr[i];
			buf[i+6] = Header.enet_srcaddr.addr[i];
		}			
		buf[12] = Header.enet_type[0];
		buf[13] = Header.enet_type[1];
		for (int i = 0; i < length; i++)
			buf[14 + i] = input[i];

		return buf;
	}

	// 釉뚮줈�뱶 罹먯뒪�듃�씪 寃쎌슦, type�씠 0xff
	public boolean Send(byte[] input, int length) {
		m_sHeader.enet_type[0] = (byte) 0x08;
		m_sHeader.enet_type[1] = (byte) 0x06;
		
		int op = byte2ToInt(input[6], input[7]);
		
		byte[] srcMac = new byte[6];
		for(int i = 0; i < 6; i++) {
			srcMac[i] = input[i+8];
		}
		SetEnetSrcAddress(srcMac);
		
		// arp request
		if(op == 1) {
			byte[] dstMac = new byte[] {-1, -1, -1, -1, -1, -1};
			SetEnetDstAddress(dstMac);
		}
		// arp reply
		else if(op == 2) {
			byte[] dstMac = new byte[6];
			for(int i = 0; i < 6; i++) {
				dstMac[i] = input[i+18];
			}
			SetEnetDstAddress(dstMac);
		}
		
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);
		return true;
	}

	public byte[] RemoveEthernetHeader(byte[] input, int length) {
		byte[] cpyInput = new byte[length - 14];
		System.arraycopy(input, 14, cpyInput, 0, length - 14);
		input = cpyInput;
		return input;
	}
	
	public synchronized boolean Receive(byte[] input) {
		byte[] data;
		byte[] temp_src = m_sHeader.enet_srcaddr.addr;
		int temp_type = byte2ToInt(input[12], input[13]); 
		
		byte[] temp_dst = new byte[6];
		System.arraycopy(input, 0, temp_dst, 0, 6);
		System.out.println(macByteToString(temp_dst));
		
		if(!(isMyPacket(input)) && ((isBroadcast(input)) || (chkAddr(input)))) {
			if(temp_type == 0x0806){
				// arp Layer濡� �쟾�넚
				data = RemoveEthernetHeader(input, input.length);
				((ARPLayer)this.GetUpperLayer(0)).Receive(data);
				return true;
			}
//			else if(temp_type == (byte)0x2090){
//				data = RemoveEthernetHeader(input, input.length);
//				((FileAppLayer)this.GetUpperLayer(1)).Receive(data);
//				return true;
//			}
		}
		
		return false;
	}
	
	public String macByteToString(byte[] byte_MacAddress) { //MAC Byte雅뚯눘�꺖�몴占� String占쎌몵嚥∽옙 癰귨옙占쎌넎
		String MacAddress = "";
		for (int i = 0; i < 6; i++) { 
			//2占쎌쁽�뵳占� 16筌욊쑴�땾�몴占� 占쏙옙�눧紐꾩쁽嚥∽옙, 域밸챶�봺�⑨옙 1占쎌쁽�뵳占� 16筌욊쑴�땾占쎈뮉 占쎈링占쎈퓠 0占쎌뱽 �겫�늿�뿫.
			MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");
			
			if (i != 5) {
				//2占쎌쁽�뵳占� 16筌욊쑴�땾 占쎌쁽�뵳占� 占쎈뼊占쎌맄 占쎈츟占쎈퓠 "-"�겫�늿肉т틠�눊由�
				MacAddress += ":";
			}
		}
		return MacAddress;
	}

    private byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }

    private int byte2ToInt(byte value1, byte value2) {
        return (int)((value1 << 8) | (value2));
    }
	
	private boolean isBroadcast(byte[] bytes) {
		for(int i = 0; i< 6; i++)
			if (bytes[i] != (byte) 0xff)
				return false;
		return true;
	}

	private boolean isMyPacket(byte[] input){
		for(int i = 0; i < 6; i++)
			if(m_sHeader.enet_srcaddr.addr[i] != input[6+i])
				return false;
		return true;
	}

	private boolean chkAddr(byte[] input) {
		byte[] temp = m_sHeader.enet_srcaddr.addr;
		for(int i = 0; i< 6; i++)
			if(m_sHeader.enet_srcaddr.addr[i] != input[i])
				return false;
		return true;
	}
	
	public void SetEnetSrcAddress(byte[] srcAddress) {
		// TODO Auto-generated method stub
		m_sHeader.enet_srcaddr.addr = srcAddress;
	}

	public void SetEnetDstAddress(byte[] dstAddress) {
		// TODO Auto-generated method stub
		m_sHeader.enet_dstaddr.addr = dstAddress;
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

	public boolean fileSend(byte[] input, int length) {
		// TODO Auto-generated method stub
		m_sHeader.enet_type[0] = (byte) 0x20;
		m_sHeader.enet_type[1] = (byte) 0x90;
		/*
		怨쇱젣#4
		�쐞�뿉�꽌 吏��젙�빐以� type�쓣 �떞�� �뿤�뜑瑜� 遺숈뿬�꽌 �븯�쐞怨꾩링�쑝濡� send�떆�궎�뒗 怨쇱젣
		*/
		byte[] bytes = ObjToByte(m_sHeader, input, length);
		this.GetUnderLayer().Send(bytes, length + 14);
		return true;
	}
}
