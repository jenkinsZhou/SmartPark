package com.tourcoo.smartpark.widget.selecter;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.luck.picture.lib.entity.LocalMedia;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.manager.GlideManager;


/**
 * @author :JenkinsZhou
 * @description : JenkinsZhou
 * @company :途酷科技
 * @date 2020年11月13日14:53
 * @Email: 971613168@qq.com
 */
public class PhotoAdapter extends BaseQuickAdapter<LocalMedia, BaseViewHolder> {
    public PhotoAdapter() {
        super(R.layout.item_grid_image);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, LocalMedia item) {
        if (!TextUtils.isEmpty(item.getRealPath())) {
            GlideManager.loadRoundImg(item.getRealPath(), helper.getView(R.id.ivLocalPhoto));
        }
        helper.addOnClickListener(R.id.ivDelete, R.id.ivLocalPhoto);
    }
}
