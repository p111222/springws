package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "my_table", schema = "coll_hub")
public class MyEntity {

    @Id
    private Long id;
    private String data;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

}
