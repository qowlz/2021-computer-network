package arp;

import java.util.ArrayList;
import java.util.Arrays;

import arp.BaseLayer.ARP_HEADER;
import arp.BaseLayer.ETHERNET_HEADER;

public class ChatAppLayer extends BaseLayer{

    CHAT_HEADER Header = new CHAT_HEADER();

    private byte[] fragBytes;
    private int fragCount = 0;
   // private ArrayList<Boolean> ackChk = new ArrayList<Boolean>();

    public ChatAppLayer(String pName) {
        // super(pName);
        // TODO Auto-generated constructor stub
        pLayerName = pName;
        //ackChk.add(true);
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
    private void fragSend(byte[] input, short length) {
        byte[] bytes = new byte[1456];
        int i = 0;
        
        //Header = ByteToObj(input, CHAT_HEADER.class);
        
        Header.capp_totlen = length;
        Header.capp_type = (byte) (0x01);

        System.arraycopy(input, 0, bytes, 0, 1456);
        Header.capp_data = bytes;

    	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
    	TCP.Header.port_dst = 0x2090;
    	TCP.Header.port_src = 0x2090;
    	TCP.Send(ObjToByte(Header));

        int maxLen = length / 1456;
        	/* 以묎컙 �떒�렪�솕  */
        Header.capp_type = (byte) (0x02);
        Header.capp_totlen = 10;
        for(i = 1; i < maxLen; i ++) {
        	//waitACK();
        	//留덉�留됱씪寃쎌슦
        	if(i + 1 < maxLen && length%10 == 0)
        		Header.capp_type = (byte) (0x03);
        	System.arraycopy(input, 1456 * i, bytes, 0, 1456);
        	Header.capp_data = bytes;
        	TCP.Header.port_dst = 0x2090;
        	TCP.Header.port_src = 0x2090;
        	TCP.Send(ObjToByte(Header));
        }
        if (length % 1456 != 0) {
        	//waitACK();
        	Header.capp_type = (byte) (0x03);
            /*怨쇱젣  */
        	Header.capp_totlen = (short) (length%1456);
            bytes = new byte[length % 1456];
            System.arraycopy(input, length - (length % 1456), bytes, 0, length % 1456);
            Header.capp_data = bytes;
            
        	TCP.Header.port_dst = 0x2090;
        	TCP.Header.port_src = 0x2090;
        	TCP.Send(ObjToByte(Header));
        	
        }
    }
 
    public boolean Send(byte[] input, short length) {
    	
    	System.out.println("CAL" + input.length);
        Header = ByteToObj(input, CHAT_HEADER.class);
        Header.capp_totlen = length;
        Header.capp_type = (byte) (0x00);
        Header.capp_data = Arrays.copyOf(input, length);
		
        System.out.println("send length" + length);
        
        //waitACK()
        if (length > 1456) {
        	fragSend(input, length);
        } //fragsend
        else {
        	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
        	TCP.Header.port_src = 0x2090;
        	TCP.Header.port_dst = 0x2090;
        	TCP.Send(ObjToByte(Header));
        } 
        return true;
    }
 
    public synchronized boolean Receive(byte[] input) {
    	
    	Header = ByteToObj(input, CHAT_HEADER.class);
    	
        if (input == null) {
        	//ackChk.add(true);
        	return true;
        }
                
        if(Header.capp_type == 0) {
        	System.out.println("chat app에서 위로전달");
        	this.GetUpperLayer(0).Receive(Header.capp_data);
        }
        else{
            /*  怨쇱젣   */
        	if(Header.capp_type == 1) {
            	fragBytes = new byte[Header.capp_totlen];
            	fragCount = 1;
            	System.arraycopy(Header.capp_data, 0, fragBytes, 0, 10);
        	}
        	else {
	        	System.arraycopy(Header.capp_data, 0, fragBytes, (fragCount++) * 10, Header.capp_totlen);
	        	if(Header.capp_type == 3) this.GetUpperLayer(0).Receive(fragBytes);
        	}
        }
        //this.GetUnderLayer().Send(new byte[] {}); // ack �넚�떊
        return true;
    }
    
}
