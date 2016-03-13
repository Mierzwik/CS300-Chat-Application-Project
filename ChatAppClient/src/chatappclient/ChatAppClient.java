/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chatappclient;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultListModel;

/**
 *
 * @author Bryan Mierzwik
 */
public class ChatAppClient {
    private static final String server = "127.0.0.1";
    private static final int port = 7656;
    public  Socket socket;
    public static PrintWriter out;
    public static BufferedReader in;
    private ClientGUI GUI;
    public static ChatAppClient client;
    private MainChatWindow chat;
    private boolean session;
    private String username;
    private Listener listen;
    private int clientID;
    
    // Map<String username, Integer userID>
    private static Map<String, Integer> currentUsers;
    
    // Map<String userId, PrivateChat privateChatWindow>
    // userID = user who messages are going to. 
    private static Map<Integer, PrivateChat> privateChats;

    
    public static void main(String[] args) {
        client = new ChatAppClient();
        client.session = false;
        currentUsers = new HashMap<>();
        privateChats = new HashMap<>();
        client.GUI = new ClientGUI();
        client.GUI.setVisible(true);

    }
    
    
    public static void sendMessage(String message){
        ChatAppClient.out.println(message);
    }
    
    public void connect(String flag, String userInfo) throws IOException {
        
        String response;
        
        try {
        client.socket = new Socket(server, port);
        ChatAppClient.out = new PrintWriter(client.socket.getOutputStream(), true);
        ChatAppClient.in = new BufferedReader(new InputStreamReader(client.socket.getInputStream()));
        } catch (IOException a) {GUI.setTextField(a.toString());}
        
        switch (flag) {
            case "login":
                ChatAppClient.out.println("/login");

        
                ChatAppClient.out.println(userInfo);
                
                while ((response = in.readLine()) != null) 
                        {
                    if (response.equals("okay")) {
                        client.GUI.destroy();
                        client.chat = new MainChatWindow();
                        client.chat.setUsername(username);
                        clientID = client.chat.getID();
                        client.chat.setVisible(true);
                        listen = new Listener(username);
                        listen.start();
                        break;
                    } else {
                        client.GUI.setError("Invalid username or password");
                    }
                }   
                break;
            case "register":
                //client.out.println("/register");
                break;
        }
    }
    
    public void disconnect() {
        try{
            //ChatAppClient.out.println("/quit");
            
            ChatAppClient.out.close();
            ChatAppClient.in.close();
            client.socket.close();
            
        } catch (IOException b) {}
    }
    
    public static void displayMessage(String message) {
        client.chat.displayMessage(message);
    }
    
    public void setUsername(String user) {
        username = user;
    } 
    
    public void addUser(String users) {
        Gson json = new Gson();
        String test = json.toJson(users);
        String[] newUser = null;
        DefaultListModel listModel = new DefaultListModel();
        //
        // Parse through json string of users
        users = users.replaceAll("\"", "").replace("{", "").replace("}", "");
        String[] temp = users.split(",");
        for (String temp1 : temp) {
            newUser = temp1.split(":");
            currentUsers.put(newUser[0], Integer.parseInt(newUser[1]));
            if (clientID == 0 && newUser[0].equals(username)) {
                clientID = Integer.parseInt(newUser[1]);
            }
        }
        
        for (Map.Entry<String, Integer> entry : currentUsers.entrySet()) {
            listModel.addElement(entry.getKey());
        }
        
        chat.addUser(listModel);
    }
    
    public void removeUser(String remove) {
        DefaultListModel listModel = new DefaultListModel();
        currentUsers.remove(remove);
        for (Map.Entry<String, Integer> entry : currentUsers.entrySet()) {
            listModel.addElement(entry.getKey());
        }
        chat.addUser(listModel);
    }
    
    // Arguments: int id
    // id is the user ID of whoever the recipient of the private chat is
    public void openPrivateChat(int id, String toUsername) {
        if (!toUsername.equals(username)) {
            PrivateChat temp = new PrivateChat(id, username, toUsername, clientID);
            privateChats.put(id, temp);
            temp.setVisible(true);
        } else {
            ChatAppClient.out.println("ERROR: You cannot open a private chat with yourself!\n");
        }
    }
    
    public void displayPrivateMessage(String[] tempMessage) {
        PrivateChat tempChat = null;
        String toUser = tempMessage[1].replace(":", "");
        String fromUser = tempMessage[2].replace(":","");
        int toID = client.getID(toUser);
        int fromID = client.getID(fromUser);
        String message = "";
        
        // Build message to send from tempMessage array
        for (int i = 2; i < tempMessage.length; i++) {
            message = message.concat(tempMessage[i] + " ");
        }
        message = message.concat("\n");
        
        // Find privateChats map entry for fromID and display the message
        // only if fromID != toID
        
        for (Map.Entry<Integer, PrivateChat> entry : privateChats.entrySet()) {
            if (entry.getKey() == fromID) {
                tempChat = entry.getValue();
                tempChat.display(message);
            }  
        }
    }
    
    // Returns the userID of a currently logged in user
    public int getID(String user) {
        int ret = -2;
        try { 
            ret = currentUsers.get(user);
        } catch (java.lang.NullPointerException invalidUser) { ret = -1; }
        return ret;
        //int ret = -1;
        //ret = currentUsers.get(user);
        //return ret;
    }
    
    // Returns boolean value depending on if a private caht window exists for not
    public boolean privateChatExists(int from) {
        boolean ret = false;
        PrivateChat temp = ChatAppClient.privateChats.get(from);
        if (temp != null) {
            ret = true;
        }
        return ret;
    }
    
    // from should be to for openPrivateChat
    public void createPrivateChat(String[] message, int from) {
        openPrivateChat(from, message[2].replace(":", ""));
        displayPrivateMessage(message);
    }
    
    public void privateChatClose(int toClose) {
        privateChats.remove(toClose);
    }
}
