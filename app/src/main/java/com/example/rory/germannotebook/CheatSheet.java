package com.example.rory.germannotebook;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class CheatSheet extends MainActivity {
    boolean isSwipe = false;
    LinearLayout.LayoutParams originalParams;
    FrameLayout.LayoutParams mainLayoutParams;
    LinearLayout mainLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat_sheet);

        TextView edit = (TextView)findViewById(R.id.textView1);
        originalParams = new LinearLayout.LayoutParams(edit.getLayoutParams());

        mainLayout = (LinearLayout)this.findViewById(R.id.linearLayoutSheet);
        mainLayoutParams = new FrameLayout.LayoutParams(mainLayout.getLayoutParams());

        ScrollView scroller = (ScrollView)findViewById(R.id.scrollViewSheet);
        scroller.setOnTouchListener(new View.OnTouchListener() {
            private int action_down_x = 0;
            private int action_up_x = 0;
            private int difference = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        action_down_x = (int) event.getX();
                        isSwipe = false;  //until now
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isSwipe) {
                            action_up_x = (int) event.getX();
                            difference = action_down_x - action_up_x;
                            if (Math.abs(difference) > 50) {
                                //swipe left or right
                                if (difference > 0) {
                                    //swipe left
                                    //Toast.makeText(CheatSheet.this, "swipe left", Toast.LENGTH_SHORT).show();
                                    //Intent activity = new Intent(getApplicationContext(), CheatSheet.class);
                                    //startActivity(activity);
                                } else {
                                    //swipe right
                                    Toast.makeText(CheatSheet.this, "swipe right", Toast.LENGTH_SHORT).show();
                                }
                                isSwipe = true;
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

        collapseSections("");

    }

    public void onClick(View view) {
        if(view.getId()==R.id.possessiveView)
            collapseSections("possessive");
        else if(view.getId()==R.id.intensifiersView)
            collapseSections("intensifiers");
        else if(view.getId()==R.id.accusativePropsView)
            collapseSections("accusativeProps");
    }

    public void collapseSections(String verbs)
    {
       // LinearLayout mainLayout = (LinearLayout)this.findViewById(R.id.linearLayoutSheet);
        int totalHeight=0;
        int numberOfWords = 0;
        for(int i = 0; i < mainLayout.getChildCount(); i++) {
            if (mainLayout.getChildAt(i) instanceof TextView)
            {
                TextView view = (TextView) mainLayout.getChildAt(i);
                //Toast.makeText(CheatSheet.this, view.getText().toString(), Toast.LENGTH_SHORT).show();
                if(!view.getTag().toString().equals("textViewer")) {
                    numberOfWords++;
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(originalParams);

                    if (verbs.isEmpty()) {
                        layoutParams.height = 0;
                    } else if (verbs.equalsIgnoreCase("possessive")) {
                        if (numberOfWords < 9) {
                            layoutParams.topMargin = 0;
                        } else {
                            layoutParams.height = 0;
                        }
                    } else if (verbs.equalsIgnoreCase("intensifiers")) {
                        if (numberOfWords > 9 && numberOfWords < 17) {
                            layoutParams.topMargin = 0;
                        } else {
                            layoutParams.height = 0;
                        }
                    } else if (verbs.equalsIgnoreCase("accusativeProps")) {
                        if (numberOfWords > 17) {
                            layoutParams.topMargin = 0;
                        } else {
                            layoutParams.height = 0;
                        }
                    }

                    view.setLayoutParams(layoutParams);
                }
            }
        }

    }

}
