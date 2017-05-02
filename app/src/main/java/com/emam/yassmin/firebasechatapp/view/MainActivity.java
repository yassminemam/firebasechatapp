package com.emam.yassmin.firebasechatapp.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emam.yassmin.firebasechatapp.R;
import com.emam.yassmin.firebasechatapp.model.ChatMessage;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static android.R.attr.max;

public class MainActivity extends AppCompatActivity {

    final static int SIGN_IN_REQUEST_CODE = 1;

    private FloatingActionButton fab;
    private FirebaseListAdapter<ChatMessage> adapter;
    private ListView listOfMessages;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private int randomNumR, randomNumG, randomNumB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.btn_fab);
        listOfMessages = (ListView) findViewById(R.id.list_of_messages);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();

        //get user's random color form the shared pref
        randomNumR = preferences.getInt("randomNumR", 0);
        randomNumB = preferences.getInt("randomNumB", 0);
        randomNumG = preferences.getInt("randomNumG", 0);

        // if there were no RGB color in shared pref create new RGB and save them in shared pref
        if (randomNumR == 0 && randomNumG == 0 && randomNumB == 0)
        {
            randomNumR = randInt(1, 255);
            randomNumG = randInt(1, 255);
            randomNumB = randInt(1, 255);

            editor.putInt("randomNumR",randomNumR);
            editor.putInt("randomNumG",randomNumG);
            editor.putInt("randomNumB",randomNumB);

            editor.apply();
        }


        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        } else {
            // User is already signed in. Therefore, display
            // a welcome Toast
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();

            // Load chat room contents
            displayChatMessages();
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );

                // Clear the input
                input.setText("");
            }
        });

    }

    private void displayChatMessages() {

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);

                if (model.getMessageUser() == FirebaseAuth.getInstance().getCurrentUser().getDisplayName())
                {
                    messageText.setTextColor(getResources().getColor(android.R.color.white));
                    int randomNumR = preferences.getInt("randomNumR", 0);
                    int randomNumG = preferences.getInt("randomNumG", 0);
                    int randomNumB = preferences.getInt("randomNumB", 0);

                    messageText.setBackgroundColor(Color.rgb(randomNumR, randomNumG, randomNumB));
                }
                else
                {
                    messageText.setTextColor(getResources().getColor(android.R.color.white));

                    int randomNumR = 128;
                    int randomNumG = 128;
                    int randomNumB = 128;

                    messageText.setBackgroundColor(Color.rgb(randomNumR, randomNumG, randomNumB));
                }

                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();

                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }

    public static int randInt(int min, int max) {

        Random rn = new Random();
        int range = max - min + 1;
        int randomNum =  rn.nextInt(range) + min;

        return randomNum;
    }
}
