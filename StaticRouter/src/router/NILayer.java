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
	
	public static byte[] srcMacAddress = null;
	
	public static byte[] srcIpAddress = null;
	
	public static int pcapIdx;

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
	
	@Override
	public boolean Send(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		if (adapters.get(pcapIdx).sendPacket(buf) != Pcap.OK) {
			System.err.println(adapters.get(pcapIdx).getErr());
			return false;
		}
		return true;
	}
	public ArrayList<PcapIf> getAdapterList() {
		return allDevs;
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
				NILayer.pcapIdx = i;
				int id = JRegistry.mapDLTToId(adapter.datalink());
				PcapHeader header = new PcapHeader(JMemory.POINTER);
				JBuffer buff = new JBuffer(JMemory.POINTER);

				if (adapter.nextEx(header, buff) != Pcap.NEXT_EX_OK) continue;

				var packet = new PcapPacket(header, buff);
				packet.scan(id);
				data = packet.getByteArray(0, packet.size());
				PcapIf device = NILayer.allDevs.get(i);
				try {
					NILayer.srcMacAddress = device.getHardwareAddress();
				} catch (IOException e) {}

				NILayer.srcIpAddress = device.getAddresses().get(0).getAddr().getData();
				UpperLayer.Receive(data);
			}
		}
		//adapter.close();
		//System.exit(0);
	}

}
