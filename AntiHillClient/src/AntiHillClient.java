import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import java.io.*;
import java.net.*;



public class AntiHillClient {
	
	public static String getLogonID(){
	    String logonID = "";
	    try{
	      while(logonID.equals("")){
	        logonID = JOptionPane.showInputDialog("닉네임을 써주세요(다른 사람이 사용 중인 닉네임 생성 불가)");
	      }
	    }catch(NullPointerException e){
	      System.exit(0);
	    }
	    return logonID;
	  }

	  public static void main(String args[]){
	    String id = getLogonID();
	    try{
	      if (args.length == 0){
	        ClientThread thread = new ClientThread();
	        thread.start();
	        thread.requestLogon(id);
	      } else if (args.length == 1){
	        ClientThread thread = new ClientThread(args[0]);
	        thread.start();
	        thread.requestLogon(id);
	      } 
	    }catch(Exception e){
	      System.out.println(e);
	    }
	}
	  
}



//채팅룸디스플레이


class ChatRoomDisplay extends JFrame implements ActionListener, KeyListener,
                                                ListSelectionListener, ChangeListener
{
  private ClientThread cr_thread;
  private String idTo;
  private boolean isSelected;
  public boolean isAdmin;

  private JLabel roomer;
  public JList roomerInfo;
  private JButton coerceOut, sendWord, sendFile, quitRoom;
  private Font font;
  private JViewport view;
  private JScrollPane jsp3;
  public JTextArea messages;
  public JTextField message;
  
  public ChatRoomDisplay(ClientThread thread){
    super("Chat-Application-대화방");

    cr_thread = thread;
    isSelected = false;
    isAdmin = false;
    font = new Font("SanSerif", Font.PLAIN, 12);

    Container c = getContentPane();
    c.setLayout(null);

    JPanel p = new JPanel();
    p.setLayout(null);
    p.setBounds(425, 10, 140, 175);
    p.setBorder(new TitledBorder(
      new EtchedBorder(EtchedBorder.LOWERED), "참여자"));


    roomerInfo = new JList();
    roomerInfo.setFont(font);
    JScrollPane jsp2 = new JScrollPane(roomerInfo);
    roomerInfo.addListSelectionListener(this);
    jsp2.setBounds(15, 25, 110, 135);
    p.add(jsp2);

    c.add(p);

    p = new JPanel();
    p.setLayout(null);
    p.setBounds(10, 10, 410, 340);
    p.setBorder(new TitledBorder(
      new EtchedBorder(EtchedBorder.LOWERED), "채팅창"));

    view = new JViewport();
    messages = new JTextArea();
    messages.setFont(font);
    messages.setEditable(false);
    view.add(messages);
    view.addChangeListener(this);
    jsp3 = new JScrollPane(view);
    jsp3.setBounds(15, 25, 380, 270);
    p.add(jsp3);

    message = new JTextField();
    message.setFont(font);
    message.addKeyListener(this);
    message.setBounds(15, 305, 380, 20);
    message.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
    p.add(message);

    c.add(p);

    coerceOut = new JButton("강 퇴 ! ! ");
    coerceOut.setFont(font);
    coerceOut.addActionListener(this);
    coerceOut.setBounds(445, 195, 100, 30);
    coerceOut.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    //c.add(coerceOut);

    sendWord = new JButton("귓 속 말 ^^");
    sendWord.setFont(font);
    sendWord.addActionListener(this);
    sendWord.setBounds(445, 235, 100, 30);
    sendWord.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
   // c.add(sendWord);

    sendFile = new JButton("파 일 전 송");
    sendFile.setFont(font);
    sendFile.addActionListener(this);
    sendFile.setBounds(445, 275, 100, 30);
    sendFile.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
   // c.add(sendFile);

    quitRoom = new JButton("나 갈 래 요");
    quitRoom.setFont(font);
    quitRoom.addActionListener(this);
    quitRoom.setBounds(445, 315, 100, 30);
    quitRoom.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    c.add(quitRoom);

    Dimension dim = getToolkit().getScreenSize();
    setSize(580, 400);
    setLocation(dim.width/2 - getWidth()/2,
                dim.height/2 - getHeight()/2);
    show();

    addWindowListener(
      new WindowAdapter() {
        public void windowActivated(WindowEvent e) {
          message.requestFocusInWindow();
        }
      }
    );

    addWindowListener(
      new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          cr_thread.requestQuitRoom();
        }
      }
    );
  }

  public void resetComponents(){
    messages.setText("");
    message.setText("");
    message.requestFocusInWindow();
  }

  public void keyPressed(KeyEvent ke){
    if (ke.getKeyChar() == KeyEvent.VK_ENTER){
      String words = message.getText();
      String data;
      String idTo;
      if(words.startsWith("/w")){
        StringTokenizer st = new StringTokenizer(words, " ");
        String command = st.nextToken();
        idTo = st.nextToken();
        data = st.nextToken();
        cr_thread.requestSendWordTo(data, idTo);
        message.setText("");
      } else {
        cr_thread.requestSendWord(words);
        message.requestFocusInWindow();
        
      }
    }
  }

  public void valueChanged(ListSelectionEvent e){
    isSelected = true;
    idTo = String.valueOf(((JList)e.getSource()).getSelectedValue());
  }

  public void actionPerformed(ActionEvent ae){
    if (ae.getSource() == coerceOut) {
      if (!isAdmin) {
        JOptionPane.showMessageDialog(this, "방장도 아니면서..ㅋㅋ",
                        "강제퇴장", JOptionPane.ERROR_MESSAGE);
      } else if (!isSelected) {
        JOptionPane.showMessageDialog(this, "강제 퇴장 시킬 닉네임은?",
                        "강제퇴장", JOptionPane.ERROR_MESSAGE);
      } else {
        cr_thread.requestCoerceOut(idTo);
        isSelected = false;
      }
    } else if (ae.getSource() == quitRoom) {
      cr_thread.requestQuitRoom();
    } else if (ae.getSource() == sendWord) {
      String idTo, data;
      if ((idTo = JOptionPane.showInputDialog("상대방 닉네임은?")) != null){
        if ((data = JOptionPane.showInputDialog("보낼 말을 써 봐요")) != null) {
          cr_thread.requestSendWordTo(data, idTo);
        }
      }
    } else if (ae.getSource() == sendFile) {
      String idTo;
      if ((idTo = JOptionPane.showInputDialog("상대방 닉네임은?")) != null){
        cr_thread.requestSendFile(idTo);
      }
    }
  }
  
  public void stateChanged(ChangeEvent e){
    jsp3.getVerticalScrollBar().setValue((jsp3.getVerticalScrollBar().getValue() + 20));    	
  }
  public void keyReleased(KeyEvent e){}
  public void keyTyped(KeyEvent e){}
}

