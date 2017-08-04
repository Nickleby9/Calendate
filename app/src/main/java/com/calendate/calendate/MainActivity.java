package com.calendate.calendate;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements SetButtonTitleDialog.OnTitleSetListener,
        NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener,
        PickImageFragment.OnImageSetListener {

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


    FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                user = FirebaseAuth.getInstance().getCurrentUser();
                user.reload();
                String displayName = firebaseAuth.getCurrentUser().getDisplayName();
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


    }

    private void checkForNewEvent() {
        mDatabase.getReference("all_events/" + user.getUid()).addValueEventListener(new ValueEventListener() {
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
                                    mDatabase.getReference("all_events/" + user.getUid() + "/" + eventUID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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
        } else if (navigationView.getMenu().getItem(0).isChecked()){
            if (pressed) {
//                super.onBackPressed();
                finishAffinity();
            } else {
                Toast.makeText(this, R.string.back_twice, Toast.LENGTH_SHORT).show();
                pressed = true;
            }
        } else {
            navigationView.getMenu().getItem(0).setChecked(true);
            Bundle bundle = new Bundle();
            bundle.putInt("fragNum", fragNum);
            ButtonsFragment buttonsFragment = new ButtonsFragment();
            buttonsFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, buttonsFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        tvUsername = (TextView) findViewById(R.id.tvUsername);
        tvEmail = (TextView) findViewById(R.id.tvEmail);
        if (user != null) {
            tvUsername.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            new AboutFragment().show(getSupportFragmentManager(), "aboutFragment");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
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
                Calendar c = Calendar.getInstance();
                args.putInt("month", c.get(Calendar.MONTH)+1);
                args.putInt("year", c.get(Calendar.YEAR));
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
                mAuth.signOut();
                Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient);
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                LoginManager.getInstance().logOut();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onImageSet(final StorageReference mStorage, final String btnId) {
        ButtonsFragment.PlaceholderFragment p = new ButtonsFragment.PlaceholderFragment();
        p.setButtonImage(mStorage, btnId);
    }

    @Override
    public void onTitleSet(String title, String btnId) {
        ButtonsFragment.PlaceholderFragment p = new ButtonsFragment.PlaceholderFragment();
        p.setButtonText(title, btnId);
    }
}
