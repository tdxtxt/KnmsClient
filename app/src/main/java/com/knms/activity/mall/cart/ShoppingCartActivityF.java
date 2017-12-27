package com.knms.activity.mall.cart;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshSExpandableListView;
import com.handmark.pulltorefresh.library.view.SExpandableListView;
import com.knms.activity.base.HeadBaseActivity;
import com.knms.activity.dialog.PayActivity;
import com.knms.activity.goods.MallActivity;
import com.knms.activity.mall.order.ConfirmOrderActivity;
import com.knms.adapter.ShopcatAdapter;
import com.knms.android.R;
import com.knms.app.KnmsApp;
import com.knms.bean.ResponseBody;
import com.knms.bean.order.neworder.RequestBuyParameters;
import com.knms.bean.shoppingcart.ShoppingCartBody;
import com.knms.bean.shoppingcart.SpecProduct;
import com.knms.bean.sku.base.SkuModel;
import com.knms.core.rxbus.BusAction;
import com.knms.core.rxbus.annotation.Subscribe;
import com.knms.core.rxbus.annotation.Tag;
import com.knms.net.RxRequestApi;
import com.knms.oncall.LoadListener;
import com.knms.util.Arith;
import com.knms.util.ConstantObj;
import com.knms.util.DialogHelper;
import com.knms.util.Tst;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * 类描述：购物车列表界面
 * 创建人：tdx
 * 创建时间：2017/8/8 10:57
 * 传参：无
 * 返回：无
 */
public class ShoppingCartActivityF extends HeadBaseActivity implements ShopcatAdapter.CheckInterface {
    public static final int REQEST_CODE = 0x00111;

    PullToRefreshSExpandableListView pullToRefreshSExpandableListView;
    SExpandableListView expandableListView;
    ShopcatAdapter adapter;
    CheckBox allCheckBox;
    TextView tvTotalPrice;
    RelativeLayout rl_status;

    /**false就是完成，ture就是编辑**/
    private boolean isEdit = false;
    private int mtotalCount = 0;//购买状态下选中的数量
    private int editmTotalCount = 0;//编辑状态下选中的数量
    private int pageNum = 1;
    @Override
    public void setCenterTitleView(TextView tv_center) {
        tv_center.setText("购物车");
    }

