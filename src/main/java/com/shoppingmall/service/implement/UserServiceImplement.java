package com.shoppingmall.service.implement;

import com.shoppingmall.common.Const;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.common.TokenCache;
import com.shoppingmall.dao.UserMapper;
import com.shoppingmall.pojo.User;
import com.shoppingmall.service.IUserService;
import com.shoppingmall.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImplement implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password){
        int resultCount = userMapper.checkByName(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("User does not exist !");
        }
        // password encrypt by MD5
        password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectUserLogin(username,password);
        if(user == null){
            return ServerResponse.createByErrorMessage("Incorrect password !");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Log in success !",user);
    }

    @Override
    public ServerResponse<String> register(User user){
        // TODO : Username Naming Rule
        // TODO : password rule
        // TODO : Validation
        ServerResponse<String> validResponse = checkValid(user.getUsername(),Const.Username);
        if(!validResponse.isSuccess()){
            return validResponse;
        }
        validResponse = checkValid(user.getEmail(),Const.Email);
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        // Set Role
        user.setRole(Const.Role.Role_Customer);
        // Store encrypted password (MD5)
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Registration Fail !");
        }
        return ServerResponse.createBySuccessMessage("Registration Success !");
    }

    @Override
    public ServerResponse<String> checkValid(String str,String type){
        if(StringUtils.isNotBlank(type)){
            if(type.equals(Const.Username)){
                if(StringUtils.isBlank(str)){
                    return ServerResponse.createByErrorMessage("Username can not be null !");
                }
                int resultCount = userMapper.checkByName(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("Username has already existed !");
                }
            }
            if(type.equals(Const.Email)){
                int resultCount = userMapper.checkByEmail(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("Email has already existed !");
                }
            }
            return ServerResponse.createBySuccessMessage("Validation Success !");
        }else{
            return ServerResponse.createByErrorMessage("Type error");
        }
    }

    @Override
    public ServerResponse<String> getQuestion(String username){
        int resultCount = userMapper.checkByName(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("User does not exist !");
        }
        String question = userMapper.selectQuestion(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }else{
            return ServerResponse.createByErrorMessage("User does not provide Question !");
        }
    }

    @Override
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Wrong Answer");
        }else{
            // Answer is correct User can reset password
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.token_prefix+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username,String newPassword,String user_token){
        if(StringUtils.isBlank(user_token)){
            return ServerResponse.createByErrorMessage("Token is empty !");
        }
        int resultCount = userMapper.checkByName(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("User does not exist !");
        }
        String token = TokenCache.getKey(TokenCache.token_prefix+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("Token has expired !");
        }
        if(StringUtils.equals(token,user_token)){
            String password = MD5Util.MD5EncodeUtf8(newPassword);
            // Update password
            int rowCount = userMapper.updatePassword(username,password);
            if(rowCount>0){
                return ServerResponse.createBySuccessMessage("Update success !");
            }
            return ServerResponse.createByErrorMessage("Update Fail !");
        }else{
            return ServerResponse.createByErrorMessage("Invalid Token !");
        }
    }

    @Override
    public ServerResponse<String> loginResetPassword(String oldPassword,String newPassword,User user){
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Old Password incorrect !");
        }
        int rowCount = userMapper.updatePassword(user.getUsername(),newPassword);
        if(rowCount>0){
            return ServerResponse.createBySuccessMessage("Update success !");
        }
        return ServerResponse.createByErrorMessage("Update Fail !");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user){
        if(user.getEmail() != null) {
            int resultCount = userMapper.checkEmailByUserId(user.getId(), user.getEmail());
            if(resultCount>0){
                return ServerResponse.createByErrorMessage("Email has already occupied by other user !");
            }
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());
        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("Update Success",updateUser);
        }
        return ServerResponse.createByErrorMessage("Update Fail");
    }

    @Override
    public ServerResponse<User> getUserInfo(Integer id){
        User user = userMapper.selectByPrimaryKey(id);
        if(user == null){
            return ServerResponse.createByErrorMessage("User does not exist!");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

}
