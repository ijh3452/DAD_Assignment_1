import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class ServerOld
{
    private static ArrayBlockingQueue<Socket> Queue;
    private static UserType usertype = null;
    private static User agent = null;
    private static User client = null;
    private static int clientNum = 0;
    
    private static final int MAX_CLIENTS = 2;
    private static final int QUEUE_SIZE = 1;
    
    public static void Authenticate(PrintWriter toClient, BufferedReader fromClient, String type) throws IOException
    {
        if("Client".equalsIgnoreCase(type))
        {
            usertype = UserType.CLIENT;
            String nickname = fromClient.readLine();
            
            System.out.println(nickname);
            
            toClient.println("Connection successful");
            toClient.flush();
            
            client = new User("", "", "", "");
            client.setUsertype(type);
            client.setNickname(nickname);
            
            toClient.println("Welcome " + client.getNickname() + "!");
            toClient.flush(); 
        }
        
        else if("Agent".equalsIgnoreCase(type))
        {
            int attempts = 3;
            usertype = UserType.AGENT;
            
            while(attempts != 0)
            {
                String id = fromClient.readLine();
                String password = fromClient.readLine();
                
                System.out.println(id);
                System.out.println(password);
                
                if("agent".equals(id) && "password".equals(password))
                {
                    toClient.println("Connection successful");
                    toClient.flush();
                    
                    agent = new User("", "", "", "");
                    agent.setUsertype(type);
                    agent.setID(id);
                    agent.setPassword(password);
                    
                    toClient.flush();
                    attempts = 0;
                }
                
                else
                {
                    attempts --;
                    toClient.println("Incorrect username and/or password!\n" + 
                            "Remaining attempts: " + attempts);
                    System.out.println("Agent failed to connect");
                    toClient.flush();
                    
                    if(attempts == 0)
                    {
                        toClient.println("Authentication failed! Program will terminate.");
                        toClient.flush();
                    }
                }
            }
        }
    }
    
    public static void log(String msg)
    {
        try
        {
            String data = msg + "\n";
            String logName = client.getNickname() + "_log.txt";
            FileWriter fw = new FileWriter(logName, true);
            fw.append(data + "\n");
            fw.flush();
            fw.close();
        }
        
        catch(IOException e)
        {
            System.out.println("Exception occurred: " + e);
        }
    }
    
    public static void main(String[] args)
    {
        ServerSocket serverSocket;
        Socket socket;
        
        try
        {
            serverSocket = new ServerSocket(4200);
            Queue = new ArrayBlockingQueue<Socket>(QUEUE_SIZE);
            
            for(int i = 0; i < MAX_CLIENTS; i ++)
            {
                new ConnectionHandler();
            }
            
            System.out.println("Server is running...");
            
            while(true)
            {
                socket = serverSocket.accept();
                PrintWriter toClient = new PrintWriter(socket.getOutputStream());
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String type = fromClient.readLine();
                System.out.println(type);
                Authenticate(toClient, fromClient, type);
                
                if("Client".equals(usertype.getTypename()))
                {
                    try
                    {
                        Queue.put(socket);
                        System.out.println("Client waiting to be put in queue");
                    }
                    
                    catch(Exception e)
                    {
                        System.out.println("Exception occurred: " + e);
                    }
                }
                
                else if("Agent".equals(usertype.getTypename()))
                {
                    new agentThread(socket);
                }
            }
        }
        
        catch(Exception e)
        {
            System.out.println("Exception occurred: " + e);
        }
    }
    
    private static class agentThread
    {
        static Socket agentSocket;
        
        public agentThread(Socket agentSocket)
        {
            this.agentSocket = agentSocket;
            new ConnectionHandler();
            new ConnectionHandler();
        }
    }
    
    private static class ConnectionHandler extends Thread
    {
        Socket clientSocket;
        
        ConnectionHandler()
        {
            start();
        }
        
        @Override
        public void run()
        {
            while(true)
            {
                try
                {
                    clientSocket = Queue.take();
                    clientNum ++;
                    PrintWriter toAgent = new PrintWriter(agentThread.agentSocket.getOutputStream());
                    toAgent.println("A new client has joined");
                    toAgent.println("Current number of clients: " + clientNum);
                    toAgent.flush();
                }
                
                catch(Exception e)
                {
                    System.out.println("Exception occurred: " + e);
                }
                
                String clientAddress = clientSocket.getInetAddress().toString();
                
                Date date = new Date();
                Server server = new Server();
                ChatMessage message = new ChatMessage();
                
                try
                {
                    BufferedReader fromClient = 
                            new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String receivedMessage;
                    
                    Thread sendThread = new Thread(new Runnable()
                    {
                        public void run()
                        {
                            while(true)
                            {
                                try
                                {
                                    BufferedReader fromAgent = 
                                            new BufferedReader(new InputStreamReader(agentThread.agentSocket.getInputStream()));
                                    PrintWriter toClient = new PrintWriter(clientSocket.getOutputStream());
                                    String receivedMessage;
                                    
                                    if((receivedMessage = (fromAgent.readLine())) != null)
                                    {
                                        message.setMessage(receivedMessage);
                                        message.setSenderIP(clientAddress);
                                        message.setDateTime(date.toString());
                                        message.setNickname(client.getNickname());

                                        String sendMessage = message.getDateTime() + " Agent: " + message.getMessage();

                                        toClient.println(sendMessage);
                                        toClient.flush();
                                        server.log(sendMessage);
                                        System.out.println("From agent: " + message.getMessage());
                                    }
                                }
                                
                                catch(Exception e)
                                {
                                    System.out.println("Exception occurred: " + e);
                                }
                            }
                        }
                    });
                    
                    sendThread.start();
                    
                    while(true)
                    {
                        if((receivedMessage = (fromClient.readLine())) != null)
                        {
                            message.setMessage(receivedMessage);
                            message.setSenderIP(clientAddress);
                            message.setDateTime(date.toString());
                            message.setNickname(client.getNickname());
                            
                            PrintWriter toAgent = new PrintWriter(agentThread.agentSocket.getOutputStream());
                            
                            String sendMessage = message.getDateTime() + " " + message.getNickname() + ": " + message.getMessage();
                            toAgent.println(sendMessage);
                            toAgent.flush();
                            
                            server.log(sendMessage);
                        }
                    }
                }
                
                catch(Exception e)
                {
                    System.out.println("Exception occurred: " + e);
                }
            }
        }
    }
}