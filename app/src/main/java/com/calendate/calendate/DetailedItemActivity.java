package com.calendate.calendate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.calendate.calendate.models.Alert;
import com.calendate.calendate.models.Event;
import com.calendate.calendate.models.EventRow;
import com.calendate.calendate.utils.MyUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class DetailedItemActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    Spinner spnRepeat;
    EditText etTitle, etDescription;
    BootstrapButton btnDate, btnTime;
    FloatingActionButton btnChange;
    LocalDateTime date;
    FirebaseDatabase mDatabase;
    FirebaseUser user;
    String key = "";
    int hours = 0, minutes = 0;
    EventRow model;
    String btnId;
    RecyclerView rvAlerts, rvDocs;
    FloatingActionButton fabAdd;
    static ArrayList<Alert> alerts = new ArrayList<>();
    AlertsAdapter alertsAdapter;
    DocsAdapter docsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_item);

//        etTitle.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mDatabase = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        model = getIntent().getParcelableExtra("model");

        etTitle = (EditText) findViewById(R.id.etTitle);
        etDescription = (EditText) findViewById(R.id.etDescription);
        btnDate = (BootstrapButton) findViewById(R.id.btnDate);
        btnTime = (BootstrapButton) findViewById(R.id.btnTime);
        spnRepeat = (Spinner) findViewById(R.id.spnRepeat);
        btnChange = (FloatingActionButton) findViewById(R.id.btnChange);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);

        alertsAdapter = new AlertsAdapter(mDatabase.getReference("all_events/" + user.getUid() + "/" + model.getEventUID() + "/alerts"));
        docsAdapter = new DocsAdapter(this, model.getEventUID(), mDatabase.getReference("all_events/" + user.getUid() + "/" + model.getEventUID() + "/documents"));
        rvAlerts = (RecyclerView) findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        rvAlerts.setAdapter(alertsAdapter);
        rvDocs = (RecyclerView) findViewById(R.id.rvDocs);
        rvDocs.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDocs.setAdapter(docsAdapter);

        MyUtils.fixBootstrapButton(this, btnDate);
        MyUtils.fixBootstrapButton(this, btnTime);

        ArrayAdapter<CharSequence> spnRepeatAdapter = ArrayAdapter.createFromResource(this, R.array.repeat, R.layout.spinner_item);
        spnRepeatAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spnRepeat.setAdapter(spnRepeatAdapter);

        btnDate.setOnClickListener(this);
        btnTime.setOnClickListener(this);
        btnChange.setOnClickListener(this);
        fabAdd.setOnClickListener(this);

        readOnce();

        changeEnabled(false);
    }

    private void readOnce() {
        DatabaseReference ref = mDatabase.getReference("all_events/" + user.getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().equals(model.getEventUID())) {
                        Event event = snapshot.getValue(Event.class);
                        etTitle.setText(event.getTitle());
                        if (event.getDescription().isEmpty()){
                            etDescription.setHint("");
                        } else {
                            etDescription.setText(event.getDescription());
                        }
                        LocalDateTime dateTime = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        btnDate.setText(dateTime.toString(MyUtils.btnDateFormat));
                        btnTime.setText(event.getTime());
                        spnRepeat.setSelection(event.getRepeatPos());
                        key = snapshot.getKey();
                        date = LocalDateTime.parse(event.getDate(), DateTimeFormat.forPattern(MyUtils.dateForamt));
                        String[] split = btnTime.getText().toString().split(":");
                        hours = Integer.valueOf(split[0]);
                        minutes = Integer.valueOf(split[1]);
                        btnId = event.getBtnId();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnChange:
                Intent intent = new Intent(this, AddItemActivity.class);
                intent.putExtra("event", model.getEventUID());
                startActivity(intent);
                break;
            case R.id.btnDate:
                if (btnDate.isClickable()) {
                    DatePickerDialog pickerDialog = new DatePickerDialog(v.getContext(), this, date.getYear(), date.getMonthOfYear() - 1, date.getDayOfMonth());
                    pickerDialog.show();
                }
                break;
            case R.id.btnTime:
                if (btnTime.isClickable()) {
                    TimePickerDialog timeDialog = new TimePickerDialog(v.getContext(), this, hours, minutes, true);
                    timeDialog.show();
                }
                break;
            case R.id.fabAdd:
                alerts.add(new Alert());
                alertsAdapter.notifyItemInserted(alerts.size() - 1);
                break;
        }
    }

    public static void removeAdapter(int pos) {
        alerts.remove(pos);
        AlertsAdapter.AlertsViewHolder.viewHolders.remove(pos);
//        alertsAdapter.notifyItemRemoved(pos);
    }

    void changeEnabled(Boolean state) {
        etDescription.setEnabled(state);
        etTitle.setEnabled(state);
        btnTime.setClickable(state);
        spnRepeat.setEnabled(state);
        btnDate.setClickable(state);
        fabAdd.setClickable(state);
        if (!state) {
            fabAdd.setVisibility(View.INVISIBLE);
            etTitle.setBackgroundResource(R.color.transparent);
            etDescription.setBackgroundResource(R.color.transparent);
            spnRepeat.setBackgroundResource(R.color.transparent);
        } else {
            fabAdd.setVisibility(View.VISIBLE);
            AlertsAdapter.AlertsViewHolder.changeAdapterEnabled(state);
            fabAdd.setVisibility(View.VISIBLE);
            btnDate.setTextColor(Color.WHITE);
            btnTime.setTextColor(Color.WHITE);
            etTitle.setBackgroundResource(R.color.caldroid_white);
            etDescription.setBackgroundResource(R.color.caldroid_white);
            spnRepeat.setBackgroundResource(R.color.caldroid_white);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        date = new LocalDateTime(year, month + 1, dayOfMonth, 0, 0);
        btnDate.setText(date.toString(MyUtils.btnDateFormat));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        hours = hourOfDay;
        minutes = minute;
        if (minute < 10)
            btnTime.setText(String.valueOf(hours) + ":0" + String.valueOf(minutes));
        else
            btnTime.setText(String.valueOf(hours) + ":" + String.valueOf(minutes));
    }

    private void editEvent() {
        alerts.clear();

        int size = AlertsAdapter.AlertsViewHolder.viewHolders.size();
        for (int i = 0; i < size; i++) {
            AlertsAdapter.AlertsViewHolder viewHolder = (AlertsAdapter.AlertsViewHolder) AlertsAdapter.AlertsViewHolder.viewHolders.get(i);
            int count = Integer.valueOf(viewHolder.etCount.getText().toString());
            int selectedItemPosition = viewHolder.spnKind.getSelectedItemPosition();
            alerts.add(i, new Alert(count, selectedItemPosition));
        }

        final String title = etTitle.getText().toString();
        final String description = etDescription.getText().toString();
        String time = btnTime.getText().toString();
        final int repeat = spnRepeat.getSelectedItemPosition();
        final String btnId = this.btnId;

        final String key = model.getEventUID();
        mDatabase.getReference("all_events/" + user.getUid()).child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Event event = new Event(title, description, date, alerts, hours, minutes, repeat, key, btnId, true, user.getDisplayName());
                mDatabase.getReference("all_events/" + user.getUid()).child(key).setValue(event);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DetailedItemActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
            }
        });

        alerts.clear();
        changeEnabled(false);
    }

    public static class AlertsAdapter extends FirebaseRecyclerAdapter<Alert, AlertsAdapter.AlertsViewHolder> {

        public AlertsAdapter(Query query) {
            super(Alert.class, R.layout.alert_item, AlertsAdapter.AlertsViewHolder.class, query);
        }

        @Override
        public AlertsAdapter.AlertsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new AlertsAdapter.AlertsViewHolder(v);
        }

        @Override
        protected void populateViewHolder(AlertsAdapter.AlertsViewHolder viewHolder, Alert model, int position) {
            viewHolder.alert = model;
            viewHolder.etCount.setText(String.valueOf(model.getCount()));
            viewHolder.spnKind.setSelection(model.getKind());
            AlertsViewHolder.viewHolders.add(position, viewHolder);
            alerts.add(new Alert());
        }


        public static class AlertsViewHolder extends RecyclerView.ViewHolder {
            EditText etCount;
            Spinner spnKind;
            FloatingActionButton fabRemove;
            Alert alert;
            static ArrayList<RecyclerView.ViewHolder> viewHolders = new ArrayList<>();

            public AlertsViewHolder(View itemView) {
                super(itemView);

                etCount = (EditText) itemView.findViewById(R.id.etCount);
                spnKind = (Spinner) itemView.findViewById(R.id.spnKind);
                fabRemove = (FloatingActionButton) itemView.findViewById(R.id.fabRemove);

                ArrayAdapter<CharSequence> spnKindAdapter = ArrayAdapter.createFromResource(itemView.getContext(), R.array.kind, R.layout.spinner_item);
                spnKindAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                spnKind.setAdapter(spnKindAdapter);

                fabRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeAdapter(getAdapterPosition());
                    }
                });

                etCount.setEnabled(false);
                spnKind.setEnabled(false);
                fabRemove.setClickable(false);
                fabRemove.setVisibility(View.INVISIBLE);
                spnKind.setBackgroundResource(R.color.transparent);
            }

            static void changeAdapterEnabled(Boolean state) {
                for (int i = 0; i < viewHolders.size(); i++) {
                    AlertsAdapter.AlertsViewHolder viewHolder = (AlertsAdapter.AlertsViewHolder) AlertsAdapter.AlertsViewHolder.viewHolders.get(i);
                    viewHolder.etCount.setEnabled(state);
                    viewHolder.spnKind.setEnabled(state);
                    viewHolder.fabRemove.setClickable(state);
                    if (!state) {
                        viewHolder.fabRemove.setVisibility(View.INVISIBLE);
//                        viewHolder.spnKind.setBackgroundResource(R.color.transparent);
                        viewHolder.spnKind.setBackgroundColor(Color.TRANSPARENT);
                    } else
                        viewHolder.fabRemove.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private static class DocsAdapter extends FirebaseRecyclerAdapter<String, DocsAdapter.DocsViewHolder> {

        Context context;
        File file;
        FirebaseUser user;
        StorageReference mStorage;

        public DocsAdapter(Context context, String eventUid, Query query) {
            super(String.class, R.layout.doc_item, DocsAdapter.DocsViewHolder.class, query);
            this.context = context;
            user = FirebaseAuth.getInstance().getCurrentUser();
            mStorage = FirebaseStorage.getInstance().getReference("documents/" + user.getUid() + "/" + eventUid);
        }

        @Override
        protected void populateViewHolder(final DocsViewHolder viewHolder, final String model, int position) {
            viewHolder.string = model;
            CompositeDisposable disposables = new CompositeDisposable();

            disposables.add(imageDownloader(model)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableObserver<File>() {
                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull File newFile) {
                            file = newFile;
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            viewHolder.file = file;
                            if (model.contains(".jpg"))
                                Glide.with(viewHolder.itemView.getContext()).asBitmap().load(file).apply(RequestOptions.overrideOf(35, 35)).into(viewHolder.ivDoc);
                            else if (model.contains(".pdf"))
                                viewHolder.ivDoc.setImageResource(R.drawable.ic_pdf);
                        }
                    }));
        }

        Observable<File> imageDownloader(final String string) {
            return Observable.defer(new Callable<ObservableSource<? extends File>>() {
                @Override
                public ObservableSource<? extends File> call() throws Exception {
                    try {
                        file = Glide.with(context).asFile().load(string).submit().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return Observable.just(file);
                }
            });
        }

        static class DocsViewHolder extends RecyclerView.ViewHolder {

            ImageView ivDoc;
            File file;
            String string;

            public DocsViewHolder(View itemView) {
                super(itemView);

                ivDoc = (ImageView) itemView.findViewById(R.id.ivDoc);

                ivDoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        if (string.toLowerCase().contains(".jpg")) {
                            intent.setDataAndType(Uri.fromFile(file), "image/jpeg");
                        }
                        if (string.toLowerCase().contains(".pdf")) {
                            intent.setDataAndType(Uri.fromFile(file), "application/pdf");
                        }
                        try {
                            Intent intent1 = Intent.createChooser(intent, "Open With");
                            view.getContext().startActivity(intent1);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(view.getContext(), "You don't have an application to open this file", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
}
