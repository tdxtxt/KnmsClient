package com.knms.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.knms.activity.ShopActivityF;
import com.knms.activity.base.BaseActivity;
import com.knms.activity.details.canbuy.ProductDetailsActivity;
import com.knms.activity.mall.cart.ShoppingCartActivityF;
import com.knms.activity.dialog.ChoiceSizeActivityDialog;
import com.knms.adapter.base.ViewHolder;
import com.knms.android.R;
import com.knms.bean.ResponseBody;
import com.knms.bean.shoppingcart.ShoppingCartBody;
import com.knms.bean.shoppingcart.SpecProduct;
import com.knms.bean.sku.base.SkuModel;
import com.knms.net.RxRequestApi;
import com.knms.util.Arith;
import com.knms.util.CommonUtils;
import com.knms.util.ConstantObj;
import com.knms.util.ImageLoadHelper;
import com.knms.util.LocalDisplay;
import com.knms.util.SystemInfo;
import com.knms.util.Tst;
import com.knms.view.tv.CounterView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.http.Field;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.R.attr.data;
import static com.knms.android.R.id.store_name;
import static com.knms.util.SystemInfo.NETWORN_NONE;


/**
 * 类描述：购物车列表界面适配器
 * 创建人：tdx
 * 创建时间：2017/8/8 10:57
 * 传参：无
 * 返回：无
 */
public class ShopcatAdapter extends BaseExpandableListAdapter {
    private List<ShoppingCartBody.ShoppingCart> groups;
    private BaseActivity mcontext;
    private CheckInterface checkInterface;
    private ShoppingCartBody.ShoppingCart currentShopCart;
    private SpecProduct currentSpecProduct;

    public ShopcatAdapter(List<ShoppingCartBody.ShoppingCart> groups, Context context) {
        this.groups = groups;
        this.mcontext = (BaseActivity) context;
        if (this.groups == null) this.groups = new ArrayList<>();
    }

    public void setNewData(List<ShoppingCartBody.ShoppingCart> data) {
        if (groups == null) groups = new ArrayList<>();
        groups.clear();
        if (data != null) groups.addAll(data);
        notifyDataSetChanged();
    }

    public void addData(List<ShoppingCartBody.ShoppingCart> data) {
        if (groups == null) groups = new ArrayList<>();
        if (data != null) {
            data.addAll(data);
        }
        notifyDataSetChanged();
    }

    public ShoppingCartBody.ShoppingCart getGroupById(String shopcartId) {
        if (TextUtils.isEmpty(shopcartId)) return null;
        if (groups == null) return null;
        for (ShoppingCartBody.ShoppingCart shopcart : groups) {
            if (shopcartId.equals(shopcart.id)) {
                return shopcart;
            }
        }
        return null;
    }

    public List<ShoppingCartBody.ShoppingCart> getData() {
        return groups;
    }

    @Override
    public int getGroupCount() {
        return this.groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (getGroup(groupPosition).specProducts == null) return 0;
        return getGroup(groupPosition).specProducts.size();
    }

