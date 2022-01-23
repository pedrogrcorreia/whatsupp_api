package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data;

import pt.isec.pd.a21280305.pedrocorreia.whatsupp.client.logic.connection.tables.Message;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.client.logic.connection.tables.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    Connection con;
    static Statement stmt;

    public DBManager() {
        String db = "jdbc:mysql://192.168.1.73:3306/whatsupp_db";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find JDBC Driver: \r\n\t " + e);
            return;
        }

        try {
            con = DriverManager.getConnection(db, "tester", "password-123");
            stmt = con.createStatement();
            System.out.println("Connection successful.");
        } catch (SQLException e) {
            System.out.println("Couldn't connect to Database: \r\n\t " + e);
        }
    }

    public String getUsernameByToken(String token){
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT username FROM users WHERE token = '" + token + "'");
            rs.next();
            return rs.getString("username");
        } catch(SQLException e){
            return null;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public int getUserIdByToken(String token){
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE token = '" + token + "'");
            rs.next();
            return rs.getInt("user_id");
        } catch(SQLException e){
            return -1;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public int getUserIdByName(String name){
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE username = '" + name + "'");
            rs.next();
            return rs.getInt("user_id");
        }catch(SQLException e){
            e.printStackTrace();
            return -1;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public List<Message> getMessages(String token, String name){
        List<Message> messages = new ArrayList<>();

        try {
            Statement stmt = con.createStatement();
            int userId = getUserIdByToken(token);
            int friendId = getUserIdByName(name);

            String query = "SELECT username, user_id, users.name, messages.message_id, messages.text, user_id_to, user_id_from, messages.sent_time " +
                    "FROM users " +
                    "JOIN messages ON user_id = user_id_to " +
                    "WHERE (user_id_from = " + userId + " and user_id_to = " + friendId + ") " +
                    "OR (user_id_from = " + friendId + " and user_id_to = " + userId + ")" +
                    "ORDER BY sent_time";
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                int sender = rs.getInt("user_id_from");
                String text = rs.getString("text");
                if(sender == userId){
                    messages.add(new Message(new User(getUsernameByToken(token)), new User(name), text));
                }else{
                    messages.add(new Message(new User(name), new User(getUsernameByToken(token)), text));
                }
            }
            return messages;
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }
}
