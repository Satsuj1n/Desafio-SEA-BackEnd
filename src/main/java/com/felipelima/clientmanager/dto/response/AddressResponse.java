package com.felipelima.clientmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
 
    private String zipCode;
    private String street;
    private String neighborhood;
    private String city;
    private String state;
    private String complement;
}
 
