package com.calendate.calendate;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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
    int year = date.getYear(), month = date.getMonthOfYear() + 1, day = date.getDayOfMonth();
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    String btnId;
    static RecyclerView rvAlerts;
    static RecyclerView rvDocs;
    static ArrayList<Alert> alerts = new ArrayList<>();
    FloatingActionButton fabAdd;
    static AlertsAdapter adapter;
    RxPermissions rxPermissions;
    StorageReference mStorage;
    static ArrayList<File> fileArray = new ArrayList<>();
    ArrayList<Bitmap> images = new ArrayList<>();
    static DocsAdapter docsAdapter;
    Event event;
    String eventKey;
    boolean isEditMode = false;
    static boolean isDeleteShown = false;
    String btnTitle = "";
    Event model;
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ((AppCompatActivity) this).getSupportActionBar().setTitle(R.string.new_event);
        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        btnId = getIntent().getStringExtra("btnId");
        btnTitle = getIntent().getStringExtra("btnTitle");

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

        /*
        spnRepeat.setVisibility(View.GONE);
        TextView tvRepeat = (TextView) findViewById(R.id.tvRepeat);
        tvRepeat.setVisibility(View.GONE);
        */

        rvAlerts = (RecyclerView) findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));

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

        if (getIntent().getParcelableExtra("event") != null) {
            model = getIntent().getParcelableExtra("event");
            eventKey = model.getEventUID();
            isEditMode = true;
            readOnce();
        } else {
            alerts.add(new Alert(1, 1, 1, true));
            adapter = new AlertsAdapter(this, getAlerts());
            rvAlerts.setAdapter(adapter);
            onClick(btnDate);
        }

        if (eventKey == null)
            eventKey = mDatabase.getReference("all_events/" + user.getUid()).push().getKey();

        etTitle.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etTitle.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

