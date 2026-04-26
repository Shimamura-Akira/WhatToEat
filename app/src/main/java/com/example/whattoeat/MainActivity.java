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

import androidx.transition.TransitionManager;
import com.google.android.material.transition.MaterialSharedAxis;

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

    private void updateRouletteData() {
        List<String> activeNames = new ArrayList<>();
        for (FoodItem item : foodList) {
            if (item.isEnabled()) {
                activeNames.add(item.getName());
            }
        }
        binding.rouletteView.setData(activeNames);
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
                updateRouletteData();
            },
            (item, isChecked) -> {
                item.setEnabled(isChecked);
                dataManager.saveFoodList(foodList);
                adapter.submitList(new ArrayList<>(foodList));
                updateRouletteData();
            },
            item -> showEditDialog(item)
        );
        
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        adapter.submitList(new ArrayList<>(foodList));
        updateRouletteData();

        binding.fabAdd.setOnClickListener(v -> showAddDialog());
        binding.btnStart.setOnClickListener(v -> startRolling());
        
        binding.rouletteView.setSpinListener(result -> {
             binding.btnStart.setEnabled(true);
             binding.tvResult.setText(result);
             isRolling = false;
             showResultDialog(result);
        });

        // 设置底部导航栏点击事件
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            int currentItemId = binding.bottomNavigation.getSelectedItemId();
            
            if (itemId == currentItemId) return true;

            // 增加平滑的左右滑动转场动画 (Material Shared Axis X)
            boolean forward = itemId == R.id.nav_list; // 从左(home)向右(list)滑动为正向
            MaterialSharedAxis sharedAxis = new MaterialSharedAxis(MaterialSharedAxis.X, forward);
            sharedAxis.setDuration(300);
            TransitionManager.beginDelayedTransition(binding.main, sharedAxis);

            if (itemId == R.id.nav_home) {
                binding.groupHome.setVisibility(android.view.View.VISIBLE);
                binding.groupList.setVisibility(android.view.View.GONE);
                return true;
            } else if (itemId == R.id.nav_list) {
                binding.groupHome.setVisibility(android.view.View.GONE);
                binding.groupList.setVisibility(android.view.View.VISIBLE);
                return true;
            }
            return false;
        });
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
                    updateRouletteData();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditDialog(FoodItem editItem) {
        EditText editText = new EditText(this);
        editText.setText(editItem.getName());
        editText.setSelection(editText.getText().length());
        
        new AlertDialog.Builder(this)
                .setTitle("编辑候选菜单")
                .setView(editText)
                .setPositiveButton("保存", (dialog, which) -> {
                    String name = editText.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "菜单名不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (name.equals(editItem.getName())) {
                        return; // 没有修改
                    }

                    for (FoodItem item : foodList) {
                        if (item != editItem && item.getName().equals(name)) {
                            Toast.makeText(this, "该菜单已存在", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // 寻找修改项在列表中的位置
                    int index = adapter.getCurrentList().indexOf(editItem);
                    if (index != -1) {
                        editItem.setName(name);
                        dataManager.saveFoodList(foodList);
                        adapter.submitList(new ArrayList<>(foodList));
                        adapter.notifyItemChanged(index);
                        updateRouletteData();
                    }
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
        binding.tvResult.setText("等待抽取...");
        
        int winnerIndex = random.nextInt(activeList.size());
        binding.rouletteView.spin(winnerIndex);
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