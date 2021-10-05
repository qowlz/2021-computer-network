package arp;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import arp.ARPLayer.ARPCache;
import arp.ARPLayer.Proxy;
import arp.ChatFileDlg.setAddressListener;

public class ArpAppLayer extends JFrame implements BaseLayer{
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;
	
	private JPanel contentPane;
	private JTextField GARPTextField;
	private JButton ARPItemDelete;
	private JButton ARPAllDelete;
	private JTable ARPTable;
	private JTable ProxyTable;
	private JButton ProxyAdd;
	private JButton ProxyDelete;
	private JTextField IPTextField;
	private JButton ARP_IPSend;
	private JButton GARPSend;
	
	private DefaultTableModel arpModel, proxyModel;
	String arpModelHeader[] = {"IpAddress", "MacAddress", "Status"};
	String[][] arpModelContents = new String[0][3];
	String proxyModelHeader[] = {"IpAddress", "MacAddress"};
	String[][] proxyModelContents = new String[0][2];
	
	// proxy window
	private JTextField proxyEntryIp;
	private JTextField proxyEntryMac;

	private static LayerManager m_LayerMgr = new LayerManager();
	
	static int adapterNumber = 0;
	
	public static void main(String[] args) throws IOException {
		NILayer niLayer = new NILayer("NI");
		EthernetLayer ethernetLayer = new EthernetLayer("Ethernet");
		ARPLayer arpLayer = new ARPLayer("ARP");
		IPLayer ipLayer = new IPLayer("IP");
		TCPLayer tcpLayer = new TCPLayer("TCP");
		ArpAppLayer arpAppLayer = new ArpAppLayer("GUI");
		
		
		m_LayerMgr.AddLayer(niLayer);
		m_LayerMgr.AddLayer(ethernetLayer);
		m_LayerMgr.AddLayer(arpLayer);
		m_LayerMgr.AddLayer(ipLayer);
		m_LayerMgr.AddLayer(tcpLayer);
		m_LayerMgr.AddLayer(arpAppLayer);

		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *TCP ( *GUI ) ) ) ) )");
		
		arpLayer.setArpAppLayer(arpAppLayer);
		ipLayer.setSrcIp(InetAddress.getLocalHost().getAddress());
		arpLayer.setSrcIp(InetAddress.getLocalHost().getAddress());
		arpLayer.setSrcMac(niLayer.GetAdapterObject(adapterNumber).getHardwareAddress());
		
		niLayer.SetAdapterNumber(0);
		niLayer.Receive();
	}
	
	
	public ArpAppLayer(String pName) throws IOException {
		pLayerName = pName;
		
		setTitle("TestARP");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 900, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		/*-----ARP Panel-----*/
		JPanel ARPPanel = new JPanel();
		ARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		ARPPanel.setToolTipText("");
		ARPPanel.setBounds(12, 23, 436, 353);
		contentPane.add(ARPPanel);
		ARPPanel.setLayout(null);
		
		arpModel = new DefaultTableModel(arpModelContents, arpModelHeader);
		ARPTable = new JTable(arpModel);
		ARPTable.setBounds(12, 20, 412, 206);
		ARPPanel.add(ARPTable);
		
		JLabel lblNewLabel = new JLabel("IP주소");
		lblNewLabel.setFont(new Font("돋움", Font.BOLD, 14));
		lblNewLabel.setBounds(16, 295, 52, 36);
		ARPPanel.add(lblNewLabel);
		
		IPTextField = new JTextField();
		IPTextField.setBounds(98, 300, 202, 30);
		ARPPanel.add(IPTextField);
		IPTextField.setColumns(10);
		
		ARPItemDelete = new JButton("Item Delete");
		ARPItemDelete.setBounds(26, 236, 146, 38);
		ARPItemDelete.addActionListener(new btnListener());
		ARPPanel.add(ARPItemDelete);
		
		ARPAllDelete = new JButton("All Delete");
		ARPAllDelete.setBounds(254, 236, 146, 38);
		ARPAllDelete.addActionListener(new btnListener());
		ARPPanel.add(ARPAllDelete);
		
		ARP_IPSend = new JButton("Send");
		ARP_IPSend.setBounds(327, 303, 73, 23);
		ARP_IPSend.addActionListener(new btnListener());
		ARPPanel.add(ARP_IPSend);
		
		
		/*-----Proxy Panel-----*/
		JPanel ProxyPanel = new JPanel();
		ProxyPanel.setBorder(new TitledBorder(null, "Proxy ARP Entry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ProxyPanel.setBounds(458, 23, 414, 227);
		contentPane.add(ProxyPanel);
		ProxyPanel.setLayout(null);
		
		proxyModel = new DefaultTableModel(proxyModelContents, proxyModelHeader);
		ProxyTable = new JTable(proxyModel);
		ProxyTable.setBounds(12, 21, 390, 151);
		ProxyPanel.add(ProxyTable);
		
		ProxyAdd = new JButton("Add");
		ProxyAdd.setBounds(22, 179, 156, 38);
		ProxyAdd.addActionListener(new btnListener());
		ProxyPanel.add(ProxyAdd);
		
		ProxyDelete = new JButton("Delete");
		ProxyDelete.setBounds(232, 179, 156, 38);
		ProxyDelete.addActionListener(new btnListener());
		ProxyPanel.add(ProxyDelete);
		
		
		/*-----GARP Panel-----*/
		JPanel GARPPanel = new JPanel();
		GARPPanel.setBorder(new TitledBorder(null, "Gratultous ARP", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GARPPanel.setBounds(460, 260, 412, 116);
		contentPane.add(GARPPanel);
		GARPPanel.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("H/W 주소");
		lblNewLabel_1.setFont(new Font("돋움", Font.BOLD, 16));
		lblNewLabel_1.setBounds(12, 41, 74, 36);
		GARPPanel.add(lblNewLabel_1);
		
		GARPTextField = new JTextField();
		GARPTextField.setBounds(98, 41, 218, 36);
		GARPPanel.add(GARPTextField);
		GARPTextField.setColumns(10);
		
		GARPSend = new JButton("Send");
		GARPSend.setBounds(332, 41, 68, 36);
		GARPSend.addActionListener(new btnListener());
		GARPPanel.add(GARPSend);
		
		
		/*-----종료, 취소-----*/
		JButton btnEnd = new JButton("종료");
		btnEnd.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnEnd.setBounds(334, 396, 114, 42);
		contentPane.add(btnEnd);
		
		JButton btnCancel = new JButton("취소");
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		btnCancel.setBounds(458, 396, 114, 42);
		contentPane.add(btnCancel);
		
		setVisible(true);
	}
	
	class proxyWindow extends JFrame{
		public proxyWindow() {
			setTitle("Proxy ARP Entry 추가");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 492, 295);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JLabel ProxyEntryIPLabel = new JLabel("IP 주소");
			ProxyEntryIPLabel.setFont(new Font("돋움", Font.BOLD, 20));
			ProxyEntryIPLabel.setBounds(86, 64, 74, 34);
			contentPane.add(ProxyEntryIPLabel);
			
			JLabel proxyEntryEthernetLabel = new JLabel("Ethernet 주소");
			proxyEntryEthernetLabel.setFont(new Font("돋움", Font.BOLD, 20));
			proxyEntryEthernetLabel.setBounds(31, 112, 129, 34);
			contentPane.add(proxyEntryEthernetLabel);
			
			proxyEntryIp = new JTextField();
			proxyEntryIp.setBounds(172, 64, 223, 30);
			contentPane.add(proxyEntryIp);
			proxyEntryIp.setColumns(10);
			
			proxyEntryMac = new JTextField();
			proxyEntryMac.setColumns(10);
			proxyEntryMac.setBounds(172, 117, 223, 30);
			contentPane.add(proxyEntryMac);
			
			JButton proxyEntryAddBtn = new JButton("Add");
			proxyEntryAddBtn.setBounds(104, 191, 97, 23);
			contentPane.add(proxyEntryAddBtn);
			proxyEntryAddBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//entry 추가
					String inputIp = proxyEntryIp.getText().trim();
					String inputMac = proxyEntryMac.getText().trim();
					
					InetAddress ip = null;
					try {
						ip = InetAddress.getByName(inputIp);
					} catch (UnknownHostException e1) {
						// TODO 자동 생성된 catch 블록
						e1.printStackTrace();
					}
					byte[] byteIp = ip.getAddress();
					
					byte[] byteMac = macStringToByte(inputMac);
					ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
					
					arpLayer.addProxy(byteIp, byteMac);
					
					setVisible(false);
				}
			});
			
			JButton proxyEntryCancelBtn = new JButton("Cancel");
			proxyEntryCancelBtn.setBounds(266, 191, 97, 23);
			contentPane.add(proxyEntryCancelBtn);
			proxyEntryCancelBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			
			setVisible(true);
		}
	}
	
	class btnListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			/*----ARP Action-----*/
			if(e.getSource() == ARP_IPSend) {
				//IP 입력 후 send버튼 눌렀을 때
				String inputIP = IPTextField.getText().trim();
				
				InetAddress ip = null;
				try {
					ip = InetAddress.getByName(inputIP);
					byte[] dstIP = ip.getAddress();
					
					ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
					arpLayer.setDstIp(dstIP);
					
					TCPLayer tcpLayer = (TCPLayer)m_LayerMgr.GetLayer("TCP");
					tcpLayer.Send(new byte[0], 0);
					
					//arpLayer.Send(new byte[0], 0);
					
				} catch (UnknownHostException e1) {
					e1.printStackTrace();
				}
				
				IPTextField.setText("");
			}
			
			else if(e.getSource() == ARPItemDelete) {
				//ItemDelete 버튼을 눌렀을 때
				ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
				int selectRow = ARPTable.getSelectedRow();
				if(selectRow == -1) {
					return;
				}
				else {
					String ipAddress = arpModel.getValueAt(selectRow, 0).toString();
					InetAddress ip = null;
					try {
						ip = InetAddress.getByName(ipAddress);
					} catch (UnknownHostException e1) {
						// TODO 자동 생성된 catch 블록
						e1.printStackTrace();
					}
					byte[] byteIp = ip.getAddress();
					arpLayer.cacheRemove(byteIp);
				}
			}
			
			else if(e.getSource() == ARPAllDelete) {
				//AllDelete 버튼을 눌렀을 때
				ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
				arpLayer.cacheRemoveAll();
			}
			
			/*----- Proxy Action -----*/
			else if(e.getSource() == ProxyAdd) {
				//Add 버튼을 눌렀을 때
				new proxyWindow();
			}
			
			else if(e.getSource() == ProxyDelete) {
				//Delete 버튼을 눌렀을 때
				ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
				
				int selectRow = ProxyTable.getSelectedRow();
				if(selectRow == -1) {
					return;
				}
				else {
					String ipAddress = proxyModel.getValueAt(selectRow, 0).toString();
					InetAddress ip = null;
					try {
						ip = InetAddress.getByName(ipAddress);
					} catch (UnknownHostException e1) {
						// TODO 자동 생성된 catch 블록
						e1.printStackTrace();
					}
					byte[] byteIp = ip.getAddress();
					arpLayer.proxyRemove(byteIp);
				}
			}
			
			/*----- GARP Action -----*/
			else if(e.getSource() == GARPSend) {
				//GARP Send버튼을 눌렀을 때
				byte[] mac = macStringToByte(GARPTextField.getText());
				
				ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
				arpLayer.setSrcMac(mac);
				try {
					arpLayer.setDstIp(InetAddress.getLocalHost().getAddress());
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				TCPLayer tcpLayer = (TCPLayer)m_LayerMgr.GetLayer("TCP");
				tcpLayer.Send(new byte[0], 0);
				
				//arpLayer.Send(new byte[0], 0);
				GARPTextField.setText("");
			}
		}
	}
	
	public String macByteToString(byte[] byte_MacAddress) { //MAC Byte주소를 String으로 변환
		String MacAddress = "";
		for (int i = 0; i < 6; i++) { 
			//2자리 16진수를 대문자로, 그리고 1자리 16진수는 앞에 0을 붙임.
			MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");
			
			if (i != 5) {
				//2자리 16진수 자리 단위 뒤에 "-"붙여주기
				MacAddress += ":";
			}
		}
		return MacAddress;
	}

	public byte[] macStringToByte(String mac) {
		// string mac 주소는 "00:00:00:00:00:00" 형태
		byte[] ret = new byte[6];

		StringTokenizer tokens = new StringTokenizer(mac, ":");

		for (int i = 0; tokens.hasMoreElements(); i++) {

			String temp = tokens.nextToken();

			try {
				ret[i] = Byte.parseByte(temp, 16);
			} catch (NumberFormatException e) {
				int minus = (Integer.parseInt(temp, 16)) - 256;
				ret[i] = (byte) (minus);
			}
		}

		return ret;
	}
	
	public String ipByteToString(byte[] stringIP) {
		String result = "";
		for(byte raw : stringIP){
			result += raw & 0xFF;
			result += ".";
		}
		return result.substring(0, result.length()-1);		
	}
	
	public void updateARPCacheTable(ArrayList<ARPCache> cache_table) {
		// GUI에 cache table 업데이트
		//ip주소를 string으로 변환 필요
		//mac주소를 string으로 변환 필요
		//ARPTable 변수 (JTable)의 row에 cache table 업데이터
		
		// 모든 행 삭제
		if (arpModel.getRowCount() > 0) {
		    for (int i = arpModel.getRowCount() - 1; i > -1; i--) {
		        arpModel.removeRow(i);
		    }
		}
		
		//cache_table의 모든 행 추가
		Iterator<ARPCache> iter = cache_table.iterator();
    	while(iter.hasNext()) {
    		ARPCache cache = iter.next();
    		String[] row = new String[3];
    		
    		row[0] = ipByteToString(cache.ip);
    		if(cache.status == false) {
    			row[1] = "??????????";
    			row[2] = "incomplete";
    		}
    		else {
    			row[1] = macByteToString(cache.mac);
    			row[2] = "complete";
    		}
    		
    		arpModel.addRow(row);
    	}
	}
	
	public void updateProxyEntry(ArrayList<Proxy> proxyEntry) {
		// GUI에 proxy Entry 업데이트
		// 모든 행 삭제
		if (proxyModel.getRowCount() > 0) {
		    for (int i = proxyModel.getRowCount() - 1; i > -1; i--) {
		        proxyModel.removeRow(i);
		    }
		}
		
		Iterator<Proxy> iter = proxyEntry.iterator();
    	while(iter.hasNext()) {
    		Proxy proxy = iter.next();
    		String[] row = new String[2];
    		
    		row[0] = ipByteToString(proxy.ip);
    		row[1] = macByteToString(proxy.mac);
    		
    		proxyModel.addRow(row);
    	}
	}
	
	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		// TODO Auto-generated method stub
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}
}
