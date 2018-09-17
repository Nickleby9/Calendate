package com.calendate.calendate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
        if (tvNum == 0)
            tvNoEvents.setVisibility(View.VISIBLE);

//        DatabaseReference query = mDatabase.getReference("events/" + user.getUid() + "/" + btnId);
        DatabaseReference query = mDatabase.getReference("all_events/" + user.getUid());
        adapter = new EventAdapter(query.orderByChild("btnId").equalTo(btnId), this, user.getUid());


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
        String userId;

        public EventAdapter(Query query, Context context, String userId) {
            super(EventRow.class, R.layout.event_item, EventViewHolder.class, query);
            this.context = context;
            this.userId = userId;
        }

        @Override
        protected void populateViewHolder(EventViewHolder viewHolder, EventRow model, int position) {
            viewHolder.tvTitle.setText(model.getTitle());
            LocalDateTime dateTime = LocalDateTime.parse(model.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
            viewHolder.tvDate.setText(dateTime.toString(MyUtils.btnDateFormat) + "\n" + model.getTime());
            viewHolder.model = model;
            viewHolder.context = this.context;
            viewHolder.userId = this.userId;
        }


        public static class EventViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvDate;
            EventRow model;
            Context context;
            String userId;

            public EventViewHolder(View itemView) {
                super(itemView);
                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
                tvDate = (TextView) itemView.findViewById(R.id.tvDate);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        FirebaseDatabase.getInstance().getReference("all_events/" + userId + "/" + model.getEventUID()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                    Event event = dataSnapshot.getValue(Event.class);
                                    if (event != null && event.isAccessible()){
                                        Intent intent = new Intent(v.getContext(), DetailedItemActivity.class);
                                        intent.putExtra("eventKey", model.getEventUID());
                                        intent.putExtra("btnId", model.getBtnId());
                                        intent.putExtra("source", "categories");
                                        v.getContext().startActivity(intent);
                                    } else {
                                        Toast.makeText(context, R.string.upload_in_progress, Toast.LENGTH_SHORT).show();
                                    }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }

        }
    }
}
