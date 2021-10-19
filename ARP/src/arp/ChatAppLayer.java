package arp;

import java.util.Arrays;

public class ChatAppLayer extends BaseLayer{

    CHAT_HEADER Header = new CHAT_HEADER();

    private byte[] fragBytes;
    private int totalBytes;
    final int MAXLEN = 1450;

    public ChatAppLayer(String pName) {
        pLayerName = pName;
    }

    public boolean Send(byte[] input, short length) {
        Header.totalLen = length;
        Header.type = (byte) (0x00);
        
        if (length <= MAXLEN) {
     
            Header.data = Arrays.copyOf(input, length);
               	
            TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
        	TCP.Header.port_src = 0x2090;
        	TCP.Header.port_dst = 0x2090;
        	TCP.Send(ObjToByte(Header));
        }else {
    
	        for(int i = 0; i < length; i +=MAXLEN) {
	        	int left_packet = ((length - i) > MAXLEN) ? MAXLEN : (length - i);
	       
	        	byte[] data = new byte[left_packet];

		        Header.type++;
		        System.out.println(data.length + " 번째 보냄");
	        	System.arraycopy(input, i, data, 0, left_packet);
	        	Header.data = Arrays.copyOf(data, left_packet);
	        	
				IPLayer IP = ((IPLayer)m_LayerMgr.GetLayer("IP"));
				IP.Header.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
	        	
	            TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
	        	TCP.Header.port_src = 0x2090;
	        	TCP.Header.port_dst = 0x2090;
	        	TCP.Send(ObjToByte(Header));
	        }      
        }
        	
        return true;
    }
 
    public boolean Receive(byte[] input) {
    	
    	Header = ByteToObj(input, CHAT_HEADER.class);
    	
        if (input == null) return true;
                
        if (Header.type == 0) {
        	this.GetUpperLayer(0).Receive(Header.data);
        	return true;
        }
        
        int offset = (Header.type -1) * MAXLEN;
        
    	if (fragBytes == null) {
        	fragBytes = new byte[Header.totalLen];
        	totalBytes = 0;
    	}

    	totalBytes += Header.data.length;
    	System.arraycopy(Header.data, 0, fragBytes, offset, Header.data.length);

    	if (Header.totalLen <= totalBytes) {
    		System.out.println(totalBytes + "채팅 받음");
    		for (byte b : fragBytes)
    			System.out.printf("%d ",b);
    		System.out.println("");
    		GetUpperLayer(0).Receive(Arrays.copyOf(fragBytes, fragBytes.length));
    		fragBytes = null;
    	}

        return true;
    
    }
}