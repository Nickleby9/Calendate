package com.calendate.calendate;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.bumptech.glide.Glide;
import com.calendate.calendate.utils.CustomBootstrapStyleDark;
import com.calendate.calendate.utils.CustomBootstrapStyleLight;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rd.PageIndicatorView;
import com.rd.animation.type.AnimationType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class ButtonsFragment extends Fragment {

    ViewPager viewPager;
    PlaceholderFragment.SectionsPagerAdapter mSectionsPagerAdapter;
    FirebaseDatabase mDatabase;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Bitmap image;
    PageIndicatorView dotsIndicator;

    public ButtonsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.buttons_container_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.categories_title);
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        dotsIndicator = (PageIndicatorView) view.findViewById(R.id.indicator);
        mSectionsPagerAdapter = new PlaceholderFragment.SectionsPagerAdapter(getChildFragmentManager());

        if (mAuth.getCurrentUser() != null)
            viewPager.setAdapter(mSectionsPagerAdapter);
        int fragNumToGo = getArguments().getInt("fragNum", 0);
        if (getArguments().getParcelable("image") != null) {
            image = getArguments().getParcelable("image");
        }

        viewPager.setCurrentItem(fragNumToGo);

        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("all_events").child(user.getUid());
            ref.keepSynced(true);
        }

        if (getArguments().getString("storage") != null) {
            PlaceholderFragment.newInstance(getArguments().getString("storage"), getArguments().getString("btnId"), (Uri) getArguments().getParcelable("uri"));
            int frag = getArguments().getInt("fragNum", 0);
            viewPager.setCurrentItem(frag);
        }
        dotsIndicator.setViewPager(viewPager);
        dotsIndicator.setCount(2);
        dotsIndicator.setSelectedColor(Color.parseColor("#039be5"));
        dotsIndicator.setUnselectedColor(Color.parseColor("#B3E5FC"));
        dotsIndicator.setAnimationType(AnimationType.SLIDE);
        dotsIndicator.setRadius(5);
    }

    public static ButtonsFragment newInstance(String path, final String btnId, Uri uri, int fragNum) {

        Bundle args = new Bundle();
        args.putString("btnId", btnId);
        args.putParcelable("uri", uri);
        args.putString("storage", path);
        args.putInt("fragNum", fragNum);
        ButtonsFragment fragment = new ButtonsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static class PlaceholderFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        int fragNum = 0;
        BootstrapButton btnTopLeft;
        BootstrapButton btnTopRight;
        BootstrapButton btnMiddleLeft;
        BootstrapButton btnMiddleRight;
        BootstrapButton btnBottomLeft;
        BootstrapButton btnBottomRight;
        String btnRef = "";
        String btnId = "";
        ImageView ivTopLeft;
        ImageView ivTopRight;
        ImageView ivMiddleLeft;
        ImageView ivMiddleRight;
        ImageView ivBottomLeft;
        ImageView ivBottomRight;
        TextView tvTopLeft;
        TextView tvTopRight;
        TextView tvMiddleLeft;
        TextView tvMiddleRight;
        TextView tvBottomLeft;
        TextView tvBottomRight;
        Bitmap image;
        private static final String IVTOPLEFT = "topLeft";
        private static final String IVTOPRIGHT = "topRight";
        private static final String IVMIDDLELEFT = "middleLeft";
        private static final String IVMIDDLERIGHT = "middleRight";
        private static final String IVBOTTOMLEFT = "bottomLeft";
        private static final String IVBOTTOMRIGHT = "bottomRight";
        int topLeft = 0, topRight = 0, middleLeft = 0, middleRight = 0, bottomLeft = 0, bottomRight = 0;
        View publicView;
        Snackbar snackbar;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(String path, final String btnId, Uri uri) {

            Bundle args = new Bundle();
            args.putString("btnId", btnId);
            args.putParcelable("uri", uri);
            args.putString("storage", path);
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setArguments(args);
            return fragment;
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_buttons, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            publicView = view;
            fragNum = getArguments().getInt(ARG_SECTION_NUMBER);
            btnTopLeft = (BootstrapButton) view.findViewById(R.id.btnTopLeft);
            btnTopRight = (BootstrapButton) view.findViewById(R.id.btnTopRight);
            btnMiddleLeft = (BootstrapButton) view.findViewById(R.id.btnMiddleLeft);
            btnMiddleRight = (BootstrapButton) view.findViewById(R.id.btnMiddleRight);
            btnBottomLeft = (BootstrapButton) view.findViewById(R.id.btnBottomLeft);
            btnBottomRight = (BootstrapButton) view.findViewById(R.id.btnBottomRight);

            ivTopLeft = (ImageView) view.findViewById(R.id.ivTopLeft);
            ivTopRight = (ImageView) view.findViewById(R.id.ivTopRight);
            ivMiddleLeft = (ImageView) view.findViewById(R.id.ivMiddleLeft);
            ivMiddleRight = (ImageView) view.findViewById(R.id.ivMiddleRight);
            ivBottomLeft = (ImageView) view.findViewById(R.id.ivBottomLeft);
            ivBottomRight = (ImageView) view.findViewById(R.id.ivBottomRight);

            tvTopLeft = (TextView) view.findViewById(R.id.tvTopLeft);
            tvTopRight = (TextView) view.findViewById(R.id.tvTopRight);
            tvMiddleLeft = (TextView) view.findViewById(R.id.tvMiddleLeft);
            tvMiddleRight = (TextView) view.findViewById(R.id.tvMiddleRight);
            tvBottomLeft = (TextView) view.findViewById(R.id.tvBottomLeft);
            tvBottomRight = (TextView) view.findViewById(R.id.tvBottomRight);

            btnTopLeft.setBootstrapBrand(new CustomBootstrapStyleLight(view.getContext()));
            btnTopRight.setBootstrapBrand(new CustomBootstrapStyleDark(view.getContext()));
            btnMiddleLeft.setBootstrapBrand(new CustomBootstrapStyleDark(view.getContext()));
            btnMiddleRight.setBootstrapBrand(new CustomBootstrapStyleLight(view.getContext()));
            btnBottomLeft.setBootstrapBrand(new CustomBootstrapStyleLight(view.getContext()));
            btnBottomRight.setBootstrapBrand(new CustomBootstrapStyleDark(view.getContext()));

            setButtonImage();

            showProgress(true, getString(R.string.loading));

            getButtonsText();
            getButtonsEventCount();
//            setButtonsImages();

            showProgress(false, "");

            btnTopLeft.setOnClickListener(this);
            btnTopRight.setOnClickListener(this);
            btnMiddleLeft.setOnClickListener(this);
            btnMiddleRight.setOnClickListener(this);
            btnBottomLeft.setOnClickListener(this);
            btnBottomRight.setOnClickListener(this);

            btnTopLeft.setOnLongClickListener(this);
            btnTopRight.setOnLongClickListener(this);
            btnMiddleLeft.setOnLongClickListener(this);
            btnMiddleRight.setOnLongClickListener(this);
            btnBottomLeft.setOnLongClickListener(this);
            btnBottomRight.setOnLongClickListener(this);
        }

        /*
        private void setButtonsImages() {
            image = loadImage(IVTOPLEFT);
            if (image != null) {
                ivTopLeft.setImageBitmap(image);
            } else {
                cacheAndLoad(IVTOPLEFT, ivTopLeft);
            }
            image = loadImage(IVTOPRIGHT);
            if (image != null) {
                ivTopRight.setImageBitmap(image);
            } else {
                cacheAndLoad(IVTOPRIGHT, ivTopRight);
            }
            image = loadImage(IVMIDDLELEFT);
            if (image != null) {
                ivMiddleLeft.setImageBitmap(image);
            } else {
                cacheAndLoad(IVMIDDLELEFT, ivMiddleLeft);
            }
            image = loadImage(IVMIDDLERIGHT);
            if (image != null) {
                ivMiddleRight.setImageBitmap(image);
            } else {
                cacheAndLoad(IVMIDDLERIGHT, ivMiddleRight);
            }
            image = loadImage(IVBOTTOMLEFT);
            if (image != null) {
                ivBottomLeft.setImageBitmap(image);
            } else {
                cacheAndLoad(IVBOTTOMLEFT, ivBottomLeft);
            }
            image = loadImage(IVBOTTOMRIGHT);
            if (image != null) {
                ivBottomRight.setImageBitmap(image);
            } else {
                cacheAndLoad(IVBOTTOMRIGHT, ivBottomRight);
            }
        }
        */

        private void getButtonsEventCount() {
            mDatabase.getReference("all_events/" + user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            if (snap.getKey().equals("btnId")) {
                                String btnSnap = snap.getValue(String.class);
                                if (btnSnap != null) {
                                    if (btnSnap.equals(IVTOPLEFT + fragNum)) {
                                        topLeft++;
                                        tvTopLeft.setText(getString(R.string.event_count) + " " + topLeft);
                                    }
                                    if (btnSnap.equals(IVTOPRIGHT + fragNum)) {
                                        topRight++;
                                        tvTopRight.setText(getString(R.string.event_count) + " " + topRight);
                                    }
                                    if (btnSnap.equals(IVMIDDLELEFT + fragNum)) {
                                        middleLeft++;
                                        tvMiddleLeft.setText(getString(R.string.event_count) + " " + middleLeft);
                                    }
                                    if (btnSnap.equals(IVMIDDLERIGHT + fragNum)) {
                                        middleRight++;
                                        tvMiddleRight.setText(getString(R.string.event_count) + " " + middleRight);
                                    }
                                    if (btnSnap.equals(IVBOTTOMLEFT + fragNum)) {
                                        bottomLeft++;
                                        tvBottomLeft.setText(getString(R.string.event_count) + " " + bottomLeft);
                                    }
                                    if (btnSnap.equals(IVBOTTOMRIGHT + fragNum)) {
                                        bottomRight++;
                                        tvBottomRight.setText(getString(R.string.event_count) + " " + bottomRight);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        boolean isButtonTextEmpty;

        private void getButtonsText() {
            mDatabase.getReference("buttons/" + user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    isButtonTextEmpty = true;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String text = snapshot.getValue(String.class);
                        String snapKey = snapshot.getKey();

                        if (text != null) {
                            if (!text.isEmpty()) {
                                isButtonTextEmpty = false;
                                if (snapKey.equals(IVTOPLEFT + fragNum))
                                    btnTopLeft.setText(text);
                                if (snapKey.equals(IVTOPRIGHT + fragNum))
                                    btnTopRight.setText(text);
                                if (snapKey.equals(IVMIDDLELEFT + fragNum))
                                    btnMiddleLeft.setText(text);
                                if (snapKey.equals(IVMIDDLERIGHT + fragNum))
                                    btnMiddleRight.setText(text);
                                if (snapKey.equals(IVBOTTOMLEFT + fragNum))
                                    btnBottomLeft.setText(text);
                                if (snapKey.equals(IVBOTTOMRIGHT + fragNum))
                                    btnBottomRight.setText(text);
                            }
                        }
                    }
                    if (isButtonTextEmpty) {
                        snackbar = Snackbar.make(publicView, R.string.snackbar_category, Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.snackbar_category_ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        private void saveImage(String btnId, Bitmap bitmap, Context context) {
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
                    if (fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void cacheAndLoad(final String btnRef, final ImageView imageView) {
            final CompositeDisposable disposables = new CompositeDisposable();

            mDatabase.getReference("button_images/" + user.getUid() + "/" + btnRef + fragNum)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String ref = dataSnapshot.getValue(String.class);
                            StorageReference mStorage;
                            if (ref == null) {
                                return;
                            } else {
                                mStorage = FirebaseStorage.getInstance().getReference(ref);
                            }
                            mStorage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        disposables.add(imageDownloader(task)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribeWith(new DisposableObserver<Bitmap>() {
                                                    Bitmap btnBitmap;

                                                    @Override
                                                    public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                                                        btnBitmap = bitmap;
                                                    }

                                                    @Override
                                                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                                    }

                                                    @Override
                                                    public void onComplete() {
                                                        saveImage(btnRef + fragNum, btnBitmap, getContext());
                                                        imageView.setImageBitmap(btnBitmap);
                                                    }
                                                }));
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


        }

        Observable<Bitmap> imageDownloader(final Task<Uri> task) {
            return Observable.defer(new Callable<ObservableSource<? extends Bitmap>>() {
                @Override
                public ObservableSource<? extends Bitmap> call() throws Exception {
                    Bitmap bitmap = null;
                    try {
                        bitmap = Glide.with(getContext()).asBitmap().load(task.getResult()).submit().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (bitmap != null)
                        return Observable.just(bitmap);
                    else
                        return null;
                }
            });
        }

        /*
        private Bitmap loadImage(String buttonName) {
            ContextWrapper cw = new ContextWrapper(getContext());
            File dir = cw.getDir("icons", Context.MODE_PRIVATE);
            File myPath = new File(dir.getPath());
            File file = new File(myPath, buttonName + fragNum + ".png");
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                return bitmap;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
        */

        @Override
        public void onClick(View v) {
            int id = v.getId();
            int tvNum = 0;
            switch (id) {
                case R.id.btnTopLeft:
                    btnRef = IVTOPLEFT;
                    tvNum = topLeft;
                    break;
                case R.id.btnTopRight:
                    btnRef = IVTOPRIGHT;
                    tvNum = topRight;
                    break;
                case R.id.btnMiddleLeft:
                    btnRef = IVMIDDLELEFT;
                    tvNum = middleLeft;
                    break;
                case R.id.btnMiddleRight:
                    btnRef = IVMIDDLERIGHT;
                    tvNum = middleRight;
                    break;
                case R.id.btnBottomLeft:
                    btnRef = IVBOTTOMLEFT;
                    tvNum = bottomLeft;
                    break;
                case R.id.btnBottomRight:
                    btnRef = IVBOTTOMRIGHT;
                    tvNum = bottomRight;
                    break;
            }
            BootstrapButton b = (BootstrapButton) v;
            onButtonPressed(btnRef, fragNum, b.getText().toString(), tvNum);
        }

        @Override
        public boolean onLongClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btnTopLeft:
                    btnRef = IVTOPLEFT;
                    break;
                case R.id.btnTopRight:
                    btnRef = IVTOPRIGHT;
                    break;
                case R.id.btnMiddleLeft:
                    btnRef = IVMIDDLELEFT;
                    break;
                case R.id.btnMiddleRight:
                    btnRef = IVMIDDLERIGHT;
                    break;
                case R.id.btnBottomLeft:
                    btnRef = IVBOTTOMLEFT;
                    break;
                case R.id.btnBottomRight:
                    btnRef = IVBOTTOMRIGHT;
                    break;
            }
            showButtonOptionsDialog(v, btnRef, fragNum);
            return false;
        }

        public void showButtonOptionsDialog(final View v, final String btnRef, final int fragNum) {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(R.string.change_button_dialog_title);
            builder.setItems(R.array.buttonOptions, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            //Change title
                            btnId = btnRef + fragNum;
                            SetButtonTitleDialog f = new SetButtonTitleDialog();
                            Bundle args = new Bundle();
                            args.putString("btnId", btnId);
                            f.setArguments(args);
                            f.show(getFragmentManager(), "setButtonTitleDialog");
                            break;
                        case 1:
                            //Change image
//                            StorageReference mStorage = FirebaseStorage.getInstance().getReference("button-icons/Approval.png");
//                            Glide.with(getContext()).using(new FirebaseImageLoader()).load(mStorage).into(ivTopLeft);

                            btnId = btnRef + fragNum;
                            PickImageFragment d = new PickImageFragment();
                            Bundle arg = new Bundle();
                            arg.putString("btnId", btnId);
                            d.setArguments(arg);
                            d.show(getChildFragmentManager(), "f");
                            break;
                    }
                }
            });
            builder.show();
        }

        public void setButtonText(String text, String btnId) {
            mDatabase.getReference("buttons/" + user.getUid() + "/" + btnId).setValue(text);
        }

        public void setButtonImage() {
            mDatabase.getReference("button_images/" + user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String btnId = snapshot.getKey();
                        String imageText = snapshot.getValue(String.class);
                        if (btnId != null && getActivity() != null) {
                            if (btnId.equals(IVTOPLEFT + fragNum)) {
                                ivTopLeft.setImageResource(getResources().getIdentifier(imageText, "drawable", getContext().getPackageName()));
                            }
                            if (btnId.equals(IVTOPRIGHT + fragNum)) {
                                ivTopRight.setImageResource(getResources().getIdentifier(imageText, "drawable", getContext().getPackageName()));
                            }
                            if (btnId.equals(IVMIDDLELEFT + fragNum)) {
                                ivMiddleLeft.setImageResource(getResources().getIdentifier(imageText, "drawable", getContext().getPackageName()));
                            }
                            if (btnId.equals(IVMIDDLERIGHT + fragNum)) {
                                ivMiddleRight.setImageResource(getResources().getIdentifier(imageText, "drawable", getContext().getPackageName()));
                            }
                            if (btnId.equals(IVBOTTOMLEFT + fragNum)) {
                                ivBottomLeft.setImageResource(getResources().getIdentifier(imageText, "drawable", getContext().getPackageName()));
                            }
                            if (btnId.equals(IVBOTTOMRIGHT + fragNum)) {
                                ivBottomRight.setImageResource(getResources().getIdentifier(imageText, "drawable", getContext().getPackageName()));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });/*
            Bitmap bitmap = loadImage(btnId.substring(0, btnId.length() - 1));
            String btn = btnId.substring(0, btnId.length() - 1);
            switch (btn) {
                case IVTOPLEFT:
                    ivTopLeft.setImageBitmap(bitmap);
                    break;
                case IVTOPRIGHT:
                    ivTopRight.setImageBitmap(bitmap);
                    break;
                case IVMIDDLELEFT:
                    ivMiddleLeft.setImageBitmap(bitmap);
                    break;
                case IVMIDDLERIGHT:
                    ivMiddleRight.setImageBitmap(bitmap);
                    break;
                case IVBOTTOMLEFT:
                    ivBottomLeft.setImageBitmap(bitmap);
                    break;
                case IVBOTTOMRIGHT:
                    ivBottomRight.setImageBitmap(bitmap);
                    break;
            }
            */
        }

        public void onButtonPressed(String btnRef, int fragNum, String btnTitle, int tvNum) {
            String btnId = btnRef + fragNum;
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("btnId", btnId);
            intent.putExtra("btnTitle", btnTitle);
            intent.putExtra("tvNum", tvNum);
            startActivity(intent);
        }

        private ProgressDialog dialog;

        private void showProgress(boolean show, String msg) {
            if (dialog == null) {
                dialog = new ProgressDialog(getContext());
                dialog.setMessage(msg);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
            }
            if (show)
                dialog.show();
            else
                dialog.dismiss();
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
        }

        @Override
        public void onDetach() {
            if (snackbar != null)
                snackbar.dismiss();
            super.onDetach();
        }

        public static class SectionsPagerAdapter extends FragmentPagerAdapter {

            public SectionsPagerAdapter(FragmentManager fm) {
                super(fm);
            }

            @Override
            public Fragment getItem(int position) {
                // getItem is called to instantiate the fragment for the given page.
                // Return a PlaceholderFragment (defined as a static inner class below).
                return PlaceholderFragment.newInstance(position + 1);
            }

            @Override
            public int getCount() {
                // Show 2 total pages.
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "1";
                    case 1:
                        return "2";
                }
                return null;
            }
        }
    }

}
