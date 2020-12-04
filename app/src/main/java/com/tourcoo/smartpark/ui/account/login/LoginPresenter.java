package com.tourcoo.smartpark.ui.account.login;

import com.tourcoo.smartpark.bean.BaseResult;
import com.tourcoo.smartpark.bean.account.ParkingInfo;
import com.tourcoo.smartpark.bean.account.TokenInfo;
import com.tourcoo.smartpark.bean.account.UserInfo;
import com.tourcoo.smartpark.core.base.mvp.BasePresenter;
import com.tourcoo.smartpark.core.control.RequestConfig;
import com.tourcoo.smartpark.core.retrofit.BaseLoadingObserver;
import com.tourcoo.smartpark.core.retrofit.BaseObserver;
import com.tourcoo.smartpark.core.utils.ToastUtil;

import java.util.List;

/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月24日9:29
 * @Email: 971613168@qq.com
 */
public class LoginPresenter extends BasePresenter<LoginModel, LoginContract.LoginView> implements LoginContract.LoginPresenter {

    @Override
    protected LoginModel createModule() {
        return new LoginModel(bindUntilEvent());
    }

    @Override
    public void start() {

    }


    @Override
    public void getParkingList(String account) {
        if (isViewDetached()) {
            return;
        }
        getModel().requestParkingInfo(new BaseLoadingObserver<BaseResult<List<ParkingInfo>>>() {
            @Override
            public void onRequestSuccess(BaseResult<List<ParkingInfo>> entity) {
                if (entity == null) {
                    return;
                }
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS) {
                    getView().showParkingList(entity.getData());
                } else {
                    ToastUtil.showNormal(entity.getErrMsg());
                }
            }
        }, account);
    }

    @Override
    public void requestLogin(String account, String pass, int parkingId) {
        if (isViewDetached()) {
            return;
        }
        getModel().requestLogin(new BaseObserver<BaseResult<TokenInfo>>() {
            @Override
            public void onRequestSuccess(BaseResult<TokenInfo> entity) {
                if (entity == null) {
                    return;
                }
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS && entity.getData() != null) {
                    getView().loginSuccess(entity.getData());
                } else {
                    ToastUtil.showNormal(entity.getErrMsg());
                    getView().loginFailed();
                }
            }

            @Override
            public void onRequestError(Throwable throwable) {
                super.onRequestError(throwable);
                getView().loginFailed();
            }
        }, account, pass, parkingId);
    }

    @Override
    public void requestUserInfo() {
        if (isViewDetached()) {
            return;
        }
        getModel().requestUserInfo(new BaseObserver<BaseResult<UserInfo>>() {
            @Override
            public void onRequestSuccess(BaseResult<UserInfo> entity) {
                if (entity == null) {
                    return;
                }
                if (entity.getCode() == RequestConfig.REQUEST_CODE_SUCCESS && entity.getData() != null) {
                    getView().showUserInfo(entity.getData());
                } else {
                    getView().closeLoadingDialog();
                    ToastUtil.showFailed(entity.getErrMsg());
                }
            }
        });
    }


}
