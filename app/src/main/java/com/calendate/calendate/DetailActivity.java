package com.calendate.calendate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.calendate.calendate.models.Alert;
import com.calendate.calendate.models.Event;
import com.calendate.calendate.models.EventRow;
import com.calendate.calendate.touchHelper.CallBack;
import com.calendate.calendate.utils.MyUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity {

    RecyclerView recycler;
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    static String btnId;
    TextView tvNoEvents;
    EventAdapter adapter;
    static String btnTitle;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("btnId", btnId);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnTitle = getIntent().getStringExtra("btnTitle");
        btnId = getIntent().getStringExtra("btnId");
        final String btnTitle = getIntent().getStringExtra("btnTitle");
        int tvNum = getIntent().getIntExtra("tvNum", 0);
        ((AppCompatActivity) this).getSupportActionBar().setTitle(btnTitle);

        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recycler = (RecyclerView) findViewById(R.id.recycler);
        tvNoEvents = (TextView) findViewById(R.id.tvNoEvents);
        tvNoEvents.setVisibility(View.INVISIBLE);
        if (tvNum == 0)
            tvNoEvents.setVisibility(View.VISIBLE);

//        DatabaseReference query = mDatabase.getReference("events/" + user.getUid() + "/" + btnId);
        DatabaseReference query = mDatabase.getReference("all_events/" + user.getUid());
        adapter = new EventAdapter(query.orderByChild("btnId").equalTo(btnId), this);


        recycler.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                CallBack(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT, adapter, this); // Making the SimpleCallback

        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        touchHelper.attachToRecyclerView(recycler);

        recycler.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddItemActivity.class);
                intent.putExtra("btnId", btnId);
                intent.putExtra("btnTitle", btnTitle);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }


    static class EventAdapter extends FirebaseRecyclerAdapter<EventRow, EventAdapter.EventViewHolder> {

        Context context;

        public EventAdapter(Query query, Context context) {
            super(EventRow.class, R.layout.event_item, EventViewHolder.class, query);
            this.context = context;
        }

        @Override
        protected void populateViewHolder(EventViewHolder viewHolder, EventRow model, int position) {
            viewHolder.tvTitle.setText(model.getTitle());
            LocalDateTime dateTime = LocalDateTime.parse(model.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
            viewHolder.tvDate.setText(dateTime.toString(MyUtils.btnDateFormat) + " - " + model.getTime());
            viewHolder.model = model;
            viewHolder.context = this.context;
        }


        public static class EventViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvDate;
            EventRow model;
            Context context;

            public EventViewHolder(View itemView) {
                super(itemView);
                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
                tvDate = (TextView) itemView.findViewById(R.id.tvDate);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), DetailedItemActivity.class);
                        intent.putExtra("model", model);
                        intent.putExtra("btnTitle", btnTitle);
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
                                        deleteDialog();
                                        break;
                                }
                            }
                        }).show();
            }

            private void deleteDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle(R.string.confirm_delete)
                        .setMessage(R.string.confirm_delete)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                                mDatabase.getReference("all_events/" + user.getUid() + "/" + model.getEventUID()).removeValue();
                                mDatabase.getReference("all_events/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            for (DataSnapshot event : snapshot.getChildren()) {
                                                if (event.getKey().equals(model.getEventUID())) {
                                                    Event eventValue = event.getValue(Event.class);
                                                    clearNotifications(eventValue);
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
            }

            private void clearNotifications(Event eventValue) {
                ArrayList<Alert> alerts = eventValue.getAlerts();

                for (int j = 0; j < alerts.size(); j++) {
                    int id = alerts.get(j).getId();
                    AlarmManager alarm = (AlarmManager) context.getSystemService(ALARM_SERVICE);

                    if (alarm != null) {
                        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                                context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        alarm.cancel(pendingIntent);
                    } else
                        Toast.makeText(context, R.string.no_alarm_service, Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
}
