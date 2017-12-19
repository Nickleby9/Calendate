package com.calendate.calendate;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;

import com.calendate.calendate.models.Event;
import com.calendate.calendate.models.EventRow;
import com.calendate.calendate.models.Friend;
import com.calendate.calendate.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserListFragment extends BottomSheetDialogFragment {

    private static final String ARG_EVENT = "model";
    RecyclerView rvUsers;
    FirebaseDatabase mDatabase;
    ArrayList<User> users = new ArrayList<>();
    FirebaseUser currentUser;
    EventRow model;

    public UserListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        model = getArguments().getParcelable(ARG_EVENT);

        rvUsers = (RecyclerView) view.findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        final Query ref = FirebaseDatabase.getInstance().getReference("friends/" + currentUser.getUid());
        rvUsers.setAdapter(new UserAdapter(ref, model, this));

    }

    private class UserAdapter extends FirebaseRecyclerAdapter<Friend, UserViewHolder> {

        EventRow eventRow = null;
        Fragment fragment;

        public UserAdapter(Query query, EventRow eventRow, Fragment fragment) {
            super(Friend.class, R.layout.user_item, UserViewHolder.class, query);
            this.eventRow = eventRow;
            this.fragment = fragment;
        }

        @Override
        public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new UserViewHolder(v, fragment);
        }

        @Override
        protected void populateViewHolder(UserViewHolder viewHolder, Friend model, int position) {
            Context context = viewHolder.tvUsername.getContext();
            viewHolder.tvUsername.setText(model.getSenderUsername());
            viewHolder.tvEmail.setText(model.getSenderEmail());
            viewHolder.user = model;
            viewHolder.eventRow = eventRow;
        }
    }

    private static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername;
        TextView tvEmail;
        Friend user;
        FirebaseUser currentUser;
        EventRow eventRow;
        Fragment fragment;
        Event event;

        public UserViewHolder(View itemView, final Fragment fragment) {
            super(itemView);
            this.fragment = fragment;
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    FirebaseDatabase.getInstance().getReference("all_events").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.getKey().equals(eventRow.getEventUID())) {
                                    event = snapshot.getValue(Event.class);
                                    event.setOwn(false);
                                    event.setCreator(currentUser.getDisplayName());
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("shared_events").child(user.getSenderUid()).child(event.getEventUID());
                                    ref.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(v.getContext(), R.string.successful_share, Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(v.getContext(), "Server error", Toast.LENGTH_SHORT).show();
                                            }
                                            if (fragment instanceof DialogFragment) {
                                                ((DialogFragment) fragment).dismiss();
                                            }
                                        }
                                    });

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