//ClientThread


class ClientThread extends Thread
{
  private WaitRoomDisplay ct_waitRoom; // 대기실
  private ChatRoomDisplay ct_chatRoom; // 채팅방
  private Socket ct_sock; 
  private DataInputStream ct_in;
  private DataOutputStream ct_out;
  private StringBuffer ct_buffer;
  private Thread thisThread;
  private String ct_logonID; // ID
  private int ct_roomNumber; // 방 번호
  private static MessageBox msgBox, logonbox, fileTransBox;

  private static final String SEPARATOR = "|";
  private static final String DELIMETER = "'";
  private static final String DELIMETER2 = "=";

  private static final int REQ_LOGON = 1001;
  private static final int REQ_CREATEROOM = 1011;
  private static final int REQ_ENTERROOM = 1021;
  private static final int REQ_QUITROOM = 1031;
  private static final int REQ_LOGOUT = 1041;
  private static final int REQ_SENDWORD = 1051;
  private static final int REQ_SENDWORDTO = 1052;
  private static final int REQ_COERCEOUT = 1053;
  private static final int REQ_SENDFILE = 1061;

  private static final int YES_LOGON = 2001;
  private static final int NO_LOGON = 2002;
  private static final int YES_CREATEROOM = 2011;
  private static final int NO_CREATEROOM = 2012;
  private static final int YES_ENTERROOM = 2021;
  private static final int NO_ENTERROOM = 2022;
  private static final int YES_QUITROOM = 2031;
  private static final int YES_LOGOUT = 2041;
  private static final int YES_SENDWORD = 2051;
  private static final int YES_SENDWORDTO = 2052;
  private static final int NO_SENDWORDTO = 2053;
  private static final int YES_COERCEOUT = 2054;
  private static final int YES_SENDFILE = 2061;
  private static final int NO_SENDFILE = 2062;
  private static final int MDY_WAITUSER = 2003;
  private static final int MDY_WAITINFO = 2013;
  private static final int MDY_ROOMUSER = 2023;
  private static final int ERR_ALREADYUSER = 3001;
  private static final int ERR_SERVERFULL = 3002;
  private static final int ERR_ROOMSFULL = 3011;
  private static final int ERR_ROOMERFULL = 3021;
  private static final int ERR_PASSWORD = 3022;
  private static final int ERR_REJECTION = 3031;
  private static final int ERR_NOUSER = 3032;

  public ClientThread(){ // Thread 초기화
    ct_waitRoom = new WaitRoomDisplay(this);
    ct_chatRoom = null;
    try{
      ct_sock = new Socket(InetAddress.getLocalHost(), 2777);
      ct_in = new DataInputStream(ct_sock.getInputStream());
      ct_out = new DataOutputStream(ct_sock.getOutputStream());
      ct_buffer = new StringBuffer(4096);
      thisThread = this;
    }catch(IOException e){
      MessageBoxLess msgout = new MessageBoxLess(ct_waitRoom, "연결에러", "서버에 접속할 수 없습니다.");
      msgout.show();
    }
  }

  public ClientThread(String hostaddr){
    ct_waitRoom = new WaitRoomDisplay(this);
    ct_chatRoom = null;
    try{
      ct_sock = new Socket(hostaddr, 2777); // 소켓 초기화
      ct_in = new DataInputStream(ct_sock.getInputStream());
      ct_out = new DataOutputStream(ct_sock.getOutputStream());
      ct_buffer = new StringBuffer(4096);
      thisThread = this;
    }catch(IOException e){
      MessageBoxLess msgout = new MessageBoxLess(ct_waitRoom, "연결에러", "서버에 접속할 수 없습니다.");
      msgout.show();
    }
  }
  
