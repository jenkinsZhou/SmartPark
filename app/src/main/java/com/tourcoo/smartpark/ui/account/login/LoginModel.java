package com.tourcoo.smartpark.ui.account.login;

import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.TokenInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.core.base.mvp.BaseModel;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.retrofit.repository.ApiRepository;
import com.trello.rxlifecycle3.LifecycleTransformer;

import java.util.List;

/**
 * @author :JenkinsZhou
 * @description : 登录
 * @company :途酷科技
 * @date 2020年11月24日9:27
 * @Email: 971613168@qq.com
 */
@SuppressWarnings("unchecked")
public class LoginModel extends BaseModel implements LoginContract.LoginModel {
    public LoginModel(LifecycleTransformer lifecycleTransformer) {
        super(lifecycleTransformer);
    }



    @Override
    public void requestParkingInfo(BaseObserver<BaseResult<List<ParkingInfo>>> observer, String account) {
        ApiRepository.getInstance().requestParkingList(account).compose(getLifecycleTransformer()).subscribe(observer);
    }

    @Override
    public void requestLogin(BaseObserver<BaseResult<TokenInfo>> observer, String account, String pass, int parkingId) {
        ApiRepository.getInstance().requestLogin(account,pass,parkingId).compose(getLifecycleTransformer()).subscribe(observer);
    }

    @Override
    public void requestUserInfo(BaseObserver<BaseResult<UserInfo>> observer) {
        ApiRepository.getInstance().requestUserInfo().compose(getLifecycleTransformer()).subscribe(observer);
    }


}
