package project;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;



public class MultiChatClient implements ActionListener, Runnable {
	private static final int LOGIN = 100; 
	private static final int LOGOUT = 200;
	private static final int EXIT = 300;
	private static final int  NOMAL = 400;;
	private static final int WISPER = 500;;
	private static final int VAN = 600;
	private static final int CPLIST= 700;
	private static final int ERR_DUP = 800;

	
	
    private String ip;
    private String id;
    private String contents;
    private Socket socket;
    private BufferedReader inMsg = null;
    private PrintWriter outMsg = null;

    private JPanel loginPanel;
    private JButton loginButton;
    private JLabel label1;
    private JTextField idInput;

    private JPanel logoutPanel;
    private JLabel label2;
    private JButton logoutButton;

    private JPanel msgPanel;
    private JTextField msgInput;
    private JButton exitButton;

    private JFrame jframe;
    private JTextArea msgOut;
    
    private JPanel chatpListPanel;
    private JLabel label3;
    private JTextArea listOut;

    private Container tab;
    private CardLayout clayout;
    private Thread thread;

    boolean status;

    public MultiChatClient(String ip) {
        this.ip = ip;

        loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());
        idInput = new JTextField(15);
        loginButton = new JButton("�α���");
        loginButton.addActionListener(this);
        label1 = new JLabel("�г���");

        loginPanel.add(label1, BorderLayout.WEST);
        loginPanel.add(idInput, BorderLayout.CENTER);
        loginPanel.add(loginButton, BorderLayout.EAST);

  
        logoutPanel = new JPanel();

        logoutPanel.setLayout(new BorderLayout());
        label2 = new JLabel();
        logoutButton = new JButton("�α׾ƿ�");

        logoutButton.addActionListener(this);
 
        logoutPanel.add(label2, BorderLayout.CENTER);
        logoutPanel.add(logoutButton, BorderLayout.EAST);

 
        msgPanel = new JPanel();
 
        msgPanel.setLayout(new BorderLayout());
        msgInput = new JTextField(30);
 
        msgInput.addActionListener(this);
        msgInput.setEditable(false); //�α��� �ϱ� ������ ä���Է� �Ұ�
        exitButton = new JButton("����");
        exitButton.addActionListener(this);
  
        msgPanel.add(msgInput, BorderLayout.CENTER);
        msgPanel.add(exitButton, BorderLayout.EAST);

        tab = new JPanel();
        clayout = new CardLayout();
        tab.setLayout(clayout);
        tab.add(loginPanel, "login");
        tab.add(logoutPanel, "logout");


        jframe = new JFrame("���� 1�� ������Ʈ");
        msgOut = new JTextArea("", 10, 30);
        
        msgOut.setEditable(false);
        
