package com.cg.gotstock.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import  org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public @Data class PortfolioResponseDTO {
    private String message;
    private Object data;
    private int statusCode;

    public PortfolioResponseDTO(String message, Object data) {
        this.message =  message;
        this.data = data;
        this.statusCode = HttpStatus.OK.value();
    }

    public PortfolioResponseDTO(String message,HttpStatus statusCode,Object  data){
        this.message = message;
        this.data = data;
        this.statusCode = statusCode.value();
    }



//    public void setMessage(String message){
//        this.message = message;
//    }
//
//    public String getMessage(){
//        return message;
//    }
//
//    public void setData(Object data){
//        this.data = data;
//    }
//
//    public Object getData(){
//        return data;
//    }
//

}