  // Thread를 실행
  public void run(){
    try{
      Thread currThread = Thread.currentThread();
      while(currThread == thisThread){
        String recvData = ct_in.readUTF(); // 상대가 입력하지 않으면 계속 대기 
        StringTokenizer st = new StringTokenizer(recvData, SEPARATOR);
        int command = Integer.parseInt(st.nextToken());
        switch(command){ // 각종 상황 발생 시
          case YES_LOGON : { // 로그인 성공 시
            logonbox.dispose();
            ct_roomNumber = 0;
            try{
              StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
              Vector roomInfo = new Vector(); // 방 정보 : 벡터로 저장
              while(st1.hasMoreTokens()){
                String temp = st1.nextToken();
                if (!temp.equals("empty")){
                  roomInfo.addElement(temp);
                }
              }
              ct_waitRoom.roomInfo.setListData(roomInfo);      
              ct_waitRoom.message.requestFocusInWindow();
            }catch(NoSuchElementException e){
              ct_waitRoom.message.requestFocusInWindow();
            }
            break;
          }
          case NO_LOGON : { // 로그인 실패 시
            String id;
            int errCode = Integer.parseInt(st.nextToken()); // 에러코드 발생
            if (errCode == ERR_ALREADYUSER){
              logonbox.dispose();
              JOptionPane.showMessageDialog(ct_waitRoom, "이미 다른 사용자가 있습니다.", "로그온", JOptionPane.ERROR_MESSAGE);
              id = AntiHillClient.getLogonID();
              requestLogon(id);
            } else if (errCode == ERR_SERVERFULL){
              logonbox.dispose();
              JOptionPane.showMessageDialog(ct_waitRoom, "대화방이 만원입니다.",
                                "로그온", JOptionPane.ERROR_MESSAGE);
              id = AntiHillClient.getLogonID();
              requestLogon(id);
            }
            break;
          }
          case MDY_WAITUSER : {
            StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
            Vector user = new Vector();
            while(st1.hasMoreTokens()){
              user.addElement(st1.nextToken());
            }
            ct_waitRoom.waiterInfo.setListData(user);
            ct_waitRoom.message.requestFocusInWindow();
            break;
          }
          case YES_CREATEROOM : { // 방 만들기 성공 시 
            ct_roomNumber = Integer.parseInt(st.nextToken());
            ct_waitRoom.hide();
            if (ct_chatRoom == null) {
              ct_chatRoom = new ChatRoomDisplay(this);
              ct_chatRoom.isAdmin = true;
            } else {
              ct_chatRoom.show();
              ct_chatRoom.isAdmin = true;
              ct_chatRoom.resetComponents();
            }
            break;
          }
          case NO_CREATEROOM : { // 방 만들기 실패 시
            int errCode = Integer.parseInt(st.nextToken()); // 에러코드 발생
            if (errCode == ERR_ROOMSFULL) {
              msgBox = new MessageBox(ct_waitRoom, "대화방개설", "더 이상 대화방을 개설 할 수 없습니다.");
              msgBox.show();
            }
            break;
          }
          case MDY_WAITINFO : {
            StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
            StringTokenizer st2 = new StringTokenizer(st.nextToken(), DELIMETER);
            
            // 방 정보와 사용자 정보는 벡터로 초기화
            Vector rooms = new Vector();
            Vector users = new Vector();
            while(st1.hasMoreTokens()){
              String temp = st1.nextToken();
              if (!temp.equals("empty")){
                rooms.addElement(temp);
              }
            }
            ct_waitRoom.roomInfo.setListData(rooms);

            while(st2.hasMoreTokens()){
              users.addElement(st2.nextToken());
            }

            ct_waitRoom.waiterInfo.setListData(users);
            ct_waitRoom.message.requestFocusInWindow();

            break;
          }
          case YES_ENTERROOM : {
            ct_roomNumber = Integer.parseInt(st.nextToken());
            String id = st.nextToken();
            ct_waitRoom.hide();
            if (ct_chatRoom == null) {
              ct_chatRoom = new ChatRoomDisplay(this);
            } else {
              ct_chatRoom.show();
              ct_chatRoom.resetComponents();
            }
            break;
          }                  
          case NO_ENTERROOM : {
            int errCode = Integer.parseInt(st.nextToken());
            if (errCode == ERR_ROOMERFULL) {
              msgBox = new MessageBox(ct_waitRoom, "대화방입장",
                        "대화방이 만원입니다.");
              msgBox.show();
            } else if (errCode == ERR_PASSWORD) {
              msgBox = new MessageBox(ct_waitRoom, "대화방입장",
                        "비밀번호가 틀립니다.");
              msgBox.show();
            }
            break;
          }
          case MDY_ROOMUSER : {
            String id = st.nextToken();
            int code = Integer.parseInt(st.nextToken());
            
            StringTokenizer st1 = new StringTokenizer(st.nextToken(), DELIMETER);
            Vector user = new Vector();
            while(st1.hasMoreTokens()){
              user.addElement(st1.nextToken());
            }
            ct_chatRoom.roomerInfo.setListData(user);
            if (code == 1) {
              ct_chatRoom.messages.append("### " + id + "님이 입장하셨습니다. ###\n");
            } else if (code == 2) {
              ct_chatRoom.messages.append("### " + id + "님이 강제퇴장 되었습니다. ###\n"); 
            } else {
              ct_chatRoom.messages.append("### " + id + "님이 퇴장하셨습니다. ###\n");
            }            
            ct_chatRoom.message.requestFocusInWindow();
            break;
          }
          case YES_QUITROOM : {
            String id = st.nextToken();
            if (ct_chatRoom.isAdmin) ct_chatRoom.isAdmin = false;
            ct_chatRoom.hide();
            ct_waitRoom.show();
            ct_waitRoom.resetComponents();
            ct_roomNumber = 0;
            break;
          }
          case YES_LOGOUT : {
            ct_waitRoom.dispose();
            if (ct_chatRoom != null){
              ct_chatRoom.dispose();
            }
            release();
            break;
          }
          case YES_SENDWORD : {
            String id = st.nextToken();
            int roomNumber = Integer.parseInt(st.nextToken());
            try{
              String data = st.nextToken();
              if (roomNumber == 0){
                ct_waitRoom.messages.append(id + " : " + data + "\n");
                if(id.equals(ct_logonID)){
                  ct_waitRoom.message.setText("");
                  ct_waitRoom.message.requestFocusInWindow();
                }
                ct_waitRoom.message.requestFocusInWindow();
              } else {
                ct_chatRoom.messages.append(id + " : " + data + "\n");
                if(id.equals(ct_logonID)){
                  ct_chatRoom.message.setText("");
                }
                ct_chatRoom.message.requestFocusInWindow();
              }

            }catch(NoSuchElementException e){
              if(roomNumber == 0) ct_waitRoom.message.requestFocusInWindow();
              else ct_chatRoom.message.requestFocusInWindow();
            }                          
            break;
          }
          case YES_SENDWORDTO : {
            String id = st.nextToken();
            String idTo = st.nextToken();
            int roomNumber = Integer.parseInt(st.nextToken());
            try{
              String data = st.nextToken();
              if (roomNumber == 0){
                if(id.equals(ct_logonID)){
                  ct_waitRoom.message.setText("");
                  ct_waitRoom.messages.append("귓속말<to:" + idTo + "> : " +data + "\n");
                } else {
                  ct_waitRoom.messages.append("귓속말<from:" + id + "> : " + data + "\n");
                } 
                ct_waitRoom.message.requestFocusInWindow();
              } else {
                
                if(id.equals(ct_logonID)){
                  ct_chatRoom.message.setText("");
                  ct_chatRoom.messages.append("귓속말<to:" + idTo + "> : " +data + "\n");
                } else {
                  ct_chatRoom.messages.append("귓속말<from:" + id + "> : " + data + "\n");
                }
                ct_chatRoom.message.requestFocusInWindow();
              }
            }catch(NoSuchElementException e){
              if(roomNumber == 0) ct_waitRoom.message.requestFocusInWindow();
              else ct_chatRoom.message.requestFocusInWindow();               
            }
            break;
          }
          case NO_SENDWORDTO : {
            String id = st.nextToken();
            int roomNumber = Integer.parseInt(st.nextToken());
            String message = "";
            if (roomNumber == 0){
              message = "대기실에 " + id + "님이 존재하지 않습니다.";
              JOptionPane.showMessageDialog(ct_waitRoom, message,
                "귓속말 에러", JOptionPane.ERROR_MESSAGE);
            } else {
              message = "이 대화방에 " + id + "님이 존재하지 않습니다.";
              JOptionPane.showMessageDialog(ct_chatRoom, message,
                "귓속말 에러", JOptionPane.ERROR_MESSAGE);
            }
            break;
          }  
          
       // 파일 보내기 기능은 넣지 않을 예정이라 삭제
          /*case REQ_SENDFILE : {
            String id = st.nextToken();
            int roomNumber = Integer.parseInt(st.nextToken());
            String message = id + "로 부터 파일전송을 수락하시겠습니까?";
            int value = JOptionPane.showConfirmDialog(ct_chatRoom, message,
              "파일수신", JOptionPane.YES_NO_OPTION);
            if (value == 1) {
              try{
                ct_buffer.setLength(0);
                ct_buffer.append(NO_SENDFILE);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(ct_logonID);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(roomNumber);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(id);
                send(ct_buffer.toString());
              }catch(IOException e){
                System.out.println(e);
              }
            } else {
              StringTokenizer addr = new StringTokenizer(InetAddress.getLocalHost().toString(), "/");
              String hostname = "";
              String hostaddr = "";
              
              hostname = addr.nextToken();
              try{
                hostaddr = addr.nextToken();
              }catch(NoSuchElementException err){
                hostaddr = hostname;
              }
              
              try{
                ct_buffer.setLength(0);
                ct_buffer.append(YES_SENDFILE);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(ct_logonID);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(roomNumber);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(id);
                ct_buffer.append(SEPARATOR);
                ct_buffer.append(hostaddr);
                send(ct_buffer.toString());
              }catch(IOException e){
                System.out.println(e);
              }
              // 파일 수신 서버실행.
              new ReciveFile();
            }
            break;              
          }
          
          case NO_SENDFILE : {
            int code = Integer.parseInt(st.nextToken());
            String id = st.nextToken();
            fileTransBox.dispose();
            
            if (code == ERR_REJECTION) {
              String message = id + "님이 파일수신을 거부하였습니다.";
              JOptionPane.showMessageDialog(ct_chatRoom, message,
                                  "파일전송", JOptionPane.ERROR_MESSAGE);
              break;
            } else if (code == ERR_NOUSER) {
              String message = id + "님은 이 방에 존재하지 않습니다.";
              JOptionPane.showMessageDialog(ct_chatRoom, message,
                                  "파일전송",  JOptionPane.ERROR_MESSAGE);
              break;
            }
          }
          case YES_SENDFILE : {
            String id = st.nextToken();
            String addr = st.nextToken();
            
            fileTransBox.dispose();
            // 파일 송신 클라이언트 실행.
            new SendFile(addr);
            break;
          }*/
          
          case YES_COERCEOUT : {
            ct_chatRoom.hide();
            ct_waitRoom.show();
            ct_waitRoom.resetComponents();
            ct_roomNumber = 0;
            ct_waitRoom.messages.append("### 방장에 의해 강제퇴장 되었습니다. ###\n");
            break;
          }
        }
        Thread.sleep(200);
      }
    }catch(InterruptedException e){
      System.out.println(e);
      release();
    }catch(IOException e){
      System.out.println(e);
      release();
    }
  }

