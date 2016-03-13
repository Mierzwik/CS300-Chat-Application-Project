package chatappserver;

import com.google.gson.Gson;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 *
 * @author Bryan Mierzwik
 */
public class DatabaseHandler {
    
    private static Connection conn = null;
    private static Statement statement = null;
    
    DatabaseHandler() {
        Properties connectionProps = new Properties();
        connectionProps.put("user", "chatserver");
        connectionProps.put("password", "securepassword");
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
            DatabaseHandler.conn = DriverManager.getConnection("jdbc:derby://localhost:1527/Users;create=true", connectionProps);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException a) { System.out.println(a.toString());  }
    }
    
    // Method Name: insertUser
    // Input:       JSON String
    // Returns:     0 if username and email do not exist. 
    //              2 if username exists
    //              3 if email exists
    //              5 if both username and email exist
    //              <0 if error
    public int insertUser(String jsonString) {
        int success = -2;
        Gson gson = new Gson();
        try {
        } catch (Exception a) {}
        
        //joinDate not added to DB yet. 
        Date joinDate = new Date();
        
        
        jsonString = gson.fromJson(jsonString, String.class);
        
        // Strip excess characters from the strings
        String[] info = jsonString.split(",");
        String username = info[0].replaceAll("\"", "").substring(1);
        String password = info[1].replaceAll("\"", "");
        String firstName = info[2].replaceAll("\"", "");
        String lastName = info[3].replaceAll("\"", "");
        String email = info[4].replaceAll("\"", "");
        int gender = Integer.parseInt(info[5].replaceAll("\"", ""));
        int country = Integer.parseInt(info[6].replaceAll("\"", "").replaceAll("]", ""));
        
        if (this.exists(username, "USERNAME")) {
            success += 3;
        } else {
            success++;  
        }
        if ((this.exists(email, "EMAIL"))) {
            success += 4;
        } else { 
            success++;
        }
        if (success == 0) {
            try {
                    DatabaseHandler.statement = conn.createStatement();
                    DatabaseHandler.statement.executeUpdate("INSERT INTO chatserver.users (USERNAME, PASSWORD, EMAIL, GENDER, COUNTRY, FIRSTNAME, LASTNAME)"
                                    + "VALUES (" + "'" + username + "'" + ", " + "'" + password + "'" + ", " + "'" + email + "'" + ", " + gender  + ", " + country + ", "
                                    + "'" + firstName + "'" + ", " + "'" + lastName + "'" + ")");
                    DatabaseHandler.statement.close();
                } catch (SQLException a) {System.out.println(a.toString());}
        }
        return success;
    }
    
    public boolean exists(String toFind, String field) {
        boolean exists = false;
        int testResults = -1;
        //query DB to see if ToFind exists
        try {
            DatabaseHandler.statement = conn.createStatement();
            // SELECT count(_field_) FROM users
            // where USERNAME = 'toFind';
            ResultSet results = DatabaseHandler.statement.executeQuery("SELECT count(" + field + ") AS rowcount FROM chatserver.users WHERE " + field + " = " + "'" + toFind + "'");
            if (results.next()) {
                testResults = results.getInt("rowcount");
            }
            
            if (testResults > 0)
                exists = true;
            DatabaseHandler.statement.close();
        } catch (SQLException a) {System.out.println(a.toString());}
        
        return exists;
    } 
    
    public void sendMessage(String message) {
        
    }
    
    public int verifyLogin(String userInfo) {
        int userID = -1;
        String user = null;
        String pass = null;
        //int ret = false;
        Gson gson = new Gson();
        //parse userInfo, set userID, user, and pass
        //String test = gson.fromJson(userInfo, String.class);
        
        String[] info = userInfo.split(",");
        String attemptedUsername = info[0].replaceAll("\"", "").substring(1);
        String attemptedPassword = info[1].replaceAll("\"", "").replaceAll("]", "");
        
        try {
        DatabaseHandler.statement = conn.createStatement();
        // SELECT userID, username, password FROM chatserver.users
        // WHERE username = user 
        // AND password = pass;
        
        ResultSet result = DatabaseHandler.statement.executeQuery("Select userID, username, password FROM Chatserver.users WHERE username = " + "'" + attemptedUsername + "'" + " AND password = '" + attemptedPassword + "'");
        if (result.next()) {
            //userID = result.getInt("USERID");
            user = result.getString("USERNAME");
            pass = result.getString("PASSWORD");
        }
        if ((user != null && pass != null) && (user.equals(attemptedUsername) && pass.equals(attemptedPassword))) {
            userID = result.getInt("USERID");
            
        }
            
        } catch (SQLException a) {System.out.println(a.toString());}
        return userID;
    }
    
    public void logMessage(String message, int sender, int recipient) {
        String[] aDate = new Date().toString().split(" ");
        String date = aDate[0] + aDate[1] + aDate[2] + ", " + aDate[5];
        String time = aDate[3];
        
        // INSERT INTO chatserver.users (DATE, TIME, MESSAGE, SENDERID, RECIPIENTID)
        // VALUES (date, time, message, sender, recipient);
        
         try {
                    DatabaseHandler.statement = conn.createStatement();
                    DatabaseHandler.statement.executeUpdate("INSERT INTO chatserver.messagelogs (DATE, TIME, MESSAGE, SENDERID, RECIPIENTID)"
                                    + "VALUES (" + "'" + date + "'" + ", " + "'" + time + "'" + ", " + "'" + message + "'" + ", " + sender  + ", " + recipient + ")");
                    DatabaseHandler.statement.close();
                } catch (SQLException a) {System.out.println(a.toString());}
    }
}