package pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pt.isec.pd.a21280305.pedrocorreia.whatsupp.whatsupp_api.data.User;

@RestController
public class LoginController {

    @PostMapping("login")
    public void login(){
//        return ResponseEntity.status(HttpStatus.OK).body("No username or password provided");
    }
}
