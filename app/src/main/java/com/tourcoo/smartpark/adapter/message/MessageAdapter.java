package com.tourcoo.smartpark.adapter.message;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.message.MessageInfo;
import com.tourcoo.smartpark.constant.ParkConstant;
import com.tourcoo.smartpark.core.CommonUtil;
import com.tourcoo.smartpark.util.StringUtil;

/**
 * @author :JenkinsZhou
 * @description : 消息适配器
 * @company :途酷科技
 * @date 2020年12月30日15:13
 * @Email: 971613168@qq.com
 */
public class MessageAdapter extends BaseQuickAdapter<MessageInfo, BaseViewHolder> {
    public MessageAdapter() {
        super(R.layout.item_message);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, MessageInfo item) {
        helper.setText(R.id.tvParkNumber, item.getNumber());
        helper.setText(R.id.tvTimeBegin, item.getCreatedAt());
        helper.setText(R.id.tvTimeNotify, item.getLeaveAt());
        helper.setText(R.id.tvFeeShould, item.getFee()+"元");
        helper.setText(R.id.tvFeeReal, item.getRealFee()+"元");
        helper.addOnClickListener(R.id.tvFeeCalculateContinue,R.id.tvFeeCalculateEnd);
        TextView tvPlantNum = helper.getView(R.id.tvPlantNum);
        tvPlantNum.setText(StringUtil.getNotNullValueLine(item.getCarNumber()));
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
        ImageView ivCheck = helper.getView(R.id.ivSelect);
        if (item.isSelect()) {
            ivCheck.setImageResource(R.mipmap.ic_checked_blue_small);
        } else {
            ivCheck.setImageResource(R.mipmap.ic_checked_gray);
        }
    }
}
