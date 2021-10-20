package arp;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class ArpAppLayer extends BaseLayer{
	
	private JFrame jframe;
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

	static int adapterNumber = 0;

	public ArpAppLayer(String pName) {
		pLayerName = pName;
		jframe = new JFrame();
		
		jframe.setTitle("TestARP");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setBounds(100, 100, 900, 500);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		jframe.setContentPane(contentPane);
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
				jframe.setVisible(false);
			}
		});
		btnEnd.setBounds(334, 396, 114, 42);
		contentPane.add(btnEnd);
		
		JButton btnCancel = new JButton("취소");
		btnCancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jframe.setVisible(false);
			}
		});
		btnCancel.setBounds(458, 396, 114, 42);
		contentPane.add(btnCancel);
		
		jframe.setVisible(true);
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

					byte[] byteIp = StrToIp(inputIp);
					
					byte[] byteMac = StrToMac(inputMac);
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

			if(e.getSource() == ARP_IPSend) {
				//IPSend버튼 눌렀을 때
				String inputIP = IPTextField.getText().trim();

				byte[] dstIP = StrToIp(inputIP);
				
				IPLayer IP = (IPLayer)m_LayerMgr.GetLayer("IP");
				IP.SendHeader.ip_dst = Arrays.copyOf(dstIP, 4);
							
				TCPLayer tcpLayer = (TCPLayer)GetUnderLayer(0);
				tcpLayer.Send(new byte[0]);

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
					
					byte[] byteIp = StrToIp(ipAddress);
					arpLayer.cacheRemove(byteIp);
				}
			}
			
			else if(e.getSource() == ARPAllDelete) {
				//AllDelete 버튼
				ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
				arpLayer.cacheRemoveAll();
			}
			

			else if(e.getSource() == ProxyAdd) {
				//Add 버튼
				new proxyWindow();
			}
			
			else if(e.getSource() == ProxyDelete) {
				//Delete 버튼
				ARPLayer arpLayer = (ARPLayer)m_LayerMgr.GetLayer("ARP");
				
				int selectRow = ProxyTable.getSelectedRow();
				if(selectRow == -1) {
					return;
				}
				else {
					String ipAddress = proxyModel.getValueAt(selectRow, 0).toString();

					byte[] byteIp = StrToIp(ipAddress);
					arpLayer.proxyRemove(byteIp);
				}
			}
			
			else if(e.getSource() == GARPSend) {
				//GARPSend 버튼
				byte[] mac = StrToMac(GARPTextField.getText());
				
				IPLayer IP = (IPLayer)m_LayerMgr.GetLayer("IP");

				BaseLayer.macAddress = mac;
				
				IP.SendHeader.ip_dst = ipAddress;
				
				TCPLayer tcpLayer = (TCPLayer)m_LayerMgr.GetLayer("TCP");
				tcpLayer.Send(new byte[0]);
				
				GARPTextField.setText("");
			}
		}
	}
	
	public void updateARPCacheTable(ArrayList<ARP_CACHE> cache_table) {
		// GUI cache table 업데이트
		
		// 모든 행 삭제
		if (arpModel.getRowCount() > 0) {
		    for (int i = arpModel.getRowCount() - 1; i > -1; i--) {
		        arpModel.removeRow(i);
		    }
		}
		
		//cacheTable -> GUI
		Iterator<ARP_CACHE> iter = cache_table.iterator();
    	while(iter.hasNext()) {
    		ARP_CACHE cache = iter.next();
    		String[] row = new String[3];
    		
    		row[0] = IpToStr(cache.ip);
    		if(cache.status == false) {
    			row[1] = "??????????";
    			row[2] = "incomplete";
    		}
    		else {
    			row[1] = MacToStr(cache.mac);
    			row[2] = "complete";
    		}
    		
    		arpModel.addRow(row);
    	}
	}
	
	public void updateProxyEntry(ArrayList<Proxy> proxyEntry) {
		// GUI proxyTable 업데이트
		
		// 모든 행 삭제
		if (proxyModel.getRowCount() > 0) {
		    for (int i = proxyModel.getRowCount() - 1; i > -1; i--) {
		        proxyModel.removeRow(i);
		    }
		}
		
		//proxyTable -> GUI
		Iterator<Proxy> iter = proxyEntry.iterator();
    	while(iter.hasNext()) {
    		Proxy proxy = iter.next();
    		String[] row = new String[2];
    		
    		row[0] = IpToStr(proxy.ip);
    		row[1] = MacToStr(proxy.mac);
    		
    		proxyModel.addRow(row);
    	}
	}
	

}
