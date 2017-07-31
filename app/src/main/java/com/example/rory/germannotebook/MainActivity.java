package com.example.rory.germannotebook;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Xml;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import android.os.Environment;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.Comparator;
import java.util.List;
import java.io.File;


public class MainActivity extends ActionBarActivity implements AdapterView.OnItemClickListener {

    ListView listView;
    ArrayAdapter<String> listAdapter;
    ArrayList<Word> words;
    String xmlText;
    boolean germanFirst;
    boolean isSwipe = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        words = new ArrayList<Word>();

        parseXMLAndPopulateListView();

        SharedPreferences settings = getSharedPreferences("GermanNotebooks", 0);
        germanFirst = settings.getBoolean("german_first", true);

        //this will cause a crash if the xml file doesn't exist!!
        listView.setOnItemClickListener(this);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A1B1B8")));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), EntryForm.class);
                i.putExtra("words", words);
                startActivity(i);

            }
        });

        listView = (ListView)findViewById(R.id.listView);
        listView.setOnTouchListener(new View.OnTouchListener() {
            private int action_down_x = 0;
            private int action_up_x = 0;
            private int difference = 0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        action_down_x = (int) event.getX();
                        isSwipe=false;  //until now
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(!isSwipe)
                        {
                            action_up_x = (int) event.getX();
                            difference = action_down_x - action_up_x;
                            if(Math.abs(difference)>200)
                            {
                                //swipe left or right
                                if(difference>0){
                                    //swipe left
                                    Toast.makeText(MainActivity.this, "swipe left", Toast.LENGTH_SHORT).show();
                                    Intent activity = new Intent(getApplicationContext(), CheatSheet.class);
                                    startActivity(activity);
                                }
                                else{
                                    //swipe right
                                    //Toast.makeText(MainActivity.this, "swipe right", Toast.LENGTH_SHORT).show();
                                }
                                isSwipe=true;
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d("action", "ACTION_UP - ");
                        action_down_x = 0;
                        action_up_x = 0;
                        difference = 0;
                        break;
                }
                return false;
            }
        });

    }

    void parseXMLAndPopulateListView()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File file = new File(sdCard.getAbsolutePath() + "/german_notebook/german_notebook_words.xml");
        if(!file.exists()) {
            return;
        }
        ArrayList<String> wordList = new ArrayList<String>();

        try {
            words.clear();
            File xmlFile = new File(file.getAbsolutePath());
            FileInputStream fis = new FileInputStream(xmlFile );

            StringBuilder builder = new StringBuilder();
            int ch;
            while((ch = fis.read()) != -1){
                builder.append((char)ch);
            }

            fis = new FileInputStream(xmlFile );


            xmlText = builder.toString();

            XmlPullParser xpp = Xml.newPullParser();
            xpp.setInput(new InputStreamReader(fis));
            //XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

            int event;
            String currentWord="";
            while ((event = xpp.next()) != XmlPullParser.END_DOCUMENT)
            {
                Word newWord = new Word();
                if (event == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("VALUE")) {
                    currentWord = xpp.getAttributeValue(0);
                    newWord.name = currentWord;
                    newWord.meaning = xpp.getAttributeValue(1);
                    newWord.example = xpp.getAttributeValue(2);
                    newWord.type = xpp.getAttributeValue(3);
                    newWord.pastSimpleVerbs = new ArrayList<>();
                    newWord.presentVerbs = new ArrayList<>();
                    newWord.futureVerbs = new ArrayList<>();

                    words.add(newWord);
                    String type = "";
                    if(newWord.type.contains("Noun"))
                    {
                        if(newWord.type.contains("M."))
                            type+=" (der)";
                        else if(newWord.type.contains("F."))
                            type+=" (die)";
                        else if(newWord.type.contains("N."))
                            type+=" (das)";
                    }

                    SharedPreferences settings = getSharedPreferences("GermanNotebooks", 0);
                    if(settings.getBoolean("german_first", true)) {
                        String wordToAddToList = xpp.getAttributeValue(0) + type + ": " + xpp.getAttributeValue(1);
                        wordList.add(wordToAddToList);
                    }
                    else{
                        String wordToAddToList = xpp.getAttributeValue(1) + ": "+xpp.getAttributeValue(0) + type;
                        wordList.add(wordToAddToList);
                    }

                }
                if (event == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("PRESENT_TENSE")) {
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        newWord.presentVerbs.add(xpp.getAttributeValue(i));
                    }
                    words.get(words.size()-1).presentVerbs = newWord.presentVerbs;
                }
                else if (event == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("PAST_SIMPLE")) {
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        newWord.pastSimpleVerbs.add(xpp.getAttributeValue(i));
                    }
                    words.get(words.size()-1).pastSimpleVerbs = newWord.pastSimpleVerbs;
                }
                else if (event == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("FUTURE")) {
                    for (int i = 0; i < xpp.getAttributeCount(); i++) {
                        newWord.futureVerbs.add(xpp.getAttributeValue(i));
                    }
                    words.get(words.size()-1).futureVerbs = newWord.futureVerbs;
                }
            }
        }  catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        };

        addWordsToListView(wordList);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        for(int i=0;i<words.size();i++)
        {
            String data=(String)parent.getItemAtPosition(position);
            if(data.contains(words.get(i).name)) {
                Intent activity = new Intent(getApplicationContext(), EntryForm.class);
                activity.putExtra("word", words.get(i));
                activity.putExtra("words", words);
                startActivity(activity);
            }
        }
    }

    //add tune types to list view
    void addWordsToListView(List<String> str){
        // Find the ListView resource.
        listView = (ListView) findViewById( R.id.listView);
        ArrayList<String> wordList = new ArrayList<String>();

        listAdapter = new ArrayAdapter<String>(this, R.layout.listview_text_item, wordList);
        int numberOfWords=0;

        for(String word : str)
        {
            listAdapter.add(word);
            numberOfWords++;
        }
        if(numberOfWords==0)
            listAdapter.add("No words found. Notebook is empty.");

        listAdapter.sort(new Comparator<String>() {
            public int compare(String object1, String object2) {
                return object1.compareToIgnoreCase(object2);
            };
        });

        listView.setAdapter( listAdapter );
        listView.setBackgroundResource(R.drawable.rounded_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("About");
            alertDialog.setMessage("Copyright Rory Walsh 2016.\n\nThe source code for this app can be viewed at https://github.com/rorywalsh");
            alertDialog.show();
            return true;
        }
        else if(id == R.id.action_share){
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "German Notebook");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, xmlText);
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        }
        else if(id == R.id.action_toggle_sort)
        {
            SharedPreferences settings = getSharedPreferences("GermanNotebooks", 0);
            SharedPreferences.Editor editor = settings.edit();
            germanFirst = !germanFirst;
            editor.putBoolean("german_first", germanFirst);
            editor.apply();
            parseXMLAndPopulateListView();

        }

        return super.onOptionsItemSelected(item);
    }
}
