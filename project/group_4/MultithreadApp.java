import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import mreceive.ReceiverThread;
import java.lang.Math;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;
import javax.swing.JFileChooser;
import msend.SenderThread;


public class MultithreadApp{

	public static void main(String []args){
		System.out.println("-------------------------------------------------------------\n");
		System.out.println("press 1 for sending");
		System.out.println("press 2 for receiving");
		System.out.print("\nEnter your choice: ");
		Scanner in = new Scanner(System.in);
		int ch = in.nextInt();
		System.out.println("-------------------------------------------------------------\n");
		switch(ch){
			case 1:
				MultithreadSend obj1 = new MultithreadSend();
				obj1.send();
				break;
			
			case 2:
				MultithreadReceive obj2 = new MultithreadReceive();
				obj2.receive();
				break;
			default:
				System.out.println("Wrong choice");
				System.exit(0);
		}
	}
}

class MultithreadSend{

	private static ArrayList<SenderThread> thread_list = new ArrayList<SenderThread>();
	private static int port = 5000;

	public String findIPReceiver() throws Exception{
	
		Process result = Runtime.getRuntime().exec("tracert -h 1 www.google.com");
	        System.out.println("Finding IP address of Receiver...");
	        // to store the output in some buffer
	        BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));
	        String info = null;
	        String[] tokens = null;
	        int i = 0 ;
	        while((info = output.readLine()) != null){
	        	i++;
	        	if(i == 5){
					tokens = info.split("\\s{1,}");
					break;
	        	}
	        }	

			String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

			Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
			Matcher matcher = pattern.matcher(tokens[tokens.length-1]);
			if (matcher.find()) {
				String temp = matcher.group();
				System.out.printf("IP address of Receiver is --> %s\n", temp);
				return temp;
			} else{
				return "0.0.0.0";
			}
	}	

	public void send(){
		try{
			int n;
			JFileChooser jfc = new JFileChooser();
			int dialog_value = jfc.showOpenDialog(null);
			if(dialog_value == JFileChooser.APPROVE_OPTION){
					File target_file = jfc.getSelectedFile();
					String original_fileName = target_file.getName();
					
					Scanner in = new Scanner(System.in);
					System.out.print("How many threads you want to create at sender -> ");		
					while((n = in.nextInt()) > 512){
						in.nextLine();
						System.out.println("Try again with value less than 513");
					}
					in.nextLine();
					//System.out.println("How many buffer size you want to keep -> ");	BUFFER_SIZE = in.nextInt();	in.nextLine();
					String host = null;
					
					System.out.print("Whether you want to enter reciver ip manually ?(y/n)-> ");
					char ch;	ch = in.next().charAt(0);		in.nextLine();
					//
					if('y' == Character.toLowerCase(ch)){
						System.out.print("Enter receiver ip -> ");
						host = in.nextLine();
					}else{
						host = this.findIPReceiver();
					}
					
					System.out.println("-------------------------------------------------------------");
					
					long original_fileSize = new File(target_file.getAbsolutePath()).length();
					long chunkSize = (long)(Math.ceil(original_fileSize/n)) + 1;
					boolean[] completed = new boolean[n];
					for(int i=0;i<n;i++){
						completed[i] = false;
					}
		
					Socket socket = new Socket(host,port);
					//we need to check whether connection established or not, if not then exit the application.
					System.out.println("Connected to Receiver");
					System.out.println("-------------------------------------------------------------");
					
					long startTime = System.currentTimeMillis();
							
					System.out.println("FileName -> "+original_fileName);
					System.out.println("FileSize -> "+Long.toString(original_fileSize));
					System.out.println("-------------------------------------------------------------");
					DataOutputStream socketOutputStream = new DataOutputStream(socket.getOutputStream());
					socketOutputStream.writeUTF(original_fileName + "SEP" + Long.toString(original_fileSize)+"SEP"+Integer.toString(n)+ "SEP" + socket.getLocalAddress().toString());
					socketOutputStream.close();
					socket.close();
					
					for(int i=0;i<n-1;i++){
						SenderThread obj = new SenderThread(host, i, target_file.getAbsolutePath(), chunkSize, chunkSize, completed);
						thread_list.add(obj);
					}
					SenderThread obj = new SenderThread(host, n-1, target_file.getAbsolutePath(), chunkSize, (long)(original_fileSize - (n-1)*chunkSize), completed);
					thread_list.add(obj);

					for(SenderThread t : thread_list){
						t.start();
					}
					
					for(SenderThread t: thread_list){
						t.join();
					}
					
					long endTime = System.currentTimeMillis();
				
					System.out.println("-------------------------------------------------------------");
					System.out.println("Total sent time: " + Long.toString(endTime - startTime));
					System.out.println("Sender's whole operation is completed");
				}else{
					System.out.println("Couldn't complete file selection.Try again");
				}
		}catch(Exception e){
			System.out.println(e);
		}
	}
}

