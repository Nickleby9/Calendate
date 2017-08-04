package com.calendate.calendate;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.calendate.calendate.models.Friend;
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

    public FriendListFragment() {
        // Required empty public constructor
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Friend friend = snapshot.getValue(Friend.class);
                    if (friend.isApproved()){
                        friends.add(friend);
                    }
                    FriendsAdapter adapter = new FriendsAdapter(getContext(), friends);
                    rvFriends.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>{

        Context context;
        LayoutInflater inflater;
        ArrayList<Friend> data;

        public FriendsAdapter(Context context, ArrayList<Friend> data) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.data = data;
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
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class FriendsViewHolder extends RecyclerView.ViewHolder{

            TextView tvUsername, tvEmail;

            public FriendsViewHolder(View itemView) {
                super(itemView);

                tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
                tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            }
        }
    }

    /*
    public static class FriendsAdapter extends FirebaseRecyclerAdapter<Friend, FriendsViewHolder> {

        public FriendsAdapter(Query query) {
            super(Friend.class, R.layout.friend_item, FriendsViewHolder.class, query);
        }

        @Override
        protected void populateViewHolder(FriendsViewHolder viewHolder, Friend model, int position) {
            viewHolder.tvUsername.setText(model.getSenderUsername());
            viewHolder.tvEmail.setText(model.getSenderEmail());
        }
    }

    private static class FriendsViewHolder extends RecyclerView.ViewHolder {

        TextView tvUsername, tvEmail;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            tvEmail = (TextView) itemView.findViewById(R.id.tvEmail);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
        }
    }
    */
}
