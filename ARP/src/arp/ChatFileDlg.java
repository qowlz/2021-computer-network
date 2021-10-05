package arp;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.jnetpcap.PcapIf;

public class ChatFileDlg extends JFrame implements BaseLayer {

	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();

	private JTextField ChattingWrite;

	Container contentPane;

	JTextArea ChattingArea; //챗팅화면 보여주는 위치
	JTextArea srcMacAddress;
	JTextArea dstMacAddress;

	JLabel lblsrc;  // Label(이름)
	JLabel lbldst;

	JButton Setting_Button; //Port번호(주소)를 입력받은 후 완료버튼설정
	JButton Chat_send_Button; //채팅화면의 채팅 입력 완료 후 data Send버튼

	JButton FileSelectButton;
	JButton FileSendButton;
	JPanel FileTransferPanel;
	JPanel FilePathPanel;
	JTextArea FileUrl;
	JProgressBar progressBar;
	
	File file;
	
	static JComboBox<String> NICComboBox;

	int adapterNumber = 0;

	String Text;

//	public static void main(String[] args) {
//	
//		/*과제
//		// 흐름대로 레이어 연결해주는 부분.. 
//		과제  */
//		m_LayerMgr.AddLayer(new NILayer("NI"));
//		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
//		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
//		m_LayerMgr.AddLayer(new ChatFileDlg("GUI"));
//
//		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *GUI ) )");
//	}

