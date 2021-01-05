package com.tourcoo.smartpark.adapter.fee;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.park.ParkSpaceInfo;
import com.tourcoo.smartpark.constant.ParkConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.util.StringUtil;

/**
 * @author :JenkinsZhou
 * @description : 待结算
 * @company :途酷科技
 * @date 2020年12月15日17:02
 * @Email: 971613168@qq.com
 */
public class WaitSettleAdapter extends BaseQuickAdapter<ParkSpaceInfo, BaseViewHolder> {
    public WaitSettleAdapter() {
        super(R.layout.item_order_wait_settle);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ParkSpaceInfo item) {
        helper.setText(R.id.tvParkNumber, item.getNumber());
        TextView tvPlantNum = helper.getView(R.id.tvPlantNum);
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
        tvPlantNum.setText(StringUtil.getNotNullValueLine(item.getCarNumber()));
        helper.addOnClickListener(R.id.tvPrintCertify, R.id.tvSettle);
        helper.setText(R.id.tvArriveTime, "入场时间:"+item.getCreatedAt());
        helper.setText(R.id.tvParkingName, item.getParking());
    }
}
