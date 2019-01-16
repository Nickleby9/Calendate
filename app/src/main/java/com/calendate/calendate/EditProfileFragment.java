package com.calendate.calendate;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapCircleThumbnail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.EasyImage;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements View.OnClickListener {

    BootstrapCircleThumbnail ivProfile;
    EditText etUsername, etPersonalNote;
    BootstrapButton btnSave;
    ArrayList<File> files = new ArrayList<>();

    public EditProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivProfile = (BootstrapCircleThumbnail) view.findViewById(R.id.ivProfile);
        etUsername = (EditText) view.findViewById(R.id.etUsername);
        etPersonalNote = (EditText) view.findViewById(R.id.etPersonalNote);
        btnSave = (BootstrapButton) view.findViewById(R.id.btnSave);

        ivProfile.setImageDrawable(getResources().getDrawable(R.drawable.profile));
        ivProfile.setOnClickListener(this);
    }

    private boolean checkStoragePermission() {
        int resultCode = ActivityCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean granted = resultCode == PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            requestPermissions( new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            onClick(ivProfile);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivProfile:
                if (!checkStoragePermission())
                    return;

                EasyImage.openChooserWithGallery(EditProfileFragment.this, "", 0);
                break;
            case R.id.btnSave:

                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity().getParent(), new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                ivProfile.setImageBitmap(BitmapFactory.decodeFile(imageFiles.get(0).getPath()));
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(getContext());
                    if (photoFile != null)
                        photoFile.delete();
                }
            }
        });
    }
}
