package chatappserver;


import com.google.gson.Gson;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;


/**
 * Database 'Users'
 * Username/Password: chatserver/securepassword
 * Tables: USERS, MESSAGELOGS, COUNTRIES, GENDER
 */
public class ChatAppServer {
    private final int port = 7656;
    private ServerSocket sSocket;
    private Socket client;
    private ChatServerGUI GUI;
    private static ChatAppServer server;
    private static DatabaseHandler DB = null;
    private static ArrayList<UserConnection> userList;
    private static Map<String, Integer> currentUsers;
    

    public void temp(String message) {
        server.GUI.textAdd(message);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
           

        server = new ChatAppServer();
        ChatAppServer.userList = new ArrayList<>();
        DB = new DatabaseHandler();
        ChatAppServer.currentUsers = new HashMap<>();
        server.GUI = new ChatServerGUI();
        server.GUI.setVisible(true);
        server.addText("About to start connection");
        server.startConnection();
        server.end();
        
    }
    
    /**********************************************************
     * Method: startConnection()
     * Arguments: None
     * Returns: Nothing
     * Description: This method starts a connection to a client
    **********************************************************/
    public void startConnection() {
        server.addText("startConnection()");
        try{
        sSocket = new ServerSocket(port);
        } catch(IOException a){}
        
        while(true) {
            try {
                client = sSocket.accept();
                UserConnection connection = new UserConnection(client, server);
                userList.add(connection);
                new Thread(connection).start();
                
            } catch (IOException e){
            server.GUI.textAdd("Client connection failed, exception: " + e);
            }
        }
    }
    
    
    public void addText(String text) {
        server.GUI.textAdd(text);
    }

    
    public void close(int clientID) {
        if (!userList.isEmpty()) {
            for (int i = 0; i < userList.size(); i++) {
                UserConnection temp = userList.get(i);
                if (temp.getID() == clientID) {
                    //temp.endSession();
                    userList.remove(temp);
                }
            }
        }
        server.GUI.textAdd("Connection to client with ID " + clientID + " has been closed");
    }
    
    // Method Name: broadcastMessage
    // Arguments:   String message, int users
    // Description: This function will broadcast messages to the appropriate
    //              users. If users == 0, it will be broadcast to the main
    //              chat channel. If users == userID, it will be a private 
    //              message
    public static void broadcastMessage(String message, int ID, int fromID) {
        server.logMessage(message, fromID, ID);
        switch (ID) {
            
            case 0:
                // Broadcast to all users in main chat
                for (int i = 0; i < userList.size(); ++i) {
                    UserConnection temp = userList.get(i);
                    temp.sendMessage(message);
                }
                break;
                
            default:
                // Broadcast to specific user
                for (int i = 0; i < userList.size(); ++i) {
                    UserConnection temp = userList.get(i);
                    if (temp.getID() == ID) {
                        
                        temp.sendMessage(message);
                    }
                }
                break;
        }
        
    }
    
    public void logMessage(String message, int fromID, int toID) {
        String[] temp = message.split(" ");
        String toSend = null;
        String temp1 = temp[1];
        if (!((temp[0].substring(0,1)).equals("/")) | (temp[0].equals("/msg"))) {
            DB.logMessage(message, fromID, toID);
        }
    }
    
    public int addUser(String user) {
        int info = -3;
        info = DB.insertUser(user);
        return info;
    }
    
    public boolean exists(String toFind, String field) {
        return ChatAppServer.DB.exists(toFind, field);
    }

    public int login(String userInfo) {
        int userID;
        userID = DB.verifyLogin(userInfo);
        if (userID > -1) {
            String[] temp = userInfo.split(",");
            String user = temp[0].replaceAll("\"", "").substring(1);
            ChatAppServer.broadcastMessage("/AddUser " + user + ":" + Integer.toString(userID), 0, 0);
            String tosend = "/AddUser " + user + ":" + Integer.toString(userID);
            ChatAppServer.broadcastMessage("SERVER: " + user + " has joined the chat!", 0, 0);
            
        }
        
        return userID;
    }
    
    private void end() {
        for (int i = userList.size(); i > 0; --i) {
                UserConnection temp = userList.get(i-1);
                    temp.endSession();
                    userList.remove(temp);
                    //
                    // remove user from list of logged on users
                    //
            }
        try {
        sSocket.close();
        } catch (IOException a) { System.out.println(a.toString()); }
    }
    
    public static void addCurrentUser(String username, int clientID) {
        ChatAppServer.currentUsers.put(username, clientID);
    }
    
    public static String getCurrentUserList() {
        String ret;
        Gson json = new Gson();
        ret = json.toJson(currentUsers);

        return ret;
    }
    
    public static void removeUser(String remove) {
        currentUsers.remove(remove);
    }
    
    public static int getID(String tempUser) {
        int ret = -2;
        try { 
            ret = currentUsers.get(tempUser);
        } catch (java.lang.NullPointerException invalidUser) { ret = -1; }
        return ret;
    }
    
}