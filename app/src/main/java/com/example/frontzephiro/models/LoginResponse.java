package com.example.frontzephiro.models;

public class LoginResponse {
    private Long id;
    private String name;
    private String token;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
