package com.example.chemapp;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class BottomSheetHelper {
    public interface OnDismissListener {
        void onDismiss();
    }

    public static BottomSheetDialog showExpandableBottomSheet(
            @NonNull Context context,
            @NonNull View contentView, String title, String[][] data) {

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);

        bottomSheetDialog.setContentView(contentView);

        FrameLayout bottomSheet = bottomSheetDialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);

        if (bottomSheet != null) {
            BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);

            behavior.setMaxHeight(ViewGroup.LayoutParams.MATCH_PARENT);

            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            behavior.setDraggable(true);

            // Add state change listener for debugging/customization
            behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_EXPANDED:
                            // Fully expanded
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            // At peek height (50%)
                            break;
                        case BottomSheetBehavior.STATE_HALF_EXPANDED:
                            // At half expanded ratio (70%)
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            bottomSheetDialog.dismiss();
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    // Handle sliding animation if needed
                }
            });
        }

        TextView view = (TextView) getTitle(context, title);
        LinearLayout table = (LinearLayout) getTable(context, data);

        LinearLayout container = contentView.findViewById(R.id.container);
        container.addView(view);
        container.addView(table);

        bottomSheetDialog.show();
        return bottomSheetDialog;
    }


    public static BottomSheetDialog showExpandableBottomSheet(
            @NonNull Context context,
            @LayoutRes int layoutResId, String title, String[][] data) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View contentView = inflater.inflate(layoutResId, null);
        return showExpandableBottomSheet(context, contentView, title, data);
    }

    private static View getTable(Context context, String[][] data){
        Drawable vDivider = ContextCompat.getDrawable(context, R.drawable.divider_vertical);



        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.result_table, null);
        TableLayout table = view.findViewById(R.id.table);

        TableRow header = new TableRow(context);
        header.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        header.setDividerDrawable(vDivider);

        int cols = data[0].length;
        int rows = data.length - 1;

        String[] header_row = data[0];

        for (int i = 0;i < cols;i++) {
            TextView h = new TextView(context); h.setText(header_row[i]); h.setTypeface(null, Typeface.BOLD);
            // h1.setBackgroundResource(R.drawable.cell_border);
            h.setPadding(20,20,20,20);
            header.addView(h);
        }

        table.addView(header);

        for (int i = 1; i < data.length;i++) {
            String[] row = data[i];
            TableRow tr = new TableRow(context);
            tr.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            tr.setDividerDrawable(vDivider);

            for (int j = 0;j < cols;j++) {
                TextView c = new TextView(context); c.setText(row[j]);
//            c2.setBackgroundResource(R.drawable.cell_border);
                c.setPadding(20,20,20,20);
                tr.addView(c);
            }

            table.addView(tr);
        }

        return view;
    }

    private static View getTitle(Context context, String title){
        TextView view = new TextView(context);
        view.setText(title);
        view.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        view.setTypeface(null, Typeface.BOLD);

        return view;
    }

}