package com.calendate.calendate;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.calendate.calendate.models.Friend;
import com.calendate.calendate.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddFriendsFragment extends Fragment {

    AutoCompleteTextView autoTv;
    RecyclerView rvUsers;
    FirebaseDatabase mDatabase;
    User user;
    FirebaseUser currentUser;
    ArrayList<String> usernames = new ArrayList<>();
    ArrayList<User> users = new ArrayList<>();
    ArrayList<User> adapterUsers = new ArrayList<>();
    ImageView ivSearch;

    public AddFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        autoTv = (AutoCompleteTextView) view.findViewById(R.id.autoTv);
        rvUsers = (RecyclerView) view.findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, usernames);

        mDatabase.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    user = snapshot.getValue(User.class);
                    if (!user.getEmail().equals(currentUser.getEmail())) {
                        users.add(user);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        autoTv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                usernames.clear();
                adapterUsers.clear();
                UsersAdapter usersAdapter = new UsersAdapter(getContext(), adapterUsers);
                if (charSequence.length() > 1 || charSequence.length() == 0) {
                    for (User singleUser : users) {
                        if (singleUser.getUsername().toLowerCase().contains(charSequence.toString().toLowerCase())) {
                            if (usernames.contains(singleUser.getUsername().toLowerCase())) {
                                return;
                            }
                            if (charSequence.toString().isEmpty()) {
                                rvUsers.setAdapter(usersAdapter);
                                return;
                            }
                            usernames.add(singleUser.getUsername().toLowerCase());
                            adapter.notifyDataSetChanged();
                            adapterUsers.add(singleUser);
                        }
                    }
                    rvUsers.setAdapter(usersAdapter);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

        Context context;
        LayoutInflater inflater;
        ArrayList<User> data;

        public UsersAdapter(Context context, ArrayList<User> data) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.friend_item, parent, false);
            return new UsersViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final UsersViewHolder holder, final int position) {
            holder.tvUsername.setText(data.get(position).getUsername());
            holder.tvEmail.setText(data.get(position).getEmail());
            holder.user = data.get(position);
            FirebaseDatabase.getInstance().getReference
                    ("friends/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener
                    (new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Friend friend = snapshot.getValue(Friend.class);
                                if (friend.getSenderUid().equals(holder.user.getUid()) && friend.isApproved()) {
                                    holder.ivApproved.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public static class UsersViewHolder extends RecyclerView.ViewHolder {

            TextView tvUsername;
            TextView tvEmail;
            User user;
            FirebaseUser currentUser;
            ImageView ivApproved;

            public UsersViewHolder(final View itemView) {
                super(itemView);
                tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
                tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
                currentUser = FirebaseAuth.getInstance().getCurrentUser();
                ivApproved = (ImageView) itemView.findViewById(R.id.ivApproved);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (ivApproved.getVisibility() == View.INVISIBLE) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                            builder.setTitle(R.string.add_friend)
                                    .setMessage(itemView.getContext().getString(R.string.approve_send_request) + " " + user.getUsername())
                                    .setPositiveButton(R.string.send_request, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Friend friend = new Friend();
                                            friend.setFriendEmail(user.getEmail());
                                            friend.setApproved(false);
                                            friend.setFriendUid(user.getUid());
                                            friend.setFriendUsername(user.getUsername());
                                            friend.setSenderUsername(currentUser.getDisplayName());
                                            friend.setSenderUid(currentUser.getUid());
                                            friend.setSenderEmail(currentUser.getEmail());
                                            FirebaseDatabase.getInstance().getReference("friends/" + friend.getFriendUid() + "/" + currentUser.getUid()).setValue(friend);
                                            Toast.makeText(view.getContext(), R.string.request_sent, Toast.LENGTH_SHORT).show();
                                            dialogInterface.dismiss();
                                        }
                                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();
                        } else {
                            Toast.makeText(view.getContext(), R.string.already_friends, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
