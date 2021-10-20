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
	private JButton ARPIPSend;
	private JButton GARPSend;
	
	private DefaultTableModel arpModel, proxyModel;
	String ARPTableHeader[] = {"ipAddress", "macAddress", "status"};
	String[][] ARPModelContents = new String[0][3];
	String ProxyTableHeader[] = {"ipAddress", "macAddress"};
	String[][] ProxyModelContents = new String[0][2];
	
	private JTextField ProxyEntryIp;
	private JTextField ProxyEntryMac;

	static int adapterNumber = 0;

	public ArpAppLayer(String pName) {
		pLayerName = pName;
		jframe = new JFrame();
		
		jframe.setTitle("ARP");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setBounds(100, 100, 880, 385);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		jframe.setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel ARPPanel = new JPanel();
		ARPPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "ARP Cache", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		ARPPanel.setToolTipText("");
		ARPPanel.setBounds(5, 5, 435, 335);
		contentPane.add(ARPPanel);
		ARPPanel.setLayout(null);
		
		arpModel = new DefaultTableModel(ARPModelContents, ARPTableHeader);
		ARPTable = new JTable(arpModel);
		ARPTable.setBounds(10, 20, 412, 206);
		ARPPanel.add(ARPTable);
		
		JLabel dstIPLable = new JLabel("IP주소");
		dstIPLable.setFont(new Font("돋움", Font.BOLD, 12));
		dstIPLable.setBounds(15, 265, 52, 36);
		ARPPanel.add(dstIPLable);
		
		IPTextField = new JTextField();
		IPTextField.setBounds(15, 293, 250, 30);
		ARPPanel.add(IPTextField);
		IPTextField.setColumns(10);
		
		ARPItemDelete = new JButton("Delete");
		ARPItemDelete.setBounds(12, 235, 146, 38);
		ARPItemDelete.addActionListener(new btnListener());
		ARPPanel.add(ARPItemDelete);
		
		ARPAllDelete = new JButton("Clear");
		ARPAllDelete.setBounds(275, 235, 146, 38);
		ARPAllDelete.addActionListener(new btnListener());
		ARPPanel.add(ARPAllDelete);
		
		ARPIPSend = new JButton("Send");
		ARPIPSend.setBounds(275, 285, 146, 38);
		ARPIPSend.addActionListener(new btnListener());
		ARPPanel.add(ARPIPSend);
		
		JPanel ProxyPanel = new JPanel();
		ProxyPanel.setBorder(new TitledBorder(null, "Proxy ARP Entry", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ProxyPanel.setBounds(445, 5, 414, 227);
		contentPane.add(ProxyPanel);
		ProxyPanel.setLayout(null);
		
		proxyModel = new DefaultTableModel(ProxyModelContents, ProxyTableHeader);
		ProxyTable = new JTable(proxyModel);
		ProxyTable.setBounds(12, 21, 390, 151);
		ProxyPanel.add(ProxyTable);
		
		ProxyAdd = new JButton("Add");
		ProxyAdd.setBounds(12, 179, 156, 38);
		ProxyAdd.addActionListener(new btnListener());
		ProxyPanel.add(ProxyAdd);
		
		ProxyDelete = new JButton("Delete");
		ProxyDelete.setBounds(245, 179, 156, 38);
		ProxyDelete.addActionListener(new btnListener());
		ProxyPanel.add(ProxyDelete);
		
		JPanel GARPPanel = new JPanel();
		GARPPanel.setBorder(new TitledBorder(null, "Gratultous ARP", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GARPPanel.setBounds(445, 240, 414, 100);
		contentPane.add(GARPPanel);
		GARPPanel.setLayout(null);
		
		JLabel MACLable = new JLabel("MAC Addr");
		MACLable.setFont(new Font("돋움", Font.BOLD, 12));
		MACLable.setBounds(12, 15, 74, 30);
		GARPPanel.add(MACLable);
		
		GARPTextField = new JTextField();
		GARPTextField.setBounds(12, 40, 300, 30);
		GARPPanel.add(GARPTextField);
		GARPTextField.setColumns(10);
		
		GARPSend = new JButton("Send");
		GARPSend.setBounds(325, 40, 65, 30);
		GARPSend.addActionListener(new btnListener());
		GARPPanel.add(GARPSend);
			
		jframe.setVisible(true);
	}
	
	class proxyWindow extends JFrame{
		public proxyWindow() {
			setTitle("Proxy ARP Entry 추가");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 490, 295);
			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JLabel ProxyEntryIpLabel = new JLabel("IP Addr");
			ProxyEntryIpLabel.setFont(new Font("돋움", Font.BOLD, 20));
			ProxyEntryIpLabel.setBounds(85, 65, 75, 35);
			contentPane.add(ProxyEntryIpLabel);
			
			JLabel proxyEntryEthernetLabel = new JLabel("Ethernet Addr");
			proxyEntryEthernetLabel.setFont(new Font("돋움", Font.BOLD, 20));
			proxyEntryEthernetLabel.setBounds(25, 108, 135, 35);
			contentPane.add(proxyEntryEthernetLabel);
			
			ProxyEntryIp = new JTextField();
			ProxyEntryIp.setBounds(170, 65, 220, 30);
			contentPane.add(ProxyEntryIp);
			ProxyEntryIp.setColumns(10);
			
			ProxyEntryMac = new JTextField();
			ProxyEntryMac.setColumns(10);
			ProxyEntryMac.setBounds(170, 110, 220, 30);
			contentPane.add(ProxyEntryMac);
			
			JButton proxyEntryAddBtn = new JButton("Add");
			proxyEntryAddBtn.setBounds(105, 215, 100, 25);
			contentPane.add(proxyEntryAddBtn);
			proxyEntryAddBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					//entry 추가
					String inputIp = ProxyEntryIp.getText().trim();
					String inputMac = ProxyEntryMac.getText().trim();

					byte[] byteIp = StrToIp(inputIp);
					byte[] byteMac = StrToMac(inputMac);
					
					ARPLayer arpLayer = (ARPLayer)layerManager.GetLayer("ARP");
					arpLayer.addProxy(byteIp, byteMac);
					
					setVisible(false);
				}
			});
			
			JButton proxyEntryCancelBtn = new JButton("Cancel");
			proxyEntryCancelBtn.setBounds(266, 215, 100, 25);
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


			if(e.getSource() == ARPIPSend) {
				//IPSend버튼 눌렀을 때
				String inputIP = IPTextField.getText().trim();

				byte[] dstIP = StrToIp(inputIP);
				
				IPLayer IP = (IPLayer) layerManager.GetLayer("IP");
				IP.SendHeader.ip_dst = Arrays.copyOf(dstIP, 4);
							
				TCPLayer tcpLayer = (TCPLayer)GetUnderLayer(0);
				tcpLayer.Send(new byte[0]);

				IPTextField.setText("");
			}else if(e.getSource() == ARPItemDelete) {
				//ItemDelete 버튼을 눌렀을 때
				ARPLayer arpLayer = (ARPLayer) layerManager.GetLayer("ARP");
				int selectRow = ARPTable.getSelectedRow();
				if(selectRow == -1) {
					return;
				}else {
					String ipAddress = arpModel.getValueAt(selectRow, 0).toString();
					
					byte[] byteIp = StrToIp(ipAddress);
					arpLayer.cacheRemove(byteIp);
				}
			}else if(e.getSource() == ARPAllDelete) {
				//AllDelete 버튼
				ARPLayer arpLayer = (ARPLayer) layerManager.GetLayer("ARP");
				arpLayer.cacheRemoveAll();
			}else if(e.getSource() == ProxyAdd) {
				//Add 버튼
				new proxyWindow();
			}else if(e.getSource() == ProxyDelete) {
				//Delete 버튼
				ARPLayer arpLayer = (ARPLayer) layerManager.GetLayer("ARP");
				
				int selectRow = ProxyTable.getSelectedRow();
				if(selectRow == -1) {
					return;
				}else {
					String ipAddress = proxyModel.getValueAt(selectRow, 0).toString();

					byte[] byteIp = StrToIp(ipAddress);
					arpLayer.proxyRemove(byteIp);
				}
			}else if(e.getSource() == GARPSend) {
				//GARPSend 버튼
				byte[] mac = StrToMac(GARPTextField.getText());
				
				IPLayer IP = (IPLayer) layerManager.GetLayer("IP");

				BaseLayer.macAddress = mac;
				
				IP.SendHeader.ip_dst = ipAddress;
				
				TCPLayer tcpLayer = (TCPLayer) layerManager.GetLayer("TCP");
				tcpLayer.Send(new byte[0]);
				
				GARPTextField.setText("");
			}
		}
	}
	
	// GUI cache table 업데이트
	public void updateARPCacheTable(ArrayList<ARP_CACHE> cache_table) {
		
		// 모든 행 삭제
		if (arpModel.getRowCount() > 0) 
		    for (int i = arpModel.getRowCount() - 1; i > -1; i--)
		        arpModel.removeRow(i);

		//cacheTable -> GUI
		Iterator<ARP_CACHE> iter = cache_table.iterator();
    	while(iter.hasNext()) {
    		ARP_CACHE cache = iter.next();
    		String[] row = new String[3];
    		
    		row[0] = IpToStr(cache.ip);
    		if(cache.status == false) {
    			row[1] = "??????????";
    			row[2] = "incomplete";
    		}else{
    			row[1] = MacToStr(cache.mac);
    			row[2] = "complete";
    		}
    		
    		arpModel.addRow(row);
    	}
	}
	
	// GUI proxyTable 업데이트
	public void updateProxyEntry(ArrayList<Proxy> proxyEntry) {
		
		// 모든 행 삭제
		if (proxyModel.getRowCount() > 0) 
		    for (int i = proxyModel.getRowCount() - 1; i > -1; i--) 
		        proxyModel.removeRow(i);
		    
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