//        fabAdd.setVisibility(View.GONE);
    }

    private void readOnce() {
        DatabaseReference ref = mDatabase.getReference("all_events/" + user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(eventKey)) {
                        Event event = snapshot.getValue(Event.class);
                        etTitle.setText(event.getTitle());
                        etDescription.setText(event.getDescription());
                        btnTime.setText(event.getTime());
                        LocalDateTime dateTime = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        btnDate.setText(dateTime.toString(MyUtils.btnDateFormat));
                        spnRepeat.setSelection(event.getRepeatPos());
                        alerts = event.getAlerts();
                        date = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        year = date.getYear();
                        month = date.getMonthOfYear() + 1;
                        day = date.getDayOfMonth();
                        String[] split = btnTime.getText().toString().split(":");
                        hours = Integer.valueOf(split[0]);
                        minutes = Integer.valueOf(split[1]);
                        calendar.set(year, month - 1, day, hours, minutes, 0);
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
                                                    fileArray.add(newFile);
                                                    docsAdapter.notifyDataSetChanged();
                                                }

                                                @Override
                                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                                }

                                                @Override
                                                public void onComplete() {

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
        if (isDeleteShown) {
            DocsAdapter.DocsViewHolder.hideDelete();
            return;
        }
        super.onBackPressed();
        alerts.clear();
        fileArray.clear();

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

                EasyImage.openChooserWithDocuments(this, getString(R.string.file_location), 0);
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
                if (source == EasyImage.ImageSource.CAMERA) {
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
                                        fileArray.add(newFile);
                                        docsAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                }));
                    } else {
                        try {
                            fileArray.add(file);
                            docsAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            Toast.makeText(this, R.string.large_files_err, Toast.LENGTH_SHORT).show();
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
                            .apply(RequestOptions.overrideOf(45, 45).centerInside()).submit().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return Observable.just(file);
            }
        });
    }

    public static void removeEventAdapter(int pos) {
        alerts.remove(pos);
        AlertsAdapter.AlertsViewHolder.viewHolders.remove(pos);
        rvAlerts.removeViewAt(pos);
        adapter.notifyItemRemoved(pos);
    }

    public static void removeDocAdapter(int pos) {
        fileArray.remove(pos);
        DocsAdapter.DocsViewHolder.viewHolders.remove(pos);
        rvDocs.removeViewAt(pos);
        docsAdapter.notifyItemRemoved(pos);
    }

    private boolean isEmptyFields() {
        int repeat = -1;
        String title = etTitle.getText().toString();
        String time = btnTime.getText().toString();
        repeat = spnRepeat.getSelectedItemPosition();

        if (title.isEmpty() || time.equals(getString(R.string.btn_set_time)) /*|| AlertsAdapter.AlertsViewHolder.viewHolders.size() == 0*/
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
        boolean isCountZero = false;
        if (size == 0) {
            int id = longToInt(System.currentTimeMillis());
            alerts.add(0, new Alert(id, 0, 0, false));
        } else {
            for (int i = 0; i < size; i++) {
                AlertsAdapter.AlertsViewHolder viewHolder = (AlertsAdapter.AlertsViewHolder) AlertsAdapter.AlertsViewHolder.viewHolders.get(i);
                int count = Integer.valueOf(viewHolder.etCount.getText().toString());
                int selectedItemPosition = viewHolder.spnKind.getSelectedItemPosition();
                int id = longToInt(System.currentTimeMillis() + "" + i);
                alerts.add(i, new Alert(id, count, selectedItemPosition, true));
                if (count == 0)
                    isCountZero = true;
            }
            if (!isCountZero)
                alerts.add(alerts.size(), new Alert(longToInt(System.currentTimeMillis()), 0, 0, false));
        }

        String title = etTitle.getText().toString();
        final String description = etDescription.getText().toString();
        String time = btnTime.getText().toString();
        int repeat = spnRepeat.getSelectedItemPosition();
        final String btnId = this.btnId;

        event = new Event(title, description, date, alerts, hours, minutes, repeat, eventKey, btnId, true, user.getDisplayName());
        mDatabase.getReference("all_events/" + user.getUid()).child(eventKey).setValue(event);
        calendar.set(date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth(), hours, minutes, 0);

        for (final File file : fileArray) {
            UploadTask uploadTask = mStorage.child("documents").child(user.getUid()).child(eventKey).child(file.getName())
                    .putFile(Uri.fromFile(file));
            final NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(AddItemActivity.this);
                builder.setContentTitle("Uploading file")
                        .setProgress((int) 100, 0, false)
                        .setSmallIcon(R.drawable.ic_stat_calendate_notification)
                        .setOngoing(true)
                        .setVibrate(new long[]{0})
                        .setSound(Uri.EMPTY)
                        .setColor(Color.argb(70, 2, 136, 209)); //colorPrimaryDark
                notificationManager.notify(longToInt(1001 + "" + file.length()), builder.build());
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int progress = (int) ((100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                        builder.setProgress(100, progress, false);
                        builder.setContentText(taskSnapshot.getBytesTransferred()/1000 + "kb / " + taskSnapshot.getTotalByteCount() / 1000 + "kb");
                        notificationManager.notify(longToInt(1001 + "" + file.length()), builder.build());
                    }
                })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot task) {
                                mDatabase.getReference("all_events/" + user.getUid()).child(eventKey).child("documents").child(String.valueOf(i)).setValue(task.getDownloadUrl().toString());
                                i++;
                                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                if (notificationManager != null) {
                                    notificationManager.cancel(longToInt(1001 + "" + file.length()));
                                }
                            }
                        });
            }
        }

        createNotification(alerts, event);

        alerts.clear();
        AlertsAdapter.AlertsViewHolder.viewHolders.clear();


        Intent intent = new Intent(AddItemActivity.this, DetailActivity.class);
        intent.putExtra("btnId", btnId);
        intent.putExtra("btnTitle", btnTitle);
        startActivity(intent);
    }

    private void createNotification(ArrayList<Alert> alerts, Event event) {
        if (model != null)
            clearNotifications(model.getAlerts());

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarm != null) {
//            Calendar eventDate = calendar;
            int repeat = event.getRepeatPos();
            Intent alarmIntent = new Intent(this, NotificationReceiver.class);
            alarmIntent.putExtra("title", event.getTitle());
            alarmIntent.putExtra("text", event.getDescription());

            switch (repeat) {
                case 0: //none
                    alarmIntent.putExtra("repeat", "none");
                    break;
                case 1: //day
                    alarmIntent.putExtra("repeat", "daily");
                    break;
                case 2: //week
                    alarmIntent.putExtra("repeat", "weekly");
                    break;
                case 3: //month
                    alarmIntent.putExtra("repeat", "monthly");
                    break;
                case 4: //year
                    alarmIntent.putExtra("repeat", "yearly");
                    break;
            }

            PendingIntent pendingIntent;
            for (int i = 0; i < alerts.size(); i++) {
//                eventDate.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
//                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
//                eventDate.set(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), hours, minutes, 0);
//                LocalDateTime eventDate = new LocalDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), hours, minutes,0,0);
                LocalDateTime eventDate = new LocalDateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), 0);
                Log.d("Hilay", "createNotification: " + eventDate.toString());

                int id = alerts.get(i).getId();
                int alarmCount = alerts.get(i).getCount();
                int alarmKind = alerts.get(i).getKind();

                alarmIntent.putExtra("id", id);
                alarmIntent.putExtra("before", alarmKind);
                alarmIntent.putExtra("beforeTime", alarmCount);

                LocalDateTime newDate = eventDate;

                if (alarmCount != 0) {
                    switch (alarmKind) {
                        case 0:
//                            eventDate.add(Calendar.MINUTE, -alarmCount);
                            newDate = eventDate.minusMinutes(alarmCount);
                            break;
                        case 1:
//                            eventDate.add(Calendar.HOUR_OF_DAY, -alarmCount);
                            newDate = eventDate.minusHours(alarmCount);
                            break;
                        case 2:
//                            eventDate.add(Calendar.DAY_OF_MONTH, -alarmCount);
                            newDate = eventDate.minusDays(alarmCount);
                            break;
                        case 3:
//                            eventDate.add(Calendar.DAY_OF_MONTH, -(alarmCount * 7));
                            newDate = eventDate.minusWeeks(alarmCount);
                            break;
                        case 4:
//                            eventDate.add(Calendar.MONTH, -alarmCount);
                            newDate = eventDate.minusMonths(alarmCount);
                            break;
                    }
                }

                Log.d("Hilay", "createNotification: " + newDate.toString());
                Calendar alertTime = Calendar.getInstance();
                alertTime.set(newDate.getYear(),newDate.getMonthOfYear() - 1, newDate.getDayOfMonth(), newDate.getHourOfDay(), newDate.getMinuteOfHour(), 0);
                pendingIntent = PendingIntent.getBroadcast(
                        this, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.set(AlarmManager.RTC_WAKEUP, alertTime.getTimeInMillis(), pendingIntent);
            }
        } else
            Toast.makeText(this, R.string.no_alarm_service, Toast.LENGTH_SHORT).show();
    }

    private void clearNotifications(ArrayList<Alert> alerts) {
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarm != null) {
            Intent alarmIntent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent;

            for (int i = 0; i < alerts.size(); i++) {
                int id = alerts.get(i).getId();
                pendingIntent = PendingIntent.getBroadcast(
                        this, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarm.cancel(pendingIntent);
            }
        } else
            Toast.makeText(this, R.string.no_alarm_service, Toast.LENGTH_SHORT).show();
    }

    int longToInt(String str) {
        long num = Long.valueOf(str);
        while (num > (Integer.MAX_VALUE)) {
            num -= Integer.MAX_VALUE;
        }
        return (int) num;
    }

    int longToInt(long num) {
        while (num > (Integer.MAX_VALUE)) {
            num -= Integer.MAX_VALUE;
        }
        return (int) num;
    }

    boolean first = true;

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = new LocalDateTime(year, month + 1, dayOfMonth, hours, minutes);
        btnDate.setText(date.toString(MyUtils.btnDateFormat));
        this.year = date.getYear();
        this.month = date.getMonthOfYear() + 1;
        this.day = date.getDayOfMonth();
        if (first) {
            onClick(btnTime);
        }
        first = false;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (minute < 10)
            btnTime.setText(String.valueOf(hourOfDay) + ":0" + String.valueOf(minute));
        else
            btnTime.setText(String.valueOf(hourOfDay) + ":" + String.valueOf(minute));
        date = new LocalDateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), hourOfDay, minute);
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
            if (!data.get(data.size() - 1).isVisible())
                data.remove(data.size() - 1);
            this.data = data;
        }

        @Override
        public AlertsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.alert_item, parent, false);
            return new AlertsViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AlertsViewHolder holder, int position) {
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
                        removeEventAdapter(getAdapterPosition());

                    }
                });
            }

        }
    }

    public static class DocsAdapter extends RecyclerView.Adapter<DocsAdapter.DocsViewHolder> {

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
            DocsViewHolder.viewHolders.add(position, holder);
            File file = data.get(position);
            holder.file = data.get(position);
            if (file != null) {
                /*if (file.getPath().toLowerCase().endsWith(".jpg")) {*/
                image = BitmapFactory.decodeFile(file.getPath());
                holder.ivDoc.setImageBitmap(image);
                /*
                    CompositeDisposable disposables = new CompositeDisposable();

                    disposables.add(imageDownloader(file)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeWith(new DisposableObserver<Bitmap>() {
                                @Override
                                public void onNext(@io.reactivex.annotations.NonNull Bitmap bitmap) {
                                    holder.ivDoc.setImageBitmap(bitmap);
                                    data.remove(holder.getAdapterPosition());
                                }

                                @Override
                                public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                                }

                                @Override
                                public void onComplete() {

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
                        Glide.with(context).asBitmap().load(file)
                                .apply(RequestOptions.overrideOf(45, 45).centerInside()).submit().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Observable.just(image);
                }
            });
        }

        static class DocsViewHolder extends RecyclerView.ViewHolder {

            ImageView ivDoc;
            static ImageView ivDelete;
            File file;
            static ArrayList<DocsViewHolder> viewHolders = new ArrayList<>();

            public DocsViewHolder(View itemView) {
                super(itemView);

                ivDoc = (ImageView) itemView.findViewById(R.id.ivDoc);
                ivDelete = (ImageView) itemView.findViewById(R.id.ivDelete);
                ivDelete.setVisibility(View.GONE);

                ivDoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        /*if (file.getPath().toLowerCase().endsWith(".jpg")) {*/
                        intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
                        Intent intent1 = Intent.createChooser(intent, "Open with");
                        view.getContext().startActivity(intent1);
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

                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeDocAdapter(getAdapterPosition());
                    }
                });

                ivDoc.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ivDelete.setVisibility(View.VISIBLE);
                        isDeleteShown = true;
                        return true;
                    }
                });
            }

            public static void hideDelete() {
                ivDelete.setVisibility(View.GONE);
                isDeleteShown = false;
            }
        }
    }

}
