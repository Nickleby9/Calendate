package com.calendate.calendate;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.calendate.calendate.caldroid.CaldroidFragment;
import com.calendate.calendate.models.Event;
import com.calendate.calendate.models.Friend;
import com.calendate.calendate.models.User;
import com.facebook.login.LoginManager;
import com.github.nisrulz.sensey.Sensey;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class MainActivity extends AppCompatActivity implements SetButtonTitleDialog.OnTitleSetListener,
        NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener,
        PickImageFragment.OnImageSetListener, FriendListFragment.OnRemainAnonymous, BindFragment.OnNotAnonymousListener,
        BindFragment.OnBindFinishedListener {

    private static final int RC_FIREBASE_SIGNIN = 2;
    FirebaseDatabase mDatabase;
    FirebaseAuth mAuth;
    FirebaseUser user;
    GoogleApiClient mGoogleApiClient;
    NavigationView navigationView;
    String buttonTitle;
    int fragNum = 0;
    TextView tvUsername;
    TextView tvEmail;
    boolean acceptedTerms = false;
    String method;
    Menu menu;

    FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.reload();
                }
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Sensey.getInstance().setupDispatchTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        fragNum = getIntent().getIntExtra("fragNum", 0) - 1;

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_buttons);
        navigationView.getMenu().performIdentifierAction(R.id.nav_buttons, 0);

        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        if (user != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("topic");
            checkForNewEvent();
            checkForNewFriend();
        }

        if (getIntent().getExtras() != null) {
            acceptedTerms = getIntent().getExtras().getBoolean("accepted");
            method = getIntent().getExtras().getString("method");
            if (acceptedTerms) {
                navigationView.getMenu().getItem(4).setChecked(true);
                Bundle bundle = new Bundle();
                bundle.putInt("fragNum", fragNum);
                BindFragment bindFragment = new BindFragment();
                bindFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, bindFragment).commit();
                switch (method) {
                    case "bind-google":
//                        onClick(btnGoogle);
                        break;
                    case "bind-facebook":
//                        onClick(btnFacebook);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        if (user != null) {
            User mUser = new User(user);
            tvUsername.setText(mUser.getUsername());
            tvEmail.setText(mUser.getEmail());
        }
        return true;
    }

    private void checkForNewEvent() {
        mDatabase.getReference("shared_events/" + user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        if (!event.isOwn()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.new_shared_event)
                                    .setMessage(event.getCreator() + " " + getString(R.string.share_msg) + " " + event.getTitle() + ".\n" + getString(R.string.share_msg_2))
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            CategoriesFragment categoriesFragment = new CategoriesFragment();
                                            Bundle bundle = new Bundle();
                                            bundle.putParcelable("event", event);
                                            categoriesFragment.setArguments(bundle);
                                            categoriesFragment.show(getSupportFragmentManager(), "categoriesFragment");
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    String eventUID = event.getEventUID();
                                    mDatabase.getReference("shared_events/" + user.getUid() + "/" + eventUID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, R.string.share_denied, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            if (!alertDialog.isShowing()) {
                                alertDialog.show();
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

    private void checkForNewFriend() {
        mDatabase.getReference("friends/" + user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Friend friend = snapshot.getValue(Friend.class);
                    if (friend != null) {
                        if (!friend.isApproved()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(R.string.new_friend_title)
                                    .setMessage(friend.getSenderUsername() + " " + getString(R.string.new_friend_msg))
                                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            friend.setApproved(true);
                                            FirebaseDatabase.getInstance().getReference("friends/" + user.getUid() + "/" + friend.getSenderUid()).setValue(friend);
                                            Friend sender = new Friend();
                                            sender.setFriendEmail(friend.getSenderEmail());
                                            sender.setApproved(true);
                                            sender.setFriendUid(friend.getSenderUid());
                                            sender.setFriendUsername(friend.getSenderUsername());
                                            sender.setSenderUsername(user.getDisplayName());
                                            sender.setSenderUid(user.getUid());
                                            sender.setSenderEmail(user.getEmail());
                                            FirebaseDatabase.getInstance().getReference("friends/" + friend.getSenderUid() + "/" + user.getUid()).setValue(sender);
                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    mDatabase.getReference("friends/" + user.getUid() + "/" + friend.getSenderUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, R.string.request_denied, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                            AlertDialog alertDialog = builder.create();
                            if (!alertDialog.isShowing()) {
                                alertDialog.show();
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

    boolean pressed = false;

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            pressed = false;
        } else if (navigationView.getMenu().getItem(0).isChecked()) {
            if (pressed) {
//                super.onBackPressed();
                finishAffinity();
            } else {
                Toast.makeText(this, R.string.back_twice, Toast.LENGTH_SHORT).show();
                pressed = true;
            }
        } else {
            goToCategoriesFragment();
        }
    }

    private void goToCategoriesFragment() {
        navigationView.getMenu().getItem(0).setChecked(true);
        Bundle bundle = new Bundle();
        bundle.putInt("fragNum", fragNum);
        ButtonsFragment buttonsFragment = new ButtonsFragment();
        buttonsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, buttonsFragment).commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_buttons:
                Bundle bundle = new Bundle();
                bundle.putInt("fragNum", fragNum);
                ButtonsFragment buttonsFragment = new ButtonsFragment();
                buttonsFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, buttonsFragment).commit();
                break;
            case R.id.nav_calendar:
                CaldroidFragment caldroid = new CaldroidFragment();
                Bundle args = new Bundle();
                DateTime dateTime = DateTime.today(TimeZone.getDefault());
                args.putInt(CaldroidFragment.MONTH, dateTime.getMonth());
                args.putInt(CaldroidFragment.YEAR, dateTime.getYear());
                caldroid.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, caldroid).commit();
                break;
            case R.id.nav_timeline:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new TimelineFragment()).commit();
                break;
            case R.id.nav_friends:
                navigationView.setCheckedItem(R.id.nav_friends);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new FriendListFragment()).commit();
                break;
            case R.id.nav_sign_out:
                signOut();
            case R.id.nav_about:
                new AboutDialog().show(getSupportFragmentManager(), "aboutFragment");
                break;
            case R.id.nav_bind:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame, new BindFragment()).commit();
                break;
            /*
                case R.id.nav_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.delete_account_title)
                        .setMessage(R.string.delete_warning)
                        .setPositiveButton(getString(R.string.procceed), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                mDatabase.getReference("all_events").child(user.getUid()).removeValue();
                                mDatabase.getReference("button_images").child(user.getUid()).removeValue();
                                mDatabase.getReference("buttons").child(user.getUid()).removeValue();
                                mDatabase.getReference("documents").child(user.getUid()).removeValue();
                                mDatabase.getReference("friends").child(user.getUid()).removeValue();
                                mDatabase.getReference("friends").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot allUsers) {
                                        for (DataSnapshot userKey : allUsers.getChildren()) {
                                            for (DataSnapshot oneFriend : userKey.getChildren()) {
                                                Friend friend = oneFriend.getValue(Friend.class);
                                                if (friend != null) {
                                                    if (friend.getSenderUid().equals(user.getUid())) {
                                                        oneFriend.getRef().removeValue();
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                mDatabase.getReference("users").child(user.getUid()).removeValue();
//                                FirebaseStorage.getInstance().getReference("documents/" + user.getUid()).;
                                user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this, R.string.success_delete, Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        navigationView.getMenu().getItem(0).setChecked(true);
                    }
                }).show();
                break;
                */
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        ContextWrapper cw = new ContextWrapper(this);
        File dir = cw.getDir("icons", Context.MODE_PRIVATE);
        File[] files = dir.listFiles();
        for (File file : files) {
            file.delete();
        }
        mAuth.signOut();
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        LoginManager.getInstance().logOut();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onImageSet(StorageReference mStorage, String btnId, Uri uri) {
//        ButtonsFragment.PlaceholderFragment p = new ButtonsFragment.PlaceholderFragment();
//        p.setButtonImage(mStorage.getPath(), btnId, image);
        ButtonsFragment fragment = ButtonsFragment.newInstance(mStorage.getPath(), btnId, uri, fragNum);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment).commit();
    }

    @Override
    public void onTitleSet(String title, String btnId) {
        ButtonsFragment.PlaceholderFragment p = new ButtonsFragment.PlaceholderFragment();
        p.setButtonText(title, btnId);
    }

    @Override
    public void onRemainAnonymous(String choice) {
        if (choice.equals("remain"))
            goToCategoriesFragment();
        else {
            navigationView.getMenu().getItem(4).setChecked(true);
            BindFragment bindFragment = new BindFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, bindFragment).commit();
        }
    }

    @Override
    public void onNotAnonymous() {
        goToCategoriesFragment();
    }

    @Override
    public void onBindFinished(FirebaseUser user) {
        this.user = user;
        onCreateOptionsMenu(menu);
    }
}
