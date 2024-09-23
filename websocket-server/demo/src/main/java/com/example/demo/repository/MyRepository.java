package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.modal.MyEntity;

import java.util.List;

@Repository
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    // Custom query methods if needed
    List<MyEntity> findAll();
}
