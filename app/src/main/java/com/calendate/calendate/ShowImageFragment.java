package com.calendate.calendate;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShowImageFragment extends DialogFragment {

    ImageView ivShowDoc;
    File file;
    ProgressBar progressBar;

    public ShowImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        file = (File) getArguments().getSerializable("image");
        ivShowDoc = (ImageView) view.findViewById(R.id.ivShowDoc);
        Picasso.with(view.getContext()).load(Uri.fromFile(file)).into(ivShowDoc);
    }

    public static ShowImageFragment newInstance(File file) {

        Bundle args = new Bundle();
        args.putSerializable("image", file);
        ShowImageFragment fragment = new ShowImageFragment();
        fragment.setArguments(args);
        return fragment;
    }


}
