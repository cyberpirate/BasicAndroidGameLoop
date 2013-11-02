package com.example.basicgameloop;

import com.example.basicgameloop.R;

import android.app.Activity;
import android.os.Bundle;

public class StartingActivity extends Activity {
	private MainLoopView gameView;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_loop);
        
        gameView = (MainLoopView) findViewById(R.id.gameView);
        gameView.setActivity(this);
	}
}
