package com.example.whattoeat;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whattoeat.databinding.ItemFoodBinding;

public class FoodAdapter extends ListAdapter<FoodItem, FoodAdapter.FoodViewHolder> {

    private final OnItemDeleteListener deleteListener;

    public interface OnItemDeleteListener {
        void onDeleteClick(FoodItem item);
    }

    protected FoodAdapter(OnItemDeleteListener deleteListener) {
        super(new DiffUtil.ItemCallback<FoodItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull FoodItem oldItem, @NonNull FoodItem newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull FoodItem oldItem, @NonNull FoodItem newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFoodBinding binding = ItemFoodBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FoodViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = getItem(position);
        holder.bind(item);
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {

        private final ItemFoodBinding binding;

        public FoodViewHolder(@NonNull ItemFoodBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FoodItem item) {
            binding.tvFoodName.setText(item.getName());
            binding.ivDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(item);
                }
            });
        }
    }
}