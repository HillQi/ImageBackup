package com.iceqi.mydemo.ui.common.java;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ShowPopUp extends Activity {
    PopupWindow popUp;
    boolean click = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        popUp = new PopupWindow(this);
        LinearLayout layout = new LinearLayout(this);
        LinearLayout mainLayout = new LinearLayout(this);
        TextView tv = new TextView(this);
        Button but = new Button(this);
        but.setText("Click Me");
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click) {
                    popUp.showAtLocation(layout, Gravity.CENTER, 10, 10);
                    popUp.update(50, 50, 500, 200);
                    click = false;
                } else {
                    popUp.dismiss();
                    click = true;
                }
            }
        });

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        tv.setText("Hi this is a sample text for popup window");
        layout.addView(tv, params);
        popUp.setContentView(layout);
        // popUp.showAtLocation(layout, Gravity.BOTTOM, 10, 10);
        mainLayout.addView(but, params);
        setContentView(mainLayout);
    }
}