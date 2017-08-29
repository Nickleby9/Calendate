package com.calendate.calendate;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.bumptech.glide.Glide;
import com.calendate.calendate.utils.CustomBootstrapStyleDark;
import com.calendate.calendate.utils.CustomBootstrapStyleLight;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;


/**
 * A simple {@link Fragment} subclass.
 */
public class ButtonsFragment extends Fragment {

    ViewPager viewPager;
    PlaceholderFragment.SectionsPagerAdapter mSectionsPagerAdapter;
    FirebaseDatabase mDatabase;
    FirebaseAuth mAuth;
    FirebaseUser user;
    int fragNum = 0;
    String imageRef = "";
    Bitmap image;

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

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        viewPager = (ViewPager) view.findViewById(R.id.viewPager);
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

        if (getArguments().getString("storage") != null){
            PlaceholderFragment.newInstance(getArguments().getString("storage"), getArguments().getString("btnId"), (Bitmap) getArguments().getParcelable("bitmap"));
            int frag = getArguments().getInt("fragNum", 0);
            viewPager.setCurrentItem(frag);
        }
    }

    public static ButtonsFragment newInstance(String path, final String btnId, Bitmap image, int fragNum) {

        Bundle args = new Bundle();
        args.putString("btnId", btnId);
        args.putParcelable("bitmap", image);
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
        private static final String IVTOPLEFT = "topLeft";
        private static final String IVTOPRIGHT = "topRight";
        private static final String IVMIDDLELEFT = "middleLeft";
        private static final String IVMIDDLERIGHT = "middleRight";
        private static final String IVBOTTOMLEFT = "bottomLeft";
        private static final String IVBOTTOMRIGHT = "bottomRight";

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
        Bitmap image;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static PlaceholderFragment newInstance(String path, final String btnId, Bitmap image) {

            Bundle args = new Bundle();
            args.putString("btnId", btnId);
            args.putParcelable("bitmap", image);
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

            btnTopLeft.setBootstrapBrand(new CustomBootstrapStyleLight(view.getContext()));
            btnTopRight.setBootstrapBrand(new CustomBootstrapStyleDark(view.getContext()));
            btnMiddleLeft.setBootstrapBrand(new CustomBootstrapStyleDark(view.getContext()));
            btnMiddleRight.setBootstrapBrand(new CustomBootstrapStyleLight(view.getContext()));
            btnBottomLeft.setBootstrapBrand(new CustomBootstrapStyleLight(view.getContext()));
            btnBottomRight.setBootstrapBrand(new CustomBootstrapStyleDark(view.getContext()));

            if (getArguments().getString("storage") != null){
                setButtonImage(getArguments().getString("storage"), getArguments().getString("btnId"), (Bitmap) getArguments().getParcelable("bitmap"));
            }

            showProgress(true, getString(R.string.loading));

            mDatabase.getReference("buttons/" + user.getUid() + "/" + IVTOPLEFT + fragNum)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            btnTopLeft.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
            mDatabase.getReference("buttons/" + user.getUid() + "/" + IVTOPRIGHT + fragNum)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            btnTopRight.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            mDatabase.getReference("buttons/" + user.getUid() + "/" + IVMIDDLELEFT + fragNum)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            btnMiddleLeft.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            mDatabase.getReference("buttons/" + user.getUid() + "/" + IVMIDDLERIGHT + fragNum)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            btnMiddleRight.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            mDatabase.getReference("buttons/" + user.getUid() + "/" + IVBOTTOMLEFT + fragNum)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            btnBottomLeft.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
            mDatabase.getReference("buttons/" + user.getUid() + "/" + IVBOTTOMRIGHT + fragNum)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            btnBottomRight.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            image = loadImage(IVTOPLEFT);
            if (image != null) {
                ivTopLeft.setImageBitmap(image);
            }
            image = loadImage(IVTOPRIGHT);
            if (image != null) {
                ivTopRight.setImageBitmap(image);
            }
            image = loadImage(IVMIDDLELEFT);
            if (image != null) {
                ivMiddleLeft.setImageBitmap(image);
            }
            image = loadImage(IVMIDDLERIGHT);
            if (image != null) {
                ivMiddleRight.setImageBitmap(image);
            }
            image = loadImage(IVBOTTOMLEFT);
            if (image != null) {
                ivBottomLeft.setImageBitmap(image);
            }
            image = loadImage(IVBOTTOMRIGHT);
            if (image != null) {
                ivBottomRight.setImageBitmap(image);
            }

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

        private void saveImage(String btnId, Uri link, Context context) {
            ContextWrapper cw = new ContextWrapper(context);
            File dir = new File("");
            dir = cw.getDir("icons", Context.MODE_PRIVATE);
            File myPath = new File(dir, btnId + ".png");
            FileOutputStream fos = null;
            try {
                Bitmap bitmap = Glide.with(getContext()).asBitmap().load(link).submit().get();
                fos = new FileOutputStream(myPath);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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

        @Override
        public void onClick(View v) {
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
            onButtonPressed(btnRef, fragNum);
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
                            StorageReference mStorage = FirebaseStorage.getInstance().getReference("button-icons/Approval.png");
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

        public void setButtonImage(String path, final String btnId, Bitmap image) {
            mDatabase.getReference("button_images/" + user.getUid() + "/" + btnId).setValue(path.replaceFirst("/", ""));
            String btn = btnId.substring(0, btnId.length() - 1);
            switch (btn) {
                case IVTOPLEFT:
                    ivTopLeft.setImageBitmap(image);
                    break;
                case IVTOPRIGHT:
                    ivTopRight.setImageBitmap(image);
                    break;
                case IVMIDDLELEFT:
                    ivMiddleLeft.setImageBitmap(image);
                    break;
                case IVMIDDLERIGHT:
                    ivMiddleRight.setImageBitmap(image);
                    break;
                case IVBOTTOMLEFT:
                    ivBottomLeft.setImageBitmap(image);
                    break;
                case IVBOTTOMRIGHT:
                    ivBottomRight.setImageBitmap(image);
                    break;
            }
        }

        public void onButtonPressed(String btnRef, int fragNum) {
            String btnId = btnRef + fragNum;
            Intent intent = new Intent(getContext(), DetailActivity.class);
            intent.putExtra("btnId", btnId);
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
