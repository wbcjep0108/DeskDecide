package com.example.deskdecide;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VoterActivity extends AppCompatActivity {

    DatabaseHelper db;
    RadioGroup rgPres, rgVPres, rgSec;
    String voterName = "", voterBirthdate = "", voterLocation = "";
    int voterAge = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter);

        db = new DatabaseHelper(this);
        rgPres = findViewById(R.id.rg_pres);
        rgVPres = findViewById(R.id.rg_vpres);
        rgSec = findViewById(R.id.rg_sec);
        Button btnFinalize = findViewById(R.id.btn_finalize);
        Button btnViewRankings = findViewById(R.id.btn_view_rankings);

        // Immediate Mandatory Popup
        showInitialMandatoryPopup();

        btnFinalize.setOnClickListener(v -> {
            if (voterName.isEmpty()) {
                Toast.makeText(this, "Please complete your info first", Toast.LENGTH_SHORT).show();
            } else if (rgPres.getCheckedRadioButtonId() == -1 || 
                       rgVPres.getCheckedRadioButtonId() == -1 || 
                       rgSec.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select one candidate for each category", Toast.LENGTH_SHORT).show();
            } else {
                showReviewDialog();
            }
        });

        btnViewRankings.setOnClickListener(v -> showRankingsPopup());
    }

    private void showInitialMandatoryPopup() {
        new AlertDialog.Builder(this)
            .setTitle("Voting Status")
            .setMessage("Have you voted yet?")
            .setCancelable(false)
            .setPositiveButton("YES", (dialog, which) -> showRankingsPopup())
            .setNegativeButton("NO", (dialog, which) -> {
                if (voterName.isEmpty()) {
                    showAgeCheckDialog();
                }
            })
            .show();
    }

    private void showAgeCheckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Age Verification");
        builder.setMessage("Enter your age to proceed:");
        final EditText etAge = new EditText(this);
        etAge.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        builder.setView(etAge);
        builder.setCancelable(false);
        builder.setPositiveButton("Next", (dialog, which) -> {
            String ageStr = etAge.getText().toString();
            if (!ageStr.isEmpty()) {
                voterAge = Integer.parseInt(ageStr);
                if (voterAge < 18) {
                    new AlertDialog.Builder(this)
                        .setMessage("Not eligible to vote")
                        .setPositiveButton("OK", (d, w) -> finish())
                        .setCancelable(false)
                        .show();
                } else {
                    showVoterInfoDialog();
                }
            } else {
                finish();
            }
        });
        builder.show();
    }

    private void showVoterInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Voter Details");
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this); etName.setHint("Name");
        final EditText etBirth = new EditText(this); etBirth.setHint("Birthdate");
        final EditText etLoc = new EditText(this); etLoc.setHint("Location (City/Address)");

        layout.addView(etName); layout.addView(etBirth); layout.addView(etLoc);
        builder.setView(layout);
        builder.setCancelable(false);
        builder.setPositiveButton("Start Voting", (dialog, which) -> {
            voterName = etName.getText().toString().trim();
            voterBirthdate = etBirth.getText().toString();
            voterLocation = etLoc.getText().toString();
            if (voterName.isEmpty()) {
                finish();
            } else {
                db.registerVoter(voterName, voterBirthdate, voterLocation, voterAge);
                loadDynamicCandidates();
            }
        });
        builder.show();
    }

    private void loadDynamicCandidates() {
        populateRadioGroup(rgPres, "Presidency");
        populateRadioGroup(rgVPres, "Vice Presidency");
        populateRadioGroup(rgSec, "Secretary");
    }

    private void populateRadioGroup(RadioGroup rg, String category) {
        rg.removeAllViews();
        Cursor cursor = db.getAllCandidates(category);
        while (cursor.moveToNext()) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId()); // CRITICAL: Assign ID so getCheckedRadioButtonId works
            rb.setText(cursor.getString(1)); 
            rb.setTextColor(getResources().getColorStateList(R.color.radio_text_selector, null));
            rg.addView(rb);
        }
        cursor.close();
    }

    private void showReviewDialog() {
        RadioButton rbPres = findViewById(rgPres.getCheckedRadioButtonId());
        RadioButton rbVPres = findViewById(rgVPres.getCheckedRadioButtonId());
        RadioButton rbSec = findViewById(rgSec.getCheckedRadioButtonId());

        if (rbPres == null || rbVPres == null || rbSec == null) return;

        String pres = rbPres.getText().toString();
        String vpres = rbVPres.getText().toString();
        String sec = rbSec.getText().toString();

        new AlertDialog.Builder(this)
            .setTitle("Review Your Vote")
            .setMessage("Review your vote for:\n\nPresident: " + pres + 
                         "\nVice President: " + vpres + 
                         "\nSecretary: " + sec)
            .setPositiveButton("Finalize Vote", (dialog, which) -> {
                saveVote(pres, "Presidency");
                saveVote(vpres, "Vice Presidency");
                saveVote(sec, "Secretary");
                Toast.makeText(this, "Vote stored in database", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Edit", null)
            .show();
    }

    private void saveVote(String candidate, String category) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        db.addVote(voterName, candidate, category, timestamp, voterLocation);
    }

    private void showRankingsPopup() {
        String[] cats = {"Presidency", "Vice Presidency", "Secretary"};
        new AlertDialog.Builder(this)
            .setTitle("View Results (Read-Only)")
            .setItems(cats, (dialog, which) -> {
                Intent intent = new Intent(this, RankingActivity.class);
                intent.putExtra("category", cats[which]);
                startActivity(intent);
            })
            .setCancelable(true)
            .setNegativeButton("Close", null)
            .show();
    }
}