  public void requestLogon(String id){ // 아이디 받고 서버에 로그 온
    try{
      logonbox = new MessageBox(ct_waitRoom, "로그온", "서버에 로그온 중입니다.");
      logonbox.show();
      ct_logonID = id;
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_LOGON);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(id);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestCreateRoom(String roomName, int roomMaxUser,
                                int isRock, String password){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_CREATEROOM);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(roomName);
      ct_buffer.append(DELIMETER);
      ct_buffer.append(roomMaxUser);
      ct_buffer.append(DELIMETER);
      ct_buffer.append(isRock);
      ct_buffer.append(DELIMETER);
      ct_buffer.append(password);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestEnterRoom(int roomNumber, String password){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_ENTERROOM);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(roomNumber);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(password);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestQuitRoom(){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_QUITROOM);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_roomNumber);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestLogout(){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_LOGOUT);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestSendWord(String data){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_SENDWORD);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_roomNumber);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(data);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestSendWordTo(String data, String idTo){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_SENDWORDTO);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_roomNumber);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(idTo);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(data);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestCoerceOut(String idTo){
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_COERCEOUT);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_roomNumber);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(idTo);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  public void requestSendFile(String idTo){
    fileTransBox = new MessageBox(ct_chatRoom, "파일전송", "상대방의 승인을 기다립니다.");
    fileTransBox.show();
    try{
      ct_buffer.setLength(0);
      ct_buffer.append(REQ_SENDFILE);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_logonID);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(ct_roomNumber);
      ct_buffer.append(SEPARATOR);
      ct_buffer.append(idTo);
      send(ct_buffer.toString());
    }catch(IOException e){
      System.out.println(e);
    }
  }

  private void send(String sendData) throws IOException{
    ct_out.writeUTF(sendData);
    ct_out.flush();
  }

  public void release(){
    if(thisThread != null){
      thisThread = null;
    }
    try{
      if(ct_out != null){
        ct_out.close();
      }
    }catch(IOException e){
    }finally{
      ct_out = null;
    }
    try{
      if(ct_in != null){
        ct_in.close();
      }
    }catch(IOException e){
    }finally{
      ct_in = null;
    }
    try{
      if(ct_sock != null){
        ct_sock.close();
      }
    }catch(IOException e){
    }finally{
      ct_sock = null;
    }
    System.exit(0);
  }
}


