package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.writer.JsonReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.client.logic.connection.tables.Message;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MessagesController {

    @GetMapping("messages")
    public ResponseEntity getMessages(@RequestHeader("Authorization") String token,
                                    @RequestParam(value = "name", required = true) String friend){
        List<Message> messages = new ArrayList<>();

        DBManager dbManager = new DBManager();

        messages = dbManager.getMessages(token, friend);
//        try{
//
//            Connection con = DriverManager.getConnection("jdbc:mysql://192.168.1.73:3306/whatsupp_db", "tester", "password-123");
//
//            Statement stmt = con.createStatement();
//
//            ResultSet rs = stmt.executeQuery("SELECT user_id FROM users WHERE token = '" + token + "'");
//            rs.next();
//            int userid = rs.getInt("user_id");
//
//            rs = stmt.executeQuery("SELECT user_id FROM users WHERE username = '" + friend + "'");
//
//            rs.next();
//            int friendid = rs.getInt("user_id");
//
//            String query = "SELECT file_path, username, user_id, users.name, messages.message_id, messages.text, user_id_to, user_id_from, messages.sent_time "
//                    +
//                    "FROM users " +
//                    "JOIN messages ON user_id = user_id_to " +
//                    "LEFT JOIN files on files.message_id = messages.message_id " +
//                    "WHERE (user_id_from = " + userid + " and user_id_to = " + friendid + ") " +
//                    "OR (user_id_from = " + userid + " and user_id_to = " + friendid + ")" +
//                    "ORDER BY sent_time";
//
//            rs = stmt.executeQuery(query);
//
//            while(rs.next()){
//                messages.add(rs.getString("text"));
//            }

        JSONObject json = new JSONObject();
        JSONArray array = new JSONArray();

        for(Message m : messages) {
            JSONObject item = new JSONObject();
            item.put("sender", m.getSender().getUsername());
            item.put("receiver", m.getReceiver().getUsername());
            item.put("message", m.getMsgTxt());
            array.appendElement(item);
        }

        json.put("messages", array);


        return ResponseEntity.status(HttpStatus.OK).body(json);
//            return messages.get(0).getMsgTxt();

//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//            return null;
//        }
    }
}
