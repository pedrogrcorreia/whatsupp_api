package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/changeName")
    public ResponseEntity changeName(@RequestHeader("NewName") String newName){

        return ResponseEntity.status(HttpStatus.OK).body(newName);
    }
}
