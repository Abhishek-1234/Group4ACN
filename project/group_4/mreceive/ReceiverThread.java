package mreceive;

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.file.Files;

public class ReceiverThread extends Thread{

	private int thread_id;
	private Socket socket = null;
	private DataInputStream inStream = null;
	private static int BUFFER_SIZE = 16384;
	private DataOutputStream outStream = null;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private boolean[] completed = null;
	private String[] details = null;
	

	public ReceiverThread(int thread_id, Socket socket, boolean[] completed){
		this.socket = socket;
		this.completed = completed;
		this.thread_id = thread_id;
	}

	public void run(){
			
			try{
				inStream = new DataInputStream(socket.getInputStream());

				String str = inStream.readUTF();
				details = str.split("SEP",2);
				System.out.println(details[0]+" ^^^^^^^^^^ "+details[1]);

				outStream = new DataOutputStream(new FileOutputStream(details[0]));	
			}catch(Exception e){
				System.out.println(e);
			}
			
			try{
				int length = 0;
				while((length = inStream.read(buffer))!= -1){
					outStream.write(buffer, 0 ,length);
					outStream.flush();
				}
				System.out.println(" data is received from  Sender thread no. -> " + Integer.toString(thread_id));

			}catch(Exception e){
				System.out.println(e);
			}

			try{
				inStream.close();
				outStream.close();
				socket.close();
				
			}catch(Exception e){
				System.out.println(e);
			}
			completed[thread_id] = true;
	}
}