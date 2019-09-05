package com.miaoshaproject.error;

/**
 * @author cj
 * @date 2019-08-28 - 18:10
 */
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);

}
