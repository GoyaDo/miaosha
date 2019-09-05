package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

/**
 * @author cj
 * @date 2019-08-28 - 12:53
 */
public interface UserService {

    //通过用于id获取用户对象的方法
    UserModel getUserById(Integer id);
    //用户注册请求
    void register(UserModel userModel) throws BusinessException;
    //用户登录
    /*
    telphone:用户注册手机
    password:用户加密后的密码
     */
    UserModel validateLogin(String telphone,String encrptPassword) throws BusinessException;
}
