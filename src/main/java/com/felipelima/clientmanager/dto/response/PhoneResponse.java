package com.felipelima.clientmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneResponse {

    private Long id;
    private String type;
    private String number;
}
