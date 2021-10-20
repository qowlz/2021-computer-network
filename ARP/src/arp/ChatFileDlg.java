package arp;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;

public class ChatFileDlg extends BaseLayer {
	/**
	 * 파일 전송 쓰레드
	 */
	class FileSendThread implements Runnable{
		FileAppLayer FileLayer;
		byte[] data;
		int length;
		String name;
		public FileSendThread(FileAppLayer layer, File file) {
			this.FileLayer = layer;
			try {
				this.data = Files.readAllBytes(file.toPath());
			} catch (IOException e) {}

			this.name = file.getName();
			this.length = data.length;
		}

		@Override
		public void run() {
			FileLayer.Send(this.data, this.length, this.name);
		}
	}

	// UI Components
	private JFrame jframe;
	private Container contentPane;
	private JTextField ChattingInput;
	private JTextArea ChattingArea;
	private JTextArea srcIpAddress;
	private JTextArea FileUrl;
	private JLabel srcLabel;
	private JLabel dstLabel;  
	private JButton SettingButton;
	private JButton ChatSendButton;
	private JButton FileSelectButton;
	private JButton FileSendButton;
	private JPanel FileTransferPanel;
	private JPanel FilePathPanel;
	public JTextArea dstIpAddress;
	public JProgressBar progressBar;
	
	private File file;
	private static JComboBox<String> NICComboBox;
	private int adapterNumber = 0;
	private String Text;
	
