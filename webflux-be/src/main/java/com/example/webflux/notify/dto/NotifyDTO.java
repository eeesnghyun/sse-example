package com.example.webflux.notify.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class NotifyDTO {

    String storeCode;
    String message;

}
