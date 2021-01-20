package com.tourcoo.smartpark.widget.keyboard;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.MediaScannerConnection;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apkfuns.logutils.LogUtils;
import com.tourcoo.smartpark.R;
import com.tourcoo.smartpark.core.utils.FileUtil;
import com.tourcoo.smartpark.core.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public class PlateKeyboardView implements View.OnClickListener {
    public static final String TAG = "PlateKeyboardView";
    private Activity mContext;
    private View parentView;
    private EditText insideEditText;
    private KeyboardView mLetterView;   //字母键盘view
    private KeyboardView mNumberView;   //数字键盘View
    private Keyboard mNumberKeyboard;   // 数字键盘
    private Keyboard mLetterKeyboard;   // 字母键盘
    private Keyboard mSymbolKeyboard;   // 中文键盘
    protected Dialog popWindow;
    private boolean isNumber = false;    // 是否数字键盘
    private boolean isSymbol = false;   // 是否是中文
    private View headerView;
    private TextView finishTextView;
    private String[] provinceShort = new String[]{"京", "津", "冀", "鲁", "晋", "蒙", "辽", "吉", "黑", "沪", "苏", "浙", "皖", "闽", "赣", "豫", "鄂", "湘", "粤", "桂", "渝", "川",
            "贵", "云", "藏", "陕", "甘", "青", "琼", "新", "港", "澳", "台", "宁"};
    private TextView[] tvList;
    private EditText mEditText;
    private boolean isTvList;
    private int currentEditTextPosition = 0;//默认当前光标在第一个TextView
    private OnKeyboardFinishListener onKeyboardFinishListener;
    private PlantLayout inputLayout;
    private boolean autoShowProvince = true;
    public void setOnKeyboardFinishListener(OnKeyboardFinishListener onKeyboardFinishListener) {
        this.onKeyboardFinishListener = onKeyboardFinishListener;
    }

    public void setOnKeyboardFinishListener(OnKeyboardFinishListener onKeyboardFinishListener, String finishText) {
        this.onKeyboardFinishListener = onKeyboardFinishListener;
        if (finishTextView != null && finishText != null) {
            finishTextView.setText(finishText);
        }
    }

    public void setEditText(EditText text) {
        mEditText = text;
    }

    public void setTvList(TextView[] tvList) {
        this.tvList = tvList;
    }

    public PlateKeyboardView(Activity context) {
        mContext = context;
        init();
        mNumberKeyboard = new Keyboard(mContext, R.xml.keyboard_numbers);
        mLetterKeyboard = new Keyboard(mContext, R.xml.keyboard_word);
        mSymbolKeyboard = new Keyboard(mContext, R.xml.keyboard_province);
        mNumberView = (KeyboardView) parentView.findViewById(R.id.keyboard_view);
        mLetterView = (KeyboardView) parentView.findViewById(R.id.keyboard_view_2);
        insideEditText = (EditText) parentView.findViewById(R.id.text);

        mNumberView.setKeyboard(mNumberKeyboard);
        mNumberView.setEnabled(true);
        mNumberView.setPreviewEnabled(false);
        mNumberView.setOnKeyboardActionListener(listener);
        mLetterView.setKeyboard(mLetterKeyboard);
        mLetterView.setEnabled(true);
        mLetterView.setPreviewEnabled(true);
        mLetterView.setOnKeyboardActionListener(listener);
        headerView = parentView.findViewById(R.id.keyboard_header);

    }

    private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {
            if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                mLetterView.setPreviewEnabled(false);
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                mLetterView.setPreviewEnabled(false);
            } else if (primaryCode == 32) {
                mLetterView.setPreviewEnabled(false);
            } else {
                mLetterView.setPreviewEnabled(true);
            }

        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            try {
                if (mEditText == null && tvList == null) {
                    return;
                }
                if (isTvList) {
                    if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                        // 隐藏键盘
                        hideKeyboard();
                    } else if (primaryCode == Keyboard.KEYCODE_DELETE || primaryCode == -35) {
                        if (currentEditTextPosition > tvList.length - 1) {
                            currentEditTextPosition = tvList.length - 1;
                        }
                        if (TextUtils.isEmpty(tvList[currentEditTextPosition].getText().toString())) {
                            // 回退键,删除字符
                            currentEditTextPosition--;
                        }
                        if (currentEditTextPosition < 0) {
                            currentEditTextPosition = 0;
                        }
                        tvList[currentEditTextPosition].setText("");
                        //将当前tv置为""并currentEditText-1
                        if (currentEditTextPosition < 1 && autoShowProvince) {
                            //切换为省份简称键盘
                            showSymbolView();
                        }
                        if (currentEditTextPosition < 0) {
                            currentEditTextPosition = 0;
                        }
                        setText(tvList);
                        if (inputLayout != null) {
                            inputLayout.setTextViewsBackground(currentEditTextPosition);
                        }
                    } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                        // 数字与字母键盘互换
                        if (isNumber) {
                            showLetterView();
                            showLetterView2();
                        } else {
                            showNumberView();
                        }
                    } else if (primaryCode == 90001) {
//                  字母与中文切换
                        if (isSymbol) {
//                            showLetterView();
                            showLetterView2();
                        } else {
                            showSymbolView();
                        }
                    } else {
                        if (currentEditTextPosition == 0 && autoShowProvince) {
                            //如果currentEditText==0代表当前为省份键盘,
                            //currentEditText+1
                            if (isSymbol) {//中文
                                tvList[0].setText(provinceShort[primaryCode]);
                            } else {
                                tvList[0].setText(Character.toString((char) primaryCode));
                            }
                            currentEditTextPosition = 1;
                            //切换为字母数字键盘
                            showLetterView2();
                        } else {
//                            //第二位必须大写字母
//                            if (currentEditText == 1 && !Character.toString((char) primaryCode).matches("[A-Z]{1}")) {
//                                return;
//                            }
                            if (currentEditTextPosition >= tvList.length - 1) {
                                currentEditTextPosition = tvList.length - 1;
                            }
                            if (isSymbol) {//中文
                                tvList[currentEditTextPosition].setText(provinceShort[primaryCode]);
                            } else {
                                tvList[currentEditTextPosition].setText(Character.toString((char) primaryCode));
                            }
                            currentEditTextPosition++;
                        }
                        setText(tvList);
                        if (inputLayout != null && currentEditTextPosition < tvList.length) {
//                            plateView.setTextViewsBackground(currentEditText);
                            inputLayout.setTextViewsBackground(currentEditTextPosition);
                        }
                    }
                } else {
                    if(mEditText == null){
                        return;
                    }
                    Editable editable = mEditText.getText();
                    int start = mEditText.getSelectionStart();
                    if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                        // 隐藏键盘
                        hideKeyboard();
                    } else if (primaryCode == Keyboard.KEYCODE_DELETE || primaryCode == -35) {
                        // 回退键,删除字符
                        if (editable != null && editable.length() > 0) {
                            if (start > 0) {
                                editable.delete(start - 1, start);
                                insideEditText.setText(editable.toString());
                            }
                        }
                        if (editable != null && editable.toString().length() < 1 && autoShowProvince) {
                            showSymbolView();
                        }
                    } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                        // 数字与字母键盘互换
                        if (isNumber) {
//                            showLetterView();
                            showLetterView2();
                        } else {
                            showNumberView();
                        }
                    } else if (primaryCode == 90001) {
//                  字母与中文切换
                        if (isSymbol) {
//                            showLetterView();
                            showLetterView2();
                        } else {
                            showSymbolView();
                        }
                    } else {
                        // 输入键盘值
                        if (isSymbol) {//中文
                            editable.insert(start, provinceShort[primaryCode]);
                        } else {
                            editable.insert(start, Character.toString((char) primaryCode));
                        }
                        insideEditText.setText(editable.toString());
                        if (editable.toString().length() >= 1) {
                            if (isSymbol) {
                                showLetterView();
                                showLetterView2();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.tag(TAG).e(e.toString());
            }

        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    };

    //  字母-中文,显示字母
    private void showLetterView2() {
        if (mLetterView != null) {
            popWindow.show();
            headerView.setVisibility(View.VISIBLE);
            mLetterView.setVisibility(View.VISIBLE);
            mNumberView.setVisibility(View.INVISIBLE);
            isSymbol = false;
            isNumber = false;
            mLetterView.setKeyboard(mLetterKeyboard);
        }
    }

    //  字母-中文,显示中文
    private void showSymbolView() {
        try {
            if (mLetterKeyboard != null) {
                popWindow.show();
                headerView.setVisibility(View.VISIBLE);
                mLetterView.setVisibility(View.VISIBLE);
                mNumberView.setVisibility(View.INVISIBLE);
                isSymbol = true;
                mLetterView.setKeyboard(mSymbolKeyboard);
            }
        } catch (Exception e) {
            LogUtils.tag(TAG).e(e.toString());
        }
    }

    //  数字-字母,显示字母键盘
    private void showLetterView() {
        try {
            popWindow.show();
            if (mLetterView != null && mNumberView != null) {
                isNumber = false;
                headerView.setVisibility(View.VISIBLE);
                mLetterView.setVisibility(View.VISIBLE);
                mNumberView.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
        }

    }

    // 数字-字母, 显示数字键盘
    private void showNumberView() {
        try {
            if (mLetterView != null && mNumberView != null) {
                popWindow.show();
                isNumber = true;
                headerView.setVisibility(View.VISIBLE);
                mLetterView.setVisibility(View.INVISIBLE);
                mNumberView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
        }

    }


    /**
     * 判断是否是字母
     */
    private boolean isLetter(String str) {
        String wordStr = "abcdefghijklmnopqrstuvwxyz";
        return wordStr.contains(str.toLowerCase());
    }

    public void hideKeyboard() {
        try {
            int visibility = mLetterView.getVisibility();
            if (visibility == View.VISIBLE) {
                headerView.setVisibility(View.GONE);
                mLetterView.setVisibility(View.GONE);
            }
            visibility = mNumberView.getVisibility();
            if (visibility == View.VISIBLE) {
                headerView.setVisibility(View.GONE);
                mNumberView.setVisibility(View.GONE);
            }
            if (popWindow != null && popWindow.isShowing()) {
                popWindow.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
        }

    }


    public void showKeyboard(TextView[] tvList) {
        isTvList = true;
        this.tvList = tvList;
//        for (int i = 0; i < tvList.length; i++) {
//            if (TextUtils.isEmpty(tvList[i].getText().toString())) {
//                currentEditTextPosition = i;
//                if (inputLayout != null) {
//                    inputLayout.setTextViewsBackground(currentEditTextPosition);
//                }
//                break;
//            }
//        }
        if (currentEditTextPosition == 0&&autoShowProvince) {
            showSymbolView();
        } else {
            showLetterView2();
        }

    }

    /**
     * 显示键盘
     *
     * @param editText
     */
    public void showKeyboard(EditText editText) {
        showKeyboard(editText, -1);
    }

    public void showKeyboard(EditText editText, int inputType) {
        try {
            isTvList = false;
            this.mEditText = editText;
            int inputText;
            if (inputType < 0) {
                inputText = mEditText.getInputType();
            } else {
                inputText = inputType;
            }
            headerView.setVisibility(View.VISIBLE);
            switch (inputText) {
                case InputType.TYPE_CLASS_NUMBER:
                    showNumberView();
                    break;
                case InputType.TYPE_CLASS_PHONE:
                case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                case InputType.TYPE_NUMBER_FLAG_DECIMAL:
//                    showLetterView();
                    showLetterView2();
                    break;
                default:
                    showSymbolView();
//                    showLetterView();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
        }
    }

    public void showKeyboard() {
        try {
            headerView.setVisibility(View.VISIBLE);
//            showSymbolView();
            showNumberView();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
        }


    }

    private void setText(TextView[] tvList) {
        String temp = "";
        for (int i = 0; i < tvList.length; i++) {
            temp += tvList[i].getText().toString();
        }
        insideEditText.setText(temp);
     /*   // 保证光标始终在最后面
        insideEditText.setSelection(temp.length());*/
    }





   /* public void showKeyboard(final LicensePlateView plateView) {
//
        showKeyboard(plateView.getTextViews());
        plateView.setOnSelect(new LicensePlateView.OnSelect() {
            @Override
            public void select(int select) {
                currentEditText = select;
                plateView.setTextViewsBackground(currentEditText);
            }
        });
    }*/


    public String getInput() {
        return insideEditText.getText().toString();
    }

    private void init() {
        if (popWindow == null) {
            parentView = LayoutInflater.from(mContext).inflate(R.layout.keyboard_key_board_popu, null);
            popWindow = new Dialog(mContext, R.style.keyboard_popupAnimation);
            finishTextView = parentView.findViewById(R.id.keyboard_finish);
            finishTextView.setOnClickListener(this);
            parentView.findViewById(R.id.keyboard_back_hide).setOnClickListener(this);
        }
        popWindow.setContentView(parentView);
        popWindow.setCanceledOnTouchOutside(true);
        Window mWindow = popWindow.getWindow();
        if (mWindow != null) {
            //必须加这两句代码
            mWindow.getDecorView().setDrawingCacheEnabled(true);
            mWindow.getDecorView().buildDrawingCache();
            mWindow.setWindowAnimations(R.style.keyboard_popupAnimation);
            mWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            mWindow.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
            mWindow.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        popWindow.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
              /*  if (contentView != null && contentView.isFocused()) {
                    contentView.clearFocus();
                }*/
            }
        });
    }

    @Override
    public void onClick(View v) {
        try {
            int i = v.getId();
            if (i == R.id.keyboard_finish) {
                if (onKeyboardFinishListener != null) {
                    onKeyboardFinishListener.onFinish(getInput());
                }
                hideKeyboard();
            } else if (i == R.id.keyboard_back_hide) {
                hideKeyboard();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.tag(TAG).e(e.toString());
        }
    }

    public interface OnKeyboardFinishListener {
        void onFinish(String input);
    }


    public void showKeyboard(final PlantLayout inputLayout) {
        this.inputLayout = inputLayout;
        showKeyboard(inputLayout.getTextViews());
        inputLayout.setOnSelect(select -> {
            currentEditTextPosition = select;
            inputLayout.setTextViewsBackground(currentEditTextPosition);
        });
    }

    public void showKeyboardNew(TextView[] tvList) {
        isTvList = true;
        this.tvList = tvList;
        for (int i = 0; i < tvList.length; i++) {
            if (TextUtils.isEmpty(tvList[i].getText().toString())) {
                currentEditTextPosition = i;
                if (inputLayout != null) {
                    inputLayout.setTextViewsBackground(currentEditTextPosition);
                }
                break;
            }
        }
        if (currentEditTextPosition == 0 && autoShowProvince) {
            showSymbolView();
        } else {
            showLetterView();
            showLetterView2();
        }
    }

    public boolean isAutoShowProvince() {
        return autoShowProvince;
    }

    public void setAutoShowProvince(boolean autoShowProvince) {
        this.autoShowProvince = autoShowProvince;
    }


    //将图像保存到SD卡中
    public String saveMyBitmap(){
        String currentPath= FileUtil.getExternalFilesDir() +"/" +System.currentTimeMillis() + ".jpg";
        File f = new File(currentPath);
        try {
            f.createNewFile();
        } catch (IOException e) {
            LogUtils.e("saveMyBitmap："+e.toString());
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("saveMyBitmap："+e.toString());
        }
        //获取bitmap关键代码
        View dialogView = popWindow.getWindow().getDecorView();
        Bitmap dialogBitmap=dialogView.getDrawingCache();
        dialogBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtils.e("saveMyBitmap："+e.toString());
        }
        MediaScannerConnection.scanFile(mContext, new String[]{currentPath}, null, null);
        return currentPath;
    }
}
