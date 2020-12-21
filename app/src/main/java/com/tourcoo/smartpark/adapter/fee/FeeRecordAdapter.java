package com.tourcoo.smartpark.adapter.fee;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.fee.DailyFeeRecord;
import com.tourcoo.smartpark.constant.ParkConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.util.StringUtil;

import static com.tourcoo.smartpark.constant.PayConstant.PAY_TYPE_ALI;
import static com.tourcoo.smartpark.constant.PayConstant.PAY_TYPE_CASH;
import static com.tourcoo.smartpark.constant.PayConstant.PAY_TYPE_WEI_XIN;

/**
 * @author :JenkinsZhou
 * @description : 费用列表适配器
 * @company :途酷科技
 * @date 2020年12月09日11:22
 * @Email: 971613168@qq.com
 */
public class FeeRecordAdapter extends BaseQuickAdapter<DailyFeeRecord, BaseViewHolder> {
    public FeeRecordAdapter() {
        super(R.layout.item_record_arrears_pay);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, DailyFeeRecord item) {
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
                tvPlantNum.setBackground(CommonUtil.getDrawable(R.drawable.bg_radius_30_green_4ebf8b));
                break;
        }
        tvPlantNum.setText(StringUtil.getNotNullValueLine(item.getCarNumber()));
        helper.setText(R.id.tvFeeShould, "¥ " + item.getFee());
        helper.setText(R.id.tvParkingName, item.getParking());
        helper.setText(R.id.tvParkingDuration, item.getDuration());
        ImageView ivParkingType = helper.getView(R.id.ivParkingType);
        switch (item.getPayType()) {
            case PAY_TYPE_ALI:
                ivParkingType.setImageResource(R.mipmap.ic_pay_type_ali);
                break;
            case PAY_TYPE_WEI_XIN:
                ivParkingType.setImageResource(R.mipmap.ic_pay_type_we_chat);
                break;
            case PAY_TYPE_CASH:
                ivParkingType.setImageResource(R.mipmap.ic_pay_type_cash);
                break;
            default:

                break;
        }
    }
}
