package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data.DBManager;

@RestController
public class GroupsController {
    @GetMapping("groups")
    public ResponseEntity getGroupsList(@RequestHeader("Authorization") String token){

        DBManager dbManager = new DBManager();

        return dbManager.getGroupsList(token);
    }
}
