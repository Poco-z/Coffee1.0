package com.qin.fragment.drawer.history;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.qin.R;
import com.qin.adapter.recyclerview.MarginDecorationVertical;
import com.qin.adapter.recyclerview.drawer.ParkingHistoryRecyclerViewAdapter;
import com.qin.application.MyApplication;
import com.qin.constant.ConstantValues;
import com.qin.fragment.BaseFragment;
import com.qin.pojo.history.OutInsuaranceHistory;
import com.qin.pojo.history.Result;
import com.qin.util.SPUtils;
import com.qin.util.ScreenUtils;
import com.qin.util.ToastUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class ParkingHistoryfragment extends BaseFragment   {

    @BindView(R.id.rlv_history_parking)
    RecyclerView mRlvHistoryParking;
    Unbinder unbinder;
    @BindView(R.id.tv_history_clear)
    TextView mTvHistoryClear;
    @BindView(R.id.ll_history_parking)
    LinearLayout mLlHistoryParking;
    @BindView(R.id.tv_history_parking)
    TextView mTvHistoryParking;

    private String mUserid;
    private Dialog mDialog;
    private String mSpUser_id;
    private ParkingHistoryRecyclerViewAdapter mAdapter;
    private List<Result> mResultList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserid = MyApplication.getUserid();
        mSpUser_id = SPUtils.getInstance(mActivity).getString("user_id", "");
        if (mUserid == null) {
            mUserid = mSpUser_id;
        }
    }

    @Override
    public View initView() {
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parkinghistory, null, false);
        unbinder = ButterKnife.bind(this, view);
        initDialog();
        mRlvHistoryParking.setLayoutManager(new LinearLayoutManager(mActivity));
        mRlvHistoryParking.setItemAnimator(new DefaultItemAnimator());
        mRlvHistoryParking.addItemDecoration(new MarginDecorationVertical(20));
        return view;
    }

    @Override
    protected void initData() {
        accessNet(mUserid, 1);
    }

    private void accessNet(String userid, int category) {
        OkGo.<String>post(ConstantValues.URL_HISTORYPARKING)
                .tag(this)
                .params("user_id", userid)
                .params("category", category)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i("ParkingHistoryfragment", response.body());
                        mDialog.dismiss();
                        String body = response.body();
                        parseeData(body);
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        mDialog.dismiss();
                        ToastUtils.showBgResource(mActivity, "网络错误！");
                    }
                });
    }

    private void parseeData(String json) {
        Gson gson = new Gson();
        OutInsuaranceHistory outInsuaranceHistory = gson.fromJson(json, OutInsuaranceHistory.class);
        String code = outInsuaranceHistory.getCode();
        mResultList = outInsuaranceHistory.getResult();
        if (code.equals("0")&&mResultList.size() > 0) {
            mTvHistoryParking.setVisibility(View.GONE);
            mLlHistoryParking.setVisibility(View.VISIBLE);
            mAdapter = new ParkingHistoryRecyclerViewAdapter(mResultList);
            mRlvHistoryParking.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new ParkingHistoryRecyclerViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, ParkingHistoryRecyclerViewAdapter.ViewName viewName, int postion) {
                    if (ParkingHistoryRecyclerViewAdapter.ViewName.TEXTVIEW == viewName) {

                    } else if (ParkingHistoryRecyclerViewAdapter.ViewName.IMAGEVIEW == viewName) {
                        int hisinfoId = mResultList.get(postion).getHisinfo_id();
                        mResultList.remove(postion);
                        accessNetDelete(mUserid,hisinfoId);
                    }
                }
            });
        }else{
            mTvHistoryParking.setVisibility(View.VISIBLE);
            mLlHistoryParking.setVisibility(View.GONE);
        }
    }

    private void accessNetDelete(String userid, int hisinfoid) {
        mDialog.show();
        OkGo.<String>post(ConstantValues.URL_HISTORYDELETE)
                .tag(this)
                .params("user_id", userid)
                .params("hisinfo_id", hisinfoid)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i("repairhistory", response.body());
                        mAdapter.notifyDataSetChanged();
                        mRlvHistoryParking.setAdapter(mAdapter);
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        mDialog.dismiss();
                        ToastUtils.showBgResource(mActivity, "网络错误！");
                    }
                });
    }

    private void accessNetDeleteAll(String userid, int category) {
        mDialog.show();
        OkGo.<String>post(ConstantValues.URL_HISTORYDELETEALL)
                .tag(this)
                .params("user_id", userid)
                .params("category", category)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        Log.i("repairhistory", response.body());
                        String body = response.body();
                        mResultList.clear();
                        mTvHistoryParking.setVisibility(View.VISIBLE);
                        mLlHistoryParking.setVisibility(View.GONE);
                        mAdapter.notifyDataSetChanged();
                        mRlvHistoryParking.setAdapter(mAdapter);
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        mDialog.dismiss();
                        ToastUtils.showBgResource(mActivity, "网络错误！");
                    }
                });
    }

    private void initDialog() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ScreenUtils.getWindowWidth(mActivity), ScreenUtils.getWindowHeight(mActivity));
        params.width = (int) (mActivity.getWindowManager().getDefaultDisplay().getWidth() * 0.5f);
        params.height = (int) (mActivity.getWindowManager().getDefaultDisplay().getWidth() * 0.5f);
        mDialog = new Dialog(mActivity);
        View view = View.inflate(mActivity, R.layout.dialog_loading, null);
        mDialog.setContentView(view);
        mDialog.setContentView(view, params);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(true);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.tv_history_clear)
    public void onViewClicked() {
        accessNetDeleteAll(mUserid,1);
    }
}
