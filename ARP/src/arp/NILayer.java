package arp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer extends BaseLayer {

	ArrayList<PcapIf> allDevs = new ArrayList<PcapIf>();
	StringBuilder errbuf = new StringBuilder();
	public Pcap pcap;

	public NILayer(String pName) {
		pLayerName = pName;	
		
		int r = Pcap.findAllDevs(allDevs, errbuf);
		
		if (r == Pcap.NOT_OK || allDevs.isEmpty()) {
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

		return false;
	}
	
	@Override
	public boolean Send(byte[] data) {
		ByteBuffer buf = ByteBuffer.wrap(data);
		if (pcap.sendPacket(buf) != Pcap.OK) {
			System.err.println(pcap.getErr());
			return false;
		}	return true;
	}
	
	public PcapIf GetAdapterObject(int iIndex) {
		return allDevs.get(iIndex);
	}
	
	public ArrayList<PcapIf> getAdapterList() {
		return allDevs;
	}
	
}

class Receive_Thread implements Runnable {
	byte[] data;
	Pcap adapter;
	BaseLayer UpperLayer;

	public Receive_Thread(Pcap adt, BaseLayer upper) {
		adapter = adt;
		UpperLayer = upper;
	}

	@Override
	public void run() {
		while (true) {
			PcapPacketHandler<String> pph = new PcapPacketHandler<String>() {
				public void nextPacket(PcapPacket packet, String user) {
					data = packet.getByteArray(0, packet.size());
					
					UpperLayer.Receive(data);
				}
			};

			adapter.loop(100000, pph, "");
		}
	}
}