//CreateRoomDisplay

class CreateRoomDisplay extends JDialog implements ActionListener, ItemListener
{
  private ClientThread client;
  private String roomName, str_password;
  private int roomMaxUser, isRock;

  private JFrame main;
  private Container c;
  private JTextField tf;
  private JPanel radioPanel;
  private JRadioButton radio1, radio2, radio3,  rock, unrock; 
  private JPasswordField password;
  private JButton ok, cancle;

  public CreateRoomDisplay(JFrame frame, ClientThread client){
    super(frame, true);
    main = frame;
    setTitle("대화방 개설");
    this.client = client;
    isRock = 0;
    roomMaxUser = 2;
    str_password = "0";

    c = getContentPane();
    c.setLayout(null);

    JLabel label;
    label = new JLabel("방제목");
    label.setBounds(10, 10, 100, 20);
    label.setForeground(Color.blue);
    c.add(label);

    tf = new JTextField();
    tf.setBounds(10, 30, 270, 20);
    c.add(tf);

    label = new JLabel("최대인원");
    label.setForeground(Color.blue);
    label.setBounds(10, 60, 100, 20);
    c.add(label);

    radioPanel = new JPanel();
    radioPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    ButtonGroup group = new ButtonGroup();
    radio1 = new JRadioButton("2명");
    radio1.setSelected(true);
    radio1.addItemListener(this);
    group.add(radio1);
    radio2 = new JRadioButton("3명");
    radio2.addItemListener(this);
    group.add(radio2);
    radio3 = new JRadioButton("4명");
    radio3.addItemListener(this);
    group.add(radio3);

    radioPanel.add(radio1);
    radioPanel.add(radio2);
    radioPanel.add(radio3);
    radioPanel.setBounds(10, 80, 280, 20);
    c.add(radioPanel);

    label = new JLabel("공개여부");
    label.setForeground(Color.blue);
    label.setBounds(10, 110, 100, 20);
  //  c.add(label);

    radioPanel = new JPanel();
    radioPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    group = new ButtonGroup();
    unrock = new JRadioButton("공개");
    unrock.setSelected(true);
    unrock.addItemListener(this);
 //   group.add(unrock);
    rock = new JRadioButton("비공개");
    rock.addItemListener(this);
 //   group.add(rock);
 //   radioPanel.add(unrock);
 //   radioPanel.add(rock);
    radioPanel.setBounds(10, 130, 280, 20);
 //   c.add(radioPanel);

    label = new JLabel("비밀번호");
    label.setForeground(Color.blue);
    label.setBounds(10, 160, 100, 20);
 //   c.add(label);

    password = new JPasswordField();
    password.setBounds(10, 180, 150, 20);
    password.setEditable(false);
  //  c.add(password);

    ok = new JButton("확 인");
    ok.setForeground(Color.blue);
    ok.setBounds(75, 220, 70, 30);
    ok.addActionListener(this);
    c.add(ok);

    cancle = new JButton("취 소");
    cancle.setForeground(Color.blue);
    cancle.setBounds(155, 220, 70, 30);
    cancle.addActionListener(this);
    c.add(cancle);

    Dimension dim = getToolkit().getScreenSize();
    setSize(300, 300);
    setLocation(dim.width/2 - getWidth()/2,
                dim.height/2 - getHeight()/2);
    show();

    addWindowListener(
      new WindowAdapter() {
        public void windowActivated(WindowEvent e) {
          tf.requestFocusInWindow();
        }
      }
    );

    addWindowListener(
      new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          dispose();
        }
      }
    );
  }

  public void itemStateChanged(ItemEvent ie){
    if (ie.getSource() == unrock){
      isRock = 0;
      str_password = "0";
      password.setText("");
      password.setEditable(false);
    } else if (ie.getSource() == rock) {
      isRock = 1;
      password.setEditable(true);
    } else if (ie.getSource() == radio1) {
      roomMaxUser = 2;
    } else if (ie.getSource() == radio2) {
      roomMaxUser = 3;
    } else if (ie.getSource() == radio3) {
      roomMaxUser = 4;
    }
  }

  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == ok){
      if(tf.getText().equals("")){
        JOptionPane.showMessageDialog(main, "방제목을 입력하세요",
                        "대화방 개설.", JOptionPane.ERROR_MESSAGE);
      } else { 
        roomName = tf.getText();
        if(isRock == 1){
          str_password = password.getText();
        }
        if(isRock ==1 && str_password.equals("")){
          JOptionPane.showMessageDialog(main, "비밀번호를 입력하세요",
                        "대화방 개설.", JOptionPane.ERROR_MESSAGE);
        } else {
          client.requestCreateRoom(roomName, roomMaxUser,
                                   isRock, str_password);
          dispose();
        }
      }
    } else {
      dispose();
    }
  }
}

