package com.calendate.calendate;

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

        btnId = getIntent().getStringExtra("btnId");
        String btnTitle = getIntent().getStringExtra("btnTitle");
        ((AppCompatActivity) this).getSupportActionBar().setTitle(btnTitle);

        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        recycler = (RecyclerView) findViewById(R.id.recycler);
        tvNoEvents = (TextView) findViewById(R.id.tvNoEvents);
        tvNoEvents.setVisibility(View.INVISIBLE);

//        DatabaseReference query = mDatabase.getReference("events/" + user.getUid() + "/" + btnId);
        DatabaseReference query = mDatabase.getReference("all_events/" + user.getUid());

        EventAdapter adapter = new EventAdapter(query.orderByChild("btnId").equalTo(btnId));
        recycler.setLayoutManager(new LinearLayoutManager(this));


        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new
                CallBack(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT, adapter); // Making the SimpleCallback

        ItemTouchHelper touchHelper = new ItemTouchHelper(simpleItemTouchCallback);

        touchHelper.attachToRecyclerView(recycler);

        recycler.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), AddItemActivity.class);
                intent.putExtra("btnId", btnId);
                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (recycler.getChildCount() == 0)
            tvNoEvents.setVisibility(View.VISIBLE);
    }


    static class EventAdapter extends FirebaseRecyclerAdapter<EventRow, EventAdapter.EventViewHolder> {


        public EventAdapter(Query query) {
            super(EventRow.class, R.layout.event_item, EventViewHolder.class, query);
        }

        @Override
        protected void populateViewHolder(EventViewHolder viewHolder, EventRow model, int position) {
            viewHolder.tvTitle.setText(model.getTitle());
            LocalDateTime dateTime = LocalDateTime.parse(model.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
            viewHolder.tvDate.setText(dateTime.toString(MyUtils.btnDateFormat) + " - " + model.getTime());
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
                                mDatabase.getReference("all_events/" + user.getUid() + "/" + model.getEventUID()).removeValue();
                                mDatabase.getReference("events/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            for (DataSnapshot event : snapshot.getChildren()) {
                                                if (event.getKey().equals(model.getEventUID())) {
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

        }
    }
}
