package com.calendate.calendate.caldroid;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.calendate.calendate.DetailedItem;
import com.calendate.calendate.MyUtils;
import com.calendate.calendate.R;
import com.calendate.calendate.models.EventRow;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

class EventsAdapter extends FirebaseRecyclerAdapter<EventRow, EventsAdapter.EventViewHolder> {

    String date;

    public EventsAdapter(Query query, String date) {
        super(EventRow.class, R.layout.event_item, EventsAdapter.EventViewHolder.class, query);
        this.date = date;
    }

    @Override
    protected void populateViewHolder(EventsAdapter.EventViewHolder viewHolder, EventRow model, int position) {
        if (model.getDate().equals(date)) {
            viewHolder.tvTitle.setText(model.getTitle());
            LocalDateTime dateTime = LocalDateTime.parse(model.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
            viewHolder.tvDate.setText(dateTime.toString(MyUtils.btnDateFormat));
            viewHolder.model = model;
        } else {

        }
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        EventRow model;

        public EventViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), DetailedItem.class);
                    intent.putExtra("model", model);
                    v.getContext().startActivity(intent);
                }
            });
        }

    }
}
