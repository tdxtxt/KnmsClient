package com.knms.view;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;

import com.knms.android.R;

public class PopDialogView extends Dialog {

	public PopDialogView(Context context) {
		this(context, R.style.Dialog);
		init();
	}
	public PopDialogView(Context context, int theme) {
        super(context, theme==0?R.style.Dialog:theme);
        init();
    }

	private void init(){
		Window window = getWindow();
		window.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
		setCancelable(true);
	}

}
