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

	private ArrayList<PcapIf> allDevs = new ArrayList<PcapIf>();
	private StringBuilder errbuf = new StringBuilder();

	private Pcap pcap;
	
	public static boolean exitProgram = false;

	public NILayer(String pName) {
		pLayerName = pName;	
		
		Pcap.findAllDevs(allDevs, errbuf);
		
		if (allDevs.isEmpty()) {
			System.out.println("네트워크 어뎁터가 없습니다.");
			return;
		}
	}
	
	public boolean setDevice(int idx, int snaplen, int flags, int timeout) {
		PcapIf device = allDevs.get(idx);
		pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);
		try {
			macAddress = device.getHardwareAddress();
			ipAddress = device.getAddresses().get(0).getAddr().getData();
			System.out.println(MacToStr(macAddress));
		} catch (IOException e) { System.out.println("주소를 찾을 수 없습니다."); }

		if (pcap == null) {
			System.out.printf("pcap 객체 생성실패 - %s\n", errbuf.toString());
			return false;
		}	return true;
	}
    
	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(this.pcap, this.GetUpperLayer(0));
		Thread obj = new Thread(thread);
		obj.start();
		return true;
	}
	
	@Override
	public boolean Send(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		if (pcap.sendPacket(buf) != Pcap.OK) {
			System.err.println(pcap.getErr());
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
	private Pcap adapter;
	private BaseLayer UpperLayer;

	public Receive_Thread(Pcap adt, BaseLayer upper) {
		adapter = adt;
		UpperLayer = upper;
	}

	@Override
	public void run() {
		int id = JRegistry.mapDLTToId(adapter.datalink());
		PcapHeader header = new PcapHeader(JMemory.POINTER);
		JBuffer buff = new JBuffer(JMemory.POINTER);
		while (adapter.nextEx(header, buff) == Pcap.NEXT_EX_OK && NILayer.exitProgram != true) {
			var packet = new PcapPacket(header, buff);
			packet.scan(id);
			data = packet.getByteArray(0, packet.size());
			UpperLayer.Receive(data);
		}
		adapter.close();
		System.exit(0);
	}

}