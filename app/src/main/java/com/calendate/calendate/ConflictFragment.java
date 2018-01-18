package com.calendate.calendate;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.calendate.calendate.models.Event;
import com.calendate.calendate.utils.MyUtils;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConflictFragment extends BottomSheetDialogFragment {

    ArrayList<Event> events = new ArrayList<>();
    RecyclerView rvConflicts;
    ConflictsAdapter adapter;

    public ConflictFragment() {
        // Required empty public constructor
    }

    public static ConflictFragment newInstance(ArrayList<Event> events) {
        Bundle args = new Bundle();
        args.putParcelableArrayList("events", events);
        ConflictFragment fragment = new ConflictFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_conflict, container, false);
        events = getArguments().getParcelableArrayList("events");
        rvConflicts = (RecyclerView) v.findViewById(R.id.rvConflicts);
        rvConflicts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ConflictsAdapter(getContext(), events);
        rvConflicts.setAdapter(adapter);
        return (v);
    }

    static class ConflictsAdapter extends RecyclerView.Adapter<ConflictsAdapter.ConflictsViewHolder> {

        Context context;
        ArrayList<Event> events;
        LayoutInflater inflater;

        public ConflictsAdapter(Context context, ArrayList<Event> events) {
            this.context = context;
            this.events = events;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public ConflictsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.event_item, parent, false);
            return new ConflictsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ConflictsViewHolder holder, int position) {
            holder.tvTitle.setText(events.get(position).getTitle());
            LocalDateTime dateTime = LocalDateTime.parse(events.get(0).getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
            String date = dateTime.toString(MyUtils.btnDateFormat);
            String time = events.get(position).getTime();
            holder.tvDate.setText(date + " - " + time);
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        public class ConflictsViewHolder extends RecyclerView.ViewHolder {

            TextView tvDate;
            TextView tvTitle;
            ImageView ivArrow;

            public ConflictsViewHolder(View itemView) {
                super(itemView);

                tvDate = (TextView) itemView.findViewById(R.id.tvDate);
                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
                ivArrow = (ImageView) itemView.findViewById(R.id.ivArrow);
                ivArrow.setVisibility(View.GONE);
            }
        }
    }
}
