
import java.io.*;
import java.net.*;
import java.util.*;
import sun.management.resources.agent;

/**
 *
 * @author soul1
 */
public class MultiThread{
    public static void main(String[] args){
        ServerSocket serverSocket;
        Socket connection;
        
        try{
            serverSocket = new ServerSocket(4200);
            System.out.println("Server is running...");
            
            while(true){
                connection = serverSocket.accept();
                new ConnectionHandler(connection);
            }
            
            
        }
        catch(IOException e){
            System.out.println("Error Occured " + e);
        }
    }
    
    private static class ConnectionHandler extends Thread{
        Socket clientSocket;
        
        ConnectionHandler(Socket client){
            clientSocket = client;
            start();
        }
        
        public void run(){
            System.out.println(clientSocket.getInetAddress().toString() + " has connected");
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            String username = reader.readLine();
            String password = reader.readLine();
            
            Queue<Socket> agents = new LinkedList();
            Queue<Socket> clients = new LinkedList();
            
            UserType user = new UserType(username, password);
            if(UserType.Authenticate() == 0){
                agents.add(clientSocket);
                writer.println("1] Single Client");
                writer.println("2] Double Client"); 
                writer.flush();
                int type = reader.getInt();
                switch(type){
                    case: 1
                            
                }
            }
            else if(UserType.Authenticate() == 1){
                clients.add(clientSocket);
            }
            else if(UserType.Authenticate() == -1){
                writer.println("Wrong Username or Password");
                writer.flush();
            }  
            if(UserType.getVerification()){
                chat(agents.remove(), clients.remove(), clients.remove());
            }
        }
    }
    
    public static void chat(Socket agent, Socket client1, Socket client2){
        
    }
}
