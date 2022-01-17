package project;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Date;
import java.text.SimpleDateFormat;


public class MultiChatServer {
	private static final int LOGIN = 100; 
	private static final int LOGOUT = 200;
	private static final int EXIT = 300;
	private static final int  NOMAL = 400;;
	private static final int WISPER = 500;;
	private static final int VAN = 600;
	private static final int CPLIST= 700;
	private static final int ERR_DUP = 800;

	private ServerSocket serverSocket = null;
	private Socket socket = null;
	
	ArrayList <ChatThread> chatlist = new ArrayList <ChatThread>(); //������ ����Ʈ
	HashMap<String, ChatThread> hash= new HashMap<String, ChatThread>(); 
	//ID�� KEY�� �ؼ� �����带 VALUE�� ���� �ִ� HASHMAP
	Date now = new Date(System.currentTimeMillis());
	SimpleDateFormat simple= new SimpleDateFormat("(a hh:mm)");
	//ä�ó��� ���� �ð��� ���� ����ϱ� ���ؼ� ����ð��� ������ ����
	
	public void start() {
		try {
			
			serverSocket = new ServerSocket(8888);
			System.out.println("server start");
			
		
			while(true) {
				socket = serverSocket.accept();				
				
				ChatThread chat = new ChatThread(socket);
				
				chatlist.add(chat);
				
				chat.start();
			}
		} catch(IOException e) {
		
			System.out.println("��ż��� �����Ұ�");
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			
		}catch(Exception e) {
			System.out.println("�������� �����Ұ�");
			if(!serverSocket.isClosed()) {
				stopServer();
			}
		}
		
	} 
	
	public void stopServer() {
		try {
			Iterator<ChatThread> iterator = chatlist.iterator(); 
			//chatlist�� �ִ� ������ ��ü�� �������� ���� iterator ��ü ����
			while (iterator.hasNext()) { //���� ��ü�� �ִ� ����
				ChatThread chat = iterator.next(); // ���� ��ü�� �����忡 ����
				chat.soket.close(); //�ش� ������ ��ż�������
				iterator.remove(); //������ ����
				
			}
			if(serverSocket!=null && !serverSocket.isClosed()) {
				serverSocket.close(); //�������� �ݱ�
			}
			System.out.println("��������");
		}catch (Exception e) {}
	}
	public static void main(String[] args) {
		MultiChatServer server = new MultiChatServer();
		server.start();
	}
	

	void broadCast(String msg) { //ä�ù� �ο� ��ü���
		for(ChatThread ct : chatlist) {
			ct.outMsg.println(msg+ simple.format(now)); 
			//�Ű������� ���� ä�ó����� �ð��� �Բ� ���
		}
	}
	void wisper(ChatThread from, ChatThread to,String msg) { //�۽ű׷���,���Ž�����,��ȭ���� �Ű�����)
		from.outMsg.println(msg+ simple.format(now)); //�۽Ž����� ä��â�� ���
		to.outMsg.println(msg+ simple.format(now)); // ���Ž����� ä��â�� ���
	}
	
