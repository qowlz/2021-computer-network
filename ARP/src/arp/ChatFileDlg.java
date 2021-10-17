package arp;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

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

	private JTextField ChattingWrite;

	JFrame jframe;
	
	Container contentPane;

	JTextArea ChattingArea; //梨쀭똿�솕硫� 蹂댁뿬二쇰뒗 �쐞移�
	JTextArea srcIpAddress;
	JTextArea dstIpAddress;

	JLabel lblsrc;  // Label(�씠由�)
	JLabel lbldst;

	JButton Setting_Button; //Port踰덊샇(二쇱냼)瑜� �엯�젰諛쏆� �썑 �셿猷뚮쾭�듉�꽕�젙
	JButton Chat_send_Button; //梨꾪똿�솕硫댁쓽 梨꾪똿 �엯�젰 �셿猷� �썑 data Send踰꾪듉

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
	
	public static void main(String[] args) {
	
//		/*과제
//		// 흐름대로 레이어 연결해주는 부분.. 
//		과제  */

		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		m_LayerMgr.AddLayer(new ARPLayer("ARP"));
		m_LayerMgr.AddLayer(new ArpAppLayer("ARPGUI"));
		m_LayerMgr.AddLayer(new IPLayer("IP"));
		m_LayerMgr.AddLayer(new TCPLayer("TCP"));
		m_LayerMgr.AddLayer(new ChatFileDlg("ChatFileGUI"));
		m_LayerMgr.AddLayer(new FileAppLayer("File"));
		m_LayerMgr.AddLayer(new ChatAppLayer("Chat"));
		
		m_LayerMgr.ConnectLayers(" NI ( *Ethernet ( *ARP ( *IP ( *TCP ( *Chat ( *ChatFileGUI ) *File ( *ChatFileGUI ) *ARPGUI ) ) ) *IP ) )");

		ARPLayer ARP = (ARPLayer) m_LayerMgr.GetLayer("ARP");
		ARP.appLayer = (ArpAppLayer) m_LayerMgr.GetLayer("ARPGUI");
				
	}

	public ChatFileDlg(String pName) {
		pLayerName = pName;
		jframe = new JFrame();

		jframe.setTitle("CHAT & FILE TRANSFER");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setBounds(250, 250, 644, 425);
		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		jframe.setContentPane(contentPane);
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

		JPanel settingPanel = new JPanel(); //Setting 愿��젴 �뙣�꼸
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

		lblsrc = new JLabel("Source IP Address");
		lblsrc.setBounds(10, 115, 170, 20); //�쐞移� 吏��젙
		settingPanel.add(lblsrc); //panel 異붽�

		srcIpAddress = new JTextArea();
		srcIpAddress.setBounds(2, 2, 170, 20); 
		srcIpAddress.setEditable(false);
		sourceAddressPanel.add(srcIpAddress);// src address

		JPanel destinationAddressPanel = new JPanel();
		destinationAddressPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		destinationAddressPanel.setBounds(10, 212, 170, 20);
		settingPanel.add(destinationAddressPanel);
		destinationAddressPanel.setLayout(null);

		lbldst = new JLabel("Destination IP Address");
		lbldst.setBounds(10, 187, 190, 20);
		settingPanel.add(lbldst);

		dstIpAddress = new JTextArea();
		dstIpAddress.setBounds(2, 2, 170, 20);
		destinationAddressPanel.add(dstIpAddress);// dst address

		JLabel NICLabel = new JLabel("NIC List");
		NICLabel.setBounds(10, 20, 170, 20);
		settingPanel.add(NICLabel);

		NICComboBox = new JComboBox();
		NICComboBox.setBounds(10, 49, 170, 20);
		settingPanel.add(NICComboBox);
		
		NILayer NI = (NILayer) m_LayerMgr.GetLayer("NI"); //肄ㅻ낫諛뺤뒪 由ъ뒪�듃�뿉 異붽��븯湲� �쐞�븳 �씤�꽣�럹�씠�뒪 媛앹껜
		
		for (int i = 0; i < NI.getAdapterList().size(); i++) { //�꽕�듃�썙�겕 �씤�꽣�럹�씠�뒪媛� ���옣�맂 �뼱�럞�꽣 由ъ뒪�듃�쓽 �궗�씠利덈쭔�겮�쓽 諛곗뿴 �깮�꽦
			//NICComboBox.addItem(((NILayer) m_LayerMgr.GetLayer("NI")).GetAdapterObject(i).getDescription());
			PcapIf pcapIf = NI.GetAdapterObject(i); //
			NICComboBox.addItem(pcapIf.getName()); // NIC �꽑�깮 李쎌뿉 �뼱�뙌�꽣瑜� 蹂댁뿬以�
		}

		NICComboBox.addActionListener(new ActionListener() { //combo諛뺤뒪瑜� �닃���쓣 �븣�쓽 �룞�옉

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//adapterNumber = NICComboBox.getSelectedIndex();
				JComboBox jcombo = (JComboBox) e.getSource();
				adapterNumber = jcombo.getSelectedIndex();
				
				NI.setDevice(adapterNumber, 64 * 1024, Pcap.MODE_PROMISCUOUS, 10 * 1000);

				srcIpAddress.setText(IpToStr(ipAddress));
				
			}
		});

		Setting_Button = new JButton("Setting");// setting
		Setting_Button.setBounds(80, 270, 100, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		Chat_send_Button = new JButton("Send");
		Chat_send_Button.setBounds(270, 230, 80, 20);
		Chat_send_Button.addActionListener(new setAddressListener());
		chattingPanel.add(Chat_send_Button);// chatting send button
		
		FileTransferPanel = new JPanel();// setting�쓣 �쐞�븳 �쐞移� 吏��젙
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

		FileSendButton = new JButton("�쟾�넚");
		FileSendButton.setEnabled(false);
		FileSendButton.setBounds(270, 50, 80, 20);
		FileSendButton.addActionListener(new setAddressListener());
		FileTransferPanel.add(FileSendButton);

		jframe.setVisible(true);

	}

	class setAddressListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == Setting_Button) { //setting 踰꾪듉 �늻瑜� �떆

				if (Setting_Button.getText() == "Reset") { //reset �닃�젮議뚯쓣 寃쎌슦,
					srcIpAddress.setText("");  //二쇱냼 怨듬갚�쑝濡� 諛붾��
					dstIpAddress.setText("");  //二쇱냼 怨듬갚�쑝濡� 諛붾��

					Setting_Button.setText("Setting"); //踰꾪듉�쓣 �늻瑜대㈃, setting�쑝濡� 諛붾��
					srcIpAddress.setEnabled(true);  //踰꾪듉�쓣 �솢�꽦�솕�떆�궡
					dstIpAddress.setEnabled(true);  //踰꾪듉�쓣 �솢�꽦�솕�떆�궡
					
				}  
				else { //�넚�닔�떊二쇱냼 �꽕�젙

					NILayer NI = (NILayer) m_LayerMgr.GetLayer("NI");
					NI.Receive();

					Setting_Button.setText("Reset"); //setting 踰꾪듉 �늻瑜대㈃ 由ъ뀑�쑝濡� 諛붾��
					srcIpAddress.setEnabled(false);  //踰꾪듉�쓣 鍮꾪솢�꽦�솕�떆�궡
					dstIpAddress.setEnabled(false);  //踰꾪듉�쓣 鍮꾪솢�꽦�솕�떆�궡  
				} 
			}

			if (e.getSource() == Chat_send_Button) { //send 踰꾪듉 �늻瑜대㈃, 
				if (Setting_Button.getText() == "Reset") { 
					String input = ChattingWrite.getText(); //梨꾪똿李쎌뿉 �엯�젰�맂 �뀓�뒪�듃瑜� ���옣
					ChattingArea.append("[SEND] : " + input + "\n"); //�꽦怨듯븯硫� �엯�젰媛� 異쒕젰
					byte[] bytes = input.getBytes(); //�엯�젰�맂 硫붿떆吏�瑜� 諛붿씠�듃濡� ���옣
					IPLayer IP = ((IPLayer)m_LayerMgr.GetLayer("IP"));
					//arp.ChatAppLayer.Send(byte[])" because the return value of "arp.LayerManager.GetLayer(String)" is null
					IP.Header.ip_dst = StrToIp(dstIpAddress.getText());
					System.out.println("CFL" + bytes.length);
	
					((ChatAppLayer)GetUnderLayer(0)).Send(bytes, (short)bytes.length);
					//梨꾪똿李쎌뿉 �엯�젰�맂 硫붿떆吏�瑜� chatApplayer濡� 蹂대깂
					ChattingWrite.setText(""); 
					//梨꾪똿 �엯�젰�� �떎�떆 鍮꾩썙以�
				} else {
					JOptionPane.showMessageDialog(null, "Address Setting Error!.");//二쇱냼�꽕�젙 �뿉�윭
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
				IPLayer IP = ((IPLayer)m_LayerMgr.GetLayer("IP"));			
				IP.Header.ip_dst = StrToIp(dstIpAddress.getText());
				
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

	public boolean Receive(byte[] input) { //硫붿떆吏� Receive
		if (input != null) {
			byte[] data = input;   //byte �떒�쐞�쓽 input data
			Text = new String(data); //�븘�옒痢듭뿉�꽌 �삱�씪�삩 硫붿떆吏�瑜� String text濡� 蹂��솚�빐以�
			ChattingArea.append("[RECV] : " + Text + "\n"); //梨꾪똿李쎌뿉 �닔�떊硫붿떆吏�瑜� 蹂댁뿬以�
			return false;
		}
		return false ;
	}


	public File getFile() {
		// TODO Auto-generated method stub
		return this.file;
	}

}
