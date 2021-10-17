package arp;

import java.util.ArrayList;

public class ChatAppLayer extends BaseLayer{
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    _CHAT_APP m_sHeader;

    private byte[] fragBytes;
    private int fragCount = 0;
   // private ArrayList<Boolean> ackChk = new ArrayList<Boolean>();

    private class _CHAT_APP {
        byte[] capp_totlen;
        byte capp_type;
        byte capp_unused;
        byte[] capp_data;

        public _CHAT_APP() {
            this.capp_totlen = new byte[2];
            this.capp_type = 0x00;
            this.capp_unused = 0x00;
            this.capp_data = null;
        }
    }

    public ChatAppLayer(String pName) {
        // super(pName);
        // TODO Auto-generated constructor stub
        pLayerName = pName;
        ResetHeader();
        //ackChk.add(true);
    }

    private void ResetHeader() {
        m_sHeader = new _CHAT_APP();
    }

    private byte[] objToByte(_CHAT_APP Header, byte[] input, int length) {
        byte[] buf = new byte[length + 4];

        buf[0] = Header.capp_totlen[0];
        buf[1] = Header.capp_totlen[1];
        buf[2] = Header.capp_type;
        buf[3] = Header.capp_unused;

        if (length >= 0) System.arraycopy(input, 0, buf, 4, length);

        return buf;
    }

    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] cpyInput = new byte[length - 4];
        System.arraycopy(input, 4, cpyInput, 0, length - 4);
        input = cpyInput;
        return input;
    }

//    private void waitACK() { //ACK 泥댄겕
//        while (ackChk.size() <= 0) {
//            try {
//                Thread.sleep(10);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        ackChk.remove(0);
//    }
  /**/
    private void fragSend(byte[] input, int length) {
        byte[] bytes = new byte[1456];
        int i = 0;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x01);

        // 泥ル쾲吏� �쟾�넚
        System.arraycopy(input, 0, bytes, 0, 1456);
        bytes = objToByte(m_sHeader, bytes, 1456);
        this.GetUnderLayer().Send(bytes);

        int maxLen = length / 1456;
        	/* 以묎컙 �떒�렪�솕  */
        m_sHeader.capp_type = (byte) (0x02);
        m_sHeader.capp_totlen = intToByte2(10);
        for(i = 1; i < maxLen; i ++) {
        	//waitACK();
        	//留덉�留됱씪寃쎌슦
        	if(i + 1 < maxLen && length%10 == 0)
        		m_sHeader.capp_type = (byte) (0x03);
        	System.arraycopy(input, 1456 * i, bytes, 0, 1456);
        	bytes = objToByte(m_sHeader, bytes, 1456);
        	this.GetUnderLayer().Send(bytes);
        }
        if (length % 1456 != 0) {
        	//waitACK();
            m_sHeader.capp_type = (byte) (0x03);
            /*怨쇱젣  */
            m_sHeader.capp_totlen = intToByte2(length%1456);
            bytes = new byte[length % 1456];
            System.arraycopy(input, length - (length % 1456), bytes, 0, length % 1456);
            bytes = objToByte(m_sHeader, bytes, bytes.length);
            this.GetUnderLayer().Send(bytes);
        }
    }
 
    public boolean Send(byte[] input, int length) {
        byte[] bytes;
        m_sHeader.capp_totlen = intToByte2(length);
        m_sHeader.capp_type = (byte) (0x00);
 
        //waitACK();//ACK�뱾�삤�뒗吏� �솗�씤
        if (length > 1456) {
        	fragSend(input, length);
        } //10蹂대떎 �겢�떆 fragsend瑜� �씠�슜�븯�뿬 �떒�렪�솕 �떆耳쒖꽌 �넚�떊
        else {
        	bytes = objToByte(m_sHeader, input, input.length);
        	this.GetUnderLayer().Send(bytes);
        } // ��踰덉＜嫄곕옉 媛숈쓬
        return true;
    }
 
    public synchronized boolean Receive(byte[] input) {
        byte[] data, tempBytes;
        int tempType = 0;

        if (input == null) {
        	//ackChk.add(true);
        	return true;
        }
        
        tempType |= (byte) (input[2] & 0xFF);
        
        if(tempType == 0) {
            /*  怨쇱젣   */
        	data = RemoveCappHeader(input, input.length);
        	this.GetUpperLayer(0).Receive(data);
        }
        else{
            /*  怨쇱젣   */
        	if(tempType == 1) {
        		int size = byte2ToInt(input[0], input[1]);
            	fragBytes = new byte[byte2ToInt(input[0], input[1])];
            	fragCount = 1;
            	tempBytes = RemoveCappHeader(input, input.length);
            	System.arraycopy(tempBytes, 0, fragBytes, 0, 10);
        	}
        	else {
	        	tempBytes = RemoveCappHeader(input, input.length);
	        	System.arraycopy(tempBytes, 0, fragBytes, (fragCount++) * 10, byte2ToInt(input[0], input[1]));
	        	if(tempType == 3) this.GetUpperLayer(0).Receive(fragBytes);
        	}
        }
        this.GetUnderLayer().Send(new byte[] {}); // ack �넚�떊
        return true;
    }
    
}
