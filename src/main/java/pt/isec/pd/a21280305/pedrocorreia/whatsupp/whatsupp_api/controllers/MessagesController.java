package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data.DBManager;


@RestController
public class MessagesController {

    @GetMapping("messages/user/{name}")
    public ResponseEntity getMessages(@RequestHeader("Authorization") String token,
                                      @PathVariable("name") String friend){

        DBManager dbManager = new DBManager();

        return dbManager.getMessagesFromUser(token, friend);
    }

    @GetMapping("messages/group/{name}")
    public ResponseEntity getMessagesGroup(@RequestHeader("Authorization") String token,
                                      @PathVariable("name") String group){

        DBManager dbManager = new DBManager();

        return dbManager.getMessagesFromGroup(token, group);
    }
}