class MultithreadReceive{

	private static int port = 5000;
	private static int BUFFER_SIZE = 16384;
	
	public void receive(){
		try{
			int n;
			ServerSocket server = new ServerSocket(port);
			ArrayList<ReceiverThread> thread_list = new ArrayList<ReceiverThread>();

			System.out.println("Receiver process started");
			System.out.println("Waiting for a Sender ...");
			Socket socket = server.accept();
			System.out.println("Sender request accepted");
			System.out.println("-------------------------------------------------------------");
			long startTime = System.currentTimeMillis();		
			DataInputStream socketInputStream = new DataInputStream(socket.getInputStream());
			String temp = socketInputStream.readUTF();
			String[] arrOfStr = temp.split("SEP", 4); 
			//System.out.println(arrOfStr[0] + " --------- "+ arrOfStr[1]);
			
			String filename = arrOfStr[0];
			n = Integer.parseInt(arrOfStr[2]);
			boolean[] completed = new boolean[n];
			
			System.out.println("FileName -> "+arrOfStr[0]);
			System.out.println("FileSize -> "+arrOfStr[1]);
			System.out.println("Sender's IP -> "+arrOfStr[3]);
			System.out.println("-------------------------------------------------------------");
			socketInputStream.close();
			socket.close();
			
			Socket sock = null;
			for(int i=0;i<n;i++){
				 while(true){
					sock = server.accept(); 
					if(sock.getInetAddress().toString().equals(arrOfStr[3])){
						//System.out.println("Right connection ==> " + sock.getInetAddress().toString());
						break;
					}else{
						System.out.println("Wrong connection was setup from " + sock.getInetAddress().toString() +", which is now being terminated");
						sock.close();
					}
				 }
				 
				ReceiverThread obj = new ReceiverThread(i,sock,completed);
				thread_list.add(obj);
			}

			for(ReceiverThread t: thread_list){
				t.start();
			}
			
			for(ReceiverThread t: thread_list){
				t.join();
			}	
			/*boolean flag;
			while(true){
				flag = true;
				for(int i=0;i<n;i++){
					if (completed[i] == false){
							flag = false;
						break;
					}
				}	
				
				if( flag == true){
					break;
				}else{
					Thread.sleep(0);
				}
			}*/
			
			System.out.println("-------------------------------------------------------------");
			long endTime = System.currentTimeMillis();
			System.out.println("Total sent time: " + Long.toString(endTime - startTime));
			System.out.println("----------------- All files received and now assembling ----------------");
			
			DataOutputStream os = new DataOutputStream((new FileOutputStream(filename)));
			File file = null;
			byte[] byte_read = new byte[BUFFER_SIZE];
			for(int i=0;i<n;i++){
				DataInputStream is = new DataInputStream((new FileInputStream("temp"+Integer.toString(i))));
				try{
					
					int length = 0;
					while((length = is.read(byte_read)) !=-1){
						
						os.write(byte_read, 0 ,length);
						os.flush();
					}
					
				}catch(Exception e){
					System.out.println("while assembling files.");
					System.out.println(e);
				}
				is.close();
				file = new File("temp"+Integer.toString(i));
				Files.deleteIfExists(file.toPath());
			}
			os.close();
			System.out.println("Assembling done!");
			System.out.println("###########################################################");
		}catch(Exception e){
			System.out.println(e);
		}
		
	}
}
