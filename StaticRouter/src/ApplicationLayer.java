import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ApplicationLayer {
	private JTable routingTable;

	private JTable arpCacheTable;

	private JTable proxyARPTable;


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
		cachePanel.setBounds(windowWidth / 2, 0, windowWidth / 2 - 20, windowHeight / 2 - 20);

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

		JPanel proxyPanel = new JPanel();
		proxyPanel.setLayout(new GridBagLayout());
		proxyPanel.setBounds(windowWidth / 2, windowHeight / 2, windowWidth / 2 - 20, windowHeight / 2 - 50);

		JLabel proxyTitle = new JLabel("Proxy ARP Table");
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		proxyPanel.add(proxyTitle, gbc);

		proxyARPTable =new JTable(new DefaultTableModel(new String[][] {},
				new String[]{"IP Address", "Ethernet Address", "Interface"}));
		JScrollPane sp3 =new JScrollPane(proxyARPTable);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.6;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		proxyPanel.add(sp3, gbc);

		JButton addProxyBtn = new JButton("Add");
		addProxyBtn.addActionListener(e -> onClickAddProxyTableBtn());
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.2;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		proxyPanel.add(addProxyBtn, gbc);

		JButton deleteProxyBtn = new JButton("Delete");
		deleteProxyBtn.addActionListener(e -> onClickDeleteProxyTableBtn());
		gbc.fill = GridBagConstraints.NONE;
		gbc.weighty = 0.2;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		proxyPanel.add(deleteProxyBtn, gbc);

		JFrame f=new JFrame();//creating instance of JFrame
		f.add(routingPanel);
		f.add(cachePanel);
		f.add(proxyPanel);
		f.setSize(windowWidth,windowHeight);
		f.setLocationRelativeTo(null);
		f.setLayout(null);//using no layout managers
		f.setVisible(true);//making the frame visible
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public ApplicationLayer()
	{
		CreateWindow();
	}

	public static void main(String[] args) {
		new ApplicationLayer();
	}

	public void onClickAddRoutingTableBtn()
	{
		//FIXME: test code
		((DefaultTableModel)routingTable.getModel()).addRow(new String[]{"t1", "t2", "t3", "t4", "t5", "t6"});
	}

	public void onClickDeleteRoutingTableBtn()
	{

	}

	public void onClickDeleteCacheTableBtn()
	{

	}

	public void onClickAddProxyTableBtn()
	{

	}

	public void onClickDeleteProxyTableBtn()
	{

	}
}