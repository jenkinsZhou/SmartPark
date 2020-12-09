package com.tourcoo.smartpark.adapter.fee;

import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.fee.ArrearsRecord;
import com.tourcoo.smartpark.core.manager.GlideManager;
import com.tourcoo.smartpark.util.StringUtil;

/**
 * @author :JenkinsZhou
 * @description : 欠费历史
 * @company :途酷科技
 * @date 2020年12月07日10:25
 * @Email: 971613168@qq.com
 */
public class ArrearsRecordHistoryAdapter extends BaseQuickAdapter<ArrearsRecord, BaseViewHolder> {
    public ArrearsRecordHistoryAdapter() {
        super(R.layout.item_record_arrears_history);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, ArrearsRecord item) {
        helper.setText(R.id.tvParkingName, StringUtil.getNotNullValue(item.getParking()));
        helper.setText(R.id.tvCarEnterTime, StringUtil.getNotNullValue(item.getCreatedAt()));
        helper.setText(R.id.tvCarExitTime, StringUtil.getNotNullValue(item.getLeaveAt()));
        helper.setText(R.id.tvParkingTime, StringUtil.getNotNullValue(item.getDuration()));
        helper.setText(R.id.tvParkingFee, StringUtil.getNotNullValue("¥ " + item.getFee()));
        ImageView ivCheck = helper.getView(R.id.ivSelect);
        if (item.isSelect()) {
            ivCheck.setImageResource(R.mipmap.ic_checked_blue_small);
        } else {
            ivCheck.setImageResource(R.mipmap.ic_checked_gray);
        }

    }
}
