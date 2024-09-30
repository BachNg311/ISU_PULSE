package coms309.backEnd.demo.entity;

import jakarta.persistence.*;

@Entity
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String dId;

    private String name;
    private String location;

    // Getters and Setters
    public String getDId() {
        return dId;
    }

    public void setDId(String dId) {
        this.dId = dId;
    }

    // Constructors
    public Department() {
    }

    public Department(String dId, String name, String location) {
        this.dId = dId;
        this.name = name;
        this.location = location;
    }
}