//MessageBox

class MessageBox extends JDialog implements ActionListener
{
  private Container c;
  private JButton bt;

  public MessageBox(JFrame parent, String title, String message){
    super(parent, false);
    setTitle(title);
    c = getContentPane();
    c.setLayout(null);
    JLabel lbl = new JLabel(message);
    lbl.setFont(new Font("SanSerif", Font.PLAIN, 12));
    lbl.setBounds(20, 10, 190, 20);
    c.add(lbl);

    bt = new JButton("확 인");
    bt.setBounds(60, 40, 70, 25);
    bt.setFont(new Font("SanSerif", Font.PLAIN, 12));
    bt.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    bt.addActionListener(this);
    c.add(bt);

    Dimension dim = getToolkit().getScreenSize();
    setSize(200, 100);
    setLocation(dim.width/2 - getWidth()/2,
                dim.height/2 - getHeight()/2);
    show();
    addWindowListener(
      new WindowAdapter(){
        public void windowClosing(WindowEvent e){
          dispose();
        }
      }
    );
  }

  public void actionPerformed(ActionEvent ae){
    if(ae.getSource() == bt){
      dispose();
    }
  }
}

//MessageBoxLess

class MessageBoxLess extends JDialog implements ActionListener
{
  private Frame client;
  private Container c;

