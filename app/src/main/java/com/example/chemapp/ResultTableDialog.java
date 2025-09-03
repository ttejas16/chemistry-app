package com.example.chemapp;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class ResultTableDialog {
    public static AlertDialog.Builder getDialogFrom(Context context, String[][] data) {
        Drawable vDivider = ContextCompat.getDrawable(context, R.drawable.divider_vertical);

        String[] header_row = data[0];

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.result_table, null);
        TableLayout table = view.findViewById(R.id.table);

        TableRow header = new TableRow(context);
        header.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        header.setDividerDrawable(vDivider);

        TextView h1 = new TextView(context); h1.setText(header_row[0]); h1.setTypeface(null, Typeface.BOLD);
//        h1.setBackgroundResource(R.drawable.cell_border);
        h1.setPadding(10,10,10,10);

        TextView h2 = new TextView(context); h2.setText(header_row[1]); h2.setTypeface(null, Typeface.BOLD);
//        h2.setBackgroundResource(R.drawable.cell_border);
        h2.setPadding(10,10,10,10);

        header.addView(h1);
        header.addView(h2);
        table.addView(header);

        for (int i = 1; i < data.length;i++) {
            String[] row = data[i];
            TableRow tr = new TableRow(context);
            tr.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            tr.setDividerDrawable(vDivider);

            TextView c1 = new TextView(context); c1.setText(row[0]);
//            c1.setBackgroundResource(R.drawable.cell_border);
            c1.setPadding(10,10,10,10);

            TextView c2 = new TextView(context); c2.setText(row[1]);
//            c2.setBackgroundResource(R.drawable.cell_border);
            c2.setPadding(10,10,10,10);

            tr.addView(c1);
            tr.addView(c2);
            table.addView(tr);
        }

        return new AlertDialog.Builder(context)
                .setTitle("Result")
                .setCancelable(true)
                .setView(view)
                .setPositiveButton("Close", null);

    }
}
