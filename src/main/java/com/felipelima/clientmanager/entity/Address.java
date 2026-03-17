package com.felipelima.clientmanager.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import javax.persistence.*;
 
@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @Column(nullable = false, length = 8)
    private String zipCode;
 
    @Column(nullable = false)
    private String street;
 
    @Column(nullable = false)
    private String neighborhood;
 
    @Column(nullable = false)
    private String city;
 
    @Column(nullable = false, length = 2)
    private String state;
 
    private String complement;
}