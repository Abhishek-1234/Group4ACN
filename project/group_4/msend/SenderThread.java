package msend;

import java.net.*;
import java.io.*;
import java.lang.Math;
import java.util.*;

public class SenderThread extends Thread{
	private int thread_id;
	private String original_fileName;
	private long chunkSize;
	private long fileSize;
	public static int BUFFER_SIZE = 16384;
	private String host = null;
	private static int port = 5000;
	private boolean[] completed = null;
	

	public SenderThread(String host, int thread_id, String original_fileName, long chunkSize, long fileSize, boolean[] completed){
		this.host = host;
		this.thread_id = thread_id;
		this.original_fileName = original_fileName;
		this.chunkSize = chunkSize;
		this.fileSize = fileSize;
		this.completed = completed;
	}

	public void run() {
		Socket socket = null;
		RandomAccessFile inStream = null;
		DataOutputStream outStream = null;
		byte[] bytes_read = new byte[BUFFER_SIZE];

		try{
			socket = new Socket(host,port);
			System.out.println("Thread "+ Integer.toString(thread_id) + " is connected");
		}catch(Exception e){
			System.out.println(e);
		}

		try{
			inStream = new RandomAccessFile(original_fileName,"r");
			outStream = new DataOutputStream(socket.getOutputStream());
			outStream.writeUTF("temp" + Integer.toString(thread_id) + "SEP" + Long.toString(fileSize));
			
		}catch(Exception e){
			System.out.println(e);

		}	

		
			Long start = (thread_id*chunkSize);
			Long end = start + fileSize;
			//System.out.println(Long.toString(start) +" to "+ Long.toString(end));
			try{

				inStream.seek(start);
				long count = (fileSize/BUFFER_SIZE);
				int remain = (int)(fileSize - count*BUFFER_SIZE);

				while(count-- != 0){
					//System.out.println(Integer.toString(thread_id)+" : count "+Long.toString(count));
					inStream.read(bytes_read);
					outStream.write(bytes_read);
				}

				byte[] temp = new byte[remain];
				inStream.read(temp);
				outStream.write(temp);

			//System.out.println("data sent complete" + Long.toString(start + end - start));
		}catch(Exception e){
			System.out.println(e);
		}

		try{
			inStream.close();
			outStream.close();
			socket.close();
			System.out.println("Sent complete from thread "+Integer.toString(thread_id));
			
		}catch(Exception e){
			System.out.println(e);
		}

		completed[thread_id] = true;		
	}
}