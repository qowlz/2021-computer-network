package arp;

import java.io.*;
import java.util.Arrays;


public class FileAppLayer extends BaseLayer {
	
    private byte[] fragBytes;
    private int totalBytes;
    private String fileName;
    
    final int MAXLEN = 1400;
    final byte INFO = 0x00;
    final byte DATA = 0x01;

	FILE_HEADER Header = new FILE_HEADER();

    public FileAppLayer(String pName) {
        // TODO Auto-generated constructor stub
        pLayerName = pName;

    }

    public boolean Send(byte[] input, int length, String name) {

        Header.fapp_seq_num = 0;
        Header.fapp_totlen = length;
        Header.fapp_msg_type = INFO;
        Header.fapp_data = name.getBytes();
        
        TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
    	TCP.Header.port_src = 0x2091;
    	TCP.Header.port_dst = 0x2091;
    	TCP.Send(ObjToByte(Header));
    	
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(length);
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
    	
        if (length <= MAXLEN) {
     
        	Header.fapp_msg_type = DATA;
            Header.fapp_data = Arrays.copyOf(input, length);
            
        	TCP.Header.port_src = 0x2091;
        	TCP.Header.port_dst = 0x2091;
        	TCP.Send(ObjToByte(Header));
        }else {  
	        for(int i = 0; i < length; i +=MAXLEN) {
	        	int left_packet = ((length - i) > MAXLEN) ? MAXLEN : (length - i);
	       
	        	byte[] data = new byte[left_packet];

	        	Header.fapp_msg_type = DATA;
		        System.out.println(data.length + " 번째 보냄");
	        	System.arraycopy(input, i, data, 0, left_packet);
	        	Header.fapp_data = Arrays.copyOf(data, left_packet);	
	        	
				IPLayer IP = ((IPLayer)m_LayerMgr.GetLayer("IP"));
				IP.Header.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
	        	
	        	TCP.Header.port_src = 0x2091;
	        	TCP.Header.port_dst = 0x2091;
	        	TCP.Send(ObjToByte(Header));
	        	
	        	Header.fapp_seq_num++;
	        	
	        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(i);

	        	try {
	    			Thread.sleep(100); // 파일전송 동시에 채팅 보내는거 확인하기위한 딜레이 빼도 문제없음
	    		} catch (InterruptedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	        }
        }
        	
        return true;
    }
 
    public boolean Receive(byte[] input) {
    	
    	Header = ByteToObj(input, FILE_HEADER.class);
    	
        if (input == null) return true;
        
        if (fragBytes == null) {
        	fragBytes = new byte[Header.fapp_totlen];
        	totalBytes = 0;
        }
        
        if (Header.fapp_msg_type == INFO) {
        	
        	System.out.println("파일 헤더 받음");
        	
        	fileName = new String(Header.fapp_data);
        	String newChat = "[FILE RECV " + fileName + "]";
        	
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(Header.fapp_totlen);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
            
        	GetUpperLayer(0).Receive(newChat.getBytes());
        	return true;
        }
        
        if (Header.fapp_msg_type == DATA) {
        	
            int offset = (Header.fapp_seq_num) * MAXLEN ;

        	totalBytes += Header.fapp_data.length;
        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(totalBytes);
        	
        	System.arraycopy(Header.fapp_data, 0, fragBytes, offset, Header.fapp_data.length);
        	
        	System.out.println(totalBytes + "파일 받음");

        	if (Header.fapp_totlen <= totalBytes) {
        		System.out.println("파일 다 받음");

        	    try{
        	        File download = new File(fileName);
        	        FileOutputStream lFileOutputStream = new FileOutputStream(download);
        	        lFileOutputStream.write(fragBytes);
        	        lFileOutputStream.close();

        	    }catch(Throwable e){ }
        	    
        	    String newChat = "[FILE RECV" + fileName + " -DONE]";
        	    
        		GetUpperLayer(0).Receive(newChat.getBytes());
            	fragBytes = null;
                totalBytes = 0;
                fileName = null;
        	}
        	 	
            return true;
        }
        return false;
    }

}