	public ChatFileDlg(String pName) {
		pLayerName = pName;

		setTitle("CHAT & FILE TRANSFER");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel chattingPanel = new JPanel();// chatting panel
		chattingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "chatting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		chattingPanel.setBounds(10, 5, 360, 276);
		contentPane.add(chattingPanel);
		chattingPanel.setLayout(null);

		JPanel chattingEditorPanel = new JPanel();// chatting write panel
		chattingEditorPanel.setBounds(10, 15, 340, 210);
		chattingPanel.add(chattingEditorPanel);
		chattingEditorPanel.setLayout(null);

		ChattingArea = new JTextArea();
		ChattingArea.setEditable(false);
		ChattingArea.setBounds(0, 0, 340, 210);
		chattingEditorPanel.add(ChattingArea);// chatting edit

		JPanel chattingInputPanel = new JPanel();// chatting write panel
		chattingInputPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		chattingInputPanel.setBounds(10, 230, 250, 20);
		chattingPanel.add(chattingInputPanel);
		chattingInputPanel.setLayout(null);

		ChattingWrite = new JTextField();
		ChattingWrite.setBounds(2, 2, 250, 20);// 249
		chattingInputPanel.add(ChattingWrite);
		ChattingWrite.setColumns(10);// writing area

		JPanel settingPanel = new JPanel(); //Setting 관련 패널
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

		lblsrc = new JLabel("Source Mac Address");
		lblsrc.setBounds(10, 115, 170, 20); //위치 지정
		settingPanel.add(lblsrc); //panel 추가

		srcMacAddress = new JTextArea();
		srcMacAddress.setBounds(2, 2, 170, 20); 
		sourceAddressPanel.add(srcMacAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination Mac Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstMacAddress = new JTextArea();
		dstMacAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstMacAddress);// dst address

		JLabel NICLabel = new JLabel("NIC List");
		NICLabel.setBounds(10, 20, 170, 20);
		settingPanel.add(NICLabel);

		NICComboBox = new JComboBox();
		NICComboBox.setBounds(10, 49, 170, 20);
		settingPanel.add(NICComboBox);
		
		
		NILayer tempNiLayer = (NILayer) m_LayerMgr.GetLayer("NI"); //콤보박스 리스트에 추가하기 위한 인터페이스 객체

		for (int i = 0; i < tempNiLayer.getAdapterList().size(); i++) { //네트워크 인터페이스가 저장된 어뎁터 리스트의 사이즈만큼의 배열 생성
			//NICComboBox.addItem(((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(i).getDescription());
			PcapIf pcapIf = tempNiLayer.GetAdapterObject(i); //
			NICComboBox.addItem(pcapIf.getName()); // NIC 선택 창에 어댑터를 보여줌
		}

		NICComboBox.addActionListener(new ActionListener() { //combo박스를 눌렀을 때의 동작

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//adapterNumber = NICComboBox.getSelectedIndex();
				JComboBox jcombo = (JComboBox) e.getSource();
				adapterNumber = jcombo.getSelectedIndex();
				System.out.println("Index: " + adapterNumber); 
				try {
					srcMacAddress.setText("");
					srcMacAddress.append(get_MacAddress(((NILayer) m_LayerMgr.GetLayer("NI"))
							.GetAdapterObject(adapterNumber).getHardwareAddress()));

				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		try {// 저절로 MAC주소 보이게하기
			srcMacAddress.append(get_MacAddress(
					((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(adapterNumber).getHardwareAddress()));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		;

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button
		
		FileTransferPanel = new JPanel();// setting을 위한 위치 지정
		FileTransferPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "File Transfer",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		FileTransferPanel.setBounds(10, 285, 360, 90);
		contentPane.add(FileTransferPanel);
		FileTransferPanel.setLayout(null);

		FilePathPanel = new JPanel();// chatting write panel
		FilePathPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		FilePathPanel.setBounds(10, 20, 250, 20);
		FileTransferPanel.add(FilePathPanel);
		FilePathPanel.setLayout(null);

		FileUrl = new JTextArea();
		FileUrl.setEditable(false);
		FileUrl.setBounds(2, 2, 250, 20);
		FilePathPanel.add(FileUrl);// chatting edit

		FileSelectButton = new JButton("File...");
		FileSelectButton.setBounds(270, 20, 80, 20);
		FileSelectButton.addActionListener(new setAddressListener());
		FileTransferPanel.add(FileSelectButton);// chatting send button

		this.progressBar = new JProgressBar(0, 100);
		this.progressBar.setBounds(10, 50, 250, 20);
		this.progressBar.setStringPainted(true);
		FileTransferPanel.add(this.progressBar);

		FileSendButton = new JButton("전송");
		FileSendButton.setEnabled(false);
		FileSendButton.setBounds(270, 50, 80, 20);
		FileSendButton.addActionListener(new setAddressListener());
		FileTransferPanel.add(FileSendButton);

		setVisible(true);

	}

	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == Setting_Button) { //setting 버튼 누를 시

				if (Setting_Button.getText() == "Reset") { //reset 눌려졌을 경우,
					srcMacAddress.setText("");  //주소 공백으로 바뀜
					dstMacAddress.setText("");  //주소 공백으로 바뀜
					Setting_Button.setText("Setting"); //버튼을 누르면, setting으로 바뀜
					srcMacAddress.setEnabled(true);  //버튼을 활성화시킴
					dstMacAddress.setEnabled(true);  //버튼을 활성화시킴
				}  
				else { //송수신주소 설정
					 
					byte[] srcAddress = new byte[6];
					byte[] dstAddress = new byte[6];

					String src = srcMacAddress.getText(); //MAC 주소를 String byte로 변환
					String dst = dstMacAddress.getText();

					String[] byte_src = src.split("-"); //Sting MAC 주소를"-"로 나눔
					for (int i = 0; i < 6; i++) {
						srcAddress[i] = (byte) Integer.parseInt(byte_src[i], 16); //16비트 (2byte)
					}

					String[] byte_dst = dst.split("-");//Sting MAC 주소를"-"로 나눔
					for (int i = 0; i < 6; i++) {
						dstAddress[i] = (byte) Integer.parseInt(byte_dst[i], 16);//16비트 (2byte)
					}

					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(srcAddress); //이부분을 통해 선택한 주소를 프로그램 상 소스주소로 사용가능
					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetDstAddress(dstAddress); //이부분을 통해 선택한 주소를 프로그램 상 목적지주소로 사용가능

					((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(adapterNumber);

					Setting_Button.setText("Reset"); //setting 버튼 누르면 리셋으로 바뀜
					dstMacAddress.setEnabled(false);  //버튼을 비활성화시킴
					srcMacAddress.setEnabled(false);  //버튼을 비활성화시킴  
				} 
			}

			if (e.getSource() == Chat_send_Button) { //send 버튼 누르면, 
				if (Setting_Button.getText() == "Reset") { 
					String input = ChattingWrite.getText(); //채팅창에 입력된 텍스트를 저장
					ChattingArea.append("[SEND] : " + input + "\n"); //성공하면 입력값 출력
					byte[] bytes = input.getBytes(); //입력된 메시지를 바이트로 저장
					
					((ChatAppLayer)m_LayerMgr.GetLayer("ChatApp")).Send(bytes, bytes.length);
					//채팅창에 입력된 메시지를 chatApplayer로 보냄
					ChattingWrite.setText(""); 
					//채팅 입력란 다시 비워줌
				} else {
					JOptionPane.showMessageDialog(null, "Address Setting Error!.");//주소설정 에러
				}
			}
			if(e.getSource() == FileSelectButton){
				JFileChooser choose = new JFileChooser();
				int file_val = choose.showOpenDialog(null);
				if(file_val == JFileChooser.APPROVE_OPTION){
					file = choose.getSelectedFile();
					FileUrl.setText(file.getPath());
					FileSelectButton.setEnabled(true);
					FileUrl.setEnabled(false);
					FileSendButton.setEnabled(true);
					progressBar.setValue(0);
				}
			}
			
			if(e.getSource() == FileSendButton){
				FileAppLayer FAlayer = (FileAppLayer)m_LayerMgr.GetLayer("FileApp");
				File_Send_Thread FST = new File_Send_Thread(FAlayer);
				Thread Send_Thread = new Thread(FST);
				Send_Thread.start();
			}
		}
	}
	
	class File_Send_Thread implements Runnable{
		FileAppLayer FAL;
		
		public File_Send_Thread(FileAppLayer layer) {
			// TODO Auto-generated constructor stub
			this.FAL = layer;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			FAL.setAndStartSendFile();
		}
		
		
	}
	public String get_MacAddress(byte[] byte_MacAddress) { //MAC Byte주소를 String으로 변환

		String MacAddress = "";
		for (int i = 0; i < 6; i++) { 
			//2자리 16진수를 대문자로, 그리고 1자리 16진수는 앞에 0을 붙임.
			MacAddress += String.format("%02X%s", byte_MacAddress[i], (i < MacAddress.length() - 1) ? "" : "");
			
			if (i != 5) {
				//2자리 16진수 자리 단위 뒤에 "-"붙여주기
				MacAddress += "-";
			}
		} 
		System.out.println("mac_address:" + MacAddress);
		return MacAddress;
	}

	public boolean Receive(byte[] input) { //메시지 Receive
		if (input != null) {
			byte[] data = input;   //byte 단위의 input data
			Text = new String(data); //아래층에서 올라온 메시지를 String text로 변환해줌
			ChattingArea.append("[RECV] : " + Text + "\n"); //채팅창에 수신메시지를 보여줌
			return false;
		}
		return false ;
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

	public File getFile() {
		// TODO Auto-generated method stub
		return this.file;
	}

}
