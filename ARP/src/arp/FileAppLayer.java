package arp;

import java.io.*;
import java.util.ArrayList;

public class FileAppLayer extends BaseLayer {
    private int count = 0;
    private String fileName; // �뙆�씪 �씠由�
    private int receivedLength = 0; // �닔�떊�븳 �뜲�씠�꽣�쓽 �겕湲�
    private int targetLength = 0; // �닔�떊�빐�빞�븯�뒗 �뙆�씪�쓽 珥� �겕湲�

    private File file; // ���옣�븷 �뙆�씪
    private ArrayList<byte[]> fileByteList; // �닔�떊�븳 �뙆�씪 �봽�젅�엫(�젙�젹 �쟾)
    private ArrayList<byte[]> fileSortList; // �닔�떊�븳 �뙆�씪�쓣 �젙�젹 �븯�뒗�뜲 �궗�슜�븯�뒗 由ъ뒪�듃

    public FileAppLayer(String pName) {
        // TODO Auto-generated constructor stub
        pLayerName = pName;
        fileByteList = new ArrayList();
    }

    public class _FAPP_HEADER {
        byte[] fapp_totlen;
        byte[] fapp_type;
        byte fapp_msg_type;
        byte fapp_unused;
        byte[] fapp_seq_num;
        byte[] fapp_data;

        public _FAPP_HEADER() {
            this.fapp_totlen = new byte[4];
            this.fapp_type = new byte[2];
            this.fapp_msg_type = 0x00;
            this.fapp_unused = 0x00;
            this.fapp_seq_num = new byte[4];
            this.fapp_data = null;
        }
    }

    _FAPP_HEADER m_sHeader = new _FAPP_HEADER();

    private void setFragmentation(int type){
        if(type == 0) { // 泥섏쓬
            m_sHeader.fapp_type[0] = (byte) 0x0;
            m_sHeader.fapp_type[1] = (byte) 0x0;
        }
        else if(type == 1) { // 以묎컙
            m_sHeader.fapp_type[0] = (byte) 0x0;
            m_sHeader.fapp_type[1] = (byte) 0x1;
        }
        else if(type == 2) { // �걹
            m_sHeader.fapp_type[0] = (byte) 0x0;
            m_sHeader.fapp_type[1] = (byte) 0x2;
        }
    }

    public void setFileMsgType(int type) { // fapp_msg_type 媛믪쓣 �꽕�젙
        m_sHeader.fapp_msg_type = (byte) type;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    } // �뙆�씪�씠由� �꽕�젙�옄

    // �뙆�씪 �겕湲� �꽕�젙�옄
    public void setFileSize(int fileSize) {
        m_sHeader.fapp_totlen[0] = (byte)(0xff&(fileSize >> 24));
        m_sHeader.fapp_totlen[1] = (byte)(0xff&(fileSize >> 16));
        m_sHeader.fapp_totlen[2] = (byte)(0xff&(fileSize >> 8));
        m_sHeader.fapp_totlen[3] = (byte)(0xff & fileSize);
    }

    public int calcSeqNum(byte[] input) { // 紐� 踰덉㎏ Frame�씤吏� 怨꾩궛(Frame�� 0踰덈��꽣 �떆�옉)
        int seqNum = 0;
        seqNum += (input[8] & 0xff) << 24;
        seqNum += (input[9] & 0xff) << 16;
        seqNum += (input[10] & 0xff) << 8;
        seqNum += (input[11] & 0xff);

        return seqNum;
    }

    public int calcFileFullLength(byte[] input) {
        int fullLength = 0;
        fullLength += (input[0] & 0xff) << 24;
        fullLength += (input[1] & 0xff) << 16;
        fullLength += (input[2] & 0xff) << 8;
        fullLength += (input[3] & 0xff);
        return fullLength;
    }


    public boolean fileInfoSend(byte[] input, int length) { // �뙆�씪 �젙蹂� �넚�떊 �븿�닔
        this.setFileMsgType(0); // �뙆�씪 �젙蹂� �넚�떊�엫�쓣 �굹���깂
        this.Send(input); // �뙆�씪 �젙蹂� �넚�떊

        return true;
    }

