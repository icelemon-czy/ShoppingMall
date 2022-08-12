package com.shoppingmall.service;

import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String str,String type);
    ServerResponse<String> getQuestion(String username);
    ServerResponse<String> checkAnswer(String username,String question,String answer);
    ServerResponse<String> forgetResetPassword(String username,String newPassword,String user_token);
    ServerResponse<String> loginResetPassword(String oldPassword,String newPassword,User user);
    ServerResponse<User> updateUserInfo(User user);
    public ServerResponse<User> getUserInfo(Integer id);
}
