package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.client.logic.connection.tables.*;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.client.logic.connection.tables.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {

    Connection con;
    static Statement stmt;

    @Autowired
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
            return -1;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public int getGroupIdByName(String name){
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT group_id FROM whatsupp_db.groups WHERE name = '" + name +"'");
            rs.next();
            return rs.getInt("group_id");
        }catch(SQLException e){
            return -1;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public boolean userInGroup(int userId, int groupId){
        try{
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT admin_user_id FROM whatsupp_db.groups WHERE group_id = " + groupId);
            rs.next();
            if(rs.getInt("admin_user_id") == userId){
                return true;
            }

            rs = stmt.executeQuery("SELECT requester_user_id, request_status FROM group_requests WHERE group_id = " + groupId);
            while(rs.next()){
                if(rs.getInt("requester_user_id") == userId && rs.getInt("request_status") == 1){
                    return true;
                }
            }
            return false;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public ResponseEntity updateUser(String token, String name){
        User me = new User(getUserIdByToken(token));

        String updateQuery = new String("UPDATE users\n" +
                "SET username = '" + name + "'\n" +
                "WHERE user_id = " + me.getID());
        try{
            stmt = con.createStatement();
            stmt.executeUpdate(updateQuery);
            if(stmt.executeUpdate(updateQuery) < 1){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
            }
            return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("New username update for the user");
        } catch (SQLException e) {
            System.out.println("[update] Error querying the database:\r\n\t" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println("SQLException problem:\r\n\t" + e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
            }
        }
    }

    public ResponseEntity getFriendsList(String token){
        User user = new User(getUserIdByToken(token));
        List<FriendsRequests> friends = new ArrayList<>();
        FriendsRequests newFriend = null;
        String query = new String("SELECT user_id, username, users.name, users.status, " +
                "requester_user_id, friend_user_id, friends_requests.request_time, " +
                "friends_requests.answer_time, friends_requests.request_status " +
                "FROM users " +
                "JOIN friends_requests ON user_id in (requester_user_id, friend_user_id) " +
                "AND friends_requests.request_status = 1 " +
                "WHERE (requester_user_id = " + user.getID() + " and friend_user_id = user_id) " +
                "or (requester_user_id = user_id and friend_user_id = " + user.getID() + ")");
        try {
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                if (rs.getInt("requester_user_id") == user.getID()) {
                    newFriend = new FriendsRequests(new User(getUsernameByToken(token)),
                            new User(rs.getString("username"), rs.getString("name"),
                                    rs.getInt("user_id"), rs.getInt("status")),
                            rs.getTimestamp("request_time"), rs.getInt("request_status"),
                            rs.getTimestamp("answer_time"));
                } else {
                    newFriend = new FriendsRequests(
                            new User(rs.getString("username"), rs.getString("name"),
                                    rs.getInt("user_id"), rs.getInt("status")),
                            new User(getUsernameByToken(token)),
                            rs.getTimestamp("request_time"), rs.getInt("request_status"),
                            rs.getTimestamp("answer_time"));
                }
                friends.add(newFriend);
            }
            rs.close();

            if(friends.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("No friends to display");
            }

            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for(FriendsRequests f : friends) {
                JSONObject item = new JSONObject();
                item.put("requester", f.getRequester().getUsername());
                item.put("receiver", f.getReceiver().getUsername());
                item.put("request date", f.getRequestTime());
                item.put("answer date", f.getAnswerTime());
                array.appendElement(item);
            }

            json.put("friends", array);

            return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body(json);

        } catch (SQLException e) {
            System.out.println("[getFriends] Error querying the database:\r\n\t" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error querying the database");
        } catch (NullPointerException e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error querying the database");
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error querying the database");
            }
        }
    }

    public ResponseEntity removeFriend(String token, String name){
        User user = new User(getUserIdByToken(token));
        User friend = new User(getUserIdByName(name));

        if(friend.getID() == -1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Authorization", token).body("No friend found");
        }

        String query = new String("DELETE FROM friends_requests WHERE (requester_user_id = " +
                user.getID() + " AND friend_user_id = " + friend.getID() + ") " +
                "OR (requester_user_id = " + friend.getID() + " AND friend_user_id = " + user.getID() + ")");
        String nextQuery = new String("DELETE files FROM files JOIN messages\n" +
                "WHERE files.message_id = messages.message_id\n" +
                "AND (messages.user_id_to = " + user.getID() + " AND messages.user_id_from = " + friend.getID() +")\n" +
                "OR (messages.user_id_to = " + friend.getID() + " AND messages.user_id_from = " + user.getID() +")");
        String finalQuery = new String("DELETE FROM messages WHERE (user_id_from = " +
                user.getID() + " AND user_id_to = " + friend.getID() + ") " +
                "OR (user_id_from = " + friend.getID() + " AND user_id_to = " + user.getID() + ")");
        try {
            Statement stmt = con.createStatement();
            if (stmt.executeUpdate(query) < 1) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Authorization", token).body("No friendship was available.");
            } else {
                stmt.close();
                stmt = con.createStatement();
                stmt.executeUpdate(nextQuery);
                stmt.close();
                stmt = con.createStatement();
                stmt.executeUpdate(finalQuery);
                return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("Friend " + name + " deleted successfully");
            }
        } catch (SQLException e) {
            System.out.println("SQLException problem:\r\n\t" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
        } finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println("SQLException problem:\r\n\t" + e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
            }
        }
    }

    public ResponseEntity getGroupsList(String token){
        User user = new User(getUserIdByToken(token));
        List<GroupRequests> groupRequests = new ArrayList<>();
        String query = new String("SELECT g.group_id, g.admin_user_id, g.name, gr.*, u.name, u.username\n" +
                "FROM whatsupp_db.groups g\n" +
                "LEFT JOIN group_requests gr ON gr.group_id = g.group_id \n" +
                "LEFT JOIN users u on g.admin_user_id = u.user_id\n" +
                "WHERE admin_user_id = " + user.getID() + "\n" +
                "OR g.group_id IN (\n" +
                "SELECT group_id\n" +
                "FROM group_requests\n" +
                "WHERE requester_user_id = " + user.getID() + "\n" +
                "AND request_status = 1)" +
                "GROUP BY g.group_id");
        try{
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
                User admin = new User(rs.getString("username"), rs.getString("name"), rs.getInt("admin_user_id"));
                if(admin.getID() == user.getID()){
                    Group newGroup = new Group(admin, rs.getString("name"), rs.getInt("group_id"));
                    GroupRequests newGR = new GroupRequests(admin, newGroup);
                    groupRequests.add(newGR);
                }
                else {
                    Group newGroup = new Group(admin, rs.getString("name"), rs.getInt("g.group_id"));
                    GroupRequests newGR = new GroupRequests(user, newGroup, rs.getTimestamp("request_time"), rs.getTimestamp("answer_time"), rs.getInt("request_status"));
                    groupRequests.add(newGR);
                }
            }
            rs.close();

            if(groupRequests.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("No groups to display");
            }

            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for(GroupRequests g : groupRequests) {
                JSONObject item = new JSONObject();
                item.put("name", g.getGroup().getName());
                item.put("admin", g.getGroup().getAdmin().getUsername());
                item.put("request date", g.getRequestTime());
                item.put("answer date", g.getAnswerTime());
                array.appendElement(item);
            }

            json.put("groups", array);

            return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body(json);
        } catch (SQLException e) {
            System.out.println("[getMessages] Error querying the database:\r\n\t" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
        } catch (NullPointerException e) {
            System.out.println("[getMessages] Error:\r\n\t" + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println("SQLException problem:\r\n\t" + e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error processing the request");
            }
        }
    }

    public ResponseEntity getMessagesFromUser(String token, String name){
        List<Message> messages = new ArrayList<>();

        try {
            Statement stmt = con.createStatement();
            int userId = getUserIdByToken(token);
            if(userId == -1){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Authorization", token).body("User not found");
            }
            int friendId = getUserIdByName(name);
            if(friendId == -1){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Authorization", token).body("Friend not found");
            }

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
                    messages.add(new Message(new User(getUsernameByToken(token)), new User(name), text, rs.getInt("message_id"), rs.getTimestamp("sent_time")));
                }else{
                    messages.add(new Message(new User(name), new User(getUsernameByToken(token)), text, rs.getInt("message_id"), rs.getTimestamp("sent_time")));
                }
            }

            if(messages.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("No messages to display");
            }

            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for(Message m : messages) {
                JSONObject item = new JSONObject();
                item.put("sender", m.getSender().getUsername());
                item.put("receiver", m.getReceiver().getUsername());
                item.put("message", m.getMsgTxt());
                item.put("sent_time", m.getTime());
                array.appendElement(item);
            }

            json.put("messages", array);

            return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body(json);
        } catch(SQLException e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error getting the messages from database.");
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }

    public ResponseEntity getMessagesFromGroup(String token, String name){
        List<Message> messages = new ArrayList<>();

        try {
            Statement stmt = con.createStatement();
            int userId = getUserIdByToken(token);
            if(userId == -1){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Authorization", token).body("User not found.");
            }
            int groupId = getGroupIdByName(name);
            if(groupId == -1){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).header("Authorization", token).body("Group not found.");
            }

            if(!userInGroup(userId, groupId)){
                return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("User isn't on the group");
            }

            String query = "SELECT username, user_id, u.name, m.message_id, user_id_from, text, sent_time, m.group_id\n" +
                    "FROM users u\n" +
                    "JOIN messages m on user_id = m.user_id_from\n" +
                    "JOIN whatsupp_db.groups g on m.group_id = g.group_id\n" +
                    "WHERE g.group_id = " + groupId + "\n" +
                    "ORDER BY sent_time";

            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                String text = rs.getString("text");
                messages.add(new Message(new User(rs.getString("username")), new Group(name, groupId), text, rs.getInt("message_id"), rs.getTimestamp("sent_time")));
            }

            if(messages.isEmpty()){
                return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body("No messages to display");
            }

            JSONObject json = new JSONObject();
            JSONArray array = new JSONArray();

            for(Message m : messages) {
                JSONObject item = new JSONObject();
                item.put("sender", m.getSender().getUsername());
                item.put("receiver group", m.getGroup().getName());
                item.put("message", m.getMsgTxt());
                item.put("sent_time", m.getTime());
                array.appendElement(item);
            }

            json.put("messages", array);

            return ResponseEntity.status(HttpStatus.OK).header("Authorization", token).body(json);
        } catch(SQLException e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).header("Authorization", token).body("Error getting the messages from the database");
        }finally {
            try {
                stmt.close();
            } catch (SQLException e) {
            }
        }
    }


}
