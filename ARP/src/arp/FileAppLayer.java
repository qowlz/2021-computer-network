package arp;

import java.io.*;
import java.util.Arrays;


public class FileAppLayer extends BaseLayer {
	
    private byte[] fragBytes;
    private byte[] fragCheck;
    private int totalBytes;
    private String fileName;
    
    private byte[] fileData;
    
    final int MAXLEN = 1400;
    final byte INFO = 0x00;
    final byte DATA = 0x01;
    final byte RESEND = 0x02;
    final byte DONE = 0x03;
    final byte DONE_OK = 0x04;
    
	FILE_HEADER SendHeader = new FILE_HEADER();
	FILE_HEADER RecvHeader = new FILE_HEADER();

    public FileAppLayer(String pName) {
        // TODO Auto-generated constructor stub
        pLayerName = pName;

    }

    public boolean Send(byte[] input, int length, String name) {

    	fileData = Arrays.copyOf(input, input.length);
    	fileName = name;
    	
    	SendHeader.fapp_seq_num = 0;
    	SendHeader.fapp_totlen = length;
    	SendHeader.fapp_msg_type = INFO;
    	SendHeader.fapp_data = name.getBytes();
        
        TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
    	TCP.SendHeader.port_src = 0x2091;
    	TCP.SendHeader.port_dst = 0x2091;
    	TCP.Send(ObjToByte(SendHeader));
    	
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(length);
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
    	
        if (length <= MAXLEN) {
     
        	SendHeader.fapp_msg_type = DATA;
        	SendHeader.fapp_data = Arrays.copyOf(input, length);
            
			IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
			IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
			
        	TCP.SendHeader.port_src = 0x2091;
        	TCP.SendHeader.port_dst = 0x2091;
        	TCP.Send(ObjToByte(SendHeader));
        }else {  
	        for(int i = 0; i < length; i +=MAXLEN) {
	        	int left_packet = ((length - i) > MAXLEN) ? MAXLEN : (length - i);
	       
	        	byte[] data = new byte[left_packet];

	        	SendHeader.fapp_msg_type = DATA;

	        	System.arraycopy(input, i, data, 0, left_packet);
	        	SendHeader.fapp_data = Arrays.copyOf(data, left_packet);	
	        	
				IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
				IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
	        	
	        	TCP.SendHeader.port_src = 0x2091;
	        	TCP.SendHeader.port_dst = 0x2091;
	        	TCP.Send(ObjToByte(SendHeader));
	        	
	        	SendHeader.fapp_seq_num++;
	        	
	        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(i+MAXLEN);

	        	try {
	    			Thread.sleep(100); // 파일전송 동시에 채팅 보내는거 확인하기위한 딜레이 빼도 문제없음
	    		} catch (InterruptedException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	        }
	        

			IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
			IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
			
        	SendHeader.fapp_msg_type = DONE;
        	SendHeader.fapp_data = new byte[] {1};
        	TCP.SendHeader.port_src = 0x2091;
        	TCP.SendHeader.port_dst = 0x2091;
        	TCP.Send(ObjToByte(SendHeader));
        }
        	
        return true;
    }
 
    public void reSend(byte[] input) {
    	
    	int length = fileData.length;
    	int idx = 0;
    	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);

        for(int i = 0; i < length; i +=MAXLEN) {
        	if (input[idx++] == 0) {
        		byte[] data = new byte[MAXLEN];

            	SendHeader.fapp_msg_type = DATA;
            	SendHeader.fapp_totlen = length;
            	SendHeader.fapp_seq_num = idx-1;

            	System.arraycopy(fileData, i, data, 0, MAXLEN);
            	SendHeader.fapp_data = Arrays.copyOf(data, MAXLEN);	
            	
    			IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
    			IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
            	
            	TCP.SendHeader.port_src = 0x2091;
            	TCP.SendHeader.port_dst = 0x2091;
            	TCP.Send(ObjToByte(SendHeader));
            	
            	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(i+MAXLEN);

            	try {
        			Thread.sleep(100); 
        		} catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        	}
        }
        
		IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
		IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
		
    	SendHeader.fapp_msg_type = DONE;
    	SendHeader.fapp_data = new byte[] {1};
    	TCP.SendHeader.port_src = 0x2091;
    	TCP.SendHeader.port_dst = 0x2091;
    	TCP.Send(ObjToByte(SendHeader));
    	
    }
    
    public boolean Receive(byte[] input) {
    	
    	RecvHeader = ByteToObj(input, FILE_HEADER.class);
    	
        if (input == null) return true;
        
        if (fragBytes == null) {
        	fragBytes = new byte[RecvHeader.fapp_totlen];
        	fragCheck = new byte[(int) Math.ceil(RecvHeader.fapp_totlen/MAXLEN)+1];
        	totalBytes = 0;
        }
            
        if (RecvHeader.fapp_msg_type == RESEND) {
        	this.reSend(RecvHeader.fapp_data);
        }
        
        if (RecvHeader.fapp_msg_type == DONE_OK) {
        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
        	fragBytes = null;
            totalBytes = 0;
            fileName = null;
        }

        if (RecvHeader.fapp_msg_type == INFO) {
        	
        	System.out.println("파일 헤더 받음");
        	
        	fileName = new String(RecvHeader.fapp_data);
        	String newChat = "[FILE RECV " + fileName + "]";
        	
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(RecvHeader.fapp_totlen);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
            
        	GetUpperLayer(0).Receive(newChat.getBytes());
        	return true;
        }
        
        if (RecvHeader.fapp_msg_type == DATA) {
        	
            int offset = (RecvHeader.fapp_seq_num) * MAXLEN ;

            fragCheck[RecvHeader.fapp_seq_num] = 1;
        	totalBytes += RecvHeader.fapp_data.length;
        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(totalBytes);
        	
        	System.arraycopy(RecvHeader.fapp_data, 0, fragBytes, offset, RecvHeader.fapp_data.length);
        	
        	System.out.println(totalBytes + "/" + RecvHeader.fapp_totlen);

        	if (RecvHeader.fapp_totlen <= totalBytes) {
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
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
                
        		IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
        		IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
        		
            	SendHeader.fapp_msg_type = DONE_OK;
            	SendHeader.fapp_data = new byte[] {1};
            	
            	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);   
            	TCP.SendHeader.port_src = 0x2091;
            	TCP.SendHeader.port_dst = 0x2091;
            	TCP.Send(ObjToByte(SendHeader));
        	}
        	 	
            return true;
        }
        
        if (RecvHeader.fapp_msg_type == DONE) { 
        	if (RecvHeader.fapp_totlen > totalBytes && totalBytes != 0) { // 재전송
            	SendHeader.fapp_msg_type = RESEND;
            	SendHeader.fapp_totlen = RecvHeader.fapp_totlen;
            	SendHeader.fapp_data = fragCheck;
            	
            	IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
				IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
            	
            	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);        	
            	TCP.SendHeader.port_src = 0x2091;
            	TCP.SendHeader.port_dst = 0x2091;
            	TCP.Send(ObjToByte(SendHeader));
        	}
        }
        return false;
    }

}