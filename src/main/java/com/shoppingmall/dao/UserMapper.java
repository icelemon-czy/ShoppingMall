package com.shoppingmall.dao;

import com.shoppingmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkByName(String name);

    int checkByEmail(String email);

    User selectUserLogin(@Param("username") String name,@Param("password") String password);

    String selectQuestion(String username);

    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    int updatePassword(@Param("username") String username,@Param("newPassword") String newPassword);

    int checkPassword(@Param("password") String password,@Param("id") Integer id);

    int checkEmailByUserId(@Param("id") Integer id,@Param("email") String email);

}