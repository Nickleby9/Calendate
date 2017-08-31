package com.calendate.calendate;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickImageFragment extends DialogFragment {

    RecyclerView rvIcons;
    FirebaseStorage mStorage = FirebaseStorage.getInstance();
    FirebaseDatabase mDatabase;
    BitmapDrawable drawable;
    ArrayList<String> imageUrls = new ArrayList<>();
    static private OnImageSetListener mListener;
    static String btnId;

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
        ImageAdapter adapter = new ImageAdapter(mDatabase.getReference("icons"), this, getContext());
        rvIcons.setAdapter(adapter);
    }

    private static class ImageAdapter extends FirebaseRecyclerAdapter<String, ImageAdapter.ImageViewHolder> {

        DialogFragment dialog;
        Context context;

        public ImageAdapter(Query query, DialogFragment dialog, Context context) {
            super(String.class, R.layout.icon_item, ImageViewHolder.class, query);
            this.dialog = dialog;
            this.context = context;
        }

        @Override
        protected void populateViewHolder(final ImageViewHolder viewHolder, String model, int position) {
            StorageReference mStorage = FirebaseStorage.getInstance().getReference("button-icons").child(model);
            mStorage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    Glide.with(context).load(task.getResult()).into(viewHolder.ivIcon);
                    viewHolder.task = task;
                }
            });
            viewHolder.mStorage = mStorage;
            viewHolder.dialog = dialog;
        }

        public static class ImageViewHolder extends RecyclerView.ViewHolder {

            ImageView ivIcon;
            String url;
            StorageReference mStorage;
            DialogFragment dialog;
            Bitmap image;
            Task<Uri> task;


            public ImageViewHolder(final View itemView) {
                super(itemView);
                ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        CompositeDisposable disposables = new CompositeDisposable();
                        dialog.dismiss();

                        disposables.add(imageDownloader(task)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableObserver<Bitmap>() {
                                    @Override
                                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {

                                    }

                                    @Override
                                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        saveImage(btnId, image, v.getContext());
                                        mListener.onImageSet(mStorage, btnId, task.getResult());
                                    }
                                }));
                    }
                });


            }

            Observable<Bitmap> imageDownloader(final Task<Uri> task) {
                return Observable.defer(new Callable<ObservableSource<? extends Bitmap>>() {
                    @Override public ObservableSource<? extends Bitmap> call() throws Exception {
                        try {
                            image = Glide.with(itemView.getContext()).asBitmap().load(task.getResult()).submit().get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        return Observable.just(image);
                    }
                });
            }

            private void saveImage(String btnId, Bitmap bitmap, Context context){
                ContextWrapper cw = new ContextWrapper(context);
                File dir = new File("");
                dir = cw.getDir("icons", Context.MODE_PRIVATE);
                File myPath = new File(dir, btnId + ".png");
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(myPath);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }
}