    // �봽�젅�엫�쓣 �떎 諛쏆븯�뒗吏� �솗�씤 �썑, 紐⑤몢 �젙�솗�엳 �닔�떊�뻽�쑝硫� �젙�젹�쓣 吏꾪뻾�븯�뒗 �븿�닔
    public boolean sortFileList(int lastFrameNumber) {
        // 紐⑤뱺 �봽�젅�엫�쓣 諛쏆븯�뒗吏� �솗�씤
        if((fileByteList.size() - 1 != lastFrameNumber) || (receivedLength != targetLength)) {
            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("�뙆�씪 �닔�떊 �떎�뙣\n");
            return false;
        }

        // ArrayList�뿉 SeqNum�쓣 Index濡� 媛�吏��룄濡� �궫�엯�븯�뿬 �젙�젹 吏꾪뻾
        fileSortList = new ArrayList();
        for(int checkSeqNum = 0; checkSeqNum < (lastFrameNumber + 1); ++checkSeqNum) {
            byte[] checkByteArray = fileByteList.remove(0);
            int arraySeqNum = this.calcSeqNum(checkByteArray);
            fileSortList.add(arraySeqNum, checkByteArray);
        }

        return true;
    }

    public void setAndStartSendFile() {
        ChatFileDlg upperLayer = (ChatFileDlg) this.GetUpperLayer(0);
        File sendFile = upperLayer.getFile();
        int sendTotalLength; // 蹂대궡�빞�븯�뒗 珥� �겕湲�
        int sendedLength; // �쁽�옱 蹂대궦 �겕湲�
        this.resetSeqNum();

        try (FileInputStream fileInputStream = new FileInputStream(sendFile)) {
            sendedLength = 0;
            BufferedInputStream fileReader = new BufferedInputStream(fileInputStream);
            sendTotalLength = (int)sendFile.length();
            this.setFileSize(sendTotalLength);
            byte[] sendData =new byte[1448];
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(sendTotalLength);
            if(sendTotalLength <= 1448) {
                // �뙆�씪 �젙蹂� �넚�떊
                setFragmentation(0);
                this.setFileMsgType(0);
                this.fileInfoSend(sendFile.getName().getBytes(), sendFile.getName().getBytes().length);

                // �뙆�씪 �뜲�씠�꽣 �넚�떊
                this.setFileMsgType(1);
                fileReader.read(sendData);
                this.Send(sendData);
                sendedLength += sendData.length;
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(sendedLength);
            } else {
                sendedLength = 0;
                // �뙆�씪 �젙蹂� �넚�떊
                this.setFragmentation(0);
                this.setFileMsgType(0);
                this.fileInfoSend(sendFile.getName().getBytes(), sendFile.getName().getBytes().length);

                // �뙆�씪 �뜲�씠�꽣 �넚�떊
                this.setFileMsgType(1);
                this.setFragmentation(1);
                while(fileReader.read(sendData) != -1 && (sendedLength + 1448 < sendTotalLength)) {
                    this.Send(sendData);
                    try {
                        Thread.sleep(4);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendedLength += 1448;
                    this.increaseSeqNum();
                    ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(sendedLength);
                }

                byte[] getRealDataFrame = new byte[sendTotalLength - sendedLength];
                this.setFragmentation(2);
                fileReader.read(sendData);

                for(int index = 0; index < getRealDataFrame.length; ++index) {
                    getRealDataFrame[index] = sendData[index];
                }

                this.Send(getRealDataFrame);
                sendedLength += getRealDataFrame.length;
                count = 0;
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(sendedLength);
            }
            fileInputStream.close();
            fileReader.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] RemoveCappHeader(byte[] input, int length) { // FileApp�쓽 Header瑜� �젣嫄고빐二쇰뒗 �븿�닔
        byte[] buf = new byte[length - 12];
        for(int dataIndex = 0; dataIndex < length - 12; ++dataIndex)
            buf[dataIndex] = input[12 + dataIndex];

        return buf;
    }

    public synchronized boolean Receive(byte[] input) { // �뜲�씠�꽣瑜� �닔�떊 泥섎━ �븿�닔
        byte[] data;

        if(checkReceiveFileInfo(input)) { // �뙆�씪�쓽 �젙蹂대�� 諛쏆� 寃쎌슦
            data = RemoveCappHeader(input, input.length); // Header�뾾�븷湲�
            String fileName = new String(data);
            fileName = fileName.trim();
            targetLength = calcFileFullLength(input); // 諛쏆븘�빞 �븯�뒗 珥� �겕湲� 珥덇린�솕
            file = new File("./" + fileName); //諛쏅뒗 寃쎈줈..

            // Progressbar 珥덇린�솕
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMinimum(0);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setMaximum(targetLength);
            ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(0);

            // 諛쏆� �겕湲�) 珥덇린�솕
            receivedLength = 0;
        } else {
            // �떒�렪�솕瑜� �븯吏� �븡�� �뜲�씠�꽣瑜� 諛쏆� 寃쎌슦
            if (checkNoFragmentation(input)) {
                data = RemoveCappHeader(input, input.length);
                fileByteList.add(this.calcSeqNum(input), data);
                try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    fileOutputStream.write(fileByteList.get(0));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // �떒�렪�솕瑜� 吏꾪뻾�븳 �뜲�씠�꽣瑜� 諛쏆� 寃쎌슦

                // �뜲�씠�꽣 �봽�젅�엫 �닔�떊
                fileByteList.add(input);
                receivedLength += (input.length - 12); // �뿤�뜑�쓽 湲몄씠�뒗 �젣�쇅

                // 留덉�留� �봽�젅�엫 �닔�떊
                if(checkLastDataFrame(input)) {
                    int lastFrameNumber = this.calcSeqNum(input);

                    if(sortFileList(lastFrameNumber)) {
                        try(FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            for (int frameCount = 0; frameCount < (lastFrameNumber + 1); ++frameCount) {
                                data = RemoveCappHeader(fileSortList.get(frameCount), fileSortList.get(frameCount).length);
                                fileOutputStream.write(data);
                            }
                            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("�뙆�씪 �닔�떊 諛� �깮�꽦 �셿猷�\n");
                            fileByteList = new ArrayList();
                        } catch (FileNotFoundException e) {
                            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("�뙆�씪 �닔�떊 �떎�뙣\n");
                            e.printStackTrace();
                        } catch (IOException e) {
                            ((ChatFileDlg)this.GetUpperLayer(0)).ChattingArea.append("�뙆�씪 �닔�떊 �떎�뙣\n");
                            e.printStackTrace();
                        }
                    }
                }
                ((ChatFileDlg)this.GetUpperLayer(0)).progressBar.setValue(receivedLength); // Progressbar 媛깆떊
            }
        }

        return true;
    }

    public void resetSeqNum() {
        this.m_sHeader.fapp_seq_num[0] = (byte)0x0;
        this.m_sHeader.fapp_seq_num[1] = (byte)0x0;
        this.m_sHeader.fapp_seq_num[2] = (byte)0x0;
        this.m_sHeader.fapp_seq_num[3] = (byte)0x0;
    }

    public void increaseSeqNum() { // Frame 踰덊샇 利앷� �븿�닔(Send�떆 Frame 踰덊샇 媛� 蹂�寃�)
        if((this.m_sHeader.fapp_seq_num[3] & 0xff) < 255)
            ++this.m_sHeader.fapp_seq_num[3];
        else if((this.m_sHeader.fapp_seq_num[2] & 0xff) < 255) {
            ++this.m_sHeader.fapp_seq_num[2];
            this.m_sHeader.fapp_seq_num[3] = 0;
        } else if((this.m_sHeader.fapp_seq_num[1] & 0xff) < 255) {
            ++this.m_sHeader.fapp_seq_num[1];
            this.m_sHeader.fapp_seq_num[2] = 0;
            this.m_sHeader.fapp_seq_num[3] = 0;
        } else if((this.m_sHeader.fapp_seq_num[0] & 0xff) < 255) {
            ++this.m_sHeader.fapp_seq_num[0];
            this.m_sHeader.fapp_seq_num[1] = 0;
            this.m_sHeader.fapp_seq_num[2] = 0;
            this.m_sHeader.fapp_seq_num[3] = 0;
        }
    }

    public boolean Send(byte[] input) { // �뜲�씠�꽣 �넚�떊 �븿�닔
    	
    	m_sHeader.fapp_data = input;
        byte[] bytes = ObjToByte(m_sHeader);
       
    	TCPLayer TCP = (TCPLayer) GetUnderLayer(0);
    	TCP.Header.port_src = 0x2091;
    	TCP.Send(ObjToByte(bytes));

        return true;
    }


    public boolean checkReceiveFileInfo(byte[] input) {
        if(input[6] == (byte)0x00)
            return true;

        return false;
    }
    public boolean checkLastDataFrame(byte[] input) { // 留덉�留� Frame�씤吏� �솗�씤
        if(input[4] == (byte) 0x0 && input[5] == (byte)0x0)
            return true;
        else if(input[4] == (byte) 0x0 && input[5] == (byte)0x2)
            return true;
        else
            return false;
    }

    public boolean checkNoFragmentation(byte[] input) { // File �뜲�씠�꽣媛� �떒�렪�솕瑜� 吏꾪뻾�븯吏� �븡�븯�뒗吏� 寃��궗�븯�뒗 �븿�닔
        if(input[4] == (byte) 0x00 && input[5] == (byte)0x0)
            return true;

        return false;
    }

}