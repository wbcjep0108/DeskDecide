package com.example.deskdecide;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DeskDecide.db";
    private static final int DATABASE_VERSION = 3; // Reset for seeding

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE voters (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, birthdate TEXT, location TEXT, age INTEGER, hasVoted INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE candidates (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, category TEXT)");
        db.execSQL("CREATE TABLE votes (id INTEGER PRIMARY KEY AUTOINCREMENT, voter_name TEXT, candidate_name TEXT, category TEXT, timestamp TEXT, location TEXT)");
        
        seedCandidates(db);
    }

    private void seedCandidates(SQLiteDatabase db) {
        String[] pres = {"Adrian M. Villanueva", "Maria L. Soriano", "Daniel R. Mendoza", "Patricia S. Alonzo", "Miguel T. Navarro", "Clarissa J. Buenaventura", "Jonathan P. Aguilar", "Regina C. Dominguez", "Rafael E. Quintos", "Andrea N. Salcedo"};
        for (String name : pres) insertCandidate(db, name, "Presidency");

        String[] vpres = {"Carlo A. Ramirez", "Bianca F. Loyola", "Vincent H. Dela Cruz", "Liza M. Paredes", "Thomas G. Estrella", "Janine R. Alcantara", "Paolo C. Fernandez", "Melissa D. Ibañez", "Noah B. Valdez", "Katrina S. Montoya"};
        for (String name : vpres) insertCandidate(db, name, "Vice Presidency");

        String[] sec = {"Mark J. Castillo", "Elaine P. Roldan", "Jerome L. Santiago", "Sophia T. Guevarra", "Bryan C. Macapagal", "Faith A. Robles", "Ivan M. Tolentino", "Nicole R. Fajardo", "Oscar B. Manalang", "Hannah D. Lacsamana", "Kevin S. De Vera", "Alyssa J. Hilario", "Patrick N. Abad", "Rosemarie E. Coronel", "Lucas F. Andrada", "Camille V. Miraflores", "Dennis G. Yumul", "Trisha C. Pangilinan", "Andrew K. Magbanua", "Sheila M. Ordoñez", "Ronald P. Escobar", "Bea L. Tamayo", "Julius R. Balagtas", "Irene Q. San Pedro"};
        for (String name : sec) insertCandidate(db, name, "Secretary");
    }

    private void insertCandidate(SQLiteDatabase db, String name, String category) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("category", category);
        db.insert("candidates", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS voters");
        db.execSQL("DROP TABLE IF EXISTS candidates");
        db.execSQL("DROP TABLE IF EXISTS votes");
        onCreate(db);
    }

    public void addCandidate(String name, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("category", category.trim());
        db.insert("candidates", null, values);
    }

    public Cursor getAllCandidates(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM candidates WHERE category = ?", new String[]{category.trim()});
    }

    public void addVote(String voterName, String candidateName, String category, String timestamp, String location) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("voter_name", voterName.trim());
        values.put("candidate_name", candidateName.trim());
        values.put("category", category.trim());
        values.put("timestamp", timestamp);
        values.put("location", location);
        db.insert("votes", null, values);

        ContentValues voterValues = new ContentValues();
        voterValues.put("hasVoted", 1);
        db.update("voters", voterValues, "name = ?", new String[]{voterName.trim()});
    }

    public boolean checkUserVoted(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM voters WHERE name = ? AND hasVoted = 1", new String[]{name.trim()});
        boolean voted = cursor.getCount() > 0;
        cursor.close();
        return voted;
    }

    public void registerVoter(String name, String birthdate, String location, int age) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("birthdate", birthdate);
        values.put("location", location);
        values.put("age", age);
        values.put("hasVoted", 0);
        db.insert("voters", null, values);
    }

    public Cursor getRankings(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT candidate_name, COUNT(*) as vote_count FROM votes WHERE category = ? GROUP BY candidate_name ORDER BY vote_count DESC", new String[]{category.trim()});
    }

    public Cursor getAllVotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM votes", null);
    }

    public void deleteVote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("votes", "id = ?", new String[]{String.valueOf(id)});
    }
}