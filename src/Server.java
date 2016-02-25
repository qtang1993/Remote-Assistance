import java.io.BufferedReader;  
import java.io.BufferedWriter;
import java.io.DataInputStream;  
import java.io.DataOutputStream;  
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;  
import java.io.InputStreamReader;  
import java.net.InetAddress;
import java.net.ServerSocket;  
import java.net.Socket;  
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.swing.filechooser.FileSystemView;
  
public class Server {  
  
	private int port;

	public Server(int port){
		this.port = port;
	}
	
	public static File createNewFile (String path, String fileName){
		File f = new File(path);
		if(!f.exists()){
		f.mkdirs();
		} 
		
		//String fileName="test.txt";
		File file = new File(f,fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return file;

	}
	
	public static void write(String content, String name){
		try {
	     	   File ff = new File(name);
	     	   FileWriter fw = new FileWriter(ff.getAbsoluteFile(), true);
	     	   BufferedWriter bw = new BufferedWriter(fw);
	     	   bw.write(content+"\r\n");
	     	   bw.close();
			} catch (IOException e) {
	     	   e.printStackTrace();
	     	}		
	}
	
	
    public static void main(String[] args) {    
        Date now = new Date(); 
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.print("Open port：");
        Scanner kbd = new Scanner(System.in);
        int port = kbd.nextInt();    	
    	
    	
    	FileSystemView fsv = FileSystemView.getFileSystemView();
    	String path = fsv.getHomeDirectory().toString();
    	System.out.print("Saving at Desktop... ");
    	String fileName = (dateFormat.format(now)+".log").trim().replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
    	System.out.println(fileName);
    	File newFile = createNewFile(path,fileName);
        Server server = new Server(port);
        server.init(path,newFile);    
    }    
    
    

    public void init(String path,File newFile) {
    	
        ServerSocket serverSocket = null;  
        try {    
            serverSocket = new ServerSocket(port);
            //Print server IP address
            try{
    			InetAddress ip = InetAddress.getLocalHost();
    			String localname = ip.getHostName();
    			String localip = ip.getHostAddress();
    			System.out.println("Local Name：" + localname);
    			System.out.println("IP: " + localip + '\n');
    		}catch(UnknownHostException e){
    			e.printStackTrace();
    		}
            
            while (true) {    
                Socket client = serverSocket.accept();
                System.out.println(client.getInetAddress() + "Connected");
                //Start two threads for read and write   
                new Thread(new ReadHandlerThread(client,newFile)).start();    
                new Thread(new WriteHandlerThread(client, newFile)).start();   
            }    
        } catch (Exception e) {    
            e.printStackTrace();    
        } finally{  
            try {  
                if(serverSocket != null){  
                    serverSocket.close();  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    } 
    /*  
     *Thread for read   
     */  
    class ReadHandlerThread implements Runnable{  
        private Socket client;
        private File file;
      
        public ReadHandlerThread(Socket client, File file) {  
            this.client = client;  
            this.file = file;
        }  
      
    	public void write(String content, String name){
    		try {
    	     	   File ff = new File(name);
    	     	   FileWriter fw = new FileWriter(ff.getAbsoluteFile(), true);
    	     	   BufferedWriter bw = new BufferedWriter(fw);
    	     	   bw.write(content+"\r\n");
    	     	   bw.close();
    			} catch (IOException e) {
    	     	   e.printStackTrace();
    	     	}		
    	}
    	
        @Override  
        public void run() {  
            DataInputStream dis = null;  
            try{  
                while(true){  
                    //read client data   
                    dis = new DataInputStream(client.getInputStream());  
                    String receiver = dis.readUTF();  
                    System.out.println( client.getInetAddress() + ":\t" + receiver );   
                    write("From: " + client.getInetAddress() + ":\t" + receiver, this.file.getAbsolutePath());
                }  
            }catch(Exception e){  
                e.printStackTrace();  
           
            }finally{  
                try {  
                    if(dis != null){  
                        dis.close(); 
                        System.out.println("Disconnected");
                        write("From: "+client.getInetAddress() + ":\tDisconnected", this.file.getAbsolutePath());
                    }  
                    if(client != null){  
                        client = null;  
                    }  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    }  
      
    /*  
     * Thread for write  
     */  
    class WriteHandlerThread implements Runnable{  
        private Socket client;
		private File file; 
		
		Date now = new Date(); 
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      
        public WriteHandlerThread(Socket client, File file) {  
            this.client = client;  
            this.file = file;
        }  
        
    	public void write(String content, String name){
    		try {
    	     	   File ff = new File(name);
    	     	   FileWriter fw = new FileWriter(ff.getAbsoluteFile(), true);
    	     	   BufferedWriter bw = new BufferedWriter(fw);
    	     	   bw.write(content+"\r\n");
    	     	   bw.close();
    			} catch (IOException e) {
    	     	   e.printStackTrace();
    	     	}		
    	}
      
        @Override  
        public void run() {  
            DataOutputStream dos = null;  
            BufferedReader br = null;  
            try{  
                while(true){  
                    //Reply to the client    
                    dos = new DataOutputStream(client.getOutputStream());    
                    System.out.print("Please enter:\t");    
                    //Keyboard input    
                    br = new BufferedReader(new InputStreamReader(System.in));  
                    String send = br.readLine();    
                    //Send data  
                    String toWrite = dateFormat.format(now) + " Sent:\t" + send;
                    if (send != null && !send.equals("\n") && !send.equals(""))
                    	write(toWrite, this.file.getAbsolutePath());
                    dos.writeUTF(send);    
                }  
            }catch(Exception e){  
                e.printStackTrace();  
            }finally{  
                try {  
                    if(dos != null){  
                        dos.close();  
                    }  
                    if(br != null){  
                        br.close();  
                    }  
                     
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
            }  
        }  
    } 
}    
  

