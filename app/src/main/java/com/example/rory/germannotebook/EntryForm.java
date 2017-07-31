package com.example.rory.germannotebook;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Word implements Serializable
{
    String name, meaning, type, example;
    ArrayList<String> presentVerbs, pastSimpleVerbs, futureVerbs;

    public Word(){
        presentVerbs = new ArrayList<String>();
        pastSimpleVerbs = new ArrayList<String>();
        futureVerbs = new ArrayList<String>();
        name = "";
        meaning = "";
        type = "";
        example = "";
    }
}

public class EntryForm extends MainActivity implements AdapterView.OnItemSelectedListener {
    private LinearLayout verbsLayout;
    Drawable originalDrawable;
    boolean editMode=true;
    boolean existingEntry = false;
    ArrayList<Word> words;
    EditText wordEdit, meaningEdit, exampleEdit;
    Spinner spinner;
    int indexOfEntry=-1;
    List<String> categories;
    String futureVerbPrefixes[];
    LinearLayout.LayoutParams originalParams;
    boolean presentVerbsExpanded = false;
    boolean pastVerbsExpanded = false;
    boolean futureVerbsExpanded = false;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry_form);
        futureVerbPrefixes = new String[6];
        futureVerbPrefixes[0] = "ich werde";
        futureVerbPrefixes[1] = "du wirst";
        futureVerbPrefixes[2] = "er/sie/es wird";
        futureVerbPrefixes[3] = "wir werden";
        futureVerbPrefixes[4] = "ihr werdet";
        futureVerbPrefixes[5] = "sie/Sie werden";

        // Spinner element
        spinner = (Spinner) findViewById(R.id.spinner);

        verbsLayout = (LinearLayout)this.findViewById(R.id.linear4);
        verbsLayout.setVisibility(LinearLayout.GONE);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);


        // Spinner Drop down elements
        categories = new ArrayList<String>();
        categories.add("Noun M.");
        categories.add("Noun F.");
        categories.add("Noun N.");
        categories.add("Adj.");
        categories.add("Verb");
        categories.add("Phrase");
        categories.add("Misc.");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        words = (ArrayList<Word>) getIntent().getSerializableExtra("words");
        if(words==null)
            words = new ArrayList<Word>();

        EditText edit = (EditText)findViewById(R.id.editText1);
        originalParams = new LinearLayout.LayoutParams(edit.getLayoutParams());

        wordEdit = (EditText) findViewById(R.id.wordText);
        meaningEdit = (EditText) findViewById(R.id.meaningText);
        exampleEdit = (EditText) findViewById(R.id.exampleText);

        collapseExpandVerbFields("");

        final Word word = (Word) getIntent().getSerializableExtra("word");
        if(word!=null) {
            initFields(word);
            EditTextsReadonlyMode(true, R.id.linear1);
            EditTextsReadonlyMode(true, R.id.linear2);
            EditTextsReadonlyMode(true, R.id.linear3);
            EditTextsReadonlyMode(true, R.id.linear4);
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A1B1B8")));
        if(!editMode) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp));
        }
        else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp));
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editMode==false) {
                    EditTextsReadonlyMode(false, R.id.linear1);
                    EditTextsReadonlyMode(false, R.id.linear2);
                    EditTextsReadonlyMode(false, R.id.linear3);
                    EditTextsReadonlyMode(false, R.id.linear4);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_black_24dp));
                    editMode=true;
                }
                else{
                    EditTextsReadonlyMode(true, R.id.linear1);
                    EditTextsReadonlyMode(true, R.id.linear2);
                    EditTextsReadonlyMode(true, R.id.linear3);
                    EditTextsReadonlyMode(true, R.id.linear4);
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_mode_edit_black_24dp));
                    editMode=false;
                    addToWordsList();
                    writeWordsToXML();
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    public void initFields(Word word)
    {

        int typeIndex = -1;
        existingEntry=true;
        editMode=false;
        originalDrawable = wordEdit.getBackground();
        wordEdit.setText(word.name);
        meaningEdit.setText(word.meaning);
        exampleEdit.setText(word.example);

        for(int i=0;i<categories.size();i++)
        {
            if(word.type.equalsIgnoreCase(categories.get(i))) {
                typeIndex = i;
                i=categories.size()+1;
            }
        }

        spinner.setSelection(typeIndex);

        LinearLayout conjugationFields = (LinearLayout)this.findViewById(R.id.linear4);
        int numberOfVerbs = 0;
        int presentVerbs = 0;
        int pastVerbs = 0;
        int futureVerbs = 0;
        for(int i = 0; i < conjugationFields.getChildCount(); i++) {
            if (conjugationFields.getChildAt(i) instanceof EditText) {
                EditText edit = (EditText) conjugationFields.getChildAt(i);
                if(numberOfVerbs<=5) {
                    edit.setText(word.presentVerbs.get(presentVerbs));
                    presentVerbs++;
                }
                else if(numberOfVerbs>5 && numberOfVerbs<12){
                    edit.setText(word.pastSimpleVerbs.get(pastVerbs));
                    pastVerbs++;
                }
                else{
                    if(word.futureVerbs.get(futureVerbs).isEmpty())
                        edit.setText(futureVerbPrefixes[futureVerbs]+" "+word.name.toLowerCase());
                    else
                        edit.setText(word.futureVerbs.get(futureVerbs));
                    futureVerbs++;
                }
                numberOfVerbs++;
                    //edit.setText(word.pastSimpleVerbs.get(conjugationCount - 6));
            }
        }

        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).name.equalsIgnoreCase(wordEdit.getText().toString())) {
                indexOfEntry = i;
            }
        }
    }

    public void onClick(View view) {
        if(view.getId()==R.id.presentView)
            collapseExpandVerbFields("present");
        else if(view.getId()==R.id.simplePastView)
            collapseExpandVerbFields("past");
        else if(view.getId()==R.id.futureView)
            collapseExpandVerbFields("future");

        presentVerbsExpanded=!presentVerbsExpanded;
        pastVerbsExpanded=!pastVerbsExpanded;
        futureVerbsExpanded=!futureVerbsExpanded;
    }

    public void collapseExpandVerbFields(String verbs)
    {
        LinearLayout conjugationFields = (LinearLayout)this.findViewById(R.id.linear4);
        int totalHeight=0;
        int numberOfVerbs = 0;
        for(int i = 0; i < conjugationFields.getChildCount(); i++) {
            if (conjugationFields.getChildAt(i) instanceof EditText)
            {
                EditText edit = (EditText) conjugationFields.getChildAt(i);
                edit.setTextSize(16);
                edit.setTextColor(Color.parseColor("#ffffff"));
                numberOfVerbs++;
                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(originalParams);

                if(verbs.isEmpty()) {
                    layoutParams.height = 0;
                    totalHeight = 340;
                }

                else if(verbs.equalsIgnoreCase("present"))
                {
                    if(numberOfVerbs<7)
                    {
                        layoutParams.topMargin = -30;
                        totalHeight = 920;
                    }
                    else{
                        layoutParams.height = 0;
                    }
                }
                else if(verbs.equalsIgnoreCase("past"))
                {
                    if (numberOfVerbs > 6 && numberOfVerbs < 13)
                    {
                        layoutParams.topMargin = -30;
                        totalHeight = 915;
                    }
                    else{
                        layoutParams.height = 0;
                    }
                }
                else if(verbs.equalsIgnoreCase("future"))
                {
                    if (numberOfVerbs > 12)
                    {
                        layoutParams.topMargin = -30;
                        totalHeight = 960;
                    }
                    else{
                        layoutParams.height = 0;
                    }
                }
                edit.setTypeface(edit.getTypeface(), Typeface.BOLD);
                edit.setLayoutParams(layoutParams);

            }
        }
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(conjugationFields.getLayoutParams());
        layoutParams.height = totalHeight;
        conjugationFields.setLayoutParams(layoutParams);
    }

    public void addToWordsList()
    {
        if(words.size()>0) {
            if(indexOfEntry>-1) {
                words.set(indexOfEntry, getWord());
            }
            else {
                words.add(getWord());
            }
        }
        else{
                words.add(getWord());
        }
    }

    public Word getWord()
    {
        Word newWord = new Word();
        newWord.name = wordEdit.getText().toString();
        newWord.meaning = meaningEdit.getText().toString();
        newWord.example = exampleEdit.getText().toString();
        newWord.type = spinner.getSelectedItem().toString();
        newWord.presentVerbs = new ArrayList<>();
        newWord.pastSimpleVerbs = new ArrayList<>();

        LinearLayout conjugationFields = (LinearLayout)this.findViewById(R.id.linear4);
        int conjugationCount = 0;
        for(int i = 0; i < conjugationFields.getChildCount(); i++) {
            if (conjugationFields.getChildAt(i) instanceof EditText) {
                EditText edit = (EditText) conjugationFields.getChildAt(i);
                conjugationCount++;
                if(conjugationCount<=6)
                    newWord.presentVerbs.add(edit.getText().toString());
                else if(conjugationCount>6 && conjugationCount<13)
                    newWord.pastSimpleVerbs.add(edit.getText().toString());
                else
                    newWord.futureVerbs.add(edit.getText().toString());

            }
        }

        return newWord;
    }
    public void writeWordsToXML()
    {
        File sdcard = Environment.getExternalStorageDirectory();
        File dir = new File(sdcard.getAbsolutePath() + "/german_notebook/");
        if(!dir.isDirectory())
            dir.mkdir();

        String fileName = "german_notebook_words.xml";
        File file = new File(dir, fileName);
        if(!file.exists())
            Toast.makeText(EntryForm.this, "XML File does not exist", Toast.LENGTH_SHORT).show();

        try {
            FileOutputStream os = new FileOutputStream(file);
            try{
                String xmlFileString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<WORDS>\n";
                for(int i=0;i<words.size();i++)
                {
                    xmlFileString+="\t<VALUE word=\""+words.get(i).name+"\" meaning=\""+words.get(i).meaning+"\" example=\""+words.get(i).example+"\" type=\""+words.get(i).type+"\"";
                    if(words.get(i).presentVerbs.size()>0)
                        xmlFileString+=">\n\t\t<PRESENT_TENSE " +
                                "ich=\""+words.get(i).presentVerbs.get(0)+
                                "\" du=\""+words.get(i).presentVerbs.get(1)+
                                "\" er_sie_es=\""+words.get(i).presentVerbs.get(2)+
                                "\" wir=\""+words.get(i).presentVerbs.get(3)+
                                "\" ihr=\""+words.get(i).presentVerbs.get(4)+
                                "\" Sie=\""+words.get(i).presentVerbs.get(5)+"\" />";
                    if(words.get(i).pastSimpleVerbs.size()>0)
                        xmlFileString+="\n\t\t<PAST_SIMPLE " +
                                "ich=\""+words.get(i).pastSimpleVerbs.get(0)+
                                "\" du=\""+words.get(i).pastSimpleVerbs.get(1)+
                                "\" er_sie_es=\""+words.get(i).pastSimpleVerbs.get(2)+
                                "\" wir=\""+words.get(i).pastSimpleVerbs.get(3)+
                                "\" ihr=\""+words.get(i).pastSimpleVerbs.get(4)+
                                "\" Sie=\""+words.get(i).pastSimpleVerbs.get(5)+"\" />";
                    if(words.get(i).futureVerbs.size()>0)
                        xmlFileString+="\n\t\t<FUTURE " +
                                "ich=\""+words.get(i).futureVerbs.get(0)+
                                "\" du=\""+words.get(i).futureVerbs.get(1)+
                                "\" er_sie_es=\""+words.get(i).futureVerbs.get(2)+
                                "\" wir=\""+words.get(i).futureVerbs.get(3)+
                                "\" ihr=\""+words.get(i).futureVerbs.get(4)+
                                "\" Sie=\""+words.get(i).futureVerbs.get(5)+"\" />";
                    if(words.get(i).pastSimpleVerbs.size()>0 ||
                            words.get(i).presentVerbs.size()>0 ||
                            words.get(i).futureVerbs.size()>0)
                        xmlFileString+="\n\t</VALUE>\n";
                    else
                        xmlFileString+=" />\n";
                }
                xmlFileString+="</WORDS>";
                os.write(xmlFileString.getBytes());
                os.close();
            }catch(IOException e){
                e.printStackTrace();
            }

        } catch(FileNotFoundException fnfe) {
            System.out.println(fnfe.getMessage());
        }

    }

    void EditTextsReadonlyMode(boolean enable, int id)
    {
        LinearLayout linear = (LinearLayout)this.findViewById(id);

        for(int i = 0; i < linear.getChildCount(); i++) {
            if(linear.getChildAt(i) instanceof EditText) {
                EditText edit = (EditText) linear.getChildAt(i);
                if(enable==true) {
                    edit.setFocusable(false);
                    edit.setBackground(null);
                } else {
                    edit.setFocusableInTouchMode(true);
                    edit.setFocusable(true);
                    edit.setBackground(originalDrawable);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        if(item=="Verb")
        {
            verbsLayout.setVisibility(LinearLayout.VISIBLE);
        }
        if(item=="Phrase")
        {
            LinearLayout layout = (LinearLayout)findViewById(R.id.linear3);
            layout.setVisibility(LinearLayout.INVISIBLE);
        }

    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //return true;
        menu.clear();
        menu.add("Remove Word");
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }
        else if(id==0){
            for(int i=0;i<words.size();i++)
            {
                if(words.get(i).name.equalsIgnoreCase(wordEdit.getText().toString())) {
                    words.remove(i);
                }
            }
            writeWordsToXML();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("words", words);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
}
