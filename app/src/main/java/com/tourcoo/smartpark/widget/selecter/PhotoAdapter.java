package com.tourcoo.smartpark.widget.selecter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.bean.LocalImage;
import com.tourcoo.smartpark.core.manager.GlideManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.pictureselector.adapter
 * email：893855882@qq.com
 * data：16/7/27
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    public static final int TYPE_CAMERA = 1;
    public static final int TYPE_PICTURE = 2;
    private LayoutInflater mInflater;
    private List<LocalImage> list = new ArrayList<>();
    private int selectMax = 6;
    private Context context;
    /**
     * 点击添加图片跳转
     */
    private onAddPicClickListener mOnAddPicClickListener;

    public interface onAddPicClickListener {
        void onAddPicClick();
    }

    public PhotoAdapter(Context context, onAddPicClickListener mOnAddPicClickListener) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        this.mOnAddPicClickListener = mOnAddPicClickListener;
    }

    public void setSelectMax(int selectMax) {
        this.selectMax = selectMax;
    }

    public void setList(List<LocalImage> list) {
        this.list = list;
    }

    public List<LocalImage> getList() {
        return list;
    }

    public List<String> getServiceUrlList() {
        List<String> urlList = new ArrayList<>();
        if (list == null) {
            return urlList;
        }
        for (LocalImage localImage : list) {
            if (localImage != null && !TextUtils.isEmpty(localImage.getServiceImageUrl())) {
                urlList.add(localImage.getServiceImageUrl());
            }
        }
        return urlList;
    }

  /*  public String getJsonPaths() {
        List<String> paths = new ArrayList<>();
        for (String media : list) {
            if (media.isCompressed() || (media.isCut() && media.isCompressed())) {
                // 压缩过,或者裁剪同时压缩过,以最终压缩过图片为准
                paths.add(media.getCompressPath());
            } else {
                // 原图
                paths.add(media.getPath());
            }
        }
        return new Gson().toJson(paths);
    }*/

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivDelete;
        ImageView ivLocalPhoto;

        public ViewHolder(View view) {
            super(view);
            ivDelete = view.findViewById(R.id.ivDelete);
            ivLocalPhoto = view.findViewById(R.id.ivLocalPhoto);
        }
    }

    @Override
    public int getItemCount() {
        if (list.size() < selectMax) {
            return list.size() + 1;
        } else {
            return list.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowAddItem(position)) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PICTURE;
        }
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = mInflater.inflate(R.layout.item_grid_image, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    private boolean isShowAddItem(int position) {
        int size = list.size();
        return position == size;
    }

    /**
     * 设置值
     */
    @Override
    public void onBindViewHolder(@NotNull final ViewHolder viewHolder, final int position) {
        //少于8张，显示继续添加的图标
        if (getItemViewType(position) == TYPE_CAMERA) {
            viewHolder.ivLocalPhoto.setImageResource(R.mipmap.ic_image_add);
            viewHolder.ivLocalPhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            viewHolder.ivLocalPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAddPicClickListener.onAddPicClick();
                }
            });
            viewHolder.ivDelete.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.ivDelete.setVisibility(View.VISIBLE);
            viewHolder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int index = viewHolder.getAdapterPosition();
                    if (mOnItemDeleteClickListener != null) {
                        mOnItemDeleteClickListener.onItemDelete(index, view);
                    }
                    // 这里有时会返回-1造成数据下标越界,具体可参考getAdapterPosition()源码，
                    // 通过源码分析应该是bindViewHolder()暂未绘制完成导致，知道原因的也可联系我~感谢
                    if (index != RecyclerView.NO_POSITION) {
                        list.remove(index);
                        notifyItemRemoved(index);
                        notifyItemRangeChanged(index, list.size());
                    }
                }
            });
            String imagePath = list.get(position).getLocalImagePath();
            GlideManager.loadRoundImg(imagePath, viewHolder.ivLocalPhoto);
            //itemView 的点击事件
            if (mItemClickListener != null) {
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int adapterPosition = viewHolder.getAdapterPosition();
                        mItemClickListener.onItemClick(adapterPosition, v);
                    }
                });
            }
        }
    }

    protected OnItemClickListener mItemClickListener;

    private OnItemDeleteClickListener mOnItemDeleteClickListener;


    public void setOnItemDeleteClickListener(OnItemDeleteClickListener mOnItemDeleteClickListener) {
        this.mOnItemDeleteClickListener = mOnItemDeleteClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }

    public interface OnItemDeleteClickListener {
        void onItemDelete(int position, View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void addData(LocalImage localImage) {
        if (localImage == null) {
            return;
        }
        if (hasOrcPhoto()) {
            //说明当前有拍照识别照片 需要替换 而不是追加
            if (localImage.isRecognize()) {
                //这里直接替换第一张图 因为默认第一张图就是识别的照片
                list.set(0, localImage);
            } else {
                //如果当前图片不是识别照片 则直接添加
                list.add(localImage);
            }
        } else {
            //没有拍照识别的照片
            if (localImage.isRecognize()) {
                //这里直接追加到第一张图
                list.add(0, localImage);
            } else {
                //如果当前图片不是识别照片 则直接添加
                list.add(localImage);
            }
        }
        this.notifyDataSetChanged();
    }


    public boolean hasOrcPhoto() {
        for (LocalImage localImage : list) {
            if (localImage == null) {
                continue;
            }
            if (localImage.isRecognize()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取orc照片的位置
     *
     * @return
     */
    public int getOrcPhotoIndex() {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null) {
                continue;
            }
            if (list.get(i).isRecognize() && list.get(i).getLocalImagePath() != null) {
                return i;
            }
        }
        return -1;
    }

}