	void updatinglist() {
		
		Set<String> list = hash.keySet();// hashmap���� ���̵�(key)�� set���� ������
		for(ChatThread ct : chatlist) {
				ct.outMsg.println(CPLIST+"/"+list); //CPLIST��ɾ�� ��ü���� ���
			}
		}
	void disconnect(ChatThread thread, String id) {
		try {
			thread.soket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		hash.remove(id); //hashmap���� ����
		chatlist.remove(thread); //chatlist���� ���� 
		
	}
	
	
	class ChatThread extends Thread {
		
		public ChatThread(Socket socket) { 
			//��ż����� �ݱ� ���ؼ� ������ ������ �� ������ �Ű������� ������ �޾Ƽ� ��������� ����
			this.soket = socket;
		}
		Socket soket;
		String msg;
		String[] rmsg;
		
		
		
		private BufferedReader inMsg = null;
		private PrintWriter outMsg = null;

		public void run() {
		
			boolean status = true;
			System.out.println("##ChatThread start...");
			try {
				
				inMsg = new BufferedReader(new InputStreamReader(socket.getInputStream())); //���ܹ߻� ���ɼ�
				outMsg = new PrintWriter(socket.getOutputStream(), true);
			

				while(status) { //���ź�
					
					msg = inMsg.readLine();
					
					rmsg = msg.split("/");
					
					int commend = Integer.parseInt(rmsg[0]); 
					//����տ� �ִ� ��ɾ ����ġ������ ó���ϱ� ���� int�� ����ȯ

		                switch (commend) {
						case LOGIN: {
							System.out.println(commend);
							if(hash.containsKey(rmsg[1])) { //id�� hashmap���� �ߺ��˻�
								this.outMsg.println(ERR_DUP+"/"+"[SERVER]" +"/" +"�α��κҰ�>ID �ߺ�"); 
								//�α��� �� ���� ä��â�� �α��� �Ұ� �ȳ��޽��� ���
								socket.close(); //���� ����
								chatlist.remove(this); //�����帮��Ʈ���� ���� 
								status = false; // ���º������� while�� Ż��
								break;
							}
							
							else{
								hash.put(rmsg[1], this); //�ߺ��� �ƴϸ� �ش� ���̵� key/ �����带 value�� �߰�
								broadCast(NOMAL+"/"+"[SERVER]" +"/"+rmsg[1]+"���� �α����߽��ϴ�."); 
								//ä��â�� �α��� �޼��� ���
								updatinglist(); //����� ������ ����Ʈ�� �۽�
								break;
							}
							
						}
						case LOGOUT: {
							disconnect(this, rmsg[1]); //�ش� ������� ������ �����ϴ� �޼��� 
							broadCast(NOMAL+"/"+"[SERVER]" +"/"+ rmsg[1] + "���� �����߽��ϴ�.");
							//������ �˸�
							updatinglist(); // ����� ä�� ������ ����Ʈ �۽�
							status = false; //while�� �ݺ�Ż��
							break;
						}
						case EXIT: {
							disconnect(this, rmsg[1]);
							broadCast(NOMAL+"/"+"[SERVER]" +"/" + rmsg[1] + "�԰� ������ ���������ϴ�.");
							updatinglist();
							status = false;
							break;
						}
						case NOMAL: {
							 broadCast(msg);
							 break;
						}
						case WISPER: {			
							ChatThread from = hash.get(rmsg[1]); // rmsg[1] �۽� id�� key������ value������ ã��
							ChatThread to =  hash.get(rmsg[2]); // rmsg[2] ���� id�� key������ value������ ã��
							wisper(from,to, msg); //ã�� �۽� ������ , ���� ������, ������ �Ű������� wisper�޼ҵ� ȣ��
							break;
						}
						case VAN : {
							if(chatlist.indexOf(this)!=0) {//0�� �����忡 ��������� �� (������ �ƴ϶��)
								this.outMsg.println(NOMAL+"/"+"[SERVER]" +"/"+ "��������� �����ϴ�."+ simple.format(now));
								//�ش� �����忡�� ������ ������ �۽�
								break;
								
							}
							else {
								broadCast(NOMAL+"/"+"[SERVER]" +"/" + rmsg[2] + "���� ���������ϼ̽��ϴ�.");
								// �ش� �����尡 �����̶�� ������� ����� ��ü���� ���
								ChatThread thread = hash.get(rmsg[2]); 
								//hashmap���� ������ id�� �ش� �����带 �˻��ؼ� thread�� ����
								thread.outMsg.println(VAN+"/");
								disconnect(thread, rmsg[2]);//������� ������ �������� 
								updatinglist();
								break;

							}	 
						}
					
						
					}//switch
				}//while
					
					

				this.interrupt();
				System.out.println("##"+this.getName()+"stop!!");
			}catch(IOException e) {
				try {
					this.soket.close();
				} catch (IOException e1) {}
				chatlist.remove(this);
				// e.printStackTrace();
				System.out.println("[ChatThread]run() IOException �߻�!!");
			}
		}
	}
	
}
