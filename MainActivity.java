package com.example.spacejunk;
import java.util.Random;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Vibrator;

public class MainActivity extends Activity implements OnTouchListener{
	
	private ImageView background;
	private ImageView meteor1, meteor2, meteor3, ufo, shields, soundState;
	private int width, height, impacts = 30, state = 1;
	private int acrossDelay = 3000;
	private Random r;
	protected Handler meteorHandler1, meteorHandler2, meteorHandler3;
	protected Handler collisionHandler, timeHandler;
	private TextView timeView, bestView;
	private SoundPool sounds;
	private int crash, gameover;
	private Vibrator v;
	private AlertDialog alert;
	private int lifeSpan = 0, best = 0;
	private static final String PREF_FILE_NAME = "PrefFile";
	private SharedPreferences pref;
	private Editor editor;
	private String time, bestTime;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.flyavoid_activity);
		
		background = (ImageView)findViewById(R.id.background);
    		background.setOnTouchListener(this);
        
		meteor1 = (ImageView)findViewById(R.id.rock);
		meteor2 = (ImageView)findViewById(R.id.rock2);
	    	meteor3 = (ImageView)findViewById(R.id.rock3);
	    	ufo = (ImageView)findViewById(R.id.ufo);
	    	shields = (ImageView)findViewById(R.id.shields);
	    	soundState = (ImageView)findViewById(R.id.soundstate);
        
	    	DisplayMetrics displaymetrics = new DisplayMetrics();
	    	getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
	    	height = displaymetrics.heightPixels;
	    	width = displaymetrics.widthPixels;
        
	    	timeView = (TextView)findViewById(R.id.time);
	    	bestView = (TextView)findViewById(R.id.best);
	    	Typeface font1 = Typeface.createFromAsset(getAssets(), "fonts/littletroublegirl.ttf");
	    	timeView.setTypeface(font1);
	    	bestView.setTypeface(font1);
	    	time = getResources().getString(R.string.time);
	    	timeView.setText(time + lifeSpan);
	    	pref = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
	    	editor = pref.edit();
	    	best = pref.getInt("value", 0);
	    	bestTime = getResources().getString(R.string.best);
	    	bestView.setText(bestTime + best);
	        
	    	r = new Random();
    
	    	meteorHandler1 = new Handler();
	    	meteorHandler2 = new Handler();
	    	meteorHandler3 = new Handler();
	    	collisionHandler = new Handler();
	    	timeHandler = new Handler();
	    
	    	sounds = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
	    	crash = sounds.load(getBaseContext(), R.raw.crumbling, 1);
	    	gameover = sounds.load(getBaseContext(), R.raw.dun_dun_dun, 1);
	    
	    	v = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setTitle(R.string.alert_title);
	    	builder.setIcon(R.drawable.rock2);
	    	builder.setMessage(R.string.alert_text)
	    	.setCancelable(false)
	    	.setPositiveButton(R.string.alert_quit, new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int id) {
	        		best = pref.getInt("value", best);
	        		if(best < lifeSpan){
	        			editor.putInt("value", lifeSpan);
	        			editor.commit();
	        		}
	            		finish();
	        	}
	    	})
	    	.setNegativeButton(R.string.alert_replay, new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int id) {
	        		best = pref.getInt("value", best);
	        		if(best < lifeSpan){
	        			editor.putInt("value", lifeSpan);
	        			editor.commit();
	        		}
	        		finish();
	        		startActivity(getIntent());
	            		dialog.cancel();
	        	}
	    	});
	    	alert = builder.create();
	}
	
	public void soundstate(View v){
		if(state == 1){
			soundState.setImageResource(R.drawable.sounds_off);
			state = 0;
		}
		else if(state == 0){
			soundState.setImageResource(R.drawable.sounds_on);
			state = 1;
		}
	}
		
	public void startItAll(){
		meteorHandler1.postDelayed(moveMeteor1, 100);
		meteorHandler2.postDelayed(moveMeteor2, 1000);
		meteorHandler3.postDelayed(moveMeteor3, 2500);
		collisionHandler.post(coll);
		timeHandler.postDelayed(timer, 1000);
	}
	public void stopItAll(){
		meteorHandler1.removeCallbacks(moveMeteor1);
		meteorHandler2.removeCallbacks(moveMeteor2);
		meteorHandler3.removeCallbacks(moveMeteor3);
		collisionHandler.removeCallbacks(coll);
		timeHandler.removeCallbacks(timer);
	}
	private final Runnable timer = new Runnable(){
		@Override 
		public void run() {
			time = getResources().getString(R.string.time);
			lifeSpan += 1;
			timeView.setText(time + lifeSpan);
			timeHandler.postDelayed(timer, 1000);
		} 
	};
	private final Runnable moveMeteor1 = new Runnable(){ 
		@Override 
		public void run() {
			moveThatMeteor1();
			meteorHandler1.postDelayed(moveMeteor1, acrossDelay);
		} 
	};
	private final Runnable moveMeteor2 = new Runnable(){ 
		@Override 
		public void run() {
			moveThatMeteor2();
			meteorHandler2.postDelayed(moveMeteor2, acrossDelay);
		} 
	};
	private final Runnable moveMeteor3 = new Runnable(){ 
		@Override 
		public void run() {
			moveThatMeteor3();
			meteorHandler3.postDelayed(moveMeteor3, acrossDelay);
		} 
	};
	private final Runnable coll = new Runnable(){
		@Override 
		public void run() {
			collision();
			collisionHandler.postDelayed(coll, 500);
		} 
	};
	public void moveThatMeteor1(){
		meteor1.setImageResource(R.drawable.rock1);
		int min = 50;
		int max = (int) ((height * 0.8) - 50);
		int y = r.nextInt((int) (max - min + 1)) + min;
		meteor1.setY(y);
		meteor1.setRotation(0);
		meteor1.setX(-10);
		meteor1.animate().setDuration(acrossDelay);
		meteor1.animate().rotation(300);
		meteor1.animate().translationXBy(width + 15);
	}
	public void moveThatMeteor2(){
		meteor2.setImageResource(R.drawable.rock2);
		int min = 50;
		int max = (int) ((height * 0.8) - 50);
		int y = r.nextInt((int) (max - min + 1)) + min;
		meteor2.setY(y);
		meteor2.setRotation(0);
		meteor2.setX(-10);
		meteor2.animate().setDuration(acrossDelay);
		meteor2.animate().rotation(400);
		meteor2.animate().translationXBy(width + 15);
	}
	public void moveThatMeteor3(){
		meteor3.setImageResource(R.drawable.rock2);
		int min = 50;
		int max = (int) ((height * 0.8) - 50);
		int y = r.nextInt((int) (max - min + 1)) + min;
		meteor3.setY(y);
		meteor3.setX(-10);
		meteor3.animate().setDuration(acrossDelay);
		meteor2.animate().rotation(270);
		meteor3.animate().translationXBy(width + 15);
	}
	public void flip(int a, View collider){
		impacts -= a;
		System.out.println("IMPACTS: " + impacts);
		if(collider.equals(meteor1) || collider.equals(meteor2) || collider.equals(meteor3)){
			((ImageView) collider).setImageResource(R.drawable.crash);
			if(state == 1){
				sounds.play(crash, 0.5f, 0.5f, 0, 0, 1.5f);
				v.vibrate(500);
			}
			else if(state == 0){}
		}
		
		ufo.setRotation(0);
		ufo.animate().setDuration(500);
		ufo.animate().rotation(360);
		if(impacts < 30 && impacts > 20){
			shields.setImageResource(R.drawable.full);
		}
		else if(impacts <= 20 && impacts > 10){
			shields.setImageResource(R.drawable.middle);
		}
		else if(impacts <= 10 && impacts > 1){
			shields.setImageResource(R.drawable.low);
		}
		else if(impacts <= 0){
			if(state == 1){
				sounds.play(gameover, 1.0f, 1.0f, 0, 0, 1.0f);
			}
			else if(state == 0){}
			alert.show();
		}
	}
	
	public boolean onTouch(View view, MotionEvent event) {
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			int ufo_y = (int)ufo.getY();
			int img_y = (int)ufo.getY() + ufo.getHeight()/2;
			int event_y = (int) event.getY();
			
			if(event_y < img_y){
				ufo.setY(ufo_y - 15);
			}
			else if(event_y > img_y){
				ufo.setY(ufo_y + 15);
			}
			break;
		case MotionEvent.ACTION_UP:
	        	view.performClick();
	        	break;
		default:
	        	break;
		}
		return true;
	}
	public void collision(){
		Rect rect_mover1 = new Rect();
		Rect rect_mover2 = new Rect();
		Rect rect_mover3 = new Rect();
		Rect rect_ufo = new Rect();
		findViewById(R.id.rock).getHitRect(rect_mover1);
		findViewById(R.id.rock2).getHitRect(rect_mover2);
		findViewById(R.id.rock3).getHitRect(rect_mover3);
		rect_ufo.left = (int) ufo.getX();
		rect_ufo.top = (int) ufo.getY();
		rect_ufo.right = rect_ufo.left + ufo.getWidth();
		rect_ufo.bottom = rect_ufo.top + ufo.getHeight();
		rect_ufo.set(rect_ufo.left+10, rect_ufo.top+5, rect_ufo.right-5, rect_ufo.bottom-5);
		
		if(Rect.intersects(rect_mover1, rect_ufo)){
			flip(2, meteor1);
		}
		else if(Rect.intersects(rect_mover2, rect_ufo)){
			flip(1, meteor2);
		}
		else if(Rect.intersects(rect_mover3, rect_ufo)){
			flip(1, meteor3);
		}
	}
	
	public void onWindowFocusChanged(boolean hasFocus) {
	    super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
	    		startItAll();
		} else {
			stopItAll();
		}
	}
	
	protected void onStart(){
		super.onStart();
	}
	protected void onPause(){
    		super.onPause();
    }
	protected void onResume(){
    		super.onResume();
    }
	protected void onDestroy() {
        	super.onDestroy();
    }
}