	public static void main(String[] args) {

		// 전체 레이어 생성 및 연결
		layerManager.AddLayer(new NILayer("NI"));
		layerManager.AddLayer(new EthernetLayer("Ethernet"));
		layerManager.AddLayer(new ARPLayer("ARP"));
		layerManager.AddLayer(new ArpAppLayer("ARPGUI"));
		layerManager.AddLayer(new IPLayer("IP"));
		layerManager.AddLayer(new TCPLayer("TCP"));
		layerManager.AddLayer(new ChatFileDlg("ChatFileGUI"));
		layerManager.AddLayer(new FileAppLayer("File"));
		layerManager.AddLayer(new ChatAppLayer("Chat"));
		layerManager.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *TCP ( *Chat ( *ChatFileGUI ) *File ( *ChatFileGUI ) *ARPGUI ) ) ) *IP ) )");

		((IPLayer) layerManager.GetLayer("IP")).RunTimerTask(1000);

		ARPLayer ARP = (ARPLayer) layerManager.GetLayer("ARP");
		ARP.setArpAppLayer((ArpAppLayer) layerManager.GetLayer("ARPGUI"));
	}

	public ChatFileDlg(String pName) {
		pLayerName = pName;
		jframe = new JFrame();

		jframe.setTitle("CHAT & FILE TRANSFER");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setBounds(250, 250, 644, 425);
		jframe.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				NILayer.exitProgram = true;
				System.exit(0);
			}
		});
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		jframe.setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);
	
		ChattingArea = new JTextArea();
		ChattingArea.setEditable(true);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);
		ChattingArea.setLineWrap(true); // Auto new line

		JPanel chattingInputPanel = new JPanel();
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingInput = new JTextField();
		ChattingInput.setBounds(2, 2, 250, 20);
		chattingInputPanel.add(ChattingInput);
		ChattingInput.setColumns(10);

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(380, 5, 236, 371);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		JPanel sourceAddressPanel = new JPanel();
		sourceAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		sourceAddressPanel.setBounds(10, 140, 170, 20);
		settingPanel.add(sourceAddressPanel);
		sourceAddressPanel.setLayout(null);

		srcLabel = new JLabel("Source IP Address");
		srcLabel.setBounds(10, 115, 170, 20);
		settingPanel.add(srcLabel);

		srcIpAddress = new JTextArea();
		srcIpAddress.setBounds(2, 2, 170, 20); 
		srcIpAddress.setEditable(false);
		sourceAddressPanel.add(srcIpAddress);

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		dstLabel = new JLabel("Destination IP Address");
		dstLabel.setBounds(10, 187, 190, 20);
		settingPanel.add(dstLabel);

		dstIpAddress = new JTextArea();
		dstIpAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstIpAddress);

		JLabel NICLabel = new JLabel("NIC List");
		NICLabel.setBounds(10, 20, 170, 20);
		settingPanel.add(NICLabel);

		NICComboBox = new JComboBox();
		NICComboBox.setBounds(10, 49, 170, 20);
		settingPanel.add(NICComboBox);

		NILayer NI = (NILayer) layerManager.GetLayer("NI");
		for (PcapIf pcapIf : NI.getAdapterList()){
			NICComboBox.addItem(pcapIf.getName()); // 어댑터List 추가
		}

		// ComboBox 이벤트 처리
		NICComboBox.addActionListener(e -> {
			JComboBox jcombo = (JComboBox) e.getSource();
			adapterNumber = jcombo.getSelectedIndex();

			NI.setDevice(adapterNumber, 65536, Pcap.MODE_PROMISCUOUS, 10 * 1000);

			srcIpAddress.setText(IpToStr(ipAddress));
		});

		SettingButton = new JButton("Setting");
		SettingButton.setBounds(10, 250, 100, 20);
		SettingButton.addActionListener(new UIEventListener());
		settingPanel.add(SettingButton);// setting

		ChatSendButton = new JButton("Send");
		ChatSendButton.setBounds(270, 230, 80, 20);
		ChatSendButton.addActionListener(new UIEventListener());
		chattingPanel.add(ChatSendButton);
		
		FileTransferPanel = new JPanel();
		FileTransferPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "File Transfer",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		FileTransferPanel.setBounds(10, 285, 360, 90);
		contentPane.add(FileTransferPanel);
		FileTransferPanel.setLayout(null);

		FilePathPanel = new JPanel();
		FilePathPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FilePathPanel.setBounds(10, 20, 250, 20);
		FileTransferPanel.add(FilePathPanel);
		FilePathPanel.setLayout(null);

		FileUrl = new JTextArea();
		FileUrl.setEditable(false);
		FileUrl.setBounds(2, 2, 250, 20);
		FilePathPanel.add(FileUrl);

		FileSelectButton = new JButton("File...");
		FileSelectButton.setBounds(270, 20, 80, 20);
		FileSelectButton.addActionListener(new UIEventListener());
		FileTransferPanel.add(FileSelectButton);

		this.progressBar = new JProgressBar(0, 100);
		this.progressBar.setBounds(10, 50, 250, 20);
		this.progressBar.setStringPainted(true);
		FileTransferPanel.add(this.progressBar);

		FileSendButton = new JButton("Send");
		FileSendButton.setEnabled(false);
		FileSendButton.setBounds(270, 50, 80, 20);
		FileSendButton.addActionListener(new UIEventListener());
		FileTransferPanel.add(FileSendButton);

		jframe.setVisible(true);
	}

	class UIEventListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == SettingButton) { //setting 버튼 이벤트

				if (SettingButton.getText() == "Reset") { // Reset 상태에서 클릭
					srcIpAddress.setText("");
					dstIpAddress.setText("");  // src, dst IP 초기화

					SettingButton.setText("Setting"); // Setting 상태로 설정
					srcIpAddress.setEnabled(true);  // 변경 가능
					dstIpAddress.setEnabled(true);  // 변경 가능

				}
				else { // Setting 상태에 클릭

					NILayer NI = (NILayer) layerManager.GetLayer("NI");
					NI.Receive(); // Receive Thread 실행

					SettingButton.setText("Reset"); // Reset 상태로 변경
					srcIpAddress.setEnabled(false);  // 변경 불가
					dstIpAddress.setEnabled(false);  // 변경 불가
				}
			}

			if (e.getSource() == ChatSendButton) { // Chat send
				if (SettingButton.getText() == "Reset") { // Src dst 가 설정된 상태
					String input = ChattingInput.getText(); // 입력칸의 Text
					ChattingArea.append("[SEND] : " + input + "\n"); //  ChattingArea에 추가
					byte[] bytes = null;
					try {
						bytes = input.getBytes("UTF-8"); // UTF-8 인코딩으로 Byte array 변환
					} catch (UnsupportedEncodingException e1) {
					}
					IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));

					IP.SendHeader.ip_dst = StrToIp(dstIpAddress.getText());

					((ChatAppLayer)GetUnderLayer(0)).Send(bytes, (short)(bytes.length));
					// byte array로 변환된 입력 String과 byte array의 길이를 Chat App Layer로 전송
					ChattingInput.setText("");
					// 입력칸 초기화
				} else {
					JOptionPane.showMessageDialog(null, "Address Setting Error!.");// IP 설정이 안된 상태
				}
			}
			if(e.getSource() == FileSelectButton){
				JFileChooser choose = new JFileChooser();
				int file_val = choose.showOpenDialog(null);
				if(file_val == JFileChooser.APPROVE_OPTION){
					file = choose.getSelectedFile();
					FileUrl.setText(choose.getSelectedFile().getPath());
					FileSelectButton.setEnabled(true);
					FileUrl.setEnabled(false);
					FileSendButton.setEnabled(true);
					progressBar.setValue(0);

		        	String newChat = "[FILE SEND " + file.getName() + "]\n";

		        	ChattingArea.append(newChat);
				}
			}

			if(e.getSource() == FileSendButton){
				IPLayer IP = ((IPLayer) layerManager.GetLayer("IP"));
				IP.SendHeader.ip_dst = StrToIp(dstIpAddress.getText());

				FileAppLayer FAlayer = (FileAppLayer) layerManager.GetLayer("File");
				FileSendThread FST = new FileSendThread(FAlayer, file);
				Thread Send_Thread = new Thread(FST);
				Send_Thread.start();
			}
		}
	}

	public boolean Receive(byte[] input) { // Chat Receive
		if (input != null) {

			byte[] data = input;   //byte chat input data
			try {
				Text = new String(data, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ChattingArea.append("[RECV] : " + Text + "\n"); // ChattingArea에 추가

			return false;
		}
		return false ;
	}
}