package net.oschina.app.improve.detail.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.app.AppOperator;
import net.oschina.app.improve.bean.Collection;
import net.oschina.app.improve.bean.SoftwareDetail;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.detail.contract.SoftDetailContract;
import net.oschina.app.improve.detail.fragments.DetailFragment;
import net.oschina.app.improve.detail.fragments.SoftWareDetailFragment;
import net.oschina.app.util.TLog;

import java.lang.reflect.Type;

import cz.msebera.android.httpclient.Header;

/**
 * Created by fei on 2016/6/13.
 * desc:   news detail  module
 */
public class SoftwareDetailActivity extends DetailActivity<SoftwareDetail, SoftDetailContract.View>
        implements SoftDetailContract.Operator {

    private String mIdent;

    @Override
    int getOptionsMenuId() {
        return 0;
    }

    /**
     * show news detail
     *
     * @param context context
     * @param id      id
     */
    public static void show(Context context, long id) {
        Intent intent = new Intent(context, SoftwareDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        intent.putExtras(bundle);
        //intent.putExtra("id", id);
        context.startActivity(intent);
    }

    /**
     * show news detail
     *
     * @param context context
     * @param ident   ident--> software Name
     */
    public static void show(Context context, String ident) {
        Intent intent = new Intent(context, SoftwareDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("ident", ident);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected boolean initBundle(Bundle bundle) {
        mIdent = bundle.getString("ident", null);
        return !TextUtils.isEmpty(mIdent) || super.initBundle(bundle);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_blog_detail;
    }

    @Override
    void requestData() {
        if (TextUtils.isEmpty(mIdent)) {
            OSChinaApi.getNewsDetail(getDataId(), OSChinaApi.CATALOG_SOFTWARE_DETAIL, getRequestHandler());
        } else {
            OSChinaApi.getSoftwareDetail(mIdent, OSChinaApi.CATALOG_SOFTWARE_DETAIL, getRequestHandler());
        }
    }

    @Override
    Class<? extends DetailFragment> getDataViewFragment() {
        return SoftWareDetailFragment.class;
    }

    @Override
    Type getDataType() {
        return new TypeToken<ResultBean<SoftwareDetail>>() {
        }.getType();
    }

    @Override
    public void toFavorite() {
        long uid = requestCheck();
        if (uid == 0)
            return;
        showWaitDialog(R.string.progress_submit);
        final SoftwareDetail softwareDetail = getData();
        OSChinaApi.getFavReverse(getDataId(), 1, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                hideWaitDialog();
                if (softwareDetail.isFavorite())
                    AppContext.showToastShort(R.string.del_favorite_faile);
                else
                    AppContext.showToastShort(R.string.add_favorite_faile);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                TLog.error(responseString);
                try {
                    Type type = new TypeToken<ResultBean<Collection>>() {
                    }.getType();

                    ResultBean<Collection> resultBean = AppOperator.createGson().fromJson(responseString, type);
                    if (resultBean != null && resultBean.isSuccess()) {
                        softwareDetail.setFavorite(!softwareDetail.isFavorite());
                        mView.toFavoriteOk(softwareDetail);
                        if (softwareDetail.isFavorite())
                            AppContext.showToastShort(R.string.add_favorite_success);
                        else
                            AppContext.showToastShort(R.string.del_favorite_success);
                    }
                    hideWaitDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(statusCode, headers, responseString, e);
                }
            }
        });
    }

    @Override
    public void toShare() {
        if (getData() != null) {
            final SoftwareDetail detail = getData();
            String title = detail.getName();
            String content = detail.getBody();
            String url = detail.getHref();
            if (!toShare(title, content, url, 1))
                AppContext.showToast("抱歉，内容无法分享！");
        } else {
            AppContext.showToast("内容加载失败！");
        }
    }
}
