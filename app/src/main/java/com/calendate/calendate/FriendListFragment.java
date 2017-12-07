package com.calendate.calendate;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.calendate.calendate.models.Friend;
import com.calendate.calendate.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
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
public class FriendListFragment extends Fragment {

    RecyclerView rvFriends;
    FloatingActionButton fabAdd;
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    ArrayList<Friend> friends = new ArrayList<>();
    TextView tvNoFriends;
    FriendsAdapter adapter;
    private OnRemainAnonymous mListener;


    public FriendListFragment() {
        // Required empty public constructor
    }

    public interface OnRemainAnonymous{
        void onRemainAnonymous(String choice);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRemainAnonymous) {
            mListener = (OnRemainAnonymous) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnRemainAnonymous");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friend_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        User mUser = new User(user);
        tvNoFriends = (TextView) view.findViewById(R.id.tvNoFriends);
        tvNoFriends.setVisibility(View.INVISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.friends_title);

        if (user.isAnonymous()){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.oops);
            builder.setMessage(R.string.friends_anonymous_error);
            builder.setPositiveButton(R.string.register_me, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onRemainAnonymous("register");
                }
            }).setNegativeButton(R.string.got_it, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    mListener.onRemainAnonymous("remain");
                }
            }).setCancelable(false).show();
        }
        if (user.isAnonymous()){
            return;
        }
        rvFriends = (RecyclerView) view.findViewById(R.id.rvFriends);

        fabAdd = (FloatingActionButton) view.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().beginTransaction().replace(R.id.frame, new AddFriendsFragment()).commit();
            }
        });

        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        mDatabase.getReference("friends/" + user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friends.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Friend friend = snapshot.getValue(Friend.class);
                    if (friend != null && friend.isApproved()) {
                        friends.add(friend);
                    }
                }
                adapter = new FriendsAdapter(getContext(), friends, user);
                rvFriends.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (adapter == null || adapter.getItemCount() == 0)
            tvNoFriends.setVisibility(View.VISIBLE);
    }

    public static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

        Context context;
        LayoutInflater inflater;
        ArrayList<Friend> data;
        FirebaseUser user;

        public FriendsAdapter(Context context, ArrayList<Friend> data, FirebaseUser user) {
            this.context = context;
            if (context != null)
                this.inflater = LayoutInflater.from(context);
            this.data = data;
            this.user = user;
        }

        @Override
        public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.friend_item, parent, false);
            return new FriendsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(FriendsViewHolder holder, int position) {
            holder.tvUsername.setText(data.get(position).getSenderUsername());
            holder.tvEmail.setText(data.get(position).getSenderEmail());
            holder.friend = data.get(position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class FriendsViewHolder extends RecyclerView.ViewHolder {

            TextView tvUsername, tvEmail;
            Friend friend;


            public FriendsViewHolder(final View itemView) {
                super(itemView);

                tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
                tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                        builder.setTitle(R.string.cancel_friendship)
                                .setMessage((context.getString(R.string.cancel_friendship_msg)) + " " + tvUsername.getText() + "?")
                                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseDatabase.getInstance().getReference("friends/" + user.getUid() + "/" + friend.getSenderUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FirebaseDatabase.getInstance().getReference("friends/" + friend.getSenderUid() + "/" + user.getUid()).removeValue();
                                    }
                                });
                                dialogInterface.dismiss();
                            }
                        }).show();
                    }
                });
            }
        }
    }
}
