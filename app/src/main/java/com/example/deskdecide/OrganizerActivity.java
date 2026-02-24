package com.example.deskdecide;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class OrganizerActivity extends AppCompatActivity {

    DatabaseHelper db;
    ListView lvRecords;
    List<VoteRecord> voteRecords;
    VoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer);

        db = new DatabaseHelper(this);
        lvRecords = findViewById(R.id.lv_records);
        voteRecords = new ArrayList<>();

        findViewById(R.id.btn_add_candidate).setOnClickListener(v -> showAddCandidateDialog());
        findViewById(R.id.btn_remove_candidate).setOnClickListener(v -> showRemoveCandidateDialog());
        findViewById(R.id.btn_view_records).setOnClickListener(v -> viewDatabaseRecords());

        findViewById(R.id.btn_pres).setOnClickListener(v -> openRanking("Presidency"));
        findViewById(R.id.btn_vpres).setOnClickListener(v -> openRanking("Vice Presidency"));
        findViewById(R.id.btn_sec).setOnClickListener(v -> openRanking("Secretary"));

        viewDatabaseRecords();
    }

    private void showAddCandidateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Candidate");
        View view = getLayoutInflater().inflate(R.layout.dialog_add_candidate, null);
        EditText etName = view.findViewById(R.id.et_candidate_name);
        Spinner spinnerCat = view.findViewById(R.id.spinner_category);
        
        String[] categories = {"Presidency", "Vice Presidency", "Secretary"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCat.setAdapter(catAdapter);

        builder.setView(view);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String category = spinnerCat.getSelectedItem().toString();
            if (!name.isEmpty()) {
                db.addCandidate(name, category);
                Toast.makeText(this, "Candidate Added.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    private void showRemoveCandidateDialog() {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT * FROM candidates", null);
        List<String> candidateNames = new ArrayList<>();
        List<Integer> candidateIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            candidateIds.add(cursor.getInt(0));
            candidateNames.add(cursor.getString(1) + " (" + cursor.getString(2) + ")");
        }
        cursor.close();

        if (candidateNames.isEmpty()) {
            Toast.makeText(this, "No candidates found.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Candidate");
        String[] items = candidateNames.toArray(new String[0]);
        builder.setItems(items, (dialog, which) -> {
            int idToRemove = candidateIds.get(which);
            db.getWritableDatabase().delete("candidates", "id = ?", new String[]{String.valueOf(idToRemove)});
            Toast.makeText(this, "Candidate Removed.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void viewDatabaseRecords() {
        Cursor cursor = db.getAllVotes();
        voteRecords.clear();
        while (cursor.moveToNext()) {
            voteRecords.add(new VoteRecord(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4)
            ));
        }
        cursor.close();
        adapter = new VoteAdapter(this, voteRecords);
        lvRecords.setAdapter(adapter);
    }

    private void openRanking(String category) {
        Intent intent = new Intent(this, RankingActivity.class);
        intent.putExtra("category", category);
        startActivity(intent);
    }

    private static class VoteRecord {
        int id;
        String name, candidate, category, timestamp;
        VoteRecord(int id, String name, String candidate, String category, String timestamp) {
            this.id = id; this.name = name; this.candidate = candidate;
            this.category = category; this.timestamp = timestamp;
        }
    }

    private class VoteAdapter extends ArrayAdapter<VoteRecord> {
        VoteAdapter(Context context, List<VoteRecord> records) {
            super(context, 0, records);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_vote_record, parent, false);
            }
            VoteRecord record = getItem(position);
            
            TextView tvSummary = convertView.findViewById(R.id.tv_record_summary);
            TextView tvTime = convertView.findViewById(R.id.tv_timestamp);
            ImageButton btnDelete = convertView.findViewById(R.id.btn_delete_record);

            String summary = record.name + " voted for " + record.candidate + " (" + record.category + ")";
            tvSummary.setText(summary);
            tvTime.setText(record.timestamp);

            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(OrganizerActivity.this)
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete this vote?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteVote(record.id);
                        viewDatabaseRecords();
                        Toast.makeText(getContext(), "Record Deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            return convertView;
        }
    }
}