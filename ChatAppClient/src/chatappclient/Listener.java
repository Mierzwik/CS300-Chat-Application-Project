/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package chatappclient;

import java.io.IOException;
import static java.lang.System.exit;
import java.util.Arrays;

/**
 *
 * @author Bryan
 */
public class Listener extends Thread {
    String username;
    
    Listener(String user) { username = user; }
    public void run() {
        String message = null;
        boolean session = true;
        while(session) {
            String[] messageTest = null;
            try {
                while ((message = ChatAppClient.in.readLine()) != null) {
                    messageTest = message.split(" ");
                    
                    switch (messageTest[0]) {
                            case "/quit":
                                session = false;
                                String toSend = Arrays.toString(messageTest).substring(8).replaceAll(",", "").replace("]", "");
                                String[] temp = toSend.split(" ");
                                ChatAppClient.client.removeUser(temp[0]);
                                ChatAppClient.displayMessage("SERVER: " + Arrays.toString(messageTest).substring(8).replaceAll(",", "").replace("]", ""));
                                
                                //remove messageTest[1] user from the logged on users list
                                
                                break;
                            
                            // Adds a user to the logged in user list
                            case "/AddUser":
                                //
                                // Parse list of users/ID in user list
                                //
                                // Add users to local list of online users
                                //
                                // Populate/add user(s) to list
                                //
                                String test = message.substring(9);
                                ChatAppClient.client.addUser(message.substring(9));
                                break;
                                
                            // Sends message to the correct private chat window
                            case "/msg":
                                // /msg tothisuser fromthisuser: message to be sent
                                // from = getID msg[2]
                                // to = getID msg[1]
                                
                                // 

                                // if the message is to you and from you, only display it. 
                                // if the message is to you and from someone else, blah?
                                int from = ChatAppClient.client.getID(messageTest[2].replace(":", ""));
                                int to = ChatAppClient.client.getID(messageTest[1].replace(":", ""));
                                boolean errorTest = (messageTest[2].equals("ERROR:"));
                                
                                if (from < 0) {
                                    ChatAppClient.client.displayPrivateMessage(messageTest);
                                    
                                } else if (from != ChatAppClient.client.getID(username)) {
                                    
                                    if (ChatAppClient.client.privateChatExists(from) != false) {
                                        ChatAppClient.client.displayPrivateMessage(messageTest);
                                        
                                    } else {
                                        ChatAppClient.client.createPrivateChat(messageTest, from);
                                    }
                                } else {
                                    ChatAppClient.client.displayPrivateMessage(messageTest);
                                }
                                break;
                                
                            default:
                                ChatAppClient.displayMessage(message + "\n");

                                break;
                        }
                }
            } catch (IOException a) {System.out.println(a.toString());}
            
        }
        this.close();
    }
    public void close() {
        ChatAppClient.client.disconnect();
        exit(0);
    }
}