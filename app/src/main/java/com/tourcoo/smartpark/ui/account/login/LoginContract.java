package com.tourcoo.smartpark.ui.account.login;

import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.TokenInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.core.base.mvp.IBaseView;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.trello.rxlifecycle3.LifecycleTransformer;

import java.util.List;

/**
 * @author :JenkinsZhou
 * @description : LoginContract
 * @company :途酷科技
 * @date 2020年11月23日18:06
 * @Email: 971613168@qq.com
 */
public class LoginContract {

    interface LoginModel  {
        /**
         * 获取停车场列表
         * @param observer
         * @param account
         */
        void requestParkingInfo(BaseObserver<BaseResult<List<ParkingInfo>>> observer, String account);

        /**
         * 登录
         * @param observer
         * @param account
         */
        void requestLogin(BaseObserver<BaseResult<TokenInfo>> observer, String account,String pass,int parkingId);


        void requestUserInfo(BaseObserver<BaseResult<UserInfo>> observer);
    }

    interface LoginView extends IBaseView {

     void showParkingList(List<ParkingInfo> parkingInfoList);
     void loginSuccess(TokenInfo tokenInfo);
        void loginFailed();
     void showUserInfo(UserInfo userInfo);
    }

    interface LoginPresenter {
        void getParkingList(String account);
        void requestLogin(String account,String pass,int parkingId);
        void requestUserInfo();
    }
}
