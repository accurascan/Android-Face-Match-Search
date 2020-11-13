package com.accurascan.facematch.sample;

import android.os.Bundle;

import com.accurascan.facematch.sample.database.DatabaseHelper;
import com.accurascan.facematch.sample.model.UserModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.inet.facelock.callback.FaceHelper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.List;

public class UserListActivity extends AppCompatActivity implements AddUserFragment.onFragmentInteractionListner {

    private int mColumnCount = 0;
    private DatabaseHelper dbHelper;
    private List<UserModel> userList;
    private AddUserFragment userFragment;
    private FaceHelper faceHelper;
    private UserListViewAdapter adapter;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        //make sure facehelper initialized using context before using empty constructor else it's nothing return
        faceHelper = new FaceHelper();
        dbHelper = new DatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.rv_user);

        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, mColumnCount));
        }
        userList = dbHelper.getAllUser();
        adapter = new UserListViewAdapter(userList);
        recyclerView.setAdapter(adapter);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fab.hide();
                getSupportActionBar().setTitle(getResources().getString(R.string.add_user));
                userFragment = AddUserFragment.newInstance(UserListActivity.this);
                userFragment.setFaceHelper(faceHelper);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, userFragment, "AddUserFragment")
                        .commit();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("AddUserFragment") != null) {
            fab.show();
            getSupportActionBar().setTitle(getString(R.string.title_activity_user_list));
            getSupportFragmentManager().beginTransaction().remove(userFragment).commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onFragmentInteraction() {
        List<UserModel> userList = dbHelper.getAllUser();
        this.userList.clear();
        this.userList.addAll(userList);
        adapter.notifyDataSetChanged();
        onBackPressed();
    }
}