package stdcomm;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;


public class StdComm {

   
   public final static int Receive_SOCKET_PORT = 13279;   
   public static String file, IP,Path;   
   
    
    public static void main(String[] args) {
        
       ServerSocket servsock = null;
       Socket sock = null;
       Scanner sc=new Scanner(System.in);
       
       System.out.println("Enter IP");
       IP=sc.nextLine();
       System.out.println("Enter path where received files should be stored in forward slash notation");
       Path=sc.nextLine();
       Send s=new Send();
       new Thread(s).start();
        try
        {
        servsock = new ServerSocket(Receive_SOCKET_PORT,10,InetAddress.getByName(IP));
        int count=1;
        while (true) {
         
        System.out.println("Listening...");
        
          sock = servsock.accept();
          file=Path+"/received"+count;
          count++;
          Receive r=new Receive(sock);
          new Thread(r).start();
          
        }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        
    }
    
    public static class Receive implements Runnable 
    {
        Socket sock = null;
        public Receive(Socket sock) {
            
            this.sock=sock;
        }
        
        @Override
        public void run()
                {

        FileOutputStream fos = null;

        
        try {
      

      // receive file
      byte [] bytearray  = new byte [8192];
      byte[] t=new byte[3];
      
      DataInputStream is=new DataInputStream(sock.getInputStream());  

      is.read(t,0,t.length);
      
       String type=new String(t, StandardCharsets.US_ASCII);
       System.out.println(type);
       switch(type)
       {
           case "mp4": file=file+".mp4";
               break;
           case "jpg": file=file+".jpg";
               break;
           case "png": file=file+".png";
               break;
           case "avi": file=file+".avi";
               break;       
       }
        System.out.println(file);
        long size;
        size=is.readLong();
        fos = new FileOutputStream(file);

        int n;
       while (size > 0 && (n = is.read(bytearray, 0, (int)Math.min(bytearray.length, size))) != -1)
    {
        fos.write(bytearray,0,n);
        size -= n;
    }

      
      System.out.println("File " + file
          + " downloaded ");
      
      fos.flush();
      fos.close();
      is.close();
      sock.close();
    }
    catch(Exception e)
    {System.out.println(e.getMessage());}
    } 
                
                }
    
    
    public static class Send implements Runnable
{

    public static String filename,RIP,flag;
    public final static int SOCKET_PORT = 13279;  
   
     
    

        @Override
        public void run() {
            
	long startTime = System.currentTimeMillis();
	
        while(true)
        {
            
        
            Scanner s=new Scanner(System.in);
            String flag=s.nextLine();
            if(flag.equals("send"))
            {
                
            System.out.println("Enter receivers IP");
            RIP=s.nextLine();
            System.out.println("Enter location with file name in forward slash notation");
            filename=s.nextLine();
             Socket sock = null;
    try {
     sock = new Socket(RIP, SOCKET_PORT);
        }
    catch(Exception e)
      {
         e.printStackTrace();
      }
    
        FileInputStream fis = null;
        BufferedInputStream bis = null;

            try
            {
           
          // send file
                int i=filename.lastIndexOf('.');
                String type=filename.substring(i+1);
              
          File myFile = new File (filename);
          byte [] bytearray  = new byte [8192];
          
         
          
          fis = new FileInputStream(myFile);
          bis = new BufferedInputStream(fis);
               
          DataOutputStream os=new DataOutputStream(sock.getOutputStream());  

          os.write(type.getBytes(StandardCharsets.UTF_8));
          os.writeLong(myFile.length());
         int count;
         while ((count = bis.read(bytearray)) > 0)
           {
            os.write(bytearray, 0, count);
           }
          
	long endTime = System.currentTimeMillis();
                 System.out.println("Total send time --> "+ Long.toString(endTime - startTime));

          os.flush();
          os.close();
          bis.close();
          fis.close();
          System.out.println("Done.");
            }
            catch(Exception e)
            {
            e.printStackTrace();
            }
                
        }
        }
        
        }
 }
    
    
    }
    

