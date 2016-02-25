import java.io.BufferedReader;  
import java.io.BufferedWriter;
import java.io.DataInputStream;  
import java.io.DataOutputStream;  
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;  
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;  
import java.util.Scanner;
  
public class Client {  

    public static void main(String[] args) {    
        System.out.print("Server IP Address：");
        Scanner kbd = new Scanner(System.in);
        String ip = kbd.nextLine();
        System.out.print("Server IP：");
        int port = kbd.nextInt();
        kbd.close();
    	handler(ip,port);   
    }  

    public static void handler(String ip, int port){  
        try {  
            //initiate a Socket with server ip and port  
            Socket client = new Socket(ip,port); 
            System.out.println("Connected "+ ip);
            //Thread start  
            new Thread(new ReadHandlerThread(client)).start();  
             
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        
    }  
    
}   


/*  
 *Thread for reading from the server
 */  
class ReadHandlerThread implements Runnable{  
    private Socket client;  
  
    public ReadHandlerThread(Socket client) {  
        this.client = client;  
    }  
    
    @Override  
    public void run() {  
        DataInputStream dis = null;
        Process p = null;
   
        try {  
        	
            while(true){  
                //Read server data    
                dis = new DataInputStream(client.getInputStream()); 
                String receiver = dis.readUTF();
                System.out.println("Server return:\t" + receiver );  
                 	
                	BufferedReader br = null;
                	InputStreamReader ir = null;
               	   	DataOutputStream dos = null;  
               	   	String line = null;
               	   	Thread write = null;
               	    
            	    try { 
            	    	//Execute server order
                       	p = Runtime.getRuntime().exec(receiver);
          	        	dos = new DataOutputStream(client.getOutputStream());
           	        	//Get process input
          	        	WriteHandlerThread wt = new WriteHandlerThread(p, dis);
           	        	write = new Thread(wt);
                	    write.start();
                	    //Get process output   	
                        InputStream in = p.getInputStream();
           	            ir = new InputStreamReader(in);
           	            br = new BufferedReader(ir);
           	            //Output and write to socket
                	    while ((line = br.readLine()) != null && wt.getRun()) {
                      		System.out.println(line);                	            	
           	            	dos.writeUTF(line);
          	            }            	           	
                		try {
                			p.waitFor();
               				p.destroy(); 					
                		} catch (InterruptedException e) {
                			// TODO Auto-generated catch block
                			e.printStackTrace();
                		}
                				
                		System.out.println("Enter to continue...");
                		dos.writeUTF("Enter to continue...");
          				write.join();      	            	
                	            
            	    } catch (Exception e) {
            	    	e.printStackTrace();
                	} finally{  
                		try {  
                			if (p.isAlive())
                	      	p.destroy();
                		} catch (Exception e) {  
                			e.printStackTrace();  
                		}  
                	} 
            	        	
            	  	System.out.println("Waiting for server...");
                
                	
            }
           
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally{  
            try {  
                if(dis != null){  
                    dis.close();  
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
 * Thread for writing to the client 
 */  
class WriteHandlerThread implements Runnable{  
    private Process p;  
    private boolean run = true;
    private DataInputStream dis;
  
    public WriteHandlerThread(Process p, DataInputStream dis) {  
        this.p = p;  
        this.dis = dis;
    } 
 
    public synchronized void stopRun() {
        this.run = false;
    }
    
    public boolean getRun(){
    	return this.run;
    }
    @Override  
    public void run() {  
        OutputStream os = null;  
        BufferedWriter bw = null;
        
        try {  
            while(run){  
                //Get output stream 
                String receiver = dis.readUTF();
                //End thread when find "enter"
                if (receiver.equals("")){
                	break;
                }
                //Server input "stop" to stop the process
                else if (receiver.equalsIgnoreCase("stop")){
                	this.run = false;
                	System.out.println("Interruptted!");
                }
                else{
                	os = p.getOutputStream();
                	bw = new BufferedWriter(new OutputStreamWriter(os));  
                	System.out.println("Server input："+ receiver);	
                	bw.write(receiver);
    	        	bw.newLine();
    	        	bw.flush();	
                }
            }  
            
        } catch (IOException e) {  
           e.printStackTrace();  
        } finally{  
            try {  
            	if (bw != null)
            		bw.close();
            	if (os != null)
                    os.close();
            	if (p.isAlive())
            		p.destroy();
            } catch (Exception e) {  
                e.printStackTrace();  
            }  
        }  
    }  
}  
  
