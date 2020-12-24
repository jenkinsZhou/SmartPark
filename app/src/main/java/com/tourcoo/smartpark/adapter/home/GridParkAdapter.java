package com.tourcoo.smartpark.adapter.home;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.ParkSpaceInfo;
import com.tourcoo.smartpark.constant.ParkConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.core.manager.GlideManager;

/**
 * @author :JenkinsZhou
 * @description :首页网格布局
 * @company :途酷科技
 * @date 2020年11月04日16:04
 * @Email: 971613168@qq.com
 */
public class GridParkAdapter extends BaseQuickAdapter<ParkSpaceInfo, BaseViewHolder> {
    public GridParkAdapter() {
        super(R.layout.item_car_info_grid_layout);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ParkSpaceInfo item) {
        ImageView imageView = helper.getView(R.id.ivParkingStatus);
        switch (item.getUsed()) {
            case 0:
                GlideManager.loadImgAuto(R.mipmap.ic_parking_gray,imageView);
                helper.setVisible(R.id.tvPlantNum,false);
                break;
            default:
                GlideManager.loadImgAuto(R.mipmap.ic_car_gray_small,imageView);
                helper.setVisible(R.id.tvPlantNum,true);
                helper.setText(R.id.tvPlantNum,item.getCarNumber());
                break;
        }
        TextView tvPlantNum = helper.getView(R.id.tvPlantNum);
        helper.setText(R.id.tvPlantNum,item.getCarNumber());
        helper.setText(R.id.tvParkingNum,item.getNumber());

        switch (item.getType()) {
            case ParkConstant.CAR_TYPE_NORMAL:
                tvPlantNum.setBackground(CommonUtil.getDrawable(R.drawable.bg_radius_30_blue_5087ff));
                break;
            case ParkConstant.CAR_TYPE_YELLOW:
                tvPlantNum.setBackground(CommonUtil.getDrawable(R.drawable.bg_radius_30_yellow_fbc95f));
                break;
            case ParkConstant.CAR_TYPE_GREEN:
                tvPlantNum.setBackground(CommonUtil.getDrawable(R.drawable.shape_gradient_radius_30_green_4ebf8b));
                break;
        }

    }
}
