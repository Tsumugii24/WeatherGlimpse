package com.yui.weatherglimpse.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yui.weatherglimpse.R;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private DatabaseHelper databaseHelper;

    public UserAdapter(Context context, List<User> users, DatabaseHelper databaseHelper) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        this.databaseHelper = databaseHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        TextView usernameTextView = convertView.findViewById(R.id.usernameTextView);
        Button editButton = convertView.findViewById(R.id.editButton);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        usernameTextView.setText(user.getUsername());

        editButton.setOnClickListener(v -> showEditDialog(user));

        deleteButton.setOnClickListener(v -> showDeleteConfirmDialog(user));

        return convertView;
    }

    private void showEditDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_user, null);
        final EditText usernameEditText = view.findViewById(R.id.usernameEditText);
        final EditText passwordEditText = view.findViewById(R.id.passwordEditText);

        usernameEditText.setText(user.getUsername());
        passwordEditText.setText(user.getPassword());

        builder.setView(view)
                .setTitle("编辑用户")
                .setPositiveButton("保存", (dialog, which) -> {
                    String newUsername = usernameEditText.getText().toString().trim();
                    String newPassword = passwordEditText.getText().toString().trim();
                    if (!newUsername.isEmpty() && !newPassword.isEmpty()) {
                        user.setUsername(newUsername);
                        user.setPassword(newPassword);
                        int rowsAffected = databaseHelper.updateUser(user);
                        if (rowsAffected > 0) {
                            notifyDataSetChanged();
                            Toast.makeText(context, "用户更新成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "用户更新失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "用户名和密码不能为空", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    private void showDeleteConfirmDialog(User user) {
        new AlertDialog.Builder(context)
                .setTitle("删除用户")
                .setMessage("确定要删除用户 " + user.getUsername() + " 吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    databaseHelper.deleteUser(user.getId());
                    users.remove(user);
                    notifyDataSetChanged();
                    Toast.makeText(context, "用户已删除", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }
}