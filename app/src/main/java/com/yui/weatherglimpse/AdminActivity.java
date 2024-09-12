package com.yui.weatherglimpse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.yui.weatherglimpse.databinding.ActivityAdminBinding;
import com.yui.weatherglimpse.utils.DatabaseHelper;
import com.yui.weatherglimpse.utils.User;
import com.yui.weatherglimpse.utils.UserAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    private ActivityAdminBinding binding;
    private DatabaseHelper databaseHelper;
    private List<User> userList;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, databaseHelper);

        binding.userListView.setAdapter(userAdapter);
        loadUsers();

        binding.addUserButton.setOnClickListener(v -> showAddUserDialog());
        binding.searchButton.setOnClickListener(v -> searchUsers());

        binding.backToLoginButton.setOnClickListener(v -> backToLogin());
        binding.goToHomeButton.setOnClickListener(v -> goToHome());

        binding.weatherMoodView.setOnClickListener(v -> changeMood());
    }

    
    private int currentMood = 0;

    private void changeMood() {
        currentMood = (currentMood + 1) % 3;
        binding.weatherMoodView.setMood(currentMood);
        String[] moods = {"晴朗", "多云", "雨天"};
        Toast.makeText(this, "当前天气心情: " + moods[currentMood], Toast.LENGTH_SHORT).show();
    }

    private void loadUsers() {
        userList.clear();
        userList.addAll(databaseHelper.getAllUsers());
        userAdapter.notifyDataSetChanged();
    }

    private void searchUsers() {
        String query = binding.searchEditText.getText().toString().trim();
        if (!query.isEmpty()) {
            userList.clear();
            userList.addAll(databaseHelper.searchUsers(query));
            userAdapter.notifyDataSetChanged();
        } else {
            loadUsers();
        }
    }

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        final EditText usernameEditText = view.findViewById(R.id.usernameEditText);
        final EditText passwordEditText = view.findViewById(R.id.passwordEditText);

        builder.setView(view)
                .setTitle("添加用户")
                .setPositiveButton("添加", (dialog, which) -> {
                    String username = usernameEditText.getText().toString().trim();
                    String password = passwordEditText.getText().toString().trim();
                    if (!username.isEmpty() && !password.isEmpty()) {
                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setPassword(password);
                        long id = databaseHelper.addUser(newUser);
                        if (id != -1) {
                            loadUsers();
                            Toast.makeText(AdminActivity.this, "用户添加成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdminActivity.this, "用户添加失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AdminActivity.this, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void backToLogin() {
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToHome() {
        Intent intent = new Intent(AdminActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