        JScrollPane jsp = new JScrollPane(msgOut,
        		JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
        		JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        
        chatpListPanel = new JPanel(); //ä�� ���ڰ� ����Ʈ�� ���� �г�
        chatpListPanel.setLayout(new BorderLayout());
        
        label3 = new JLabel("ä�� ������ ����Ʈ"); // ��
        listOut =new JTextArea("",10,10); //ä�������ڸ� ��Ÿ�� ����
        listOut.setEditable(false); //�����Ұ�
        JScrollPane jsp2 = new JScrollPane(listOut,
        		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chatpListPanel.add(label3,BorderLayout.NORTH); //�гο� �󺧰� ��ũ���� ���� ����
        chatpListPanel.add(jsp2,BorderLayout.CENTER);
        
        
        
        
        jframe.add(tab, BorderLayout.NORTH);
        jframe.add(jsp, BorderLayout.WEST);
        jframe.add(chatpListPanel,BorderLayout.EAST);
        jframe.add(msgPanel, BorderLayout.SOUTH);
       
        clayout.show(tab, "login");
        
        jframe.pack();
        
        jframe.setResizable(false);
        
        jframe.setVisible(true);

    }

    public void connectServer() {
        try {
           
            socket = new Socket(ip, 8888); //���ܹ߻� ���ɼ�
            System.out.println("[Client]Server ���� ����!!");

    
            inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream())); //���ܹ߻� ���ɼ�
            outMsg = new PrintWriter(socket.getOutputStream(), true);

    
            outMsg.println(LOGIN+"/"+id); //LOGIN ��ɾ�� �ش� ID ���

 
            thread = new Thread(this);
            thread.start();
        } catch(IOException e) { //�ش���Ʈ�� ������ �����ϰ� ���� ���� ���
            // e.printStackTrace();
            System.out.println("��������Ұ�");
            if(!socket.isClosed()) {
            	stopClient();
            }
            return;
        }
    }
    public void stopClient() {
    	System.out.println("�������");

        msgOut.setText(""); //ä��â ����
        listOut.setText(" "); //������ â ����
        msgInput.setEditable(false); //ä���ԷºҰ�
        clayout.show(tab, "login"); 
        status = false;
        
        if(socket!=null && !socket.isClosed()) {
        	try {
				socket.close(); //���� �߻� ���ɼ�
			} catch (IOException e) {}
        } 
    	
    }


    public void actionPerformed(ActionEvent arg0) {
        Object obj = arg0.getSource();

 
        if(obj == exitButton) {
        	outMsg.println(EXIT+"/"+id );
        	stopClient();
        	System.exit(0);
            
        } 
        else if(obj == loginButton) {
            id = idInput.getText().trim();
            label2.setText("��ȭ�� : " + id);
            clayout.show(tab, "logout");
            msgInput.setEditable(true); //ä���Է� â Ȱ��ȭ(ä���Է� ����)
            connectServer();
        } 
        
        else if(obj == logoutButton) {
   
            outMsg.println(LOGOUT+"/"+id );   
        
           stopClient();
            

          
            
            
        } 
        else if(obj == msgInput) {
        	Thread thread = new Thread() { 
        		//��� ������ ���λ���(������� ��� ���� ����� ��� �����常 sleep ��Ű�� ����)
        		//�Է� ������� ��� ���� �ؾ� ä�����ѽð����� ������ ä��â�� �߰��ǹǷ�
        		@Override
        		public void run() {
        			contents = msgInput.getText();
        			//�Է�â�� ���� contents�� ����
            		
            		if(contents.indexOf("to")==0) { 
            			// ó�� ������ to (���� �ڵ�� �߰��� to�� ����� ���� �Ұ�)
            			int begin = contents.indexOf(" ") + 1;   
            			//  to 1111 �ȳ��ϼ��� �� ��� ó�� ��ĭ �����ڸ�����
            			int end = contents.indexOf(" ", begin);
            			//���ڸ� ����x(+1 ����)  // ���� ��ĭ����(������ �ڸ��� ���� �ȵ�)
            			String toid = contents.substring(begin, end);
            			//contents���� �ش� �κ��� ã�� id�� ����
            			
            			String wisper = contents.substring(end+1); 
            			//�ι�° ��ĭ �����ڸ����� �������� �̾Ƽ� wisper�� ����(����)
            			outMsg.println(WISPER+"/"+id + "/"+ toid+ "/" + wisper); 
            			// �� ������ /�� �����ؼ� ���
            		}
            		else if(contents.indexOf("van")==0) { //ó�� ������ van
            			int begin = contents.indexOf(" ") + 1;  
            			//  to 1111 �ȳ��ϼ��� �� ��� ó�� ��ĭ �����ڸ�����
            			String vanid = contents.substring(begin); 
            			//contents���� �ش� �κ��� ã�� vanid�� ����
            			
            			outMsg.println(VAN+"/"+id + "/"+ vanid); // �� ������ /�� �����ؼ� ���
            		
            		}
            		else {
            			outMsg.println(NOMAL+"/"+id + "/" + contents);
            		
            			int len = contents.length();
            			if(len>30){
            				try {
    							
    							msgOut.append("30�ڸ� �ʰ��Ͽ� ��������� ���� 10�ʰ� �Է��� �����մϴ�.\n"); 
    							//�ش� Ŭ���̾�Ʈ���� ä��â�� �޽��� ���
    							msgInput.setText(""); //�Է�â ����
    							msgInput.setEditable(false); // ä���Է�ĭ �����Ұ�
    							Thread.sleep(10000); //10�ʰ� ����
    							msgInput.setEditable(true);//�ٽ� �츲
    						
    							
            				} catch (Exception e) {
    							// TODO Auto-generated catch block
    							e.printStackTrace();
    						}
            				
    	        		} //if
               
            		}//else
            		msgInput.setText("");
        		} //run
        	};// thread
        	thread.start();
        	
        } //else if(obj == msgInput)
    }//action
        	
        		
    
    public void run() {

        String msg;
        String[] rmsg;


        status = true;

        while(status) { //���ź�
            try {
           
                msg = inMsg.readLine();
                rmsg = msg.split("/");
                int commend = Integer.parseInt(rmsg[0]);
                //0�� �ε����� ���� ��ɾ INT������ ����ȯ
                switch (commend) {
                
	                case WISPER: { //�ӼӸ��� �� ���
	                	msgOut.append(rmsg[1] + ">>"+rmsg[2] + "\n" + rmsg[3] +"\n");
	                	//�ӼӸ��� ���� ����� ��������� ä��â�� ǥ��	
	                	break;
	                
	                }
					case CPLIST: { //ä�������� ����Ʈ�� �� ���
						String []userlist = rmsg[1].split(",");
						// 1�� �ε����� �ִ� ������ ID SET�� ,�� �����ڷ� �Ͽ� userlist�迭�� ��� 
						int size = userlist.length;
						listOut.setText(" "); //������ ����Ʈâ ����
						
						for(int i = 0;i<size;i++) { // ��� �ϳ��� �о�鿩�� ������ ����Ʈ�� �߰�
							listOut.append(userlist[i]);
							listOut.append("\n");
						}
						
						break;
					}
					case VAN:{
						clayout.show(tab, "login"); //�α��ι�ư �ٲٱ�
						stopClient();
						
					}
					case ERR_DUP:{ //id �ߺ����� ������ �ð��� ��쿡 ó��
						
						stopClient();
						msgOut.append(rmsg[1] + ">"+rmsg[2] + "\n");
						break;
					}
					default:
						msgOut.append(rmsg[1] + ">"+rmsg[2] + "\n");
						break;
				}//switch
                
                msgOut.setCaretPosition(msgOut.getDocument().getLength());
            } catch(Exception e) {
                // e.printStackTrace();
                status = false;
            }
        }//while
        
        

        System.out.println("[MultiChatClient]" + thread.getName() + "�����");
    }

    public static void main(String[] args) {
        MultiChatClient mcc = new MultiChatClient("192.168.1.100");
    }
}
