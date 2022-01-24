package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data.DBManager;

@RestController
public class UserController {

    @PostMapping("/changeName")
    public ResponseEntity changeName(@RequestHeader("Authorization") String token,
                                @RequestHeader("NewName") String newName){

        DBManager dbManager = new DBManager();
        return dbManager.updateUser(token, newName);
    }
}
