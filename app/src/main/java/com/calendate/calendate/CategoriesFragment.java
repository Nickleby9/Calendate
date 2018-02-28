package com.calendate.calendate;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.calendate.calendate.models.Event;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoriesFragment extends BottomSheetDialogFragment {

    RecyclerView rvCategories;
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    Event model;
    String source, btnId;
    int year = 0, month = 0, day = 0;

    public CategoriesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        rvCategories = (RecyclerView) view.findViewById(R.id.rvCategories);
        source = getArguments().getString("source");
        if (source.equals("share"))
            model = getArguments().getParcelable("event");
        else {
            btnId = getArguments().getString("btnId");
            year = getArguments().getInt("year");
            month = getArguments().getInt("month");
            day = getArguments().getInt("day");
        }

        CategoriesAdapter adapter = new CategoriesAdapter(mDatabase.getReference("buttons/" + user.getUid()), model, this, source, year, month, day);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(adapter);
    }

    private static class CategoriesAdapter extends FirebaseRecyclerAdapter<String, CategoriesAdapter.CategoriesViewHolder> {

        Event model;
        Fragment fragment;
        String source;
        int year = 0, month = 0, day = 0;

        public CategoriesAdapter(Query query, Event model, Fragment fragment, String source, int year, int month, int day) {
            super(String.class, R.layout.category_item, CategoriesViewHolder.class, query);
            this.model = model;
            this.fragment = fragment;
            this.source = source;
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @Override
        public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new CategoriesViewHolder(v, fragment);
        }

        @Override
        protected void populateViewHolder(CategoriesViewHolder viewHolder, String model, int position) {
            viewHolder.tvCategory.setText(model);
            viewHolder.model = this.model;
            viewHolder.btnId = model;
            viewHolder.source = source;
            viewHolder.year = year;
            viewHolder.month = month;
            viewHolder.day = day;
        }

        public class CategoriesViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategory;
            Event model;
            String btnId;
            FirebaseUser user;
            String source;
            int year = 0, month = 0, day = 0;

            public CategoriesViewHolder(View itemView, final Fragment fragment) {
                super(itemView);

                tvCategory = (TextView) itemView.findViewById(R.id.tvCategory);
                user = FirebaseAuth.getInstance().getCurrentUser();

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                            FirebaseDatabase.getInstance().getReference("buttons/" + user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String btnValue = snapshot.getValue(String.class);
                                        if (btnValue.equals(btnId)) {
                                            btnId = snapshot.getKey();
                                            if (source.equals("share")) {
                                            FirebaseDatabase.getInstance().getReference("shared_events/" + user.getUid()).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        Event event = snapshot.getValue(Event.class);
                                                        if (event.getEventUID().equals(model.getEventUID())) {
                                                            event.setBtnId(btnId);
                                                            event.setOwn(true);/*
                                                        model.setBtnId(btnId);
                                                        model.setOwn(true);*/
                                                            FirebaseDatabase.getInstance().getReference("all_events/" + user.getUid()).child(event.getEventUID()).setValue(event);
                                                            snapshot.getRef().removeValue();
                                                            if (fragment instanceof DialogFragment) {
                                                                ((DialogFragment) fragment).dismiss();
                                                            }
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }  else if (source.equals("calendar")){
                                                Intent intent = new Intent(v.getContext(), AddItemActivity.class);
                                                intent.putExtra("year", year);
                                                intent.putExtra("month", month);
                                                intent.putExtra("day", day);
                                                v.getContext().startActivity(intent);
                                            }
                                        }
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
