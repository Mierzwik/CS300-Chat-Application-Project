package chatappserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.IOException;
import com.google.gson.Gson;

/**
 *
 * @author Bryan Mierzwik
 */
public class UserConnection implements Runnable{
    
    private PrintWriter out = null;
    private BufferedReader in= null;  
    private int clientID;
    private String username;
    ChatAppServer test = null;
    Socket client = null;
    
    UserConnection(Socket newClient, ChatAppServer newTest) {
        clientID = -1;
        client = newClient;
        test = newTest;
        try {
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException a) {}
    }
    
    @Override
    public void run() {
        boolean thisSession = false;
        String input = null;
        String userInfo = null;
        String temp = null;
        String currentUserList = null;
        try {
        while ((input = in.readLine()) != null) {
            switch (input) {
                case "/register": 
                    register();
                    break;
                case "/login":
                    while ((temp = in.readLine()) != null) {
                        userInfo = temp;
                        break;
                    }
                    
                    int ID;
                    ID = test.login(userInfo);
                    if (ID > 0) {
                        thisSession = true;
                        clientID = ID;
                        String[] tempUser = userInfo.split(",");
                        username = tempUser[0].replaceAll("\"", "").substring(1);
                        //
                        // add user and user ID to list of logged on users, static list in ChatAppServer
                        ChatAppServer.addCurrentUser(username, clientID);
                        currentUserList = "/AddUser " + ChatAppServer.getCurrentUserList();
                        
                        // send list of logged on users to client, use /AddUser as first word of message
                        //ChatAppServer.broadcastMessage(currentUserList, clientID, 0);
                        //
                        
                    }
                    break;
                default:
                    input = "broken";
                    break;
            }
            break;
        }
        }catch(IOException c){}
           
        if (thisSession == false){
            try {
                if (!input.equals("/register")) {
                    out.println("fail");
                }
            client.close();
            } catch (IOException a){}
            test.close(clientID);
        } else {
            test.addText("User " + username + " connected. UserID: " + clientID);
            session(thisSession, currentUserList);
        }
        
    }
    
    public boolean register() {
        boolean ret = false;
        int success = -1;
        String received;
        String jsontext = null; 
        Gson gson = new Gson();

        try {
            while ((received = in.readLine()) != null){
                jsontext = gson.toJson(received);
                break;
            }
        }catch(IOException a) {}

        
        success = test.addUser(jsontext);
        
        out.println(Integer.toString(success));
        
        return ret;
    }
    
    public void session(boolean thisSession, String add) {
        String input;
        out.println("okay");
        out.println(add);
        while (thisSession){
            try {
                while ((input = in.readLine()) != null) {
                    String[] tempInput = input.split(" ");
                    switch (tempInput[0]) {
                        case "/quit":
                            String toSend = "/quit " + username + " has logged out.\n";
                            ChatAppServer.removeUser(username);
                            ChatAppServer.broadcastMessage(toSend, 0, 0);
                            thisSession = false;
                            test.close(clientID);
                            this.endSession();
                            break;
                            
                        case "/msg":
                            int toID = ChatAppServer.getID(tempInput[1]);
                            if ( toID < 0) {
                                ChatAppServer.broadcastMessage("/msg " + tempInput[2] + " ERROR: User has logged off", clientID, 0);
                            } else {
                                ChatAppServer.broadcastMessage(input, toID, clientID);
                            }
                            break;
                            
                        default:
                            //client.close();
                            ChatAppServer.broadcastMessage(input, 0, clientID);
                            break;
                        }
                    }
            } catch (IOException a) {}
        }
    }
    
    public void sendMessage(String message) {
        this.out.println(message);
    }
    
    public void setID(int toSet) {
        this.clientID = toSet;
    }
    
    public int getID() {
        return clientID;
    }
    
    public void endSession() {
        
        
        try {
            boolean test = client.isClosed();
            if (!client.isClosed()) {
                client.close();
            }
            in.close();
            out.close();
        }catch (IOException a) { System.out.println(a.toString());}
        
    }
    
    public String getUser() {
        return username;
    }
}