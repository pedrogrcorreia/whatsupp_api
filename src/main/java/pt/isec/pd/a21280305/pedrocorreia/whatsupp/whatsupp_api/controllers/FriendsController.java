package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data.DBManager;

@RestController
public class FriendsController {

    @GetMapping("friends")
    public ResponseEntity getFriendList(@RequestHeader("Authorization") String token){

        DBManager dbManager = new DBManager();

        return dbManager.getFriendsList(token);
    }

    @PostMapping("friends/delete")
    public ResponseEntity deleteFriend(@RequestHeader("Authorization") String token,
                                       @RequestParam(value = "name", required = true) String name){

        DBManager dbManager = new DBManager();

        return dbManager.removeFriend(token, name);
    }
}
