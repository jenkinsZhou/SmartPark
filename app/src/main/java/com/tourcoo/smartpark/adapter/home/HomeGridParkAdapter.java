package com.tourcoo.smartpark.adapter.home;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.core.manager.GlideManager;

/**
 * @author :JenkinsZhou
 * @description :首页网格布局
 * @company :途酷科技
 * @date 2020年11月04日16:04
 * @Email: 971613168@qq.com
 */
public class HomeGridParkAdapter extends BaseQuickAdapter<ParkSpaceInfo, BaseViewHolder> {
    public HomeGridParkAdapter() {
        super(R.layout.item_car_info_grid_layout);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ParkSpaceInfo item) {
        ImageView imageView = helper.getView(R.id.ivParkingStatus);
        switch (item.getUsed()) {
            case 0:
                GlideManager.loadImgAuto(R.mipmap.ic_parking_gray,imageView);
                break;
            default:
                GlideManager.loadImgAuto(R.mipmap.ic_car_gray_small,imageView);
//                GlideManager.loadImg(R.mipmap.ic_parking_gray,helper.getView(R.id.ivParkingStatus));
//                GlideManager.loadImg(ContextCompat.getDrawable(mContext,R.mipmap.ic_car_gray_small),helper.getView(R.id.ivParkingStatus));
                break;
        }
        helper.setText(R.id.tvParkingNum,item.getParkingNumber());
        helper.setText(R.id.tvPlantNum,item.getCarNumber());
    }
}
