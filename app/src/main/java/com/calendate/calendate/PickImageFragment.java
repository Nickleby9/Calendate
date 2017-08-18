package com.calendate.calendate;


import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickImageFragment extends DialogFragment {

    RecyclerView rvIcons;
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference mStorageRef;
    FirebaseDatabase mDatabase;
    BitmapDrawable drawable;
    ArrayList<String> imageUrls = new ArrayList<>();
    static private OnImageSetListener mListener;
    static String btnId;

    public PickImageFragment() {
        // Required empty public constructor
    }

    public interface OnImageSetListener {
        void onImageSet(StorageReference mStorage, String btnId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnImageSetListener) {
            mListener = (OnImageSetListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnImageSetListener");
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
        return inflater.inflate(R.layout.fragment_pick_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvIcons = (RecyclerView) view.findViewById(R.id.rvIcons);
        mStorageRef = FirebaseStorage.getInstance().getReference().child("button-icons/approval.png");
        mDatabase = FirebaseDatabase.getInstance();
        btnId = getArguments().getString("btnId");

        rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 4));
        ImageAdapter adapter = new ImageAdapter(mDatabase.getReference("icons"), this);
        rvIcons.setAdapter(adapter);
    }

    private static class ImageAdapter extends FirebaseRecyclerAdapter<String, ImageAdapter.ImageViewHolder> {

        DialogFragment dialog;

        public ImageAdapter(Query query, DialogFragment dialog) {
            super(String.class, R.layout.icon_item, ImageViewHolder.class, query);
            this.dialog = dialog;
        }

        @Override
        protected void populateViewHolder(final ImageViewHolder viewHolder, String model, int position) {
            StorageReference mStorage = FirebaseStorage.getInstance().getReference("button-icons").child(model);
            mStorage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Glide.with(viewHolder.ivIcon.getContext()).load(task.getResult()).into(viewHolder.ivIcon);
//                   Picasso.with(viewHolder.ivIcon.getContext()).load(model).into(viewHolder.ivIcon);
                }
            });
            viewHolder.mStorage = mStorage;
            viewHolder.dialog = dialog;
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder{

            ImageView ivIcon;
            String url;
            StorageReference mStorage;
            DialogFragment dialog;

            public ImageViewHolder(View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.onImageSet(mStorage, btnId);
                        dialog.dismiss();
                    }
                });
            }
        }
    }
}
