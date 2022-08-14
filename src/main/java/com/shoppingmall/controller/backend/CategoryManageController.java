package com.shoppingmall.controller.backend;

import com.shoppingmall.common.Const;
import com.shoppingmall.common.ResponseCode;
import com.shoppingmall.common.ServerResponse;
import com.shoppingmall.pojo.Category;
import com.shoppingmall.pojo.User;
import com.shoppingmall.service.ICategoryService;
import com.shoppingmall.service.IUserService;
import javax.servlet.http.HttpSession;
import net.sf.jsqlparser.schema.Server;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private ICategoryService iCategoryService;

    @RequestMapping(value = "addCategory.do")
    @ResponseBody
    public ServerResponse<String> addCategory(HttpSession session,String categoryName,@RequestParam(value = "parentId",defaultValue = "0") int parentId){
        // First check whether user is admin
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "User does not log in!");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.addCategory(categoryName,parentId);
        }else{
            return ServerResponse.createByErrorMessage("Unauthorized Operation!");
        }
    }

    @RequestMapping(value = "setCategoryName.do")
    @ResponseBody
    public ServerResponse<String> setCategoryName(HttpSession session,Integer id,String newCategoryName){
        // First check whether user is admin
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "User does not log in!");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.setCategory(newCategoryName, id);
        }else{
            return ServerResponse.createByErrorMessage("Unauthorized Operation!");
        }
    }
    @RequestMapping(value = "getCategory.do")
    @ResponseBody
    public ServerResponse getChildrenCategory(HttpSession session,@RequestParam(value = "id",defaultValue = "0") Integer id){
        // First check whether user is admin
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "User does not log in!");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.getChildrenCategory(id);
        }else{
            return ServerResponse.createByErrorMessage("Unauthorized Operation!");
        }
    }

    public ServerResponse getAllDescendantCategory(HttpSession session,@RequestParam(value = "id",defaultValue = "0") Integer parentId){
        User user = (User) session.getAttribute(Const.Current_User);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "User does not log in!");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iCategoryService.getAllDescendantCategory(parentId);
        }else{
            return ServerResponse.createByErrorMessage("Unauthorized Operation!");
        }
    }
}
