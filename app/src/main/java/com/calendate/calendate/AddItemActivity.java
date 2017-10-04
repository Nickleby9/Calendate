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
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.calendate.calendate.fileChooser.FileUtils;
import com.calendate.calendate.models.Alert;
import com.calendate.calendate.models.Event;
import com.calendate.calendate.utils.MyUtils;
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
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.calendate.calendate.R.array.kind;

public class AddItemActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final int PERMISSION_CALENDAR_WRITE = 1;
    private static final int REQUEST_CHOOSER = 2;
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
    RxPermissions rxPermissions;
    StorageReference mStorage;
    ArrayList<File> fileArray = new ArrayList<>();
    ArrayList<Bitmap> images = new ArrayList<>();
    DocsAdapter docsAdapter;
    Event event;
    String eventKey;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ((AppCompatActivity) this).getSupportActionBar().setTitle("New event");
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
        rvDocs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        docsAdapter = new DocsAdapter(this, fileArray);
        rvDocs.setAdapter(docsAdapter);

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
            eventKey = getIntent().getStringExtra("event");
            isEditMode = true;
            readOnce();
        } else {
            alerts.add(new Alert(1, 1));
            adapter = new AlertsAdapter(this, getAlerts());
            rvAlerts.setAdapter(adapter);
            onClick(btnDate);
        }

        if (eventKey == null)
            eventKey = mDatabase.getReference("all_events/" + user.getUid()).push().getKey();

    }

    File newUriFile;

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
//                        key = snapshot.getKey();
                        alerts = event.getAlerts();
                        date = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        String[] split = btnTime.getText().toString().split(":");
                        hours = Integer.valueOf(split[0]);
                        minutes = Integer.valueOf(split[1]);
                        btnId = event.getBtnId();
                        adapter = new AlertsAdapter(AddItemActivity.this, alerts);
                        rvAlerts.setAdapter(adapter);

                        mDatabase.getReference("all_events/" + user.getUid() + "/" + event.getEventUID() + "/documents").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    CompositeDisposable disposables = new CompositeDisposable();

                                    String path = dataSnapshot1.getValue(String.class);
                                    disposables.add(fileDownloader(path)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeWith(new DisposableObserver<File>() {
                                                @Override
                                                public void onNext(@io.reactivex.annotations.NonNull File newFile) {
                                                    newUriFile = newFile;
                                                    fileArray.add(newUriFile);
                                                    docsAdapter.notifyDataSetChanged();
                                                }

                                                @Override
                                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                                }

                                                @Override
                                                public void onComplete() {
//                                                    fileArray.add(newUriFile);
//                                                    docsAdapter.notifyDataSetChanged();
                                                }
                                            }));
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    File uriFile;

    Observable<File> fileDownloader(final String path) {
        return Observable.defer(new Callable<ObservableSource<? extends File>>() {
            @Override
            public ObservableSource<? extends File> call() throws Exception {
                try {
                    uriFile = Glide.with(AddItemActivity.this).asFile().load(path).submit().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Observable.just(uriFile);
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
                }
                break;
            case R.id.fabAdd:
                alerts.add(new Alert());
                adapter.notifyItemInserted(alerts.size() - 1);
                break;
            case R.id.ivAttach:
                if (!checkStoragePermission())
                    return;

//                Intent getContentIntent = FileUtils.createGetContentIntent();
//                Intent intent = Intent.createChooser(getContentIntent, "Choose your file");
//                startActivityForResult(intent, REQUEST_CHOOSER);

                EasyImage.openChooserWithDocuments(this, "Where is your file located?", 0);
                break;
        }
    }

    private void createNotification(Event event) {

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.DTSTART, date.getMillisOfSecond());
        intent.putExtra(CalendarContract.Events.ALL_DAY, false);
//        intent.putExtra(CalendarContract.Events.DURATION, );
//        intent.putExtra(CalendarContract.Events.RRULE, );
//        intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, );
        intent.putExtra(CalendarContract.Events.TITLE, event.getTitle());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, event.getTitle());
        intent.putExtra(CalendarContract.Events.HAS_ALARM, 1);
        intent.putExtra(CalendarContract.Events.CALENDAR_ID, event.getEventUID());

        /*
        ArrayList<Alert> alerts = event.getAlerts();
        int count = alerts.get(0).getCount();
        int kind = alerts.get(0).getKind();

        long newDateInMillis = 0;
        long time = 0;

        switch (kind) {
            case 0: //minutes
                time = 60 * 1000;
                break;
            case 1: //hours
                time = 60 * 60 * 1000;
                break;
            case 2: //days
                time = 24 * 60 * 60 * 1000;
                break;
            case 3: //weeks
                time = 7 * 24 * 60 * 60 * 1000;
                break;
            case 4: //months
                time = 604800000;
                break;
        }
        time = time * count;
        newDateInMillis = date.getMillisOfSecond() - time;

        Intent intent = new Intent(this, NotificationReceiver.class);
        alerts.remove(0);
        intent.putExtra("event", event);
        intent.putExtra("num", alerts.size());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, newDateInMillis, pendingIntent);
*/
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

    File smallFile;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new EasyImage.Callbacks() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                Toast.makeText(AddItemActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                fileArray.add(imageFiles.get(0));
                docsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                if (source == EasyImage.ImageSource.CAMERA){
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(AddItemActivity.this);
                    if (photoFile != null)
                        photoFile.delete();
                }
            }
        });
        switch (requestCode) {
            case REQUEST_CHOOSER:
                if (data == null) {
                    return;
                }
                Uri uri = data.getData();

                // Get the File path from the Uri
                String path = FileUtils.getPath(this, uri);

                // Alternatively, use FileUtils.getFile(Context, Uri)
                if (path != null && FileUtils.isLocal(path)) {
                    File file = new File(path);
                    if (file.getPath().toLowerCase().endsWith(".jpg")) {
                        CompositeDisposable disposables = new CompositeDisposable();

                        disposables.add(imageDownloader(file)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeWith(new DisposableObserver<File>() {
                                    @Override
                                    public void onNext(@io.reactivex.annotations.NonNull File newFile) {
                                        smallFile = newFile;
                                    }

                                    @Override
                                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {
                                        fileArray.add(smallFile);
                                        docsAdapter.notifyDataSetChanged();
                                    }
                                }));
                    } else {
                        try {
                            fileArray.add(file);
                            docsAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(this, "Files too large!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        }
    }

    Observable<File> imageDownloader(final File file) {
        return Observable.defer(new Callable<ObservableSource<? extends File>>() {
            @Override
            public ObservableSource<? extends File> call() throws Exception {
                try {
                    Glide.with(AddItemActivity.this).asFile().load(file)
                            .apply(RequestOptions.overrideOf(35, 35).centerInside()).submit().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Observable.just(file);
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

    int i = 1;

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

//        if (getIntent().getStringExtra("event") == null)
//            key = mDatabase.getReference("all_events/" + user.getUid()).push().getKey();

        event = new Event(title, description, date, alerts, hours, minutes, repeat, eventKey, btnId, true, user.getDisplayName());
        mDatabase.getReference("all_events/" + user.getUid()).child(eventKey).setValue(event);


        for (File file : fileArray) {
            mStorage.child("documents").child(user.getUid()).child(eventKey).child(file.getName())
                    .putFile(Uri.fromFile(file))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot task) {
                            mDatabase.getReference("all_events/" + user.getUid()).child(eventKey).child("documents").child(String.valueOf(i)).setValue(task.getDownloadUrl().toString());
                            i++;
                        }
                    });
        }

//        createNotification(event);

        alerts.clear();
        AlertsAdapter.AlertsViewHolder.viewHolders.clear();


        Intent intent = new Intent(AddItemActivity.this, DetailActivity.class);
        intent.putExtra("btnId", btnId);
        startActivity(intent);

    }

    boolean first = true;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = new LocalDateTime(year, month + 1, dayOfMonth, 0, 0);
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;
        btnDate.setText(date.toString(MyUtils.btnDateFormat));
        if (first)
            onClick(btnTime);
        first = false;
        date = LocalDateTime.now();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (minute < 10)
            btnTime.setText(String.valueOf(hourOfDay) + ":0" + String.valueOf(minute));
        else
            btnTime.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
        date = new LocalDateTime(year, month + 1, day, hourOfDay, minute);
        hours = hourOfDay;
        minutes = minute;
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

    private class DocsAdapter extends RecyclerView.Adapter<DocsAdapter.DocsViewHolder> {

        Context context;
        LayoutInflater inflater;
        ArrayList<File> data;
        Bitmap image;

        public DocsAdapter(Context context, ArrayList<File> data) {
            this.context = context;
            this.inflater = LayoutInflater.from(context);
            this.data = data;
        }

        @Override
        public DocsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.doc_item, parent, false);
            return new DocsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final DocsViewHolder holder, int position) {
            File file = data.get(position);
            holder.file = data.get(position);
            if (file != null) {
                /*if (file.getPath().toLowerCase().endsWith(".jpg")) {*/
                    image = BitmapFactory.decodeFile(file.getPath());
                    CompositeDisposable disposables = new CompositeDisposable();

                    disposables.add(imageDownloader(file)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableObserver<Bitmap>() {
                                @Override
                                public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                                    image = bitmap;
                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                }

                                @Override
                                public void onComplete() {
                                    holder.ivDoc.setImageBitmap(image);
                                }
                            }));
                /*} else {
                    holder.ivDoc.setImageResource(R.drawable.ic_pdf_icon);
                }*/

            }
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        Observable<Bitmap> imageDownloader(final File file) {
            return Observable.defer(new Callable<ObservableSource<? extends Bitmap>>() {
                @Override
                public ObservableSource<? extends Bitmap> call() throws Exception {
                    try {
                        Glide.with(AddItemActivity.this).asBitmap().load(file)
                                .apply(RequestOptions.overrideOf(35, 35).centerInside()).submit().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Observable.just(image);
                }
            });
        }

        class DocsViewHolder extends RecyclerView.ViewHolder {

            ImageView ivDoc;
            File file;

            public DocsViewHolder(View itemView) {
                super(itemView);

                ivDoc = (ImageView) itemView.findViewById(R.id.ivDoc);

                ivDoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        /*if (file.getPath().toLowerCase().endsWith(".jpg")) {*/
                            intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
                            Intent intent1 = Intent.createChooser(intent, "Open with");
                            startActivity(intent1);
                        /*}*/

                        /*
                        if (file.getPath().toLowerCase().endsWith(".pdf")) {
                            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                            try {
                                Intent intent1 = Intent.createChooser(intent, "Open With");
                                startActivity(intent1);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(AddItemActivity.this, "You don't have an application to open this file", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AddItemActivity.this, "For now only PDF and JPG files are supported", Toast.LENGTH_SHORT).show();
                        }*/
                    }
                });

                ivDoc.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        return false;
                    }
                });
            }
        }
    }

}
