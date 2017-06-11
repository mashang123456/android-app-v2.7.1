package net.oschina.app.improve.notice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.oschina.app.improve.account.AccountHelper;
import net.oschina.common.BuildConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by JuQiu
 * on 16/8/18.
 */
public final class NoticeManager {
    public static final int FLAG_CLEAR_MENTION = 0x1;
    public static final int FLAG_CLEAR_LETTER = 0x2;
    public static final int FLAG_CLEAR_REVIEW = 0x3;
    public static final int FLAG_CLEAR_FANS = 0x4;
    public static final int FLAG_CLEAR_LIKE = 0x5;
    public static final int FLAG_CLEAR_ALL = 0x6;

    private static NoticeManager INSTANCE;

    static {
        INSTANCE = new NoticeManager();
    }

    private NoticeManager() {

    }

    private final List<NoticeNotify> mNotifies = new ArrayList<>();
    private NoticeBean mNotice;

    public static NoticeBean getNotice() {
        final NoticeBean bean = INSTANCE.mNotice;
        if (bean == null) {
            return new NoticeBean();
        } else {
            return bean;
        }
    }

    public static void bindNotify(NoticeNotify noticeNotify) {
        INSTANCE.mNotifies.add(noticeNotify);
        INSTANCE.check(noticeNotify);
    }

    public static void unBindNotify(NoticeNotify noticeNotify) {
        INSTANCE.mNotifies.remove(noticeNotify);
    }

    private void check(NoticeNotify noticeNotify) {
        if (mNotice != null)
            noticeNotify.onNoticeArrived(mNotice);
    }

    public static void init(Context context) {
        // 未登陆时不启动服务
        if (!AccountHelper.isLogin()) {
            return;
        }
        // 启动服务
        NoticeServer.startAction(context);
        // 注册广播
        IntentFilter filter = new IntentFilter(NoticeServer.FLAG_BROADCAST_REFRESH);
        context.registerReceiver(INSTANCE.mReceiver, filter);
    }

    public static void stopListen(Context context) {
        try {
            context.unregisterReceiver(INSTANCE.mReceiver);
        } catch (IllegalArgumentException e) {
            if (BuildConfig.DEBUG)
                e.printStackTrace();
        }
    }

    public static void exitServer(Context context) {
        NoticeServer.startActionExit(context);
    }

    /**
     * 已读清理Context
     *
     * @param context Context
     * @param type    {@link #FLAG_CLEAR_MENTION}, {@link #FLAG_CLEAR_LETTER},
     *                {@link #FLAG_CLEAR_REVIEW},{@link #FLAG_CLEAR_FANS},
     *                {@link #FLAG_CLEAR_LIKE}
     */
    public static void clear(Context context, int type) {
        NoticeServer.startActionClear(context, type);
    }

    public interface NoticeNotify {
        void onNoticeArrived(NoticeBean bean);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null &&
                    NoticeServer.FLAG_BROADCAST_REFRESH.equals(intent.getAction())) {
                Serializable serializable = intent.getSerializableExtra(NoticeServer.EXTRA_BEAN);
                if (serializable != null) {
                    try {
                        onNoticeChanged((NoticeBean) serializable);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }
                }
            }
        }
    };

    private void onNoticeChanged(NoticeBean bean) {
        mNotice = bean;
        //  Notify all
        for (NoticeNotify notify : mNotifies) {
            notify.onNoticeArrived(mNotice);
        }
    }
}