    @Override
    protected int layoutResID() {
        return R.layout.activity_shoppingcart;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQEST_CODE://得到的数据
                SkuModel resultSku = (SkuModel) data.getSerializableExtra("resultSku");
                if(adapter != null) adapter.updateItem(resultSku);
                break;
        }
    }

    @Override
    protected void initView() {
        pullToRefreshSExpandableListView = findView(R.id.refresh_layout);
        pullToRefreshSExpandableListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        expandableListView = pullToRefreshSExpandableListView.getRefreshableView();
        allCheckBox = findView(R.id.checkbox);
        tvTotalPrice = findView(R.id.tv_totalprice);
        rl_status = findView(R.id.rl_status);

        OverScrollDecoratorHelper.setUpOverScroll(expandableListView);
    }
    @Override
    protected void initData() {
        allCheckBox.setOnClickListener(onclick);
        findView(R.id.tvBtn_delete).setOnClickListener(onclick);
        findView(R.id.tvBtn_buy).setOnClickListener(onclick);
        setRightMenuCallBack(new RightCallBack() {
            @Override
            public void setRightContent(TextView tv, ImageView icon) {
                tv.setText("编辑");
                icon.setVisibility(View.GONE);
            }
            @Override
            public void onclick() {
                TextView tvMenu = findView(R.id.tv_title_right);
                if("编辑".equals(tvMenu.getText())){
                    tvMenu.setText("完成");
                    isEdit = true;
                    setEditStatus(isEdit);
                    MobclickAgent.onEvent(ShoppingCartActivityF.this, "shopcart_clickEdit");
                }else if("完成".equals(tvMenu.getText())){
                    tvMenu.setText("编辑");
                    isEdit = false;
                    setEditStatus(isEdit);
                }
            }
        });
        adapter = new ShopcatAdapter(null,this);
        adapter.setCheckInterface(this);//关键步骤1：设置复选框的接口
        expandableListView.setGroupIndicator(null); //设置属性 GroupIndicator 去掉向下箭头
        expandableListView.setLoadingMoreEnabled(true);
        expandableListView.setmLoadingListener(new SExpandableListView.LoadingListener() {
            @Override
            public void onLoadMore() {
                reqApi();
            }
        });
        expandableListView.setAdapter(adapter);
        pullToRefreshSExpandableListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<SExpandableListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<SExpandableListView> refreshView) {
                pageNum = 1;
                reqApi();
                expandableListView.loadMoreComplete();
            }
        });
        pullToRefreshSExpandableListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshSExpandableListView.setRefreshing();
            }
        },200);
    }

    @Override
    protected void reqApi() {
        final Map<String,Object> pamas = new HashMap<>();
        pamas.put("pageIndex",pageNum);
        pamas.put("userId",ConstantObj.TEMP_USERID);
        RxRequestApi.getInstance().getApiService().getShoppingCartProducts(pamas)
                .compose(this.<ResponseBody<ShoppingCartBody>>applySchedulers())
                .subscribe(new Action1<ResponseBody<ShoppingCartBody>>() {
                    @Override
                    public void call(ResponseBody<ShoppingCartBody> body) {
                        pullToRefreshSExpandableListView.onRefreshComplete();
                        if(body.isSuccess1()){
                            updateView(body.data);
                            pageNum ++;
                        }else{
                            if (pageNum == 1) findView(R.id.tv_title_right).setVisibility(View.GONE);
                            Tst.showToast(body.desc);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (pageNum == 1) findView(R.id.tv_title_right).setVisibility(View.GONE);
                        pullToRefreshSExpandableListView.onRefreshComplete();
                        Tst.showToast(throwable.getMessage());
                    }
                });
    }
    private void updateView(ShoppingCartBody data) {
        if(data == null) return;
        if(data.shopCarts == null) return;

        if (pageNum == 1) {
            if (data.shopCarts.size() > 0) {
                adapter.setNewData(data.shopCarts);
                expandListView();
                findView(R.id.tv_title_right).setVisibility(View.VISIBLE);
            } else {
                KnmsApp.getInstance().showDataEmpty(rl_status, "购物车空空如也~赶紧去逛逛", R.drawable.no_data_1, "去逛逛", new LoadListener() {
                    @Override
                    public void onclick() {//去逛逛
                        Intent intent = new Intent(ShoppingCartActivityF.this, MallActivity.class);
                        startActivity(intent);
                    }
                });
                findView(R.id.tv_title_right).setVisibility(View.GONE);
            }
        } else {
            if (data != null && data.shopCarts.size() > 0) {
                adapter.addData(data.shopCarts);
                expandableListView.loadMoreComplete();
                expandListView();
            }else{
                expandableListView.loadMoreEnd();
            }
        }

        if (data != null && data.shopCarts.size() > 0) {
            if (isCheckAll(adapter.getData())) {
                allCheckBox.setChecked(true);//全选
            } else {
                allCheckBox.setChecked(false);//反选
            }
            calulate();
        }
    }
    View.OnClickListener onclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id){
                case R.id.checkbox:
                    doCheckAll();
                    break;
                case R.id.tvBtn_buy:
                    MobclickAgent.onEvent(ShoppingCartActivityF.this, "shopcart_clickSettlement");
                    if(!checkBuyData()) return;
                    if((mtotalCount > 20)){
                        Tst.showToast("单次最多结算20种商品");
                        return;
                    }
                    List<RequestBuyParameters> data = createParameter();
                    Map<String,Object> map=new HashMap<>();
                    map.put("buyParameters",data);
                    startActivityAnimGeneral(ConfirmOrderActivity.class, map);
                    break;
                case R.id.tvBtn_delete:
                    MobclickAgent.onEvent(ShoppingCartActivityF.this, "shopcart_clickDelete");
                    if(!checkDeleteData()) return;
                    DialogHelper.showPromptDialog(ShoppingCartActivityF.this,
                            "", "确定删除选中商品?", "取消", "", "确定", new DialogHelper.OnMenuClick() {
                                @Override
                                public void onLeftMenuClick() {}
                                @Override
                                public void onCenterMenuClick() {}
                                @Override
                                public void onRightMenuClick() {
                                    removeCartSku();
                                }
                            });
                    break;
            }
        }
    };
    @Subscribe(tags = {@Tag(BusAction.CREATE_ORDER)})
    public void refresh(String action){
        pullToRefreshSExpandableListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullToRefreshSExpandableListView.setRefreshing();
            }
        },200);
    }
    public void expandListView(){
        if(adapter == null) return;
        if(adapter.getData() == null) return;
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i); //关键步骤:初始化，将ExpandableListView以展开的方式显示
        }
    }

    Map<String,List<SpecProduct>> mapChilds = new HashMap<>();
    List<ShoppingCartBody.ShoppingCart> deleteGroups = new ArrayList<>();

    private void  removeCartSku(){
        showProgress();
        mapChilds.clear();
        deleteGroups.clear();
        if(adapter == null) return;
        if(adapter.getData() == null) return;
        Observable.just(adapter.getData())
        .flatMap(new Func1<List<ShoppingCartBody.ShoppingCart>, Observable<ResponseBody>>() {
            @Override
            public Observable<ResponseBody> call(List<ShoppingCartBody.ShoppingCart> shoppingCarts) {
                List<String> shopcartSpecIds = new ArrayList<>();
                for (int i = 0;i < shoppingCarts.size();i++) {
                    ShoppingCartBody.ShoppingCart group = shoppingCarts.get(i);
                    /**选中分组的，直接将group添加进容器**/
                    if(group.isCheck) {
                        deleteGroups.add(group);
                        continue;
                    }
                    /**没有选中分组的，判断子元素**/

                    //但没有子元素，也将group添加进容器
                    List<SpecProduct> childs = group.specProducts;
                    if(!(childs != null && childs.size() > 0)){
                        deleteGroups.add(group);
                        continue;
                    }
                    //把子元素拿出来看是否有被选中的，有吧子元素添加到容器
                    List<SpecProduct> deleteChild = new ArrayList<>();
                    for (SpecProduct child : childs) {
                        if(child.isCheck){
                            deleteChild.add(child);
                        }
                    }
                    mapChilds.put(group.id,deleteChild);
                }

                //组合skuId数组
                if(deleteGroups != null && deleteGroups.size() > 0){
                    for (ShoppingCartBody.ShoppingCart cart : deleteGroups) {
                        for (SpecProduct item : cart.specProducts) {
                            shopcartSpecIds.add(item.skuId);
                        }
                    }
                }
                for (String shopcartId : mapChilds.keySet()) {
                    List<SpecProduct> childs = mapChilds.get(shopcartId);
                    for (SpecProduct item : childs) {
                        shopcartSpecIds.add(item.skuId);
                    }
                }
                if(shopcartSpecIds != null && shopcartSpecIds.size() > 0){
                    Map<String,Object> params = new HashMap<>();
                    params.put("specificationIdList",TextUtils.join(",",shopcartSpecIds));
                    params.put("userId",ConstantObj.TEMP_USERID);
                    return  RxRequestApi.getInstance().getApiService().removeShopCartSku(params);
                }
                return Observable.empty();
            }
        }).compose(this.<ResponseBody>applySchedulers())
        .subscribe(new Action1<ResponseBody>() {
            @Override
            public void call(ResponseBody body) {
                hideProgress();
                if(body.isSuccess1()){
                    doDelete();
                }else {
                    Tst.showToast(body.desc);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                hideProgress();
            }
        });

    }
    /**
     * 删除操作
     * 1.不要边遍历边删除,容易出现数组越界的情况
     * 2.把将要删除的对象放进相应的容器中，待遍历完，用removeAll的方式进行删除
     */
    private void doDelete() {
        if(adapter == null) return;
        if(adapter.getData() == null) return;

        List<ShoppingCartBody.ShoppingCart> groups = adapter.getData();
        if(deleteGroups != null) groups.removeAll(deleteGroups);
        if(mapChilds != null){
            for (String shopcartId : mapChilds.keySet()) {
                List<SpecProduct> childs = mapChilds.get(shopcartId);
                ShoppingCartBody.ShoppingCart shopcart = adapter.getGroupById(shopcartId);
                if(shopcart != null && shopcart.specProducts != null) {
                    shopcart.specProducts.removeAll(childs);
                }
            }
        }
        calulate();
        //重新设置购物车
        adapter.notifyDataSetChanged();
        if(!(adapter.getData() != null && adapter.getData().size() > 0)){
            findView(R.id.tv_title_right).setVisibility(View.GONE);
            KnmsApp.getInstance().showDataEmpty(rl_status, "购物车空空如也~赶紧去逛逛", R.drawable.no_data_1, "去逛逛", new LoadListener() {
                @Override
                public void onclick() {//去逛逛
                    Intent intent = new Intent(ShoppingCartActivityF.this, MallActivity.class);
                    startActivity(intent);
                    finshActivity();
                }
            });
        }
    }
    /**
     * 检查有数据是否被选中
     * @return ture：有   false：没有
     */
    private boolean checkBuyData(){
        if(0 == mtotalCount){
            Tst.showToast("请至少选择一种商品");
        }
        return !(0 == mtotalCount);
    }
    private boolean checkDeleteData(){
        if(0 == editmTotalCount){
            Tst.showToast("请至少选择一种商品");
        }
        return !(0 == editmTotalCount);
    }
    /**
     * 设置是否为编辑界面
     * @param isEdit
     */
    private void setEditStatus(boolean isEdit){
        if(isEdit){
            pullToRefreshSExpandableListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }else{
            pullToRefreshSExpandableListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        }
        List<ShoppingCartBody.ShoppingCart> data = adapter.getData();
        if(data == null) return;
        for (ShoppingCartBody.ShoppingCart item : data) {
            item.isEditor = isEdit;
        }
        adapter.notifyDataSetChanged();

        View layoutComplete = findViewById(R.id.rl_menu_complete);
        View layoutEdit = findViewById(R.id.rl_menu_edit);
        if(isEdit){
            layoutComplete.setVisibility(View.GONE);
            layoutEdit.setVisibility(View.VISIBLE);
        }else{
            layoutComplete.setVisibility(View.VISIBLE);
            layoutEdit.setVisibility(View.GONE);
            calulate();
        }

        TextView tvMenu = findView(R.id.tv_title_right);
        if("编辑".equals(tvMenu.getText().toString())){
            if(adapter.getData() != null && adapter.getData().size() > 0){
                tvMenu.setVisibility(View.VISIBLE);
            }else{
                tvMenu.setVisibility(View.GONE);
            }
        }
    }
    @Override
    public String setStatisticsTitle() {
        return null;
    }

    @Override
    public void checkGroup(int groupPosition, boolean isChecked) {
        ShoppingCartBody.ShoppingCart group = adapter.getGroup(groupPosition);
        List<SpecProduct> childs = group.specProducts;
        for (SpecProduct child : childs) {
            child.isCheck = isChecked;
        }
        if (isCheckAll(adapter.getData())) {
            allCheckBox.setChecked(true);//全选
        } else {
            allCheckBox.setChecked(false);//反选
        }
        adapter.notifyDataSetChanged();
        calulate();
    }
    @Override
    public void checkChild(int groupPosition, int childPosition, boolean isChecked) {
        boolean allChildSameState = true; //判断该组下面的所有子元素是否处于同一状态
        ShoppingCartBody.ShoppingCart group = adapter.getGroup(groupPosition);
        List<SpecProduct> childs = group.specProducts;
        for (SpecProduct child : childs) {
            //不选全中
            if (child.isCheck != isChecked) {
                allChildSameState = false;
                break;
            }
        }
        if (allChildSameState) {
            group.isCheck = isChecked;//如果子元素状态相同，那么对应的组元素也设置成这一种的同一状态
        } else {
            group.isCheck = false;//否则一律视为未选中
        }

        if (isCheckAll(adapter.getData())) {
            allCheckBox.setChecked(true);//全选
        } else {
            allCheckBox.setChecked(false);//反选
        }

        adapter.notifyDataSetChanged();
        calulate();
    }
    /**
     * 计算商品总价格，操作步骤
     * 1.先清空全局计价,计数
     * 2.遍历所有的子元素，只要是被选中的，就进行相关的计算操作
     * 3.给textView填充数据
     */
    private void calulate() {
//        if(isEdit) return;
        mtotalCount = 0;
        editmTotalCount = 0;
        double totalPrice = 0.00;
        if(adapter == null) return;
        List<ShoppingCartBody.ShoppingCart> gourps = adapter.getData();
        if(gourps != null && gourps.size() > 0){
            for (ShoppingCartBody.ShoppingCart gourp : gourps) {
                if(gourp != null && gourp.specProducts != null){
                    for (SpecProduct child : gourp.specProducts) {
                        if(child.isCheck){
                            if(child.state == 1){
                                mtotalCount ++;
                                totalPrice = Arith.add(totalPrice, Arith.mul(child.buyCount, child.price));
                            }
                            editmTotalCount ++;
                        }
                    }
                }
            }
        }
        tvTotalPrice.setText("￥"+ Arith.clearZero(totalPrice));
    }
    private List<RequestBuyParameters> createParameter(){
        List<RequestBuyParameters> data = new ArrayList<>();
        List<ShoppingCartBody.ShoppingCart> gourps = adapter.getData();
        if(gourps != null && gourps.size() > 0){
            for (ShoppingCartBody.ShoppingCart gourp : gourps) {
                if(gourp != null && gourp.specProducts != null){
                    for (SpecProduct child : gourp.specProducts) {
                        if(child.isCheck && child.state == 1){
                            RequestBuyParameters item = new RequestBuyParameters();
                            item.buyQuantity = child.buyCount;
                            item.specificationId = child.skuId;
                            data.add(item);
                        }
                    }
                }
            }
        }
        return data;
    }
    /**
     * @return 判断组元素是否全选
     */
    private boolean isCheckAll(List<ShoppingCartBody.ShoppingCart> groups) {
        for (ShoppingCartBody.ShoppingCart group : groups) {
            if (!group.isCheck) {
                return false;
            }
        }
        return true;
    }
    /**
     * 全选和反选
     * 错误标记：在这里出现过错误
     */
    private void doCheckAll() {
        if(adapter == null) return;
        if(adapter.getData() == null) return;
        List<ShoppingCartBody.ShoppingCart> data = adapter.getData();
        for (ShoppingCartBody.ShoppingCart group : data) {
            group.isCheck = allCheckBox.isChecked();
            List<SpecProduct> childs = group.specProducts;
            if(childs != null && childs.size() > 0){
                for (SpecProduct child : childs) {
                    child.isCheck = allCheckBox.isChecked();
                }
            }
        }
        adapter.notifyDataSetChanged();
        calulate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        KnmsApp.getInstance().onDestroy();
    }
}
