package router;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.StringTokenizer;

public abstract class BaseLayer {
	public static class ETHERNET_HEADER {
		byte[] mac_dst = new byte[6];
		byte[] mac_src = new byte[6];
		short frame_type = 0;
		byte[] data = null;
	}
	public static class ARP_HEADER {
		short hard_type = 1;
		short prot_type = 0x0800;
		byte hard_size = 6;
		byte prot_size = 4;
		short opcode = 0;
		byte[] mac_src = new byte[6];
		byte[] ip_src = new byte[4];
		byte[] mac_dst = new byte[6];
		byte[] ip_dst = new byte[4];
	}
	public static class IP_HEADER {
		byte ip_version = 4;
		byte tos = 0;
		short len = 5;
		short id = 0;
		short frag_offset = 0;
		byte ttl = 0;
		byte protocol = 0;
		short cksum = 0;
		byte[] ip_src = new byte[4];
		byte[] ip_dst = new byte[4];
		byte[] data = null;
	}
	
    public static class ARP_CACHE{
    	public byte[] ip = new byte[4];
    	public byte[] mac = new byte[6];
    	public boolean status;
    	
    	public ARP_CACHE(byte[] ipAddress, byte[] macAddress, boolean status) {
    		this.ip = ipAddress;
    		this.mac = macAddress;
    		this.status = status;
    	}
    }
    
    public static class ROUTING_ENRTY{
    	public String dst = new String();
    	public String mask = new String();
    	public String gateway = new String();
    	public String flag = new String();
    	public String Interface = new String();
    	public String Metric = new String();
    	
    	public ROUTING_ENRTY(String dst, String mask, String gateway, String flag, String Interface, String Metric) {
    		this.dst = dst;
    		this.mask = mask;
    		this.gateway = gateway;
    		this.flag = flag;
    		this.Interface = Interface;
    		this.Metric = Metric;
    	}
    }
    
	
	public int nUpperLayerCount = 0;
	public int nUnderLayerCount = 0;
	
	public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	public static LayerManager layerManager = new LayerManager();

	public String pLayerName = null;
	
	public String GetLayerName() {
		return pLayerName;
	}

	public BaseLayer GetUnderLayer(int nindex) {
		if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
			return null;
		return p_aUnderLayer.get(nindex);
	}

	public BaseLayer GetUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
	}

	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}
	
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}

	public boolean Send(byte[] input) {
		this.GetUnderLayer(0).Send(input);
		return false;
	}

	public boolean Receive(byte[] input) {

		this.GetUpperLayer(0).Receive(input);
		return false;
	}
	
	public static byte[] ObjToByte(Object obj) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(baos);
		
		for (Field field : obj.getClass().getDeclaredFields()) {		
			try {
				field.setAccessible(true);
				Object value = field.get(obj);
		        if (field.getType().toString().equals("class [B")) {
		        	byte[] d = (byte[]) value;
		        	if (d != null)
			        	for(byte b: d) 
			        		out.writeByte(b);		        		
		        }else if (field.getType().toString().equals("short")) {
		        	short d = (short) value;
		        	out.writeByte((byte)(d  >> 8));
		        	out.writeByte((byte)(d));
		        }else if (field.getType().toString().equals("int")) {
		        	int d = (int) value;
		    		out.writeByte((byte)(d  >> 24));
		    		out.writeByte((byte)(d  >> 16));
		        	out.writeByte((byte)(d  >> 8));
		        	out.writeByte((byte)(d));
		        }else  if (field.getType().toString().equals("byte")) {
		        	byte d = (byte) value;
		        	out.writeByte(d);
		        }
		    } catch (IllegalAccessException | IOException e) {}
		}
		
		 return baos.toByteArray();
	}
	
	public static <T> T ByteToObj (byte[] bytes, Class<T> type)
	{
		
		int idx=0;
		Object obj = null;

		try {
			obj = type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {}

		for (Field field : obj.getClass().getDeclaredFields()) {
			try {
				field.setAccessible(true);
				Object value = field.get(obj);
		        if (field.getType().toString().equals("class [B")) {
		        	
		        	byte[] d = (byte[]) value;
		        	if (d == null) {
		        		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        		DataOutputStream out = new DataOutputStream(baos);
		        		
		        		while(idx < bytes.length)
							try {out.writeByte(bytes[idx++]);} catch (IOException e) {}

		        		d = baos.toByteArray();
		        		
		        	}else {
			        	for(int i = 0; i < d.length; i++) 
			        		d[i] = bytes[idx++];	
		        	}
		        		
		        	field.set(obj, d);
		        }else if (field.getType().toString().equals("short")) {
		        	field.set(obj, ByteBuffer.wrap(bytes, idx, 2).order(ByteOrder.BIG_ENDIAN).getShort());
		        	idx += 2;
		        }else if (field.getType().toString().equals("int")) {
		        	field.set(obj, ByteBuffer.wrap(bytes, idx, 4).order(ByteOrder.BIG_ENDIAN).getInt());
		        	idx += 4;
		        }else  if (field.getType().toString().equals("byte")) {
		        	field.set(obj,bytes[idx]);
		        	idx++;
		        }
		    } catch (IllegalAccessException e) {}
		}	
	    return type.cast(obj);
	}
	
	public static String MacToStr(byte[] mac) {
		StringBuilder result = new StringBuilder();
		for (byte b : mac) {
			if (result.length() != 0) result.append(":");	
			result.append(Integer.toHexString((b < 0) ? b + 256 : b).toUpperCase());		
		}
		return result.toString();
	}
	
	public static byte[] StrToMac(String mac) {
		byte[] result = new byte[6];
		StringTokenizer tokens = new StringTokenizer(mac, ":");
		
		for (int i = 0; tokens.hasMoreElements(); i++) {
			String temp = tokens.nextToken();
			try {
				result[i] = Byte.parseByte(temp, 16);
			} catch (NumberFormatException e) {
				int minus = (Integer.parseInt(temp, 16)) - 256;
				result[i] = (byte) (minus);
			}
		}
		return result;
	}
	
	public static String IpToStr(byte[] ip) {
		String result = "";
		for(byte b : ip){
			result += b & 0xFF;
			result += ".";
		}
		return result.substring(0, result.length()-1);		
	}
	
	public static byte[] StrToIp(String ip) {
		byte[] result = new byte[4];
		int idx=0;
		
		for (String s :  ip.split("\\.")) {
			result[idx++] = (byte)(Integer.parseInt(s));
		}
		return result;
	}

	public static boolean isBroadcast(byte[] addr) {
		for (byte val : addr) 
			if (val != (byte)0xFF) return false;
		return true;
	}
	
	public static byte[] getMaskedIP(byte[] addr, byte[] mask) {
		byte[] ip = new byte[4];
		for(int idx=0; idx < 4; idx++){
			ip[idx] = (byte) (addr[idx] & mask[idx]);
		}
		return ip;
	}

}