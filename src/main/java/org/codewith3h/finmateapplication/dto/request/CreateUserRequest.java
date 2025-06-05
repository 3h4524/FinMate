package org.codewith3h.finmateapplication.dto.request;


import lombok.Data;

@Data
public class CreateUserRequest {

    private String name;
    private String email;
    private String password;
}