  public MessageBoxLess(JFrame parent, String title, String message){
    super(parent, true);
    setTitle(title);
    c = getContentPane();
    c.setLayout(null);
    JLabel lbl = new JLabel(message);
    lbl.setFont(new Font("SanSerif", Font.PLAIN, 12));
    lbl.setBounds(20, 10, 190, 20);
    c.add(lbl);

    JButton bt = new JButton("O K");
    bt.setBounds(60, 40, 70, 25);
    bt.setFont(new Font("SanSerif", Font.PLAIN, 12));
    bt.addActionListener(this);
    bt.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    c.add(bt);

    Dimension dim = getToolkit().getScreenSize();
    setSize(200, 100);
    setLocation(dim.width/2 - getWidth()/2,
                dim.height/2 - getHeight()/2);
    show();
    client = parent;
  }
  public void actionPerformed(ActionEvent ae){
    dispose();
    System.exit(0);
  }
}

//WaitListCellRenderer

class WaitListCellRenderer extends JLabel implements ListCellRenderer
{
  protected static Border m_noFocusBorder;
  protected FontMetrics m_fm = null;
  protected Insets m_insets = new Insets(0, 0, 0, 0);
  protected int m_defaultTab = 50;
  protected int[] m_tabs = null;

  private int count;

  public WaitListCellRenderer(){
    super();
    m_noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    setOpaque(true);
    setBorder(m_noFocusBorder);
    count = 0;
  }

  public Component getListCellRendererComponent(JList list,
    Object value, int index, boolean isSelected, boolean cellHasFocus){
    setText(value.toString());
    setBackground(isSelected ? list.getSelectionBackground() :
      list.getBackground());
    setForeground(isSelected ? list.getSelectionForeground() :
      list.getForeground());

    setFont(list.getFont());
    setBorder((cellHasFocus) ? UIManager.getBorder(
      "List.focusCellHighlightBorder") : m_noFocusBorder);

    return this;
  }

  public void setDefaultTab(int defaultTab){
    m_defaultTab = defaultTab;
  }

  public int getDefaultTab(){
    return m_defaultTab;
  }

  public void setTabs(int[] tabs){
    m_tabs =tabs;
  }

  public int[] getTabs(){
    return m_tabs;
  }

  public int getTab(int index){
    if (m_tabs == null)
      return m_defaultTab * index;
    int len = m_tabs.length;
    if(index >= 0 && index < len)
      return m_tabs[index];
    return m_tabs[len-1] + m_defaultTab * (index-len-1);
  }

  public void paint(Graphics g){
    m_fm = g.getFontMetrics();
    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    getBorder().paintBorder(this, g, 0, 0, getWidth(), getHeight());

    g.setColor(getForeground());
    g.setFont(getFont());
    m_insets = getInsets();
    int x = m_insets.left;
    int y = m_insets.top + m_fm.getAscent();

    StringTokenizer st = new StringTokenizer(getText(), "=");
    while(st.hasMoreTokens()){
      String temp = st.nextToken();
      g.drawString(temp, x, y);
      x += m_fm.stringWidth(temp);
      if (!st.hasMoreTokens())
        break;
      int index = 0;
      while (x >= getTab(index)) index++;
      x = getTab(index);
    }
  }
}

class WaitRoomDisplay extends JFrame implements ActionListener,KeyListener,
MouseListener, ChangeListener
{
private ClientThread cc_thread;
private int roomNumber;
private String password, select;
private boolean isRock, isSelected;

private JLabel rooms, waiter, label;
public JList roomInfo, waiterInfo;
private JButton create, join, sendword, logout;
private Font font;
private JViewport view;
private JScrollPane jsp3;
public JTextArea messages;
public JTextField message;

public WaitRoomDisplay(ClientThread thread){
super("Chat-Application-대기실");

cc_thread = thread;
roomNumber = 0;
password = "0";
isRock = false;
isSelected = false;
font = new Font("SanSerif", Font.PLAIN, 12);

Container c = getContentPane();
c.setLayout(null);

rooms = new JLabel("대화방");

JPanel p = new JPanel();
p.setLayout(null);
p.setBounds(5, 10, 460, 215);
p.setFont(font);
p.setBorder(new TitledBorder(
new EtchedBorder(EtchedBorder.LOWERED), "대화방 목록"));

label = new JLabel("번 호");
label.setBounds(15, 25, 40, 20);
label.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
label.setFont(font);
p.add(label);

label = new JLabel("제 목");
label.setBounds(55, 25, 210, 20);
label.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
label.setFont(font);
p.add(label);

label = new JLabel("현재/최대");
label.setBounds(265, 25, 60, 20);
label.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
label.setFont(font);
p.add(label);

label = new JLabel("공개여부");
label.setBounds(325, 25, 60, 20);
label.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
label.setFont(font);
p.add(label);

label = new JLabel("개 설 자");
label.setBounds(385, 25, 58, 20);
label.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
label.setFont(font);
p.add(label);

roomInfo = new JList();
roomInfo.setFont(font);
WaitListCellRenderer renderer = new WaitListCellRenderer();
JScrollPane jsp1 = new JScrollPane(roomInfo);
roomInfo.addMouseListener(this);
renderer.setDefaultTab(20);
renderer.setTabs(new int[]{40, 265, 285, 315, 375, 430});
roomInfo.setCellRenderer(renderer);
jsp1.setBounds(15, 45, 430, 155);
p.add(jsp1);

c.add(p);

p = new JPanel();
p.setLayout(null);
p.setBounds(470, 10, 150, 215);
p.setBorder(new TitledBorder(
new EtchedBorder(EtchedBorder.LOWERED), "대기자"));

waiterInfo = new JList();
waiterInfo.setFont(font);
JScrollPane jsp2 = new JScrollPane(waiterInfo);
jsp2.setBounds(15, 25, 115, 175);
p.add(jsp2);

c.add(p);

p = new JPanel();
p.setLayout(null);
p.setBounds(5, 230, 460, 200);
p.setBorder(new TitledBorder(
new EtchedBorder(EtchedBorder.LOWERED), "채팅창"));

view = new JViewport();
messages = new JTextArea();
messages.setEditable(false);
messages.setFont(font);   
view.add(messages);
view.addChangeListener(this);
jsp3 = new JScrollPane(view);
jsp3.setBounds(15, 25, 430, 135);
view.addChangeListener(this);
p.add(jsp3);

view = (JViewport) jsp3.getViewport().getView();
view.addChangeListener(this);

message = new JTextField();
message.setFont(font);
message.setBounds(15, 170, 430, 20);
message.addKeyListener(this);
message.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));
p.add(message);

