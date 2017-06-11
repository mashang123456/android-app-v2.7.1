package net.oschina.app.improve.detail.activities;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.app.AppOperator;
import net.oschina.app.improve.bean.Collection;
import net.oschina.app.improve.bean.NewsDetail;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.bean.comment.Comment;
import net.oschina.app.improve.comment.CommentsActivity;
import net.oschina.app.improve.detail.contract.NewsDetailContract;
import net.oschina.app.improve.detail.fragments.DetailFragment;
import net.oschina.app.improve.detail.fragments.NewsDetailFragment;

import java.lang.reflect.Type;

import cz.msebera.android.httpclient.Header;

/**
 * Created by fei on 2016/6/13.
 * desc:   news detail  module
 */
public class NewsDetailActivity extends DetailActivity<NewsDetail, NewsDetailContract.View>
        implements NewsDetailContract.Operator {

    /**
     * show news detail
     *
     * @param context context
     * @param id      id
     */
    public static void show(Context context, long id) {
        Intent intent = new Intent(context, NewsDetailActivity.class);
        intent.putExtra("id", id);
        context.startActivity(intent);
    }

    int getType() {
        return 6;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_blog_detail;
    }

    @Override
    void requestData() {
        OSChinaApi.getNewsDetail(getDataId(), OSChinaApi.CATALOG_NEWS_DETAIL, getRequestHandler());
    }

    @Override
    Class<? extends DetailFragment> getDataViewFragment() {
        return NewsDetailFragment.class;
    }

    @Override
    Type getDataType() {
        return new TypeToken<ResultBean<NewsDetail>>() {
        }.getType();
    }

    @Override
    public void toFavorite() {
        long uid = requestCheck();
        if (uid == 0)
            return;
        showWaitDialog(R.string.progress_submit);
        final NewsDetail newsDetail = getData();
        OSChinaApi.getFavReverse(getDataId(), getType(), new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                hideWaitDialog();
                if (newsDetail.isFavorite())
                    AppContext.showToastShort(R.string.del_favorite_faile);
                else
                    AppContext.showToastShort(R.string.add_favorite_faile);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                try {
                    Type type = new TypeToken<ResultBean<Collection>>() {
                    }.getType();

                    ResultBean<Collection> resultBean = AppOperator.createGson().fromJson(responseString, type);
                    if (resultBean != null && resultBean.isSuccess()) {
                        newsDetail.setFavorite(!newsDetail.isFavorite());
                        mView.toFavoriteOk(newsDetail);
                        if (newsDetail.isFavorite())
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
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean createOptionsMenu = super.onCreateOptionsMenu(menu);
        if (createOptionsMenu) {
            mCommentCountView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentsActivity.show(NewsDetailActivity.this, mDataId, OSChinaApi.COMMENT_NEWS, OSChinaApi.COMMENT_NEW_ORDER);
                }
            });
        }
        return createOptionsMenu;
    }

    @Override
    public void toShare() {
        if (getData() != null) {
            final NewsDetail detail = getData();
            String title = detail.getTitle();
            String content = detail.getBody();
            String url = detail.getHref();
            if (!toShare(title, content, url, 6))
                AppContext.showToast("抱歉，内容无法分享！");
        } else {
            AppContext.showToast("内容加载失败！");
        }
    }


    @Override
    public void toSendComment(long id, long commentId, long commentAuthorId, String comment) {
        long uid = requestCheck();
        if (uid == 0)
            return;

        if (TextUtils.isEmpty(comment)) {
            AppContext.showToastShort(R.string.tip_comment_content_empty);
            return;
        }
        OSChinaApi.pubNewsComment(id, commentId, commentAuthorId, comment, new TextHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showWaitDialog(R.string.progress_submit);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                AppContext.showToast("评论失败!");
                hideWaitDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                try {
                    Type type = new TypeToken<ResultBean<Comment>>() {
                    }.getType();

                    ResultBean<Comment> resultBean = AppOperator.createGson().fromJson(responseString, type);
                    if (resultBean.isSuccess()) {
                        Comment respComment = resultBean.getResult();
                        if (respComment != null) {
                            NewsDetailContract.View view = mView;
                            if (view != null) {
                                view.toSendCommentOk(respComment);
                            }
                        }
                    }
                    hideWaitDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                    onFailure(statusCode, headers, responseString, e);
                }
                hideWaitDialog();
            }
        });

    }
}
