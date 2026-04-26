package com.example.whattoeat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.EditText;
import android.widget.Toast;
import android.view.HapticFeedbackConstants;

import com.example.whattoeat.databinding.ActivityMainBinding;
import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;
    private FoodAdapter adapter;
    private DataManager dataManager;
    private List<FoodItem> foodList;

    private Handler handler;
    private Random random;
    private boolean isRolling = false;
    private Runnable rollRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler = new Handler(Looper.getMainLooper());
        random = new Random();

        initData();
        initView();
    }

    private void initData() {
        dataManager = new DataManager(this);
        foodList = new ArrayList<>(dataManager.getFoodList());
    }

    private void initView() {
        adapter = new FoodAdapter(
            item -> {
                foodList.remove(item);
                dataManager.saveFoodList(foodList);
                adapter.submitList(new ArrayList<>(foodList));
            },
            (item, isChecked) -> {
                item.setEnabled(isChecked);
                dataManager.saveFoodList(foodList);
                // Trigger view update in adapter for alpha change smoothly
                adapter.submitList(new ArrayList<>(foodList));
            }
        );
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        adapter.submitList(new ArrayList<>(foodList));

        binding.fabAdd.setOnClickListener(v -> showAddDialog());
        binding.btnStart.setOnClickListener(v -> startRolling());
    }

    private void showAddDialog() {
        EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("添加候选菜单")
                .setView(editText)
                .setPositiveButton("确定", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "菜单名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    for (FoodItem item : foodList) {
                        if (item.getName().equals(name)) {
                            Toast.makeText(this, "该菜单已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    FoodItem newItem = new FoodItem(name);
                    foodList.add(newItem);
                    dataManager.saveFoodList(foodList);
                    adapter.submitList(new ArrayList<>(foodList));
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void startRolling() {
        if (isRolling) return;

        List<FoodItem> activeList = new ArrayList<>();
        for (FoodItem item : foodList) {
            if (item.isEnabled()) {
                activeList.add(item);
            }
        }
        
        if (activeList.isEmpty()) {
            Toast.makeText(this, "请先勾选至少一个候选菜单", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (activeList.size() == 1) {
            String result = activeList.get(0).getName();
            binding.tvResult.setText(result);
            binding.tvResult.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            showResultDialog(result);
            return;
        }

        isRolling = true;
        binding.btnStart.setEnabled(false);

        rollRunnable = new Runnable() {
            @Override
            public void run() {
                int index = random.nextInt(activeList.size());
                binding.tvResult.setText(activeList.get(index).getName());
                binding.tvResult.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);
                handler.postDelayed(this, 50); // Change text every 50ms
            }
        };

        handler.post(rollRunnable);

        // Stop rolling after 2 seconds
        handler.postDelayed(() -> {
            handler.removeCallbacks(rollRunnable);
            isRolling = false;
            binding.btnStart.setEnabled(true);
            
            // The text already holds the final randomly chosen result from the last tick
            String result = binding.tvResult.getText().toString();
            binding.tvResult.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            showResultDialog(result);
        }, 2000);
    }

    private void showResultDialog(String result) {
        new AlertDialog.Builder(this)
                .setTitle("决定了！")
                .setMessage("我们今天去吃：" + result + "！")
                .setPositiveButton("太棒了", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && rollRunnable != null) {
            handler.removeCallbacks(rollRunnable);
        }
    }
}