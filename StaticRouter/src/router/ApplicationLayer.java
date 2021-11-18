package router;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jnetpcap.PcapIf;

import java.awt.*;

public class ApplicationLayer extends BaseLayer{
	private JTable routingTable;

	private JTable arpCacheTable;

	public void CreateWindow()
	{
		final int windowWidth = 960;
		final int windowHeight = 540;
		var gbc = new GridBagConstraints();

		// TODO: use GridBagLayout
		JPanel routingPanel = new JPanel();
		routingPanel.setBounds(0, 0, windowWidth / 2, windowHeight);

		JLabel routingTitle = new JLabel("Static Routing Table");
		routingPanel.add(routingTitle);

		routingTable = new JTable(new DefaultTableModel(new String[][] {},
				new String[]{"Destination", "Netmask", "Gateway", "Flag", "Interface", "Metric"}));
		JScrollPane sp=new JScrollPane(routingTable);
		routingPanel.add(sp);

		JButton addRoutingTblBtn = new JButton("Add");
		addRoutingTblBtn.addActionListener(e -> onClickAddRoutingTableBtn());
		routingPanel.add(addRoutingTblBtn);

		JButton removeRoutingTblBtn = new JButton("Remove");
		removeRoutingTblBtn.addActionListener(e -> onClickDeleteRoutingTableBtn());
		routingPanel.add(removeRoutingTblBtn);

		JPanel cachePanel = new JPanel();
		cachePanel.setLayout(new GridBagLayout());
		cachePanel.setBounds(windowWidth / 2, 7 , windowWidth / 2 - 20 , windowHeight - 65);

		gbc.weightx = 1;

		JLabel cacheTitle = new JLabel("ARP Cache Table");
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		cachePanel.add(cacheTitle, gbc);

		arpCacheTable = new JTable(new DefaultTableModel(new String[][] {},
				new String[]{"IP Address", "Ethernet Address", "Interface", "Flag"}));
		JScrollPane sp2 =new JScrollPane(arpCacheTable);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.6;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		cachePanel.add(sp2, gbc);

		JButton deleteCacheTblBtn = new JButton("Delete");
		deleteCacheTblBtn.addActionListener(e -> onClickDeleteCacheTableBtn());
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.2;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		cachePanel.add(deleteCacheTblBtn, gbc);


		JFrame f=new JFrame();//creating instance of JFrame
		f.add(routingPanel);
		f.add(cachePanel);
		f.setSize(windowWidth,windowHeight);
		f.setLocationRelativeTo(null);
		f.setLayout(null);//using no layout managers
		f.setVisible(true);//making the frame visible
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public ApplicationLayer(String pName)
	{
		pLayerName = pName;
		CreateWindow();
	}

	public static void main(String[] args) {
		
		layerManager.AddLayer(new NILayer("NI"));
		layerManager.AddLayer(new ApplicationLayer("App"));
	}
	public class ProxyTableAddPopup extends JFrame {

		private JPanel contentPane;
		
		private JTextField DstIp;
		private JTextField NetMask;
		private JTextField Gateway;

		
		private JCheckBox FlagU;
		private JCheckBox FlagG;
		private JCheckBox FlagH;

		private JComboBox<String> Ninterface;
		
		ProxyTableAddPopup(){

			setTitle("Routing table add");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(100, 100, 490, 295);
			contentPane = new JPanel();
			
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);
			contentPane.setLayout(null);
			
			JLabel ProxyEntryIpLabel = new JLabel("Destination");
			ProxyEntryIpLabel.setFont(new Font("µ¸¿ò", Font.BOLD, 10));
			ProxyEntryIpLabel.setBounds(25, 35, 75, 35);
			contentPane.add(ProxyEntryIpLabel);
			
			JLabel proxyEntryEthernetLabel = new JLabel("NetMask");
			proxyEntryEthernetLabel.setFont(new Font("µ¸¿ò", Font.BOLD, 10));
			proxyEntryEthernetLabel.setBounds(25, 70, 135, 35);
			contentPane.add(proxyEntryEthernetLabel);
			
			DstIp = new JTextField();
			DstIp.setBounds(170, 35, 220, 30);
			contentPane.add(DstIp);
			DstIp.setColumns(10);
			
			NetMask = new JTextField();
			NetMask.setColumns(10);
			NetMask.setBounds(170, 70, 220, 30);
			contentPane.add(NetMask);
			
			JButton routeTableAddBtn = new JButton("Add");
			routeTableAddBtn.setBounds(105, 215, 100, 25);
			contentPane.add(routeTableAddBtn);
			
			JButton routeTableCancelBtn = new JButton("Cancel");
			routeTableCancelBtn.setBounds(266, 215, 100, 25);
			contentPane.add(routeTableCancelBtn);
			routeTableCancelBtn.addActionListener(e->{
				dispose();
			});
			
			JLabel gatewayLabel = new JLabel("Gateway");
			gatewayLabel.setFont(new Font("µ¸¿ò", Font.BOLD, 10));
			gatewayLabel.setBounds(25, 105, 135, 35);
			contentPane.add(gatewayLabel);
			
			Gateway = new JTextField();
			Gateway.setColumns(10);
			Gateway.setBounds(170, 105, 220, 30);
			contentPane.add(Gateway);
			
			JLabel flagLabel = new JLabel("Flag");
			flagLabel.setFont(new Font("µ¸¿ò", Font.BOLD, 10));
			flagLabel.setBounds(25, 140, 135, 35);
			contentPane.add(flagLabel);
			
			JCheckBox FlagU = new JCheckBox("UP");
			JCheckBox FlagG = new JCheckBox("Gateway");
			JCheckBox FlagH = new JCheckBox("Host");
			FlagU.setBounds(170, 140, 50 , 30);
			FlagG.setBounds(220, 140, 100, 30);
			FlagH.setBounds(320, 140, 80 , 30);

			contentPane.add(FlagU);
			contentPane.add(FlagG);
			contentPane.add(FlagH);

			JLabel interfaceLabel = new JLabel("Interface");
			interfaceLabel.setFont(new Font("µ¸¿ò", Font.BOLD, 10));
			interfaceLabel.setBounds(25, 175, 135, 35);
			contentPane.add(interfaceLabel);
			
			Ninterface = new JComboBox();
			Ninterface.setBounds(170,180,100,20);
			contentPane.add(Ninterface);
			NILayer NI = (NILayer) layerManager.GetLayer("NI"); 
			for (PcapIf pcapIf : NI.getAdapterList()) {
				Ninterface.addItem(pcapIf.getName());
			}
			routeTableAddBtn.addActionListener(e -> {
				String dst = DstIp.getText().trim();
				String netMask = NetMask.getText().trim();
				String gateway = Gateway.getText().trim();
				String nInf = Ninterface.getSelectedItem().toString().trim();
				boolean flagU = FlagU.isSelected();
				if (!dst.equals("") && !netMask.equals("") && flagU	&& !gateway.equals("") && !nInf.equals("")) {
					String flag = "U";
					if (FlagG.isSelected()) flag+="G";
					if (FlagH.isSelected()) flag+="H";
					((DefaultTableModel)routingTable.getModel()).addRow(new String[]{dst, netMask, gateway, flag, nInf});
				}
			});
	
			setVisible(true);
		}

		
	}
	public void onClickAddRoutingTableBtn()
	{
		//FIXME: test code
		new ProxyTableAddPopup();
	}

	public void onClickDeleteRoutingTableBtn()
	{
		int row = routingTable.getSelectedRow();
		if (row == -1) {
			return;
		}
		((DefaultTableModel) routingTable.getModel()).removeRow(row);
	}

	public void onClickDeleteCacheTableBtn()
	{
		int row = arpCacheTable.getSelectedRow();
		if (row == -1) {
			return;
		}
		((DefaultTableModel) arpCacheTable.getModel()).removeRow(row);
	}

}