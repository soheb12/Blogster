package com.example.skyscraper.photoblog;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FloatingActionButton addPostBtn;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout mainContainer;
    private Fragment homeFragment;
    private Fragment accountFragment;
    private Fragment notificationFragment;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Photo blog");

        addPostBtn = findViewById(R.id.addPostBtn);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        mainContainer = findViewById(R.id.main_container);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        homeFragment = new HomeFragment();
        notificationFragment = new NotificationFragment();
        accountFragment = new AccountFragment();

        initializeFragment();



        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.bottom_nav_home : replaceFragment(homeFragment);
                    return true;
                    case R.id.bottom_nav_notif : replaceFragment(notificationFragment);
                    return true;
                    case R.id.bottom_nav_account : replaceFragment(accountFragment);
                    return true;
                    default:return false;
                }
            }
        });



        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newPost = new Intent(MainActivity.this , NewPostActivity.class);
                startActivity(newPost);
            }
        });



    }


    private void replaceFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(fragment == homeFragment)
        {
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);
        }

        else if(fragment == notificationFragment)
        {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);
        }

        else
        {
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);
        }

        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    private void initializeFragment()
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container,homeFragment);
        fragmentTransaction.add(R.id.main_container,notificationFragment);
        fragmentTransaction.add(R.id.main_container,accountFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
       // bottomNavigationView.setVisibility(View.GONE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if(FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            String currentUserId = mAuth.getCurrentUser().getUid();



            firebaseFirestore.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful())
                    {
                        if(!task.getResult().exists())
                        {
                            Intent setupIntent = new Intent(MainActivity.this , SetupActiviity.class);
                            startActivity(setupIntent);
                        }
                    }else
                    {
                        String errMsg = task.getException().getMessage();
                        //Toast.makeText(MainActivity.this,"(DATA EXIST TASK) : \n" + errMsg,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else
        {
            goToLogin();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.main_action_logout_btn:
                logout();
                return true;

            case R.id.main_action_settings_btn:
                Intent intent = new Intent(MainActivity.this,SetupActiviity.class);
                startActivity(intent);
                return true;
             
            default:
                return false;
        }
    }

    private void logout() {
        mAuth.signOut();
        goToLogin();
    }

    private void goToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();//so that user can't come back to previous activity by pressing the back button
    }

}
