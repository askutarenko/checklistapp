/*
 * Copyright © 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.g11x.checklistapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.g11x.checklistapp.data.ChecklistItem;
import com.g11x.checklistapp.language.Language;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChecklistItemActivity extends AppCompatActivity {

  private ChecklistItem checklistItem;
  private Language language;
  private AppPreferences.LanguageChangeListener languageChangeListener;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    setContentView(R.layout.activity_checklist_item);

    language = AppPreferences.getLanguageOverride(this);
    languageChangeListener = new AppPreferences.LanguageChangeListener(this) {

      @Override
      public void onChanged(String newValue) {
        ChecklistItemActivity.this.onLanguageChange(newValue);
      }
    };

    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
        this.getIntent().getStringExtra("databaseRefUrl"));

    databaseRef.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        checklistItem = dataSnapshot.getValue(ChecklistItem.class);
        createUI();
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.e("", "Unable to fetch database item.");
      }
    });

  }

  private void onLanguageChange(String newValue) {
    language = Language.valueOf(newValue);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    languageChangeListener.unregister(this);
  }

  private void createUI() {
    TextView name = (TextView) findViewById(R.id.name);
    name.setText(checklistItem.getName(language));

    TextView description = (TextView) findViewById(R.id.description);
    description.setText(checklistItem.getDescription(language));

    Button getDirections = (Button) findViewById(R.id.directions);
    getDirections.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ChecklistItemActivity.this.onClickGetDirections();
      }
    });

    ToggleButton isDone = (ToggleButton) findViewById(R.id.doneness);
    isDone.setChecked(checklistItem.isDone());
    isDone.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        ChecklistItemActivity.this.onClickIsDone();
      }
    });
  }

  private void onClickIsDone() {
    checklistItem.setDone(!checklistItem.isDone());
  }

  private void onClickGetDirections() {
    startActivity(new Intent(Intent.ACTION_VIEW, checklistItem.getDirections()));
  }
}
