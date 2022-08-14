package com.shoppingmall.controller.backend;

import com.shoppingmall.common.Const;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.User;
import com.shoppingmall.service.IUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/manage/user")
public class UserManageController {
    @Autowired
    private IUserService iUserService;
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            User user = response.getData();
            if(user.getRole() == Const.Role.Role_Admin){
                session.setAttribute(Const.Current_User,user);
                return response;
            }else{
                return ServerResponse.createByErrorMessage("User is not manager!");
            }
        }
        return response;
    }

}
