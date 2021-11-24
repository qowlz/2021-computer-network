package router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.PcapIf;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;

public class NILayer extends BaseLayer {

	public static ArrayList<PcapIf> allDevs = new ArrayList<PcapIf>();
	private StringBuilder errbuf = new StringBuilder();
	
	private ArrayList<Pcap> adapters = new ArrayList<Pcap>();

	public NILayer(String pName) {
		pLayerName = pName;	
		
		Pcap.findAllDevs(allDevs, errbuf);
		
		for (PcapIf device : allDevs)
		{
			Pcap pcap = Pcap.openLive(device.getName(), 1000, Pcap.MODE_PROMISCUOUS, 1, errbuf);
			if (pcap != null)
				adapters.add(pcap);
		}
		
		if (allDevs.isEmpty()) {
			System.out.println("네트워크 어뎁터가 없습니다.");
			return;
		}
	}
    
	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(this.adapters, this.GetUpperLayer(0));
		Thread obj = new Thread(thread);
		obj.start();
		return true;
	}
	
	public boolean Send(byte[] data, int interfaceID) {
		
		ByteBuffer buf = ByteBuffer.wrap(data);
		if (adapters.get(interfaceID).sendPacket(buf) != Pcap.OK) {
			System.err.println(adapters.get(interfaceID).getErr());
			return false;
		}
		return true;
	}
	
	public ArrayList<PcapIf> getAdapterList() {
		return allDevs;
	}
	
	public static byte[] getMacAddress(int interfaceID) {
		byte[] mac = null;
		try {
			mac = NILayer.allDevs.get(interfaceID).getHardwareAddress();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mac;
	}
	
	public static byte[] getIpAddress(int interfaceID) {
		byte[] ip = null;
		ip = NILayer.allDevs.get(interfaceID).getAddresses().get(0).getAddr().getData(); // 선택된 interface port로 ip 지정
		return ip;
	}
}

class Receive_Thread implements Runnable {
	private byte[] data;
	private ArrayList<Pcap> adapters;
	private BaseLayer UpperLayer;

	public Receive_Thread(ArrayList<Pcap> adts, BaseLayer upper) {
		adapters = adts;
		UpperLayer = upper;
	}

	@Override
	public void run() {
		while (true) {
	
			for (int i = 0; i < adapters.size(); i++){
				Pcap adapter = adapters.get(i);
				int id = JRegistry.mapDLTToId(adapter.datalink());
				PcapHeader header = new PcapHeader(JMemory.POINTER);
				JBuffer buff = new JBuffer(JMemory.POINTER);

				if (adapter.nextEx(header, buff) != Pcap.NEXT_EX_OK) continue;
				
				PcapPacket packet = new PcapPacket(header, buff);
				packet.scan(id);
				
				data = packet.getByteArray(0, packet.size());
				UpperLayer.Receive(data, i);
			}
		}
	}
}