c.add(p);

create = new JButton("대화방개설");
create.setFont(font);
create.setBounds(500, 250, 100, 30);
create.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
create.addActionListener(this);
c.add(create);

join = new JButton("대화방참여");
join.setFont(font);
join.setBounds(500, 290, 100, 30);
join.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
join.addActionListener(this);
c.add(join);

sendword = new JButton("귓속말ㅋㅋ");
sendword.setFont(font);
sendword.setBounds(500, 330, 100, 30);
sendword.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
sendword.addActionListener(this);
//c.add(sendword);

logout = new JButton("로 그 아 웃");
logout.setFont(font);
logout.setBounds(500, 370, 100, 30);
logout.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
logout.addActionListener(this);
c.add(logout);

Dimension dim = getToolkit().getScreenSize();
setSize(640, 460);
setLocation(dim.width/2 - getWidth()/2,
dim.height/2 - getHeight()/2);
show();

addWindowListener(
new WindowAdapter() {
public void windowActivated(WindowEvent e) {
message.requestFocusInWindow();
}
}
);

addWindowListener(
new WindowAdapter(){
public void windowClosing(WindowEvent e){
cc_thread.requestLogout();
}
}
);
}

public void resetComponents(){
messages.setText("");
message.setText("");
roomNumber = 0;
password = "0";
isRock = false;
isSelected = false;
message.requestFocusInWindow();
}

public void keyPressed(KeyEvent ke){
if (ke.getKeyChar() == KeyEvent.VK_ENTER){
String words = message.getText();
String data;
String idTo;
if(words.startsWith("/w")){
StringTokenizer st = new StringTokenizer(words, " ");
String command = st.nextToken();
idTo = st.nextToken();
data = st.nextToken();
cc_thread.requestSendWordTo(data, idTo);
message.setText("");
} else {
cc_thread.requestSendWord(words);
message.requestFocusInWindow();
}
}
}

public void mouseClicked(MouseEvent e){
try{
isSelected = true;
String select = String.valueOf(((JList)e.getSource()).getSelectedValue());
setSelectedRoomInfo(select);
}catch(Exception err){}
}

public void actionPerformed(ActionEvent ae){
if(ae.getSource() == create){
CreateRoomDisplay createRoom = new CreateRoomDisplay(this, cc_thread);
} else if(ae.getSource() == join){     
if(!isSelected){
JOptionPane.showMessageDialog(this, "들어가고 싶은 방 골라요!!",
"대화방 입장.", JOptionPane.ERROR_MESSAGE);
} else if(isRock && password.equals("0")){
if ((password = JOptionPane.showInputDialog("비밀번호를 입력하세요.")) != null){
if (!password.equals("")){
cc_thread.requestEnterRoom(roomNumber, password);
password = "0";
} else {
password = "0";
cc_thread.requestEnterRoom(roomNumber, password);
}
} else {
password = "0";
}
} else {
cc_thread.requestEnterRoom(roomNumber, password);
}
} else if(ae.getSource() == logout){
cc_thread.requestLogout();
} else if(ae.getSource() == sendword){
String idTo, data;
if ((idTo = JOptionPane.showInputDialog("닉네임을 입력하세요^^")) != null){ 
if ((data = JOptionPane.showInputDialog("메세지를 입력하세요ㅋ")) != null){
cc_thread.requestSendWordTo(data, idTo);
}
}
}

}

private void setSelectedRoomInfo(String select){
StringTokenizer st = new StringTokenizer(select, "=");
roomNumber = Integer.parseInt(st.nextToken());
String roomName = st.nextToken();
int maxUser = Integer.parseInt(st.nextToken());
int user = Integer.parseInt(st.nextToken());
isRock = st.nextToken().equals("비공개") ? true : false;
}

public void stateChanged(ChangeEvent e){
jsp3.getVerticalScrollBar().setValue((jsp3.getVerticalScrollBar().getValue() + 20));
}

public void keyReleased(KeyEvent e){}
public void keyTyped(KeyEvent e){}
public void mousePressed(MouseEvent e){}
public void mouseReleased(MouseEvent e){}
public void mouseEntered(MouseEvent e){}
public void mouseExited(MouseEvent e){}
}