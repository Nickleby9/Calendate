package com.calendate.calendate;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickImageFragment extends DialogFragment {

    RecyclerView rvIcons;
    FirebaseDatabase mDatabase;
    static private OnImageSetListener mListener;
    static String btnId;
    ArrayList<Drawable> icons = new ArrayList<>();
    ArrayList<String> iconText = new ArrayList<>();

    public PickImageFragment() {
        // Required empty public constructor
    }

    public interface OnImageSetListener {
        void onImageSet(StorageReference mStorage, String btnId, Uri uri);
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
//        mListener = null;
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
        mDatabase = FirebaseDatabase.getInstance();
        btnId = getArguments().getString("btnId");

        rvIcons.setLayoutManager(new GridLayoutManager(getContext(), 4));

        for (int i = 1; i<50; i++){
            int rid = getContext().getResources().getIdentifier("icon_" + i, "drawable",
                            getContext().getPackageName());
            Drawable drawable = getResources().getDrawable(rid);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), rid);
            Bitmap.createScaledBitmap(bitmap,50,50,false);
            icons.add(drawable);
            iconText.add("icon_" + i);
        }

        IconsAdapter adapter = new IconsAdapter(icons, getContext(), btnId, iconText, this);
        rvIcons.setAdapter(adapter);

    }

    private class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.IconsViewHolder>{

        ArrayList<Drawable> data = new ArrayList<>();
        LayoutInflater inflater;
        Context context;
        String btnId;
        ArrayList<String> iconText;
        DialogFragment dialog;

        public IconsAdapter(ArrayList<Drawable> data, Context context, String btnId, ArrayList<String> iconText, DialogFragment dialog) {
            this.data = data;
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.btnId = btnId;
            this.iconText = iconText;
            this.dialog = dialog;
        }

        @Override
        public IconsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new IconsViewHolder(inflater.inflate(R.layout.icon_item, parent, false));
        }

        @Override
        public void onBindViewHolder(IconsViewHolder holder, int position) {
            Drawable icon = data.get(position);
            holder.ivIcons.setImageDrawable(icon);
            holder.btnId = btnId;
            holder.iconText = iconText.get(position);
            holder.me = dialog;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class IconsViewHolder extends RecyclerView.ViewHolder{

            ImageView ivIcons;
            String btnId;
            String iconText;
            DialogFragment me;
            FirebaseUser user;

            public IconsViewHolder(View itemView) {
                super(itemView);

                ivIcons = (ImageView) itemView.findViewById(R.id.ivIcon);
                user = FirebaseAuth.getInstance().getCurrentUser();

                ivIcons.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseDatabase.getInstance().getReference("button_images/" + user.getUid()).child(btnId).setValue(iconText);
                        me.dismiss();
                    }
                });
            }
        }
    }
}
