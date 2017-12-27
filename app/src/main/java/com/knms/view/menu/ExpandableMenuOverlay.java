package com.knms.view.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.knms.android.R;
import com.knms.util.LocalDisplay;
import com.knms.util.ScreenUtil;

/**
 * Created by Administrator on 2016/9/18.
 */
public class ExpandableMenuOverlay extends ImageButton implements DialogInterface.OnKeyListener, View.OnClickListener {

    private static final String TAG = "ExpandableMenuOverlay";

    private static final float DEFAULT_DIM_AMOUNT = 0.8f;

    private Dialog mDialog;
    private ExpandableButtonMenu mButtonMenu;

    private float dimAmount = DEFAULT_DIM_AMOUNT;
    private boolean mAdjustViewSize = true;

    protected boolean mDismissing;

    public ExpandableMenuOverlay(Context context) {
        this(context, null, 0);
    }

    public ExpandableMenuOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandableMenuOverlay(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExpandableMenuOverlay, 0, 0);
            try {
                mAdjustViewSize = a.getBoolean(R.styleable.ExpandableMenuOverlay_adjustViewSize, true);
                dimAmount = a.getFloat(R.styleable.ExpandableMenuOverlay_dimAmount, DEFAULT_DIM_AMOUNT);
            } finally {
                a.recycle();
            }
        }

        init(attrs);
    }

    public void init(AttributeSet attrs) {
        // We create a fake dialog which dims the screen and we display the expandable menu as content
        mDialog = new Dialog(getContext(), android.R.style.Theme_Translucent_NoTitleBar);
        mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
//        lp.alpha=0.5f;
        lp.dimAmount = dimAmount;
        mDialog.getWindow().setAttributes(lp);

        mButtonMenu = new ExpandableButtonMenu(getContext(), attrs);
        mButtonMenu.setButtonMenuParentOverlay(this);

        mDialog.setContentView(mButtonMenu);
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                setVisibility(View.INVISIBLE);
                mButtonMenu.toggle();
            }
        });

        // Catch events when keyboard button are clicked. Used to dismiss the menu
        // on 'back' button
        mDialog.setOnKeyListener(this);

        // Clicking this view will expand the button menu
        setOnClickListener(this);

    }

    /**
     * Show the dialog, dimming the screen and expanding the button menu
     */
    public void show() {
        mDialog.show();
    }
    public void hide(){
        if (mButtonMenu.isExpanded()) {
            mDismissing = true;
            mButtonMenu.toggle();
        }
    }
    /**
     * Dismiss the dialog, removing screen dim and hiding the expanded menu
     */
    public void dismiss() {
        mButtonMenu.setAnimating(false);
        mDialog.dismiss();
    }

    /**
     * Show the view that expands the button menu
     */
    public void showInitButton() {
        setVisibility(View.VISIBLE);
    }

    /**
     * Set a callback on expanded menu button clicks
     *
     * @param listener
     */
    public void setOnMenuButtonClickListener(ExpandableButtonMenu.OnMenuButtonClick listener) {
        mButtonMenu.setOnMenuButtonClickListener(listener);
    }

    /**
     * Get underlying expandable buttom menu
     *
     * @return
     */
    public ExpandableButtonMenu getButtonMenu() {
        return mButtonMenu;
    }


    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled() && !mDismissing) {
            hide();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == getId()) {
            show();
        }
    }

    /**
     * Adjusts size of this view to match the underlying button menu. This allows
     * a smooth transition between this view and the close button behind it.
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (mAdjustViewSize) {
            if (!(getParent() instanceof RelativeLayout)) {
                throw new IllegalStateException("Only RelativeLayout is supported as parent of this view");
            }

            final int sWidth = ScreenUtil.getScreenWidth();
            final int sHeight = ScreenUtil.getScreenHeight();

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();

            params.width = (int) (sWidth * mButtonMenu.getMainButtonSize());
            params.height = LocalDisplay.dp2px(66);
            params.setMargins(0, 0, 0, (int) (sHeight * mButtonMenu.getBottomPadding()));
        }
    }

}

