package com.calendate.calendate;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.calendate.calendate.models.Alert;
import com.calendate.calendate.models.Event;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.EasyImage;

import static com.calendate.calendate.R.array.kind;

public class AddItem extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final int PERMISSION_CALENDAR_WRITE = 1;
    Spinner spnRepeat;
    EditText etTitle, etDescription;
    BootstrapButton btnDate, btnTime;
    ImageView ivAttach;
    FloatingActionButton btnSave;
    LocalDateTime date = new LocalDateTime(LocalDateTime.now());
    int hours = 0, minutes = 0;
    int year = date.getYear(), month = date.getMonthOfYear(), day = date.getDayOfMonth();
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    String btnId;
    static RecyclerView rvAlerts;
    RecyclerView rvDocs;
    static ArrayList<Alert> alerts = new ArrayList<>();
    FloatingActionButton fabAdd;
    static AlertsAdapter adapter;
    String bundleID = "";
    String key;
    RxPermissions rxPermissions;
    StorageReference mStorage;
    ArrayList<File> fileArray = new ArrayList<>();
    ArrayList<Bitmap> images = new ArrayList<>();
    DocsAdapter docsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        btnId = getIntent().getStringExtra("btnId");

        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        spnRepeat = (Spinner) findViewById(R.id.spnRepeat);
        btnTime = (BootstrapButton) findViewById(R.id.btnTime);
        btnDate = (BootstrapButton) findViewById(R.id.btnDate);
        ivAttach = (ImageView) findViewById(R.id.ivAttach);
        btnSave = (FloatingActionButton) findViewById(R.id.btnSave);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        rxPermissions = new RxPermissions(this);
        mStorage = FirebaseStorage.getInstance().getReference();

        rvDocs = (RecyclerView) findViewById(R.id.rvDocs);
        rvDocs.setLayoutManager(new GridLayoutManager(this, 1, LinearLayoutManager.VERTICAL, false));

        rvAlerts = (RecyclerView) findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));

        etTitle.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etTitle.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

        btnDate.setOnClickListener(this);
        btnTime.setOnClickListener(this);
        fabAdd.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        ivAttach.setOnClickListener(this);

        MyUtils.fixBootstrapButton(this, btnDate);
        MyUtils.fixBootstrapButton(this, btnTime);

        ArrayAdapter<CharSequence> spnRepeatAdapter = ArrayAdapter.createFromResource(this, R.array.repeat, R.layout.spinner_item);
        spnRepeatAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spnRepeat.setAdapter(spnRepeatAdapter);

        if (getIntent().getStringExtra("event") != null) {
            bundleID = getIntent().getStringExtra("event");
            readOnce();
        } else {
            alerts.add(new Alert(1, 1));
            adapter = new AlertsAdapter(this, getAlerts());
            rvAlerts.setAdapter(adapter);
        }

    }

    private void readOnce() {
        DatabaseReference ref = mDatabase.getReference("all_events/" + user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(bundleID)) {
                        Event event = snapshot.getValue(Event.class);
                        etTitle.setText(event.getTitle());
                        etDescription.setText(event.getDescription());
                        btnTime.setText(event.getTime());
                        LocalDateTime dateTime = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        btnDate.setText(dateTime.toString(MyUtils.btnDateFormat));
                        spnRepeat.setSelection(event.getRepeatPos());
                        key = snapshot.getKey();
                        alerts = event.getAlerts();
                        date = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        String[] split = btnTime.getText().toString().split(":");
                        hours = Integer.valueOf(split[0]);
                        minutes = Integer.valueOf(split[1]);
                        btnId = event.getBtnId();
                        adapter = new AlertsAdapter(AddItem.this, alerts);
                        rvAlerts.setAdapter(adapter);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        alerts.clear();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnDate:
                DatePickerDialog pickerDialog = new DatePickerDialog(v.getContext(), this, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
                pickerDialog.show();
                break;
            case R.id.btnTime:
                TimePickerDialog timeDialog = new TimePickerDialog(v.getContext(), this, hours, minutes, true);
                timeDialog.show();
                break;
            case R.id.btnSave:
                if (isEmptyFields()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle(getString(R.string.error));
                    dialog.setMessage(getString(R.string.error_empty_fields));
                    dialog.setNeutralButton(R.string.str_continue, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
                } else {
                    addNewEvent();
                    mStorage.child("documents").child(user.getUid()).putFile(Uri.fromFile(fileArray.get(0))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddItem.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                break;
            case R.id.fabAdd:
                alerts.add(new Alert());
                adapter.notifyItemInserted(alerts.size() - 1);
                break;
            case R.id.ivAttach:
                if (!checkStoragePermission())
                    return;
                EasyImage.openChooserWithDocuments(this, "Attach your document", 0);
                break;
        }
    }

    private boolean checkStoragePermission() {
        int resultCode = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean granted = resultCode == PackageManager.PERMISSION_GRANTED;

        if (!granted) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        return granted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            onClick(ivAttach);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {

            }

            @Override
            public void onImagesPicked(@NonNull final List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if (imageFiles.size() > 0) {
                    Uri uri = Uri.fromFile(imageFiles.get(0));
                    Bitmap bitmap = BitmapFactory.decodeFile(imageFiles.get(0).getPath());
                    images.add(bitmap);
                    fileArray.add(imageFiles.get(0));
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {

            }
        });
    }

    public static void removeAdapter(int pos) {
        alerts.remove(pos);
        AlertsAdapter.AlertsViewHolder.viewHolders.remove(pos);
        rvAlerts.removeViewAt(pos);
        adapter.notifyItemRemoved(pos);
    }

    private boolean isEmptyFields() {
        int repeat = -1;
        String title = etTitle.getText().toString();
        String time = btnTime.getText().toString();
        repeat = spnRepeat.getSelectedItemPosition();

        if (title.isEmpty() || time.equals(getString(R.string.btn_set_time)) || AlertsAdapter.AlertsViewHolder.viewHolders.size() == 0
                || repeat == -1 || btnDate.getText().toString().equals(getString(R.string.add_item_pick_a_date))) {
            return true;
        } else {
            return false;
        }
    }

    private void addNewEvent() {

        alerts.clear();

        int size = rvAlerts.getChildCount();
        for (int i = 0; i < size; i++) {
            AlertsAdapter.AlertsViewHolder viewHolder = (AlertsAdapter.AlertsViewHolder) AlertsAdapter.AlertsViewHolder.viewHolders.get(i);
            int count = Integer.valueOf(viewHolder.etCount.getText().toString());
            int selectedItemPosition = viewHolder.spnKind.getSelectedItemPosition();
            alerts.add(i, new Alert(count, selectedItemPosition));
        }

        String title = etTitle.getText().toString();
        final String description = etDescription.getText().toString();
        String time = btnTime.getText().toString();
        int repeat = spnRepeat.getSelectedItemPosition();
        String btnId = this.btnId;

        if (getIntent().getStringExtra("event") == null)
            key = mDatabase.getReference("all_events/" + user.getUid()).push().getKey();

        final Event event = new Event(title, description, date, alerts, hours, minutes, repeat, key, btnId, true, user.getDisplayName());
        mDatabase.getReference("all_events/" + user.getUid()).child(key).setValue(event);


        alerts.clear();
        AlertsAdapter.AlertsViewHolder.viewHolders.clear();
        Intent intent = new Intent(AddItem.this, DetailActivity.class);
        intent.putExtra("btnId", btnId);
        startActivity(intent);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = new LocalDateTime(year, month + 1, dayOfMonth, 0, 0);
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;
        btnDate.setText(date.toString(MyUtils.btnDateFormat));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (minute < 10)
            btnTime.setText(String.valueOf(hourOfDay) + ":0" + String.valueOf(minute));
        else
            btnTime.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
        date = new LocalDateTime(year, month, day, hourOfDay, minute);
    }

    private ArrayList<Alert> getAlerts() {
        return alerts;
    }

    private static class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertsViewHolder> {
        LayoutInflater inflater;
        Context context;
        ArrayList<Alert> data;

        public AlertsAdapter(Context context, ArrayList<Alert> data) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.data = data;
        }

        @Override
        public AlertsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.alert_item, parent, false);
            return new AlertsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AlertsViewHolder holder, int position) {
            holder.alert = data.get(position);
            holder.etCount.setText(String.valueOf(data.get(position).getCount()));
            holder.spnKind.setSelection(data.get(position).getKind());
            AlertsViewHolder.viewHolders.add(position, holder);
        }


        @Override
        public int getItemCount() {
            return data.size();
        }

        static class AlertsViewHolder extends RecyclerView.ViewHolder {
            EditText etCount;
            Spinner spnKind;
            Alert alert;
            FloatingActionButton fabRemove;
            static ArrayList<AlertsViewHolder> viewHolders = new ArrayList<>();

            public AlertsViewHolder(final View itemView) {
                super(itemView);
                etCount = (EditText) itemView.findViewById(R.id.etCount);
                spnKind = (Spinner) itemView.findViewById(R.id.spnKind);
                fabRemove = (FloatingActionButton) itemView.findViewById(R.id.fabRemove);

                ArrayAdapter<CharSequence> spnKindAdapter = ArrayAdapter.createFromResource(itemView.getContext(), kind, R.layout.spinner_item);
                spnKindAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spnKind.setAdapter(spnKindAdapter);

                fabRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAdapter(getAdapterPosition());

                    }
                });
            }

        }
    }

    private static class DocsAdapter extends RecyclerView.Adapter<DocsAdapter.DocsViewHolder> {

        Context context;
        LayoutInflater inflater;
        ArrayList<Bitmap> data;

        public DocsAdapter(Context context, ArrayList<Bitmap> data) {
            this.context = context;
            this.inflater =LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public DocsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.doc_item, parent, false);
            return new DocsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(DocsViewHolder holder, int position) {
            Bitmap bitmap = data.get(position);
            if (bitmap != null){
                holder.ivDoc.setImageBitmap(bitmap);
            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class DocsViewHolder extends RecyclerView.ViewHolder {

            ImageView ivDoc;

            public DocsViewHolder(View itemView) {
                super(itemView);

                ivDoc = (ImageView) itemView.findViewById(R.id.ivDoc);
            }
        }
    }

}
