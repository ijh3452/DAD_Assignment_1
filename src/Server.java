
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;


public class Server
{
    private static Customer customer = null;
    
    public static void main(String[] args)
    {
        ServerSocket serverSocket;
        Socket socket;
        
        try
        {
            serverSocket = new ServerSocket(4200);
            System.out.println("Server is running...");
            
            while(true)
            {
                socket = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(socket);
                handler.start();
            }
        }
        
        catch(IOException e)
        {
            System.out.println("Exception occurred during server startup: " + e);
        }
    }
    
    public static class ConnectionHandler extends Thread
    {
        Socket server;
        ArrayList<Agent> agentList = new ArrayList<Agent>();
        ArrayList<Customer> customerList = new ArrayList<Customer>();
        ConnectionHandler(Socket socket)
        {
            server = socket;
        }
        
        @Override
        public void run()
        {
            try
            {
                PrintWriter toClient = new PrintWriter(server.getOutputStream());
                BufferedReader fromClient = new BufferedReader(new InputStreamReader(server.getInputStream()));
                
                String usertype = fromClient.readLine();
                String username = null;
                
                if("customer".equalsIgnoreCase(usertype))
                {
                    username = fromClient.readLine();
                    customer = new Customer(username, server);
                    customerList.add(customer);
                    boolean agentFound = false;
                    int x = 0;
                    
                    while(agentFound)
                    {
                        for(x = 0; x < agentList.size(); x ++)
                        {
                            if(agentList.get(x).getNumOfCustomers() < 2)
                            {
                                agentList.get(x).addCustomer(server);
                                agentFound = true;
                                toClient.println("agent found");
                                break;
                            }
                        }
                    }
                    
                    Socket agentSocket = agentList.get(x).getAgentSocket();
                    String userInput = null;
                    Date timestamp = new Date();
                    
                    while (!("/exit".equals(userInput)))
                    {
                        userInput = fromClient.readLine();
                        ChatMessage cm = new ChatMessage(userInput, username, timestamp.toString());
                        sendMessage(agentSocket, cm);
                    }
                }
                
                else if ("agent".equalsIgnoreCase(usertype))
                {
                    String id = fromClient.readLine();
                    String pw = fromClient.readLine();
                    
                    if(Authenticate(id, pw, server) == true)
                    {
                        toClient.println("agent authentication successful");
                        toClient.flush();
                        Agent agent = new Agent(server);
                        agentList.add(agent);
                        String userInput = null;
                        Date timestamp = new Date();
                        Socket receiver = null;
                        int y, z;
                        
                        while(!("/exit".equals(userInput)))
                        {
                            if(agentList.get(agentList.indexOf(server)).getNumOfCustomers() == 1 || agentList.get(agentList.indexOf(server)).getNumOfCustomers() == 2)
                            {
                                toClient.println("customer found");
                                
                                for(y = 0; y < customerList.size(); y ++)
                                {
                                    for(z = 0; z < agent.customerSocketList.size(); z ++)
                                    {
                                        if(customerList.get(y).getCustomerSocket() == agent.getCustomerSocket(z))
                                        {
                                            receiver = customerList.get(y).getCustomerSocket();
                                            username = customerList.get(y).getUsername();
                                            toClient.println(username);
                                            break;
                                        }
                                    }
                                }
                                
                                if(!("/exit".equals(userInput)))
                                {
                                    ChatMessage cm = new ChatMessage(userInput, username, timestamp.toString());
                                }
                            }
                        }
                    }
                    
                    String userInput = "";
                    Chat agentChat = new Chat(server);
                    Socket reciever;
                    
                    toClient.println("Enter -1 to exit");
                    toClient.println("Waiting for Client");
                    
                    while(!("-1".equals(userInput)))
                    {
                        
                        if(agentList.get(agentList.indexOf(server)).getNumOfCustomers() == 1)
                        {
                            //menu
                            if (!("-1".equals(userInput)))
                            {
                                cm(userInput, username, timestamp);
                                sendMessage (agentList.get(agentList.indexOf(server)).getCustomerSocket(1), cm);
                                userInput = fromClient.readLine();
                            }
                        }
                        
                        else if(agentList.get(agentList.indexOf(server)).getNumOfCustomers() == 2)
                        {
                            //menu
                            String reply = fromClient.readLine();
                            
                            switch(reply.charAt(0))
                            {
                                case '1':
                                    reciever = agentList.get(agentList.indexOf(server)).getCustomerSocket(1);
                                    break;
                                    
                                case '2':
                                    reciever = agentList.get(agentList.indexOf(server)).getCustomerSocket(2);
                                    break;
                                    
                                case '-':
                                    userInput = "-1";
                                    break;
                                    
                                default:
                                    toClient.println("Enter a valid option");
                                    continue;
                            }
                            
                            if (!("-1".equals(userInput)))
                            {
                                cm(userInput, username, timestamp);
                                sendMessage(reciever, cm);
                                userInput = fromClient.readLine();
                            }
                        }
                    }
                    
                    agentChat.kill();
                }
            }
            
            catch(IOException e)
            {
                System.out.println("Exception occurred in run(): " + e);
            }
        }
    }
    
    public static boolean Authenticate(String id, String pw, Socket socket)
    {
        try
        {
            int attempts = 3;
            PrintWriter toClient = new PrintWriter(socket.getOutputStream());

            while(attempts != 0)
            {
                if("agent".equals(id) && "password".equals(pw))
                {
                    System.out.println("Agent authentication successful");
                    toClient.println("Authentication successful!");
                    toClient.flush();
                    attempts = 0;
                    return true;
                }

                else
                {
                    attempts --;
                    System.out.println("Incorrect ID and/or Password.");
                    System.out.println("Remaining attempts: " + attempts);
                    toClient.println("Incorrect ID and/or Password! Please try again.\n" + 
                            "Remaining attempts: " + attempts);
                    toClient.flush();
                    
                    if(attempts == 0)
                    {
                        System.out.println("Agent login attempt failed.");
                        toClient.println("Agent authentication failed! Program will now terminate.");
                        return false;
                    }
                }
            }
        }
        
        catch(IOException e)
        {
            System.out.println("Exception occurred during authentication: " + e);
            return false;
        }
        return false;
    }

    public static void sendMessage (Socket socket, ChatMessage cm) throws IOException
    {
        ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
        sOutput.writeObject(cm);
    }
    
    public static class Chat extends Thread 
    {
        boolean reading = true;
        Socket client;
        
        Chat(Socket socket)
        {
            client = socket;
            start();
        }
        
        @Override
        public void run()
        {
            try
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while(reading)
                {
                    input.readLine();
                }
            }
            
            catch(IOException e)
            {
                System.out.println("Exception occurred: " + e);
            }
        }
        
        public void kill()
        {
            reading = false;
        }
    }
}
