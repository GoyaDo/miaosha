package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPassMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPass;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.beans.Transient;

/**
 * @author cj
 * @date 2019-08-28 - 12:53
 */

//可以通过这个service获取用户的领域模型的对象
@Service
public class UserServiceImpl implements UserService {

    //这里报错但不影响正常运行，可以给UserDOMapper加上@Repository,标识这个mapper，但是已经开启了自动扫描，所以可以用标注
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPassMapper userPassMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取到对应用户dataobject
        //这个userdo不能后传给前端，需要加model
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);

        if (userDO == null) {
            return null;
        }
        //通过用户id获取对应的用户加密密码信息
        UserPass userPass = userPassMapper.selectByUserId(userDO.getId());
        return convertFromDataObject(userDO, userPass);
    }

    @Override
    @Transient
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if (StringUtils.isEmpty(userModel.getName())
//                || userModel.getGender() == null
//                || userModel.getAge() == null
//                || StringUtils.isEmpty(userModel.getTelphone())) {
//            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
        }


        //实现model->dataobject方法
        UserDO userDO = convertFromModel(userModel);
        try {
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已存在");
        }


        userModel.setId(userDO.getId());

        UserPass userPass = convertPasswordFromModel(userModel);
        userPassMapper.insertSelective(userPass);
        return;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过用户手机获取用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);

        if (userDO == null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPass userPass= userPassMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPass);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(encrptPassword,userModel.getEncrptPassword())){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userModel;
    }

    private UserPass convertPasswordFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserPass userPass = new UserPass();
        userPass.setEncrptPassword(userModel.getEncrptPassword());
        userPass.setUserId(userModel.getId());
        return userPass;
    }

    private UserDO convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }


    private UserModel convertFromDataObject(UserDO userDO, UserPass userPasswordDo) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        //把对应的userdo属性copy到usermodel
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDo != null) {
            //不能使用copy，里面的id字段是重复的
            userModel.setEncrptPassword(userPasswordDo.getEncrptPassword());
        }
        return userModel;
    }
}
