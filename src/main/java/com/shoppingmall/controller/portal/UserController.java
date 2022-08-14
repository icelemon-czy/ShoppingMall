package com.shoppingmall.controller.portal;

import com.shoppingmall.common.Const;
import com.shoppingmall.common.ResponseCode;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.User;
import com.shoppingmall.service.IUserService;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handle url request
 */
@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> login_response = iUserService.login(username,password);
        if(login_response.isSuccess()){
            session.setAttribute(Const.Current_User,login_response.getData());
        }
        return login_response;
    }

    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.Current_User);
        return ServerResponse.createBySuccess("Log Out Success !");
    }

    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
         return iUserService.register(user);
    }

    @RequestMapping(value = "checkValid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type){
         return iUserService.checkValid(str,type);
    }

    @RequestMapping(value = "getUserInfo.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.Current_User);
        if(user!=null){
            return ServerResponse.createBySuccess(user);
        }else{
            return ServerResponse.createByErrorMessage("User does not log in !");
        }
    }

    @RequestMapping(value = "getQuestion.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> getQuestion(String username){
        return iUserService.getQuestion(username);
    }

    @RequestMapping(value = "checkAnswer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        return iUserService.checkAnswer(username,question,answer);
    }

    @RequestMapping(value = "forgetResetPassword.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String newPassword,String token){
        return iUserService.forgetResetPassword(username, newPassword, token);
    }

    @RequestMapping(value = "loginResetPassword.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> loginResetPassword(String oldPassword,String newPassword,HttpSession session){
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorMessage("User does not log in!");
        }
        return iUserService.loginResetPassword(oldPassword,newPassword,user);
    }

    @RequestMapping(value = "updateUserInfo.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session,User user){
        User currentUser = (User) session.getAttribute(Const.Current_User);
        if(currentUser == null){
            return ServerResponse.createByErrorMessage("User does not log in!");
        }
        // To avoid different user
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateUserInfo(user);
        if(response.isSuccess()){
            session.setAttribute(Const.Current_User,response.getData());
        }
        return response;
    }

    @RequestMapping(value = "getUserInformation.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInformation(HttpSession session){
        User user = (User) session.getAttribute(Const.Current_User);
        if(user==null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"User need to log in !");
        }
        return iUserService.getUserInfo(user.getId());
    }



}
