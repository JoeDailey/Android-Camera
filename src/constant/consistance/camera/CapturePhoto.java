package constant.consistance.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;


import constant.consistance.camera.R;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class CapturePhoto extends Activity {
	private SurfaceView preview=null;
	private SurfaceHolder previewHolder=null;
	private Camera camera=null;
	private boolean inPreview=false;
	private boolean cameraConfigured=false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_capture_photo);
		
	}
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		preview=(SurfaceView)findViewById(R.id.preview);
		previewHolder=preview.getHolder();
		previewHolder.addCallback(surfaceCallback);
		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		LinearLayout animationTarget = (LinearLayout) this.findViewById(R.id.Camera_Controls);
		animationTarget.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent event) {
				if(!optionsOut){
					click(view);
					return true;
				}
				return false;
			}
		});
		Animation animation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_in);
		animationTarget.startAnimation(animation);


		FrameLayout animationSettingsTarget = (FrameLayout) this.findViewById(R.id.Camera_Settings);
		Animation SettingsAnimation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_scenes_in);
		animationSettingsTarget.startAnimation(SettingsAnimation);

	}

	private int currentCameraId;
	@Override
	public void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info=new Camera.CameraInfo();

			for (int i=0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, info);

				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					findViewById(R.id.Camera_Direction_Control).setVisibility(View.VISIBLE);
					currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
				}
			}
		}

		if (camera == null) {
			camera=Camera.open();
		}
		Camera.Parameters params = camera.getParameters();
		
		if(camera.getParameters().getSupportedFocusModes().contains(Parameters.FLASH_MODE_AUTO))
			params.setFocusMode(Parameters.FLASH_MODE_AUTO);

		camera.setParameters(params);


		initButtons();
		//		List<String> scenes = camera.getParameters().getSupportedSceneModes();
		//		Log.e("scenes", "s: "+scenes);
		//		List<String> colors = camera.getParameters().getSupportedColorEffects();
		//		Log.e("colors", "c: "+colors);
		//		List<String> flash = camera.getParameters().getSupportedFlashModes();
		//		Log.e("flash", "f: "+flash);
		//		List<String> focus = camera.getParameters().getSupportedFocusModes();
		//		Log.e("focus", "f: "+focus);

		startPreview();
	}

	private boolean optionsOut=false;
	public void click(View view){
		LinearLayout animationTarget = (LinearLayout) this.findViewById(R.id.Camera_Controls);
		Animation animation;

		if(optionsOut)
			animation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_in);
		else
			animation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_out);
		optionsOut = !optionsOut;
		animationTarget.startAnimation(animation);

		FrameLayout settings = (FrameLayout) this.findViewById(R.id.Camera_Settings);
		Animation settingAnimation;

		if(effectsOut){
			settingAnimation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_scenes_in);
			effectsOut = !effectsOut;
			settings.startAnimation(settingAnimation);
		}

	}


	private int timer = 0;
	private Drawable[] timerDrawables;
	public void Timer_Click(View view){
		if(optionsOut){
			timer = (timer+5)%20;
			((ImageButton)findViewById(R.id.Camera_Timer_Control)).setImageDrawable(timerDrawables[timer/5]);
		}
	}
	private boolean effectsOut=false;
	public void Flash_Click(View view){
		if(optionsOut){
			FrameLayout animationTarget = (FrameLayout) this.findViewById(R.id.Camera_Settings);
			Animation animation;
	
			findViewById(R.id.Camera_Effects_Settings).setVisibility(View.GONE);
			findViewById(R.id.Camera_Scene_Settings).setVisibility(View.GONE);
			findViewById(R.id.Camera_Flash_Settings).setVisibility(View.VISIBLE);
			if(!effectsOut){
				animation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_scenes_out);
				effectsOut = !effectsOut;
				animationTarget.startAnimation(animation);
			}
		}
	}	
	public void Effect_Click(View view){
		if(optionsOut){
			FrameLayout animationTarget = (FrameLayout) this.findViewById(R.id.Camera_Settings);
			Animation animation;
	
			findViewById(R.id.Camera_Effects_Settings).setVisibility(View.VISIBLE);
			findViewById(R.id.Camera_Scene_Settings).setVisibility(View.GONE);
			findViewById(R.id.Camera_Flash_Settings).setVisibility(View.GONE);
			if(!effectsOut){
				animation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_scenes_out);
				effectsOut = !effectsOut;
				animationTarget.startAnimation(animation);
			}
		}
	}
	public void Scene_Click(View view){
		if(optionsOut){
			FrameLayout animationTarget = (FrameLayout) this.findViewById(R.id.Camera_Settings);
			Animation animation;
	
			findViewById(R.id.Camera_Effects_Settings).setVisibility(View.GONE);
			findViewById(R.id.Camera_Scene_Settings).setVisibility(View.VISIBLE);
			findViewById(R.id.Camera_Flash_Settings).setVisibility(View.GONE);
			if(!effectsOut){
				animation = AnimationUtils.loadAnimation(this, R.anim.camera_controls_scenes_out);
				effectsOut = !effectsOut;
				animationTarget.startAnimation(animation);
			}
		}
	}
	public void take_click(View view){
		for(int i = 0; i < timer;i++)
			try {Thread.sleep(1000);} catch (InterruptedException e) {}
		camera.takePicture(null, null, photoCallback);
		AnimationDrawable ad = (AnimationDrawable) ((ImageButton)findViewById(R.id.Camera_Take)).getDrawable();
		ad.start();
	}
	public void ratio_click(View view){
		if(optionsOut){
			wide=!wide;
			if(wide)
				((ImageButton)findViewById(R.id.Camera_Ratio_Control)).setImageResource(R.drawable.camera_control_ratio_wide);
			else
				((ImageButton)findViewById(R.id.Camera_Ratio_Control)).setImageResource(R.drawable.camera_control_ratio_full);
			Camera.Parameters params = camera.getParameters();
			Camera.Size prevSize = getBestPreviewSize(params);
			Camera.Size captSize = getSmallestPictureSize(params);
			params.setPreviewSize(prevSize.width, prevSize.height);
			params.setPictureSize(captSize.width, captSize.height);
			camera.setParameters(params);
		}
	}
	public void switch_click(View view){
		if(optionsOut){
			if (inPreview) {
				camera.stopPreview();
			}
			camera.release();
	
			if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
				currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
			}
			else {
				currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
			}
			camera = Camera.open(currentCameraId);
	
			initialized = false;
			initButtons();
			initPreview();
			startPreview();
		}
	}
	@Override
	public void onPause() {
		if (inPreview) {
			camera.stopPreview();
		}

		camera.release();
		camera=null;
		inPreview=false;

		super.onPause();
	}

	private List<Size> captWide = new ArrayList<Camera.Size>();
	private List<Size> captFull = new ArrayList<Camera.Size>();

	private List<Size> prevWide = new ArrayList<Camera.Size>();
	private List<Size> prevFull = new ArrayList<Camera.Size>();
	private boolean wide = true;
	private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
		Camera.Size result=null;
		List<Camera.Size> Sizes =  parameters.getSupportedPreviewSizes();
		for (Camera.Size size : Sizes) {
			if(size.height*4/size.width == 3)
				prevFull.add(size);
			else
				prevWide.add(size);
		}
		if(wide)
			Sizes = prevWide;
		else
			Sizes = prevFull;
		for (Camera.Size size : Sizes) {
			if (result == null) {
				result=size;
			}
			else {
				int resultArea=result.width * result.height;
				int newArea=size.width * size.height;

				if (newArea > resultArea) {
					result=size;
				}
			}
		}
		//		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
		//			if (size.width <= width && size.height <= height) {
		//				if (result == null) {
		//					result=size;
		//				}
		//				else {
		//					int resultArea=result.width * result.height;
		//					int newArea=size.width * size.height;
		//
		//					if (newArea > resultArea) {
		//						result=size;
		//					}
		//				}
		//			}
		//		}
		return(result);
	}

	private Camera.Size getSmallestPictureSize(Camera.Parameters parameters) {
		Camera.Size result=null;
		String sizes = "";
		List<Camera.Size> Sizes =  parameters.getSupportedPictureSizes();
		for (Camera.Size size : Sizes) {
			if(size.height*4/size.width == 3)
				captFull.add(size);
			else
				captWide.add(size);
		}
		if(wide)
			Sizes = captWide;
		else
			Sizes = captFull;
		for (Camera.Size size : Sizes) {
			if (result == null) {
				result=size;
			}
			else {
				int resultArea=result.width * result.height;
				int newArea=size.width * size.height;

				if (newArea > resultArea) {
					result=size;
				}
			}
		}
		Log.i("Sizes", sizes);
		return(result);
	}
	private boolean initialized = false;
	private void initButtons(){
		if(!initialized){
			((ViewGroup)findViewById(R.id.Camera_Scene_Settings)).removeAllViews();
			((ViewGroup)findViewById(R.id.Camera_Effects_Settings)).removeAllViews();
			((ViewGroup)findViewById(R.id.Camera_Flash_Settings)).removeAllViews();
			initialized = true;
			List<String> scenes = camera.getParameters().getSupportedSceneModes();
			Log.e("scenes", "s:"+scenes);
			if(scenes!=null){
				int count = 0;
				for(String s : scenes){
					if(s.equals(Parameters.SCENE_MODE_AUTO)){ 			addScene(createButton(SCENE_MODE_AUTO));		count++;}
					if(s.equals(Parameters.SCENE_MODE_ACTION)){			addScene(createButton(SCENE_MODE_ACTION));		count++;}
					if(s.equals(Parameters.SCENE_MODE_BEACH)){			addScene(createButton(SCENE_MODE_BEACH));		count++;}
					if(s.equals(Parameters.SCENE_MODE_CANDLELIGHT)){ 	addScene(createButton(SCENE_MODE_CANDLELIGHT));	count++;}
					if(s.equals(Parameters.SCENE_MODE_FIREWORKS)){ 		addScene(createButton(SCENE_MODE_FIREWORKS));	count++;}
					if(s.equals(Parameters.SCENE_MODE_HDR)){ 			addScene(createButton(SCENE_MODE_HDR));			count++;}
					if(s.equals(Parameters.SCENE_MODE_LANDSCAPE)){ 		addScene(createButton(SCENE_MODE_LANDSCAPE));	count++;}
					if(s.equals(Parameters.SCENE_MODE_NIGHT)){ 			addScene(createButton(SCENE_MODE_NIGHT));		count++;}
					if(s.equals(Parameters.SCENE_MODE_NIGHT_PORTRAIT)){	addScene(createButton(SCENE_MODE_NIGHT));		count++;}
					if(s.equals(Parameters.SCENE_MODE_PARTY)){ 			addScene(createButton(SCENE_MODE_PARTY));		count++;}
					if(s.equals(Parameters.SCENE_MODE_PORTRAIT)){ 		addScene(createButton(SCENE_MODE_PORTRAIT));	count++;}
					if(s.equals(Parameters.SCENE_MODE_SNOW)){ 			addScene(createButton(SCENE_MODE_SNOW));		count++;}
					if(s.equals(Parameters.SCENE_MODE_SPORTS)){			addScene(createButton(SCENE_MODE_SPORTS));		count++;}
					if(s.equals(Parameters.SCENE_MODE_STEADYPHOTO)){ 	addScene(createButton(SCENE_MODE_STEADYPHOTO));	count++;}
					if(s.equals(Parameters.SCENE_MODE_SUNSET)){			addScene(createButton(SCENE_MODE_SUNSET));		count++;}
					if(s.equals(Parameters.SCENE_MODE_THEATRE)){ 		addScene(createButton(SCENE_MODE_THEATRE));		count++;}
				}
				if(count == 0)
					findViewById(R.id.Camera_Scene_Control).setVisibility(View.GONE);
				else
					findViewById(R.id.Camera_Scene_Control).setVisibility(View.VISIBLE);
			}else
				findViewById(R.id.Camera_Scene_Control).setVisibility(View.GONE);

			List<String> effects = camera.getParameters().getSupportedColorEffects();
			Log.e("effects", "e:"+effects);
			if(effects!=null){
				int count = 0;
				for(String e : effects){
					if(e.equals(Parameters.EFFECT_AQUA)) {		addEffect(createButton(EFFECT_AQUA));		count++;}
					if(e.equals(Parameters.EFFECT_BLACKBOARD)){ addEffect(createButton(EFFECT_BLACKBOARD));	count++;}
					if(e.equals(Parameters.EFFECT_MONO)){		addEffect(createButton(EFFECT_MONO));		count++;}
					if(e.equals(Parameters.EFFECT_NEGATIVE)){	addEffect(createButton(EFFECT_NEGATIVE));	count++;}
					if(e.equals(Parameters.EFFECT_NONE)){		addEffect(createButton(EFFECT_NONE));		count++;}
					if(e.equals(Parameters.EFFECT_POSTERIZE)){ 	addEffect(createButton(EFFECT_POSTERIZE));	count++;}
					if(e.equals(Parameters.EFFECT_SEPIA)) {		addEffect(createButton(EFFECT_SEPIA));		count++;}
					if(e.equals(Parameters.EFFECT_SOLARIZE)){ 	addEffect(createButton(EFFECT_SOLARIZE));	count++;}
					if(e.equals(Parameters.EFFECT_WHITEBOARD)){ addEffect(createButton(EFFECT_WHITEBOARD));	count++;}
				}
				if(count == 0)
					findViewById(R.id.Camera_Effect_Control).setVisibility(View.GONE);
				else
					findViewById(R.id.Camera_Effect_Control).setVisibility(View.VISIBLE);
			}else
				findViewById(R.id.Camera_Effect_Control).setVisibility(View.GONE);
			List<String> flashes = camera.getParameters().getSupportedFlashModes();
			Log.e("flashes", "f:"+flashes);
			if(flashes!=null){
				int count = 0;
				for(String e : flashes){
					if(e.equals(Parameters.FLASH_MODE_AUTO)){ 		addFlash(createButton(FLASH_MODE_AUTO));	count++;}
					if(e.equals(Parameters.FLASH_MODE_OFF)){ 		addFlash(createButton(FLASH_MODE_OFF));		count++;}
					if(e.equals(Parameters.FLASH_MODE_ON)){			addFlash(createButton(FLASH_MODE_ON));		count++;}
					if(e.equals(Parameters.FLASH_MODE_RED_EYE)){	addFlash(createButton(FLASH_MODE_RED_EYE));	count++;}
					if(e.equals(Parameters.FLASH_MODE_TORCH)){ 		addFlash(createButton(FLASH_MODE_TORCH));	count++;}
				}
				if(count == 1)
					findViewById(R.id.Camera_Flash_Control).setVisibility(View.GONE);
				else
					findViewById(R.id.Camera_Flash_Control).setVisibility(View.VISIBLE);

			}else
				findViewById(R.id.Camera_Flash_Control).setVisibility(View.GONE);

			timerDrawables = new Drawable[4];
			timerDrawables[0] = getResources().getDrawable(R.drawable.action_camera_timmer_0);
			timerDrawables[1] = getResources().getDrawable(R.drawable.action_camera_timmer_1);
			timerDrawables[2] = getResources().getDrawable(R.drawable.action_camera_timmer_2);
			timerDrawables[3] = getResources().getDrawable(R.drawable.action_camera_timmer_3);
		}
	}

	private void addScene(View view){
		((LinearLayout)findViewById(R.id.Camera_Scene_Settings)).addView(view);
	}
	private void addEffect(View view){
		((LinearLayout)findViewById(R.id.Camera_Effects_Settings)).addView(view);
	}
	private void addFlash(View view){
		((LinearLayout)findViewById(R.id.Camera_Flash_Settings)).addView(view);
	}
	private ImageButton createButton(int ID){
		ImageButton b = new ImageButton(this);
		b.setId(ID);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
		b.setLayoutParams(param);
		b.setBackgroundResource(R.color.transparent);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Camera.Parameters params = camera.getParameters();
				ImageButton SceneControl = (ImageButton) findViewById(R.id.Camera_Scene_Control);
				ImageButton EffectControl = (ImageButton) findViewById(R.id.Camera_Effect_Control);
				ImageButton FlashControl = (ImageButton) findViewById(R.id.Camera_Flash_Control);
				switch (view.getId()) {
				case SCENE_MODE_AUTO: 			params.setSceneMode(Parameters.SCENE_MODE_AUTO); 			SceneControl.setImageResource(R.drawable.camera_scenes_auto); 			break;
				case SCENE_MODE_ACTION: 		params.setSceneMode(Parameters.SCENE_MODE_ACTION); 			SceneControl.setImageResource(R.drawable.camera_scenes_action); 		break;
				case SCENE_MODE_BEACH: 			params.setSceneMode(Parameters.SCENE_MODE_BEACH); 			SceneControl.setImageResource(R.drawable.camera_scenes_beach); 			break;
				case SCENE_MODE_CANDLELIGHT: 	params.setSceneMode(Parameters.SCENE_MODE_CANDLELIGHT); 	SceneControl.setImageResource(R.drawable.camera_scenes_candlelight); 	break;
				case SCENE_MODE_FIREWORKS: 		params.setSceneMode(Parameters.SCENE_MODE_FIREWORKS); 		SceneControl.setImageResource(R.drawable.camera_scenes_fireworks); 		break;
				case SCENE_MODE_HDR: 			params.setSceneMode(Parameters.SCENE_MODE_HDR); 			SceneControl.setImageResource(R.drawable.camera_scenes_hdr); 			break;
				case SCENE_MODE_LANDSCAPE: 		params.setSceneMode(Parameters.SCENE_MODE_LANDSCAPE); 		SceneControl.setImageResource(R.drawable.camera_scenes_landscape); 		break;
				case SCENE_MODE_NIGHT: 			params.setSceneMode(Parameters.SCENE_MODE_NIGHT); 			SceneControl.setImageResource(R.drawable.camera_scenes_night); 			break;
				case SCENE_MODE_NIGHT_PORTRAIT: params.setSceneMode(Parameters.SCENE_MODE_NIGHT_PORTRAIT); 	SceneControl.setImageResource(R.drawable.camera_scenes_night_portrait); break;
				case SCENE_MODE_PARTY: 			params.setSceneMode(Parameters.SCENE_MODE_PARTY); 			SceneControl.setImageResource(R.drawable.camera_scenes_party); 			break;
				case SCENE_MODE_PORTRAIT: 		params.setSceneMode(Parameters.SCENE_MODE_PORTRAIT); 		SceneControl.setImageResource(R.drawable.camera_scenes_portrait); 		break;
				case SCENE_MODE_SNOW: 			params.setSceneMode(Parameters.SCENE_MODE_SNOW); 			SceneControl.setImageResource(R.drawable.camera_scenes_snow); 			break;
				case SCENE_MODE_SPORTS: 		params.setSceneMode(Parameters.SCENE_MODE_SPORTS);			SceneControl.setImageResource(R.drawable.camera_scenes_sport); 			break;
				case SCENE_MODE_STEADYPHOTO: 	params.setSceneMode(Parameters.SCENE_MODE_STEADYPHOTO);		SceneControl.setImageResource(R.drawable.camera_scenes_steady); 		break;
				case SCENE_MODE_SUNSET: 		params.setSceneMode(Parameters.SCENE_MODE_SUNSET); 			SceneControl.setImageResource(R.drawable.camera_scenes_sunset); 		break;
				case SCENE_MODE_THEATRE: 		params.setSceneMode(Parameters.SCENE_MODE_THEATRE); 		SceneControl.setImageResource(R.drawable.camera_scenes_theater); 		break;

				case EFFECT_AQUA: 		params.setColorEffect(Parameters.EFFECT_AQUA); 				EffectControl.setImageResource(R.drawable.camera_effect_aqua); 			break;
				case EFFECT_BLACKBOARD: params.setColorEffect(Parameters.EFFECT_BLACKBOARD); 		EffectControl.setImageResource(R.drawable.camera_effect_blackboard); 	break;
				case EFFECT_MONO: 		params.setColorEffect(Parameters.EFFECT_MONO); 				EffectControl.setImageResource(R.drawable.camera_effect_mono); 			break;
				case EFFECT_NEGATIVE: 	params.setColorEffect(Parameters.EFFECT_NEGATIVE); 			EffectControl.setImageResource(R.drawable.camera_effect_negative); 		break;
				case EFFECT_NONE: 		params.setColorEffect(Parameters.EFFECT_NONE); 				EffectControl.setImageResource(R.drawable.camera_effect_none); 			break;
				case EFFECT_POSTERIZE: 	params.setColorEffect(Parameters.EFFECT_POSTERIZE); 		EffectControl.setImageResource(R.drawable.camera_effect_posterize); 	break;
				case EFFECT_SEPIA: 		params.setColorEffect(Parameters.EFFECT_SEPIA); 			EffectControl.setImageResource(R.drawable.camera_effect_sepia); 		break;
				case EFFECT_SOLARIZE: 	params.setColorEffect(Parameters.EFFECT_SOLARIZE); 			EffectControl.setImageResource(R.drawable.camera_effect_solarize); 		break;
				case EFFECT_WHITEBOARD: params.setColorEffect(Parameters.EFFECT_WHITEBOARD); 		EffectControl.setImageResource(R.drawable.camera_effect_whiteboard); 	break;

				case FLASH_MODE_AUTO: 		params.setFlashMode(Parameters.FLASH_MODE_AUTO); 			FlashControl.setImageResource(R.drawable.action_camera_flash_auto); 	break;
				case FLASH_MODE_OFF: 		params.setFlashMode(Parameters.FLASH_MODE_OFF);			FlashControl.setImageResource(R.drawable.action_camera_flash_off); 		break;
				case FLASH_MODE_ON: 		params.setFlashMode(Parameters.FLASH_MODE_ON); 			FlashControl.setImageResource(R.drawable.action_camera_flash_on); 		break;
				case FLASH_MODE_RED_EYE: 	params.setFlashMode(Parameters.FLASH_MODE_RED_EYE); 		FlashControl.setImageResource(R.drawable.action_camera_flash_redeye); 	break;
				case FLASH_MODE_TORCH: 		params.setFlashMode(Parameters.FLASH_MODE_TORCH); 		FlashControl.setImageResource(R.drawable.action_camera_flash_torch); 	break;
				}
				camera.setParameters(params);
			}
		});
		switch (ID) {
		case SCENE_MODE_AUTO: 			b.setImageResource(R.drawable.camera_scenes_auto); break;
		case SCENE_MODE_ACTION: 		b.setImageResource(R.drawable.camera_scenes_action); break;
		case SCENE_MODE_BEACH: 			b.setImageResource(R.drawable.camera_scenes_beach); break;
		case SCENE_MODE_CANDLELIGHT:	b.setImageResource(R.drawable.camera_scenes_candlelight); break;
		case SCENE_MODE_FIREWORKS:		b.setImageResource(R.drawable.camera_scenes_fireworks); break;
		case SCENE_MODE_HDR: 			b.setImageResource(R.drawable.camera_scenes_hdr); break;
		case SCENE_MODE_LANDSCAPE: 		b.setImageResource(R.drawable.camera_scenes_landscape); break;
		case SCENE_MODE_NIGHT: 			b.setImageResource(R.drawable.camera_scenes_night); break;
		case SCENE_MODE_NIGHT_PORTRAIT: b.setImageResource(R.drawable.camera_scenes_night_portrait); break;
		case SCENE_MODE_PARTY: 			b.setImageResource(R.drawable.camera_scenes_party); break;
		case SCENE_MODE_PORTRAIT: 		b.setImageResource(R.drawable.camera_scenes_portrait); break;
		case SCENE_MODE_SNOW: 			b.setImageResource(R.drawable.camera_scenes_snow); break;
		case SCENE_MODE_SPORTS: 		b.setImageResource(R.drawable.camera_scenes_sport); break;
		case SCENE_MODE_STEADYPHOTO: 	b.setImageResource(R.drawable.camera_scenes_steady); break;
		case SCENE_MODE_SUNSET: 		b.setImageResource(R.drawable.camera_scenes_sunset); break;
		case SCENE_MODE_THEATRE: 		b.setImageResource(R.drawable.camera_scenes_theater); break;
		case EFFECT_AQUA: 			b.setImageResource(R.drawable.camera_effect_aqua); break;
		case EFFECT_BLACKBOARD: 	b.setImageResource(R.drawable.camera_effect_blackboard); break;
		case EFFECT_MONO: 			b.setImageResource(R.drawable.camera_effect_mono); break;
		case EFFECT_NEGATIVE: 		b.setImageResource(R.drawable.camera_effect_negative); break;
		case EFFECT_NONE: 			b.setImageResource(R.drawable.camera_effect_none); break;
		case EFFECT_POSTERIZE: 		b.setImageResource(R.drawable.camera_effect_posterize); break;
		case EFFECT_SEPIA: 			b.setImageResource(R.drawable.camera_effect_sepia); break;
		case EFFECT_SOLARIZE: 		b.setImageResource(R.drawable.camera_effect_solarize); break;
		case EFFECT_WHITEBOARD: 	b.setImageResource(R.drawable.camera_effect_whiteboard); break;
		case FLASH_MODE_AUTO: 			b.setImageResource(R.drawable.action_camera_flash_auto); break;
		case FLASH_MODE_OFF: 			b.setImageResource(R.drawable.action_camera_flash_off); break;
		case FLASH_MODE_ON: 			b.setImageResource(R.drawable.action_camera_flash_on); break;
		case FLASH_MODE_RED_EYE: 		b.setImageResource(R.drawable.action_camera_flash_redeye); break;
		case FLASH_MODE_TORCH: 			b.setImageResource(R.drawable.action_camera_flash_torch); break;
		}
		return b;
	}
	private void initPreview() {
		if (camera != null && previewHolder.getSurface() != null) {
			try {
				camera.setPreviewDisplay(previewHolder);
			}
			catch (Throwable t) {
				Log.e("PreviewDemo-surfaceCallback",
						"Exception in setPreviewDisplay()", t);
				Toast.makeText(CapturePhoto.this, t.getMessage(), Toast.LENGTH_LONG).show();
			}

			if (!cameraConfigured) {
				Camera.Parameters parameters=camera.getParameters();
				Camera.Size size=getBestPreviewSize(parameters);
				Camera.Size pictureSize=getSmallestPictureSize(parameters);

				if (size != null && pictureSize != null) {
					parameters.setPreviewSize(size.width, size.height);
					parameters.setPictureSize(pictureSize.width, pictureSize.height);
					parameters.setPictureFormat(ImageFormat.JPEG);
					camera.setParameters(parameters);
					cameraConfigured=true;
				}
			}
		}
	}

	private void startPreview() {
		if (cameraConfigured && camera != null) {
			camera.startPreview();
			inPreview=true;
		}
	}

	private Drawable getDrawableId(String setting){
		if(setting.equals(Parameters.SCENE_MODE_AUTO)) return getResources().getDrawable(R.drawable.camera_scenes_auto);
		if(setting.equals(Parameters.SCENE_MODE_ACTION)) return getResources().getDrawable(R.drawable.camera_scenes_action);
		if(setting.equals(Parameters.SCENE_MODE_BEACH)) return getResources().getDrawable(R.drawable.camera_scenes_beach);
		if(setting.equals(Parameters.SCENE_MODE_CANDLELIGHT)) return getResources().getDrawable(R.drawable.camera_scenes_candlelight);
		if(setting.equals(Parameters.SCENE_MODE_FIREWORKS)) return getResources().getDrawable(R.drawable.camera_scenes_fireworks);
		if(setting.equals(Parameters.SCENE_MODE_HDR)) return getResources().getDrawable(R.drawable.camera_scenes_hdr);
		if(setting.equals(Parameters.SCENE_MODE_LANDSCAPE)) return getResources().getDrawable(R.drawable.camera_scenes_landscape);
		if(setting.equals(Parameters.SCENE_MODE_NIGHT)) return getResources().getDrawable(R.drawable.camera_scenes_night);
		if(setting.equals(Parameters.SCENE_MODE_NIGHT_PORTRAIT)) return getResources().getDrawable(R.drawable.camera_scenes_night_portrait);
		if(setting.equals(Parameters.SCENE_MODE_PARTY)) return getResources().getDrawable(R.drawable.camera_scenes_party);
		if(setting.equals(Parameters.SCENE_MODE_PORTRAIT)) return getResources().getDrawable(R.drawable.camera_scenes_portrait);
		if(setting.equals(Parameters.SCENE_MODE_SNOW)) return getResources().getDrawable(R.drawable.camera_scenes_snow);
		if(setting.equals(Parameters.SCENE_MODE_SPORTS)) return getResources().getDrawable(R.drawable.camera_scenes_sport);
		if(setting.equals(Parameters.SCENE_MODE_STEADYPHOTO)) return getResources().getDrawable(R.drawable.camera_scenes_steady);
		if(setting.equals(Parameters.SCENE_MODE_SUNSET)) return getResources().getDrawable(R.drawable.camera_scenes_sunset);
		if(setting.equals(Parameters.SCENE_MODE_THEATRE)) return getResources().getDrawable(R.drawable.camera_scenes_theater);
		return null;
	}

	SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
		public void surfaceCreated(SurfaceHolder holder) {}

		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			initPreview();
			startPreview();
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			// no-op
		}
	};

	Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			try{
				//Display
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 2;
				findViewById(R.id.Camera_Confirm).setVisibility(View.INVISIBLE);
				Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length, opts);
				findViewById(R.id.Camera_Confirm).setVisibility(View.VISIBLE);
				((ImageView)findViewById(R.id.Camera_Confirm_Image)).setImageBitmap(b);
				//end Display

				//Save
				String timeStamp = DateTime.now().toString("yyyy_MM_dd_HH_mm_ss");//TODO
				File rootsd = Environment.getExternalStorageDirectory();
				File dcim = new File(rootsd.getAbsolutePath() + "/DCIM");
				File App = new File(dcim.getAbsoluteFile() + "/Captifeye");
				File photo = new File(App, timeStamp+".jpg");


				try{
					Log.e("Path", photo.getPath());
					photo.mkdirs();
					if (photo.exists()) {
						photo.delete();
					}
					FileOutputStream fos = new FileOutputStream(photo.getPath());

					fos.write(data);
					fos.close();
				}catch(Exception e){
					try {
						photo = new File(photo.getAbsolutePath().replace("sdcard0", "sdcard1"));

						Log.e("new Path", photo.getPath());
						photo.mkdirs();
						if (photo.exists()) {
							photo.delete();
						}
						FileOutputStream fos = new FileOutputStream(photo.getPath());

						fos.write(data);
						fos.close();
					}
					catch (Exception ex) {

						Log.e("Error saving Picture", ex.toString());
						Toast.makeText(CapturePhoto.this, "Error Saving Picture", Toast.LENGTH_SHORT).show();
					}
				}
				findViewById(R.id.Camera_Confirm_Choices).setVisibility(View.VISIBLE);
				final File photoFile = photo;
				findViewById(R.id.Camera_Confirm_Yes).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent returnIntent = new Intent();
						 returnIntent.putExtra("resultURI",photoFile.toURI().toString());
						 setResult(RESULT_OK,returnIntent);
						Log.e("End", "end");
						finish();
						return;
					}
				});
				findViewById(R.id.Camera_Confirm_No).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						photoFile.delete();
						findViewById(R.id.Camera_Confirm).setVisibility(View.GONE);
						findViewById(R.id.Camera_Confirm_Choices).setVisibility(View.GONE);
					}
				});
				camera.startPreview();
				inPreview=true;
			}catch(Exception e){
				Log.e("PictureCallback", e.toString());
			}
		}
	};


	public final static int SCENE_MODE_AUTO = 1;
	public final static int SCENE_MODE_ACTION = 2;
	public final static int SCENE_MODE_BEACH = 3;
	public final static int SCENE_MODE_CANDLELIGHT = 4;
	public final static int SCENE_MODE_FIREWORKS = 5;
	public final static int SCENE_MODE_HDR = 6;
	public final static int SCENE_MODE_LANDSCAPE = 7;
	public final static int SCENE_MODE_NIGHT = 8;
	public final static int SCENE_MODE_NIGHT_PORTRAIT = 9;
	public final static int SCENE_MODE_PARTY = 10;
	public final static int SCENE_MODE_PORTRAIT = 11;
	public final static int SCENE_MODE_SNOW = 12;
	public final static int SCENE_MODE_SPORTS = 13;
	public final static int SCENE_MODE_STEADYPHOTO = 14;
	public final static int SCENE_MODE_SUNSET = 15;
	public final static int SCENE_MODE_THEATRE = 16;
	/////////////////////////////////////////////////
	private final static int EFFECT_AQUA = 17;
	private final static int EFFECT_BLACKBOARD = 18;
	private final static int EFFECT_MONO = 19;
	private final static int EFFECT_NEGATIVE = 20;
	private final static int EFFECT_NONE = 21;
	private final static int EFFECT_POSTERIZE = 22;
	private final static int EFFECT_SEPIA = 23;
	private final static int EFFECT_SOLARIZE = 24;
	private final static int EFFECT_WHITEBOARD = 25;
	/////////////////////////////////////////////////
	private final static int FLASH_MODE_AUTO = 26;
	private final static int FLASH_MODE_OFF = 27;
	private final static int FLASH_MODE_ON = 28;
	private final static int FLASH_MODE_RED_EYE = 29;
	private final static int FLASH_MODE_TORCH = 30;
	/////////////////////////////////////////////////
	protected void onDestroy() {
//		NewPost.instance.onPostCompleteThread(null);
		super.onDestroy();
	}
}
