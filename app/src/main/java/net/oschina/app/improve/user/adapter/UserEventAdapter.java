package net.oschina.app.improve.user.adapter;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.oschina.app.OSCApplication;
import net.oschina.app.R;
import net.oschina.app.improve.base.adapter.BaseGeneralRecyclerAdapter;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.bean.Event;
import net.oschina.app.improve.bean.SubBean;
import net.oschina.app.util.StringUtils;
import net.qiujuer.genius.ui.compat.UiCompat;

import java.util.Map;

/**
 * Created by fei
 * on 2016/12/2.
 * desc:
 */

public class UserEventAdapter extends BaseGeneralRecyclerAdapter<SubBean> implements BaseRecyclerAdapter.OnLoadingHeaderCallBack {

    private OSCApplication.ReadState mReadState;

    public UserEventAdapter(Callback callback, int mode) {
        super(callback, mode);
        mReadState = OSCApplication.getReadState("sub_list");
        setOnLoadingHeaderCallBack(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderHolder(ViewGroup parent) {
        return new HeaderViewHolder(mHeaderView);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    protected RecyclerView.ViewHolder onCreateDefaultViewHolder(ViewGroup parent, int type) {
        return new EventViewHolder(mInflater.inflate(R.layout.item_list_sub_event, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindDefaultViewHolder(RecyclerView.ViewHolder holder, SubBean item, int position) {
        EventViewHolder vh = (EventViewHolder) holder;
        vh.tv_event_title.setText(item.getTitle());
        SubBean.Image image = item.getImage();
        if (image != null) {
            mCallBack.getImgLoader()
                    .load(image.getHref() != null && image.getHref().length > 0 ? image.getHref()[0] : null)
                    .placeholder(R.drawable.bg_normal)
                    .into(vh.iv_event);
        } else {
            vh.iv_event.setImageResource(R.drawable.bg_normal);
        }

        Resources resources = mContext.getResources();

        Map<String, Object> extras = item.getExtra();
        if (extras != null) {
            vh.tv_event_pub_date.setText(StringUtils.getDateString(extras.get("eventStartDate").toString()));
            vh.tv_event_member.setText(Double.valueOf(extras.get("eventApplyCount").toString()).intValue() + "人参与");

            switch (Double.valueOf(extras.get("eventStatus").toString()).intValue()) {
                case Event.STATUS_END:
                    setText(vh.tv_event_state, R.string.event_status_end, R.drawable.bg_event_end, 0x1a000000);
                    setTextColor(vh.tv_event_title, UiCompat.getColor(resources, R.color.light_gray));
                    break;
                case Event.STATUS_ING:
                    setText(vh.tv_event_state, R.string.event_status_ing, R.drawable.bg_event_ing, 0xFF24cf5f);
                    break;
                case Event.STATUS_SING_UP:
                    setText(vh.tv_event_state, R.string.event_status_sing_up, R.drawable.bg_event_end, 0x1a000000);
                    setTextColor(vh.tv_event_title, UiCompat.getColor(resources, R.color.light_gray));
                    break;
            }
            int typeStr = R.string.oscsite;
            switch (Double.valueOf(extras.get("eventType").toString()).intValue()) {
                case Event.EVENT_TYPE_OSC:
                    typeStr = R.string.event_type_osc;
                    break;
                case Event.EVENT_TYPE_TEC:
                    typeStr = R.string.event_type_tec;
                    break;
                case Event.EVENT_TYPE_OTHER:
                    typeStr = R.string.event_type_other;
                    break;
                case Event.EVENT_TYPE_OUTSIDE:
                    typeStr = R.string.event_type_outside;
                    break;
            }
            vh.tv_event_type.setText(typeStr);
        }

        vh.tv_event_title.setTextColor(UiCompat.getColor(resources,
                mReadState.already(item.getKey())
                        ? R.color.text_desc_color : R.color.text_title_color));

    }

    private void setText(TextView tv, int textRes, int bgRes, int textColor) {
        tv.setText(textRes);
        tv.setVisibility(View.VISIBLE);
        tv.setBackgroundResource(bgRes);
        tv.setTextColor(textColor);
    }

    private void setTextColor(TextView tv, int textColor) {
        tv.setTextColor(textColor);
        tv.setVisibility(View.VISIBLE);
    }

    private static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tv_event_title, tv_description, tv_event_pub_date, tv_event_member, tv_event_state, tv_event_type;
        ImageView iv_event;

        public EventViewHolder(View itemView) {
            super(itemView);
            tv_event_title = (TextView) itemView.findViewById(R.id.tv_event_title);
            tv_event_state = (TextView) itemView.findViewById(R.id.tv_event_state);
            tv_event_type = (TextView) itemView.findViewById(R.id.tv_event_type);
            tv_description = (TextView) itemView.findViewById(R.id.tv_description);
            tv_event_pub_date = (TextView) itemView.findViewById(R.id.tv_event_pub_date);
            tv_event_member = (TextView) itemView.findViewById(R.id.tv_event_member);
            iv_event = (ImageView) itemView.findViewById(R.id.iv_event);
        }
    }

}
