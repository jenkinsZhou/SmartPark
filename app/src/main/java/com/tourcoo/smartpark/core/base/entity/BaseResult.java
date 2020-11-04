package com.tourcoo.smartpark.core.base.entity;

/**
 * @author :JenkinsZhou
 * @description :后台返回数据泛型实体类
 * @company :途酷科技
 * @date 2020年10月29日10:55
 * @Email: 971613168@qq.com
 */
public class BaseResult<T> {
    public int code;
    public String msg;
    public T data;
}
