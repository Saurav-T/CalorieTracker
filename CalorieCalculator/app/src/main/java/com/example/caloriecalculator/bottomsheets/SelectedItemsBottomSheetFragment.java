package com.example.caloriecalculator.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.caloriecalculator.R;
import com.example.caloriecalculator.adapters.SelectedItemsAdapter;
import com.example.caloriecalculator.models.FoodItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SelectedItemsBottomSheetFragment extends BottomSheetDialogFragment
        implements SelectedItemsAdapter.OnItemRemovedListener {

    private static final String ARG_SELECTED_ITEMS = "selected_items";
    private static final String TAG = "SelectedItemsBottomSheet";

    private RecyclerView selectedItemsRecycler;
    private TextView emptyStateText;
    private SelectedItemsAdapter adapter;
    private List<FoodItem> originalItems;

    public SelectedItemsBottomSheetFragment() {}

    public static SelectedItemsBottomSheetFragment newInstance(List<FoodItem> selectedItems) {
        SelectedItemsBottomSheetFragment fragment = new SelectedItemsBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELECTED_ITEMS, new ArrayList<>(selectedItems));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_selected_items, container, false);

        initViews(view);
        setupRecyclerView();
        loadSelectedItems();

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.bottomSheetBgColor, typedValue, true);

        dialog.setOnShowListener(d -> {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setPadding(0, 0, 0, 0);
                bottomSheet.setBackgroundColor(typedValue.data);
            }
        });

        return dialog;
    }

    private void initViews(View view) {
        selectedItemsRecycler = view.findViewById(R.id.selected_items);
        emptyStateText = view.findViewById(R.id.empty_state_text);
    }

    private void setupRecyclerView() {
        selectedItemsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SelectedItemsAdapter();
        adapter.setOnItemRemovedListener(this);
        selectedItemsRecycler.setAdapter(adapter);
    }

    private void loadSelectedItems() {
        originalItems = new ArrayList<>();
        if (getArguments() != null) {
            @SuppressWarnings("unchecked")
            List<FoodItem> items = (List<FoodItem>) getArguments().getSerializable(ARG_SELECTED_ITEMS);
            Log.d(TAG, "📦 Loaded " + (items != null ? items.size() : 0) + " selected items");

            if (items != null && !items.isEmpty()) {
                originalItems.addAll(items);
                adapter.updateItems(items);
                selectedItemsRecycler.setVisibility(View.VISIBLE);
                if (emptyStateText != null) {
                    emptyStateText.setVisibility(View.GONE);
                }
            } else {
                selectedItemsRecycler.setVisibility(View.GONE);
                if (emptyStateText != null) {
                    emptyStateText.setVisibility(View.VISIBLE);
                    emptyStateText.setText("No items selected");
                }
            }
        }
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (emptyStateText != null) {
            if (isEmpty) {
                emptyStateText.setVisibility(View.VISIBLE);
                selectedItemsRecycler.setVisibility(View.GONE);
            } else {
                emptyStateText.setVisibility(View.GONE);
                selectedItemsRecycler.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemRemoved(FoodItem foodItem, int position) {
        Log.d(TAG, "🗑️ Removed: " + foodItem.getName());
        adapter.removeItem(position);
        toggleEmptyState(adapter.getItemCount() == 0);

        Bundle result = new Bundle();
        result.putLong("removed_item_id", foodItem.getId());
        getParentFragmentManager().setFragmentResult("item_removed", result);

        Log.d(TAG, "📤 Sent removal notification for item ID: " + foodItem.getId());
    }
}