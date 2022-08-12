package com.shoppingmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
// If var is null then we do not included in json serialize
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String message;
    private T data;

    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status,T data,String message){
        this.status = status;
        this.data = data;
        this.message = message;
    }

    private ServerResponse(int status,String message){
        this.status = status;
        this.message = message;
    }

    @JsonIgnore
    // It will not be included in Json serialize
    public boolean isSuccess(){
        return this.status == ResponseCode.Success.getCode();
    }

    public int getStatus(){
        return this.status;
    }

    public T getData(){
        return this.data;
    }

    public String getMessage(){
        return this.message;
    }

    public static <T>ServerResponse<T> createBySuccess(){
        return new ServerResponse<>(ResponseCode.Success.getCode());
    }
    public static <T>ServerResponse<T> createBySuccessMessage(String message){
        return new ServerResponse<>(ResponseCode.Success.getCode(),message);
    }
    public static <T>ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<>(ResponseCode.Success.getCode(),data);
    }
    public static <T>ServerResponse<T> createBySuccess(String message,T data){
        return new ServerResponse<>(ResponseCode.Success.getCode(),data,message);
    }

    public static <T>ServerResponse<T> createByError(){
        return new ServerResponse<>(ResponseCode.ERROR.getCode());
    }
    public static <T>ServerResponse<T> createByErrorMessage(String message){
        return new ServerResponse<>(ResponseCode.ERROR.getCode(),message);
    }
    public static <T>ServerResponse<T> createByErrorCodeMessage(int ErrorCode,String message){
        return new ServerResponse<>(ErrorCode,message);
    }

}
