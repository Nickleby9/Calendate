package com.calendate.calendate;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.calendate.calendate.models.Event;
import com.calendate.calendate.models.EventRow;
import com.calendate.calendate.touchHelper.CallBack;
import com.calendate.calendate.utils.MyUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TimelineFragment extends Fragment {

    RecyclerView recycler;
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    ArrayList<Event> events = new ArrayList<>();
    EventRow model;

    public TimelineFragment() {
        // Required empty public constructor
    }

    ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timelife, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recycler = (RecyclerView) view.findViewById(R.id.recycler);

        EventAdapter adapter = new EventAdapter(mDatabase.getReference("all_events/" + user.getUid()).orderByChild("date"));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                CallBack(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT, adapter, getContext()); // Making the SimpleCallback
        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        touchHelper.attachToRecyclerView(recycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);
    }

    static class EventAdapter extends FirebaseRecyclerAdapter<EventRow, DetailActivity.EventAdapter.EventViewHolder> {

        public EventAdapter(Query query) {
            super(EventRow.class, R.layout.event_item, DetailActivity.EventAdapter.EventViewHolder.class, query);
        }

        @Override
        protected void populateViewHolder(DetailActivity.EventAdapter.EventViewHolder viewHolder, EventRow model, int position) {
            viewHolder.tvTitle.setText(model.getTitle());
            LocalDateTime dateTime = LocalDateTime.parse(model.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
            viewHolder.tvDate.setText(dateTime.toString(MyUtils.btnDateFormat));
            viewHolder.model = model;
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
                        Intent intent = new Intent(v.getContext(), DetailedItemActivity.class);
                        intent.putExtra("model", model);
                        v.getContext().startActivity(intent);
                    }
                });
            }

            void optionsDialog(final View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle(R.string.change_button_dialog_title)
                        .setItems(R.array.itemOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        //Share
                                        UserListFragment userListFragment = new UserListFragment();
                                        dialog.dismiss();
                                        if (v.getContext() instanceof FragmentActivity) {
                                            Bundle bundle = new Bundle();
                                            bundle.putParcelable("model", model);
                                            userListFragment.setArguments(bundle);
                                            userListFragment.show(((FragmentActivity) v.getContext()).getSupportFragmentManager(), "fragment");
                                        }
                                        break;
                                    case 1:
                                        //Delete
                                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                                        builder.setTitle(R.string.confirm_delete)
                                                .setMessage(R.string.confirm_delete)
                                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                        mDatabase.getReference("all_events/" + user.getUid() + "/" + model.getEventUID()).removeValue();
                                                        mDatabase.getReference("events/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                                    for (DataSnapshot event : snapshot.getChildren()) {
                                                                        if (event.getKey().equals(model.getEventUID())){
                                                                            event.getRef().removeValue();
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        dialogInterface.dismiss();
                                                    }
                                                })
                                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                }).show();
                                        break;
                                }
                            }
                        }).show();
            }

        }
    }
}