    @Override
    public ShoppingCartBody.ShoppingCart getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public SpecProduct getChild(int groupPosition, int childPosition) {
        return getGroup(groupPosition).specProducts.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final ShoppingCartBody.ShoppingCart item = getGroup(groupPosition);
        ViewHolder viewHolder = ViewHolder.get(mcontext, convertView, parent, R.layout.item_shopcat_group, groupPosition);
        TextView storeName = viewHolder.getView(store_name);
        storeName.setText(item.shopName);
        storeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!item.isEditor) {
                    MobclickAgent.onEvent(mcontext, "shopcart_clickIntoShop");
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("shopId", item.shopId);
                    mcontext.startActivityAnimGeneral(ShopActivityF.class, params);
                }
            }
        });
        CheckBox storeCheckBox = viewHolder.getView(R.id.store_checkBox);
        storeCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isCheck = ((CheckBox) v).isChecked();
                if (checkInterface != null)
                    checkInterface.checkGroup(groupPosition, ((CheckBox) v).isChecked());
            }
        });
        storeCheckBox.setChecked(item.isCheck);
        return viewHolder.getConvertView();
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final SpecProduct item = getChild(groupPosition, childPosition);
        ViewHolder viewHolder = ViewHolder.get(mcontext, convertView, parent, R.layout.item_shopcat_product, childPosition);

        ((TextView) viewHolder.getView(R.id.tv_orprice)).getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); //中划线

        viewHolder.setText(R.id.tv_name, item.productName)
                .setText(R.id.tv_specs, item.specDesc)
                .setText(R.id.tv_specs_edit, item.specDesc)
                .setText(R.id.tv_price, CommonUtils.addMoneySymbol(Arith.clearZero(item.price)))
                .setText(R.id.tv_orprice, CommonUtils.addMoneySymbol(Arith.clearZero(item.orprice)))
                .setText(R.id.tv_count, "×" + item.buyCount);//

        if (item.state == 0) {//失效
            viewHolder.getView(R.id.tv_state).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_state, "失效");
        } else if (item.state == 1) {//正常
            viewHolder.getView(R.id.tv_state).setVisibility(View.GONE);
        } else if (item.state == 2) {//暂未开抢
            viewHolder.getView(R.id.tv_state).setVisibility(View.VISIBLE);
            viewHolder.setText(R.id.tv_state, "暂未\n开抢");
        }

        ImageView imageView = viewHolder.getView(R.id.iv_logo);
        ImageLoadHelper.getInstance().displayImage(mcontext, item.specImg, imageView, LocalDisplay.dp2px(75), LocalDisplay.dp2px(75));

        View viewshowSpecs = viewHolder.getView(R.id.rl_specs);
        viewshowSpecs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (item.state == 0) {//失效商品
//                    Tst.showToast("商品已失效,无法操作");
                    return;
                }
                currentSpecProduct = item;
                currentShopCart = getGroup(groupPosition);
                Intent intent = new Intent(mcontext, ChoiceSizeActivityDialog.class);
                intent.putExtra("goid", item.productId);
                intent.putExtra("initSkuId", item.skuId);
                intent.putExtra("isShowCount", false);
                intent.putExtra("initCount", item.buyCount);
                mcontext.startActivityForResult(intent, ShoppingCartActivityF.REQEST_CODE);
            }
        });

        CheckBox singleCheckBox = viewHolder.getView(R.id.checkbox);
        if (item.state == 1) {//正常商品
            singleCheckBox.setVisibility(View.VISIBLE);
        } else {//非正常商品
            if (getGroup(groupPosition).isEditor) {
                singleCheckBox.setVisibility(View.VISIBLE);
            } else {
                singleCheckBox.setVisibility(View.INVISIBLE);
            }
        }
        if (getGroup(groupPosition).isEditor) {//处于编辑状态
            singleCheckBox.setChecked(item.isCheck);
        } else {
            singleCheckBox.setChecked(item.isCheck = (item.isCheck && (item.state == 1)));
        }
        singleCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isCheck = ((CheckBox) v).isChecked();
                ((CheckBox) v).setChecked(((CheckBox) v).isChecked());
                if (checkInterface != null)
                    checkInterface.checkChild(groupPosition, childPosition, ((CheckBox) v).isChecked());
            }
        });

        View layoutComplete = viewHolder.getView(R.id.layout_complete);
        View layoutEdit = viewHolder.getView(R.id.layout_edit);
        if (getGroup(groupPosition).isEditor) {
            layoutComplete.setVisibility(View.INVISIBLE);
            layoutEdit.setVisibility(View.VISIBLE);

            final CounterView countView = viewHolder.getView(R.id.countView);
            countView.setCountValue(item.buyCount);
            countView.setToastMaxmsg(item.toastLimitMsg);
            countView.setToastMinmsg("商品不能减少了，请点击下方‘删除’移除商品");
            //设置最大限制
            countView.setMaxValue(item.userLimitBuyNumber);
            countView.setApi(true);
            if (item.state == 0) {
                countView.setEnabled(false);
            } else {
                countView.setEnabled(true);
            }
            countView.setCallback(new CounterView.IChangeCoutCallback() {
                @Override
                public void change(int count) {
                    if (item.state == 0) {//失效商品
                        Tst.showToast("商品已失效,无法操作");
                        return;
                    }
                    if (SystemInfo.getNetworkState() == NETWORN_NONE) {
                        Tst.showToast("网络不给力，请检查网络设置");
                        countView.setEnabled(true);
                        return;
                    }
                    if (subscription != null) subscription.unsubscribe();
                    Map<String, Object> params = new HashMap<>();
                    params.put("shoppingCartSpecificationId", item.shopcartSpecId);
                    params.put("planNumber", count);
                    params.put("userId", ConstantObj.TEMP_USERID);
                    subscription = RxRequestApi.getInstance().getApiService().setShopCartSkuNumber(params)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(new Action1<ResponseBody<ShoppingCartBody>>() {
                                @Override
                                public void call(ResponseBody<ShoppingCartBody> body) {
                                    if (body.isSuccess1()) {
                                        if (body.data != null && body.data.product != null) {
                                            countView.setCountValue(body.data.product.buyCount);
                                            item.buyCount = body.data.product.buyCount;
                                        }
                                    } else {
                                        Tst.showToast(body.data.product.toastFailureMsg);
                                    }
                                    countView.setEnabled(true);
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    countView.setEnabled(true);
                                }
                            });
                }
            });
        } else {
            layoutComplete.setVisibility(View.VISIBLE);
            layoutEdit.setVisibility(View.INVISIBLE);
            layoutComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!getGroup(groupPosition).isEditor) {
                        MobclickAgent.onEvent(mcontext, "shopcart_clickIntoProduct");
                        Map<String, Object> params = new HashMap<>();
                        params.put("id", item.productId);
                        mcontext.startActivityAnimGeneral(ProductDetailsActivity.class, params);
                    }
                }
            });
            viewHolder.getView(R.id.rlBtn_pic).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!getGroup(groupPosition).isEditor) {
                        MobclickAgent.onEvent(mcontext, "shopcart_clickIntoProduct");
                        Map<String, Object> params = new HashMap<>();
                        params.put("id", item.productId);
                        mcontext.startActivityAnimGeneral(ProductDetailsActivity.class, params);
                    }
                }
            });
        }
        return viewHolder.getConvertView();
    }

    Subscription subscription;

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public void setCheckInterface(CheckInterface checkInterface) {
        this.checkInterface = checkInterface;
    }

    public void updateItem(SkuModel model) {
        if (currentSpecProduct == null) return;
        if (model == null) return;
        if (currentSpecProduct.skuId.equals(model.skuId)) return;
        Map<String, Object> params = new HashMap<>();
        params.put("shoppingCartSpecificationId", currentSpecProduct.shopcartSpecId);
        params.put("specificationId", model.skuId);
        params.put("userId", ConstantObj.TEMP_USERID);
        RxRequestApi.getInstance().getApiService().setShopCartSkuSpec(params)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody<ShoppingCartBody>>() {
                    @Override
                    public void call(ResponseBody<ShoppingCartBody> body) {
                        if (body.isSuccess1()) {
                            if (body.data != null && body.data.shopCarts != null && body.data.shopCarts.size() > 0) {
                                List<SpecProduct> oldData = currentShopCart.specProducts;
                                List<SpecProduct> newData = body.data.shopCarts.get(0).specProducts;
                                for (SpecProduct newSpec : newData) {
                                    for (SpecProduct oldSpec : oldData) {
                                        if (newSpec.skuId.equals(oldSpec.skuId)) {
                                            newSpec.isCheck = oldSpec.isCheck;
                                        }
                                    }
                                }
                                oldData.clear();
                                oldData.addAll(newData);
                                notifyDataSetChanged();
                            } else {
                                Tst.showToast("数据为空");
                            }
                        } else {
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Tst.showToast("修改失败");
                    }
                });
        notifyDataSetChanged();
    }

    /**
     * 店铺的复选框
     */
    public interface CheckInterface {
        /**
         * 组选框状态改变触发的事件
         *
         * @param groupPosition 组元素的位置
         * @param isChecked     组元素的选中与否
         */
        void checkGroup(int groupPosition, boolean isChecked);

        /**
         * 子选框状态改变触发的事件
         *
         * @param groupPosition 组元素的位置
         * @param childPosition 子元素的位置
         * @param isChecked     子元素的选中与否
         */
        void checkChild(int groupPosition, int childPosition, boolean isChecked);
    }
}
