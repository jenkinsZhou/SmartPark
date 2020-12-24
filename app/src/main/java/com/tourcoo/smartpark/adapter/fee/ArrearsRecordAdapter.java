package com.tourcoo.smartpark.adapter.fee;

import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.fee.ArrearsRecord;
import com.tourcoo.smartpark.constant.ParkConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.util.StringUtil;


/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年12月10日13:52
 * @Email: 971613168@qq.com
 */
public class ArrearsRecordAdapter extends BaseQuickAdapter<ArrearsRecord, BaseViewHolder> {
    public ArrearsRecordAdapter() {
        super(R.layout.item_record_arrears);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ArrearsRecord item) {
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
        helper.setText(R.id.tvFeeShould, "¥ " + item.getFee());
        helper.setText(R.id.tvParkingName, item.getParking());
        helper.setText(R.id.tvParkingDuration, item.getDuration());
    }
}
