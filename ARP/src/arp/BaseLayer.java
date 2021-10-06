package arp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

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
		byte ip_version = 0;
		byte tos = 0;
		short len = 0;
		short id = 0;
		short frag_offset = 0;
		byte ttl = 0;
		byte protocol = 0;
		short cksum = 0;
		byte[] ip_src = new byte[4];
		byte[] ip_dst = new byte[4];
		byte[] data = null;
	}
	public static class TCP_HEADER {
		short port_src = 0;
		short port_dst = 0;
		int seq = 0;
		int ack = 0;
		byte offset = 0;
		byte flag = 0;
		short window = 0;
		short cksum = 0;
		short urgptr = 0;
		byte[] pad = new byte[4];
		byte[] data = null;
	}
	
	public static byte[] macAddress = new byte[6];
	public static byte[] ipAddress = new byte[4];
	
	public final int m_nUpperLayerCount = 0;
	public final String m_pLayerName = null;
	public final BaseLayer mp_UnderLayer = null;
	public final ArrayList<BaseLayer> mp_aUpperLayer = new ArrayList<BaseLayer>();
	
	public int nUpperLayerCount = 0;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	
	public String pLayerName = null;
	
	public String GetLayerName() {
		return pLayerName;
	}

	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	public BaseLayer GetUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	public void SetUnderLayer(BaseLayer pUnderLayer) {
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
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
		this.GetUnderLayer().Send(input);
		return false;
	}

	public boolean Receive(byte[] input) {
		this.GetUpperLayer(0).Receive(input);
		return false;
	}

	
	public static String MacToStr(final byte[] mac) {
		final StringBuilder buf = new StringBuilder();
		for (byte b : mac) {
			if (buf.length() != 0) buf.append(":");	
			buf.append(Integer.toHexString((b < 0) ? b + 256 : b).toUpperCase());		
		}
		return buf.toString();
	}
	
	public static boolean isBroadcast(byte[] addr) {

		for (byte val : addr) {
			if (val != (byte)0xFF) return false;
		}
		return true;
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
	
    public static byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[0] |= (byte) ((value & 0xFF00) >> 8);
        temp[1] |= (byte) (value & 0xFF);

        return temp;
    }

    public static int byte2ToInt(byte value1, byte value2) {
	    return (int)((value1 << 8) | (value2));
	}

}
