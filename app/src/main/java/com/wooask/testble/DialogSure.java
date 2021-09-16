package com.wooask.testble;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


/**
 * @author vondear
 * @date 2016/7/19
 * 确认 弹出框
 */
public class DialogSure extends Dialog {

    private TextView tvCancelSave;

    public DialogSure(Context context, int themeResId) {
        super(context, themeResId);
        initView();
    }

    public DialogSure(Context context, boolean cancelable, DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        initView();
    }

    public DialogSure(Context context) {
        super(context);
        initView();
    }


    public TextView getCancelSaveView() {
        return tvCancelSave;
    }


    private void initView() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sure_false, null);
        dialogView.findViewById(R.id.tv_false).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvCancelSave = dialogView.findViewById(R.id.tvCancelSave);
        setContentView(dialogView);
    }

}
