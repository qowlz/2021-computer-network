package arp;

import java.util.Arrays;

public class ChatAppLayer extends BaseLayer{

    CHAT_HEADER SendHeader = new CHAT_HEADER();
    CHAT_HEADER RecvHeader = new CHAT_HEADER();

    private byte[] fragBytes;
    private int totalBytes;
    final int MAXLEN = 1450;

    public ChatAppLayer(String pName) {
        pLayerName = pName;
    }

    public boolean Send(byte[] input, short length) {
    	SendHeader.totalLen = length;
    	SendHeader.type = (byte) (0x00);
        
        if (length <= MAXLEN) {
     
        	SendHeader.data = Arrays.copyOf(input, length);
               	
            TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
        	TCP.SendHeader.port_src = 0x2090;
        	TCP.SendHeader.port_dst = 0x2090;
        	TCP.Send(ObjToByte(SendHeader));
        }else {
    
	        for(int i = 0; i < length; i +=MAXLEN) {
	        	int left_packet = ((length - i) > MAXLEN) ? MAXLEN : (length - i);
	       
	        	byte[] data = new byte[left_packet];

	        	SendHeader.type++;
	        	System.arraycopy(input, i, data, 0, left_packet);
	        	SendHeader.data = Arrays.copyOf(data, left_packet);
	        	
				IPLayer IP = ((IPLayer)m_LayerMgr.GetLayer("IP"));
				IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
	        	
	            TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
	        	TCP.SendHeader.port_src = 0x2090;
	        	TCP.SendHeader.port_dst = 0x2090;
	        	TCP.Send(ObjToByte(SendHeader));
	        }      
        }
        	
        return true;
    }
 
    public boolean Receive(byte[] input) {
    	
    	RecvHeader = ByteToObj(input, CHAT_HEADER.class);
    	
        if (input == null) return true;
                
        if (RecvHeader.type == 0) {
        	this.GetUpperLayer(0).Receive(RecvHeader.data);
        	return true;
        }
        
        int offset = (RecvHeader.type -1) * MAXLEN;
        
    	if (fragBytes == null) {
        	fragBytes = new byte[RecvHeader.totalLen];
        	totalBytes = 0;
    	}

    	totalBytes += RecvHeader.data.length;
    	System.arraycopy(RecvHeader.data, 0, fragBytes, offset, RecvHeader.data.length);

    	if (RecvHeader.totalLen <= totalBytes) {
    		GetUpperLayer(0).Receive(Arrays.copyOf(fragBytes, fragBytes.length));
    		fragBytes = null;
    	}

        return true;
    
    }
}