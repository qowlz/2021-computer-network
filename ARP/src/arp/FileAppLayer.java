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
    	
    	SendHeader.seqNum = 0;
    	SendHeader.totalLen = length;
    	SendHeader.msgType = INFO;
    	SendHeader.data = name.getBytes();
        
        TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
    	TCP.SendHeader.port_src = 0x2091;
    	TCP.SendHeader.port_dst = 0x2091;
    	TCP.Send(ObjToByte(SendHeader));
    	
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(length);
        ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
    	
        if (length <= MAXLEN) {
     
        	SendHeader.msgType = DATA;
        	SendHeader.data = Arrays.copyOf(input, length);
            
			IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
			IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
			
        	TCP.SendHeader.port_src = 0x2091;
        	TCP.SendHeader.port_dst = 0x2091;
        	TCP.Send(ObjToByte(SendHeader));
        }else {  
	        for(int i = 0; i < length; i +=MAXLEN) {
	        	int left_packet = ((length - i) > MAXLEN) ? MAXLEN : (length - i);
	       
	        	byte[] data = new byte[left_packet];

	        	SendHeader.msgType = DATA;

	        	System.arraycopy(input, i, data, 0, left_packet);
	        	SendHeader.data = Arrays.copyOf(data, left_packet);
	        	
				IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
				IP.SendHeader.ip_dst = StrToIp(((ChatFileDlg)GetUpperLayer(0)).dstIpAddress.getText());
	        	
	        	TCP.SendHeader.port_src = 0x2091;
	        	TCP.SendHeader.port_dst = 0x2091;
	        	TCP.Send(ObjToByte(SendHeader));
	        	
	        	SendHeader.seqNum++;
	        	
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
			
        	SendHeader.msgType = DONE;
        	SendHeader.data = new byte[] {1};
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

            	SendHeader.msgType = DATA;
            	SendHeader.totalLen = length;
            	SendHeader.seqNum = idx-1;

            	System.arraycopy(fileData, i, data, 0, MAXLEN);
            	SendHeader.data = Arrays.copyOf(data, MAXLEN);
            	
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
		
    	SendHeader.msgType = DONE;
    	SendHeader.data = new byte[] {1};
    	TCP.SendHeader.port_src = 0x2091;
    	TCP.SendHeader.port_dst = 0x2091;
    	TCP.Send(ObjToByte(SendHeader));
    	
    }
    
    public boolean Receive(byte[] input) {
    	
    	RecvHeader = ByteToObj(input, FILE_HEADER.class);
    	
        if (input == null) return true;
        
        if (fragBytes == null) {
        	fragBytes = new byte[RecvHeader.totalLen];
        	fragCheck = new byte[(int) Math.ceil(RecvHeader.totalLen /MAXLEN)+1];
        	totalBytes = 0;
        }
            
        if (RecvHeader.msgType == RESEND) {
        	this.reSend(RecvHeader.data);
        }
        
        if (RecvHeader.msgType == DONE_OK) {
        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
        	fragBytes = null;
            totalBytes = 0;
            fileName = null;
        }

        if (RecvHeader.msgType == INFO) {
        	
        	System.out.println("파일 헤더 받음");
        	
        	fileName = new String(RecvHeader.data);
        	String newChat = "[FILE RECV " + fileName + "]";
        	
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(RecvHeader.totalLen);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);
            
        	GetUpperLayer(0).Receive(newChat.getBytes());
        	return true;
        }
        
        if (RecvHeader.msgType == DATA) {
        	
            int offset = (RecvHeader.seqNum) * MAXLEN ;

            fragCheck[RecvHeader.seqNum] = 1;
        	totalBytes += RecvHeader.data.length;
        	((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(totalBytes);
        	
        	System.arraycopy(RecvHeader.data, 0, fragBytes, offset, RecvHeader.data.length);
        	
        	System.out.println(totalBytes + "/" + RecvHeader.totalLen);

        	if (RecvHeader.totalLen <= totalBytes) {
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
        		
            	SendHeader.msgType = DONE_OK;
            	SendHeader.data = new byte[] {1};
            	
            	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);   
            	TCP.SendHeader.port_src = 0x2091;
            	TCP.SendHeader.port_dst = 0x2091;
            	TCP.Send(ObjToByte(SendHeader));
        	}
        	 	
            return true;
        }
        
        if (RecvHeader.msgType == DONE) {
        	if (RecvHeader.totalLen > totalBytes && totalBytes != 0) { // 재전송
            	SendHeader.msgType = RESEND;
            	SendHeader.totalLen = RecvHeader.totalLen;
            	SendHeader.data = fragCheck;
            	
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