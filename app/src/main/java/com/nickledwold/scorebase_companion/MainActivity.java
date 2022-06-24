package com.nickledwold.scorebase_companion;
import androidx.appcompat.app.AppCompatActivity;
import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.hubs.SubscriptionHandler2;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView deductionOneTextView;
    private TextView nameTextView;
    private TextView clubTextView;
    private TextView categoryTextView;
    private TextView otherInfoTextView;
    private TextView deductionTwoTextView;
    private TextView deductionThreeTextView;
    private TextView deductionFourTextView;
    private TextView deductionFiveTextView;
    private TextView deductionSixTextView;
    private TextView deductionSevenTextView;
    private TextView deductionEightTextView;
    private TextView deductionNineTextView;
    private TextView deductionTenTextView;
    private TextView deductionStabilityTextView;
    private TextView panelAndRoleTextView;
    private TextView judgeNameTextView;
    private Button submitButton;
    private TextView scoreText;
    private TextView scoreTextText;
    private String panelNumber;
    private String roleType;
    private String discipline;
    private String ipAddressAssignMode;
    private SharedPreferences SP;
    private static final String TAG = "MainActivity";
    private int[] deductionsArray;
    private String interfaceType = "TRADeduction";
    private final Context mContext = this;
    private SignalRService mService;
    private boolean mBound = false;
    private Handler mHandler; // to display Toast message
    private int elements;
    private boolean fullExercise;
    private long lastClickTime;
    private long DOUBLE_CLICK_TIME_DELTA = 300;
    private Toast toast;
    private boolean inputAllowed = false;
    private Handler connectionHandler;


    private long startTime = 5 * 60 * 1000; //5 minutes
    //private long startTime = 1 * 20 * 1000; //20 seconds
    private final long interval = 10 * 6 * 1000; //1 minute
    private CountDownTimer countDownTimer;
    private boolean showWallpaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        final View decorView = getWindow().getDecorView();

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(flags);
        decorView
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        updateSettings();

        final Intent intent = new Intent();
        intent.setClass(mContext, SignalRService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


        connectionHandler = new Handler(Looper.getMainLooper());
        connectionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mService != null) {
                    if (mService.mHubConnection.getState() == ConnectionState.Connected) {
                        System.out.println("Connected");
                    } else {
                        System.out.println("Disconnected");
                        if (mBound) {
                            unbindService(mConnection);
                            mBound = false;
                        }
                        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
                    }
                }
                connectionHandler.postDelayed(this,5000);
                return;
            }
        }, 1000);

        ToggleInput(false);
        countDownTimer = new MyCountDownTimer(startTime,interval);
    }

    private void ClearScoreAndScoreText() {
        scoreText.setText("");
        scoreText.requestLayout();

        scoreTextText.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(showWallpaper) {
            updateSettings();
            showWallpaper = false;
        }
        countDownTimer.cancel();
        countDownTimer.start();
        if(toast != null)
            toast.cancel();
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to SignalRService, cast the IBinder and get SignalRService instance
            SignalRService.LocalBinder binder = (SignalRService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            String CLIENT_METHOD_BROADAST_MESSAGE = "AddMessage";
            mService.mHubProxy.on(CLIENT_METHOD_BROADAST_MESSAGE,
                    new SubscriptionHandler2<String, String>() {
                        @Override
                        public void run(final String name,final String msg) {
                            final String finalMsg =  msg.toString();
                            // display Toast message
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    System.out.print(finalMsg);
                                    if(finalMsg.equals("ping")){
                                        onTouchEvent(null);
                                        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
                                    }
                                    if(!(finalMsg.startsWith("CompetitorInfo:")||finalMsg.startsWith("FlightComplete")||finalMsg.startsWith("JudgeInfo:"))) return;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onTouchEvent(null);
                                            String judgeReEntry = null;
                                            String[] messageParts = finalMsg.split(";");
                                            for (String part : messageParts) {
                                                String[] subMessageParts = part.split(":");
                                                if (subMessageParts[0].equals("CompetitorInfo")) {
                                                    if(finalMsg.contains("P"+panelNumber+"|")){
                                                        String[] secondPartOfMessageArray = messageParts[1].split(":");
                                                        String panelAndRoleOfMessage = secondPartOfMessageArray[0];
                                                        if(!(panelAndRoleOfMessage.equals("P"+panelNumber+"|"+roleType) || panelAndRoleOfMessage.equals("P"+panelNumber+"|All") || (panelAndRoleOfMessage.equals("P"+panelNumber+"|E") && roleType.startsWith("E")))){
                                                            continue;
                                                        }
                                                    }
                                                    String[] competitorInfo = subMessageParts[1].split(",");
                                                    nameTextView = findViewById(R.id.nameTextView);
                                                    clubTextView = findViewById(R.id.clubTextView);
                                                    categoryTextView = findViewById(R.id.categoryTextView);
                                                    otherInfoTextView = findViewById(R.id.otherInfoTextView);
                                                    scoreTextText.setVisibility(View.VISIBLE);
                                                    nameTextView.setText(competitorInfo[0].replace("&comma",","));
                                                    clubTextView.setText(competitorInfo[1].replace("&comma",","));
                                                    categoryTextView.setText(competitorInfo[2].replace("&comma",","));
                                                    otherInfoTextView.setText(competitorInfo[3]);
                                                    ClearScores(true);
                                                    HideCompetitorSummary();
                                                    ReduceOpacityOfDeductionBoxes(interfaceType.equals("DMTDeduction") ? 2 : interfaceType.equals("TUMDeduction") ? 8 : 10);
                                                    inputAllowed = false;
                                                    ToggleInput(false);
                                                }
                                                if (subMessageParts[0].equals("ElementsConfirmed")) {
                                                    elements = Integer.parseInt(subMessageParts[1]);
                                                    fullExercise = discipline.equals("DMT") ? elements == 2 : discipline.equals("TUM") ? elements == 8 : elements == 10;
                                                    ReduceOpacityOfDeductionBoxes(elements);
                                                    inputAllowed = true;
                                                    if(elements > 0) {
                                                        ToggleInput(true);
                                                        ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Please enter your score", Toast.LENGTH_SHORT);
                                                    }else{
                                                        ToggleInput(false);
                                                        ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Zero score", Toast.LENGTH_LONG);
                                                    }
                                                }
                                                if (subMessageParts.length > 1 && subMessageParts[1].equals("ReEnter")){
                                                    judgeReEntry = subMessageParts[0];
                                                }
                                                if (subMessageParts.length > 1 && subMessageParts[1].equals("ReEnter") && (subMessageParts[0].equals("P"+panelNumber+"|"+roleType) || subMessageParts[0].equals("P"+panelNumber+"|All") || (subMessageParts[0].equals("P"+panelNumber+"|E") && roleType.startsWith("E")))){
                                                    ClearScores(true);
                                                    inputAllowed = true;
                                                    ToggleInput(true);
                                                    ShowCustomToast(R.layout.custom_toast_amber, (ViewGroup) findViewById(R.id.custom_toast_layout_amber), "Please re-enter", Toast.LENGTH_LONG);
                                                }
                                                if (subMessageParts[0].equals("Elements")) {
                                                    if(judgeReEntry != null && !judgeReEntry.equals("P"+panelNumber+"|"+roleType)) {
                                                        //do nothing as it is a judge re-entry but not for this judge
                                                    }else {
                                                        elements = Integer.parseInt(subMessageParts[1]);
                                                        fullExercise = discipline.equals("DMT") ? elements == 2 : discipline.equals("TUM") ? elements == 8 : elements == 10;
                                                        ReduceOpacityOfDeductionBoxes(elements);
                                                        inputAllowed = true;
                                                        ToggleInput(true);
                                                    }
                                                }
                                                if (subMessageParts[0].equals("FlightComplete")) {
                                                    ClearScores(false);
                                                    //ClearCompetitorInfo();
                                                    inputAllowed = false;
                                                    ToggleInput(false);
                                                    ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Flight complete", Toast.LENGTH_LONG);
                                                }
                                                if (subMessageParts[0].equals("CompetitorSummary")) {
                                                    scoreText.setText("");
                                                    inputAllowed = false;
                                                    ToggleInput(false);
                                                    ShowCompetitorSummary(subMessageParts[1]);
                                                }
                                                if (subMessageParts[0].equals("ConfirmElements") && roleType.equals("CJP")) {
                                                    PopUpClass popUpClass = new PopUpClass();
                                                    popUpClass.showPopupWindow((ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0));
                                                }
                                                if (subMessageParts[0].equals("JudgeInfo")) {
                                                    String[] judges = subMessageParts[1].split("\\|");
                                                    for (String judge : judges) {
                                                        if(judge.startsWith(roleType)){
                                                            String[] judgeRoleAndName = judge.split(",");
                                                            judgeNameTextView = findViewById(R.id.judgeNameTextView);
                                                            judgeNameTextView.setText(judgeRoleAndName[1]);
                                                            SharedPreferences.Editor editor = SP.edit();
                                                            editor.putString("judgeName", judgeRoleAndName[1]);
                                                            editor.commit();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                    , String.class,String.class);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void HideCompetitorSummary() {
        if(roleType.equals("CJP")) {
            scoreTextText.setText("PENALTY");
        } else {
            scoreTextText.setText("SCORE");
        }
    }

    private void ShowCompetitorSummary(String scores) {
        String[] scoreParts = scores.split(",");
        scoreText.setText(ReplaceEmptyScore(scoreParts[0],2));
        scoreText.setTextColor(Color.WHITE);
        scoreTextText.setText("TOTAL EXECUTION SCORE");
    }

    private String ReplaceEmptyScore(String score, int decimals){
        if(score.isEmpty() || score == null || score.trim().isEmpty()){
            return decimals == 2 ? "0.00" : decimals == 1 ? "0.0" : "";
        }
        return score;
    }

    private void ClearCompetitorInfo() {
        nameTextView = findViewById(R.id.nameTextView);
        clubTextView = findViewById(R.id.clubTextView);
        otherInfoTextView = findViewById(R.id.otherInfoTextView);
        scoreTextText.setVisibility(View.INVISIBLE);
        nameTextView.setText("");
        clubTextView.setText("");
        otherInfoTextView.setText("");
    }

    private void ClearScores(boolean clearScoreText) {
        if(!interfaceType.equals("FullScore")) {
            deductionOneTextView.setText("");
            deductionTwoTextView.setText("");
            if(interfaceType.equals("TRADeduction") || interfaceType.equals("TUMDeduction")) {
                deductionThreeTextView.setText("");
                deductionFourTextView.setText("");
                deductionFiveTextView.setText("");
                deductionSixTextView.setText("");
                deductionSevenTextView.setText("");
                deductionEightTextView.setText("");
                if(interfaceType.equals("TRADeduction")) {
                    deductionNineTextView.setText("");
                    deductionTenTextView.setText("");
                }
            }
            deductionStabilityTextView.setText("");
        }
        if(!interfaceType.equals("FullScore")) {
            deductionsArray = getDeductions();
        }
        if(clearScoreText) {
            scoreText.setText("");
        }
    }

    private void UpdateActiveDeductionBox(int[] deductionsArray) {
        if(!inputAllowed) return;
        if(interfaceType.equals("FullScore")) return;

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add((ImageView)findViewById(R.id.deuctionOnePanelImageView));
        imageViews.add((ImageView)findViewById(R.id.deuctionTwoPanelImageView));
        imageViews.add((ImageView)findViewById(R.id.deuctionStabilityPanelImageView));
        if(interfaceType.equals("TRADeduction") || interfaceType.equals("TUMDeduction")){
            imageViews.add(2,(ImageView)findViewById(R.id.deuctionThreePanelImageView));
            imageViews.add(3,(ImageView)findViewById(R.id.deuctionFourPanelImageView));
            imageViews.add(4,(ImageView)findViewById(R.id.deuctionFivePanelImageView));
            imageViews.add(5,(ImageView)findViewById(R.id.deuctionSixPanelImageView));
            imageViews.add(6,(ImageView)findViewById(R.id.deuctionSevenPanelImageView));
            imageViews.add(7,(ImageView)findViewById(R.id.deuctionEightPanelImageView));
            if(interfaceType.equals("TRADeduction")) {
                imageViews.add(8, (ImageView) findViewById(R.id.deuctionNinePanelImageView));
                imageViews.add(9, (ImageView) findViewById(R.id.deuctionTenPanelImageView));
            }
        }
        for (int i = 0; i < deductionsArray.length; i++) {
            imageViews.get(i).setImageDrawable(getDrawable(R.drawable.bluepanel));
        }
        int firstEmpty = find(deductionsArray, -1);
        if(firstEmpty == -1)return;
        imageViews.get(firstEmpty).setImageDrawable(getDrawable(R.drawable.bluepanel_lighter));
    }

    private void ReduceOpacityOfDeductionBoxes(int elementsInExercise) {
        if(interfaceType.equals("FullScore")) return;
        if(interfaceType.equals("DMTDeduction") && elementsInExercise > 1) elementsInExercise = 3;
        if(interfaceType.equals("TUMDeduction") && elementsInExercise > 7) elementsInExercise = 9;
        if(interfaceType.equals("TRADeduction") && elementsInExercise > 9) elementsInExercise = 11;

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add((ImageView)findViewById(R.id.deuctionOnePanelImageView));
        imageViews.add((ImageView)findViewById(R.id.deuctionTwoPanelImageView));
        imageViews.add((ImageView)findViewById(R.id.deuctionStabilityPanelImageView));
        List<TextView> textViews = new ArrayList<>();
        textViews.add((TextView)findViewById(R.id.deductionOneTextView));
        textViews.add((TextView)findViewById(R.id.deductionTwoTextView));
        textViews.add((TextView)findViewById(R.id.deductionStabilityTextView));

        if(interfaceType.equals("TRADeduction") || interfaceType.equals("TUMDeduction")){
            imageViews.add(2,(ImageView)findViewById(R.id.deuctionThreePanelImageView));
            imageViews.add(3,(ImageView)findViewById(R.id.deuctionFourPanelImageView));
            imageViews.add(4,(ImageView)findViewById(R.id.deuctionFivePanelImageView));
            imageViews.add(5,(ImageView)findViewById(R.id.deuctionSixPanelImageView));
            imageViews.add(6,(ImageView)findViewById(R.id.deuctionSevenPanelImageView));
            imageViews.add(7,(ImageView)findViewById(R.id.deuctionEightPanelImageView));
            textViews.add(2,(TextView)findViewById(R.id.deductionThreeTextView));
            textViews.add(3,(TextView)findViewById(R.id.deductionFourTextView));
            textViews.add(4,(TextView)findViewById(R.id.deductionFiveTextView));
            textViews.add(5,(TextView)findViewById(R.id.deductionSixTextView));
            textViews.add(6,(TextView)findViewById(R.id.deductionSevenTextView));
            textViews.add(7,(TextView)findViewById(R.id.deductionEightTextView));

            if(interfaceType.equals("TRADeduction")) {
                imageViews.add(8, (ImageView) findViewById(R.id.deuctionNinePanelImageView));
                imageViews.add(9, (ImageView) findViewById(R.id.deuctionTenPanelImageView));
                textViews.add(8, (TextView) findViewById(R.id.deductionNineTextView));
                textViews.add(9, (TextView) findViewById(R.id.deductionTenTextView));
            }
        }
        for (int i = 0; i < elementsInExercise; i++) {
            imageViews.get(i).setAlpha(1.0f);
            textViews.get(i).setAlpha(1.0f);
        }
        for (int i = elementsInExercise; i < imageViews.size(); i++) {
            imageViews.get(i).setAlpha(0.4f);
            textViews.get(i).setAlpha(0.4f);
        }
    }


    public void deleteButtonpressed(View view){
        onTouchEvent(null);
        if(!inputAllowed) return;
        if(interfaceType.equals("FullScore")) {
            int length = scoreText.getText().toString().length();
            if (length > 0) {
                scoreText.setText(scoreText.getText().toString().substring(0, length - 1));
            }
        }else {
            deductionsArray = getDeductions();
            int firstEmpty = find(deductionsArray, -1);
            if (firstEmpty == 0) return;
            if (firstEmpty == -1) firstEmpty = deductionsArray.length;
            deductionsArray[firstEmpty - 1] = -1;
            setDeductions(deductionsArray);
            UpdateScore(deductionsArray);
            UpdateActiveDeductionBox(deductionsArray);
        }
    }


    @Override
    protected void onStop() {
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        // call the superclass method first
        super.onStop();
    }
    @Override
    public void onResume() {
        super.onResume();
        updateSettings();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy()
    {

        super.onDestroy();
    }

    public void submitButtonpressed(View button){
        onTouchEvent(null);
        String buttonText = ((Button)button).getText().toString();
        if(buttonText.equals("RE-ENTER")){
            ShowCustomToast(R.layout.custom_toast_amber,(ViewGroup)findViewById(R.id.custom_toast_layout_amber),"Re-entry requested\n\nPlease wait", Toast.LENGTH_SHORT);
            mService.sendMessage("RequestReEntry");
            submitButton.setEnabled(false);
            submitButton.setTextColor(Color.GRAY);
        }
        if(!inputAllowed) return;
        String message = "";
        if (interfaceType.equals("FullScore")){
            String scoreTextValue = scoreText.getText().toString();
            if(TextUtils.isEmpty(scoreTextValue)) {
                ShowCustomToast(R.layout.custom_toast_red,(ViewGroup)findViewById(R.id.custom_toast_layout_red),"Please enter a score before submitting", Toast.LENGTH_SHORT);
                return;
            }
            message = scoreText.getText().toString();
        }else {
                int firstEmpty = find(deductionsArray, -1);
                if (firstEmpty != -1) {
                    if((!fullExercise && firstEmpty != elements) || (fullExercise && firstEmpty != elements + 1)) {
                        ShowCustomToast(R.layout.custom_toast_red,(ViewGroup)findViewById(R.id.custom_toast_layout_red),"Please enter all deductions", Toast.LENGTH_SHORT);
                        return;
                    }
                }
                if(elements == 0)return;
            for (int i = 0; i < deductionsArray.length; i++) {
                    message += deductionsArray[i];
                    if (i != deductionsArray.length - 1) message += ",";
                }
        }
        // Send the request message.
        try
        {
            System.out.print("message = "+ message);
            mService.sendMessage(message);
            inputAllowed = false;
            ShowCustomToast(R.layout.custom_toast_green,(ViewGroup)findViewById(R.id.custom_toast_layout_green),"Submitted", Toast.LENGTH_SHORT);
            ToggleInput(false);
            submitButton = findViewById(R.id.submitButton);
            submitButton.setBackground(getDrawable(R.drawable.reenter_button_background));
            submitButton.setText("RE-ENTER");
            submitButton.setEnabled(true);
            submitButton.setTextColor(Color.WHITE);
        }
        catch (Exception err)
        {
            System.out.println("Sending the message failed." +  err.getMessage());
        }
    }

    private void ToggleInput(boolean enabled) {
        int textColor = enabled ? Color.WHITE : Color.GRAY;
        int imageAlpha = enabled ? 255 : 100;
        scoreText.setTextColor(textColor);
        if(!interfaceType.equals("FullScore")) {
            deductionOneTextView.setTextColor(textColor);
            deductionTwoTextView.setTextColor(textColor);
            if (!discipline.equals("DMT")) {
                deductionThreeTextView.setTextColor(textColor);
                deductionFourTextView.setTextColor(textColor);
                deductionFiveTextView.setTextColor(textColor);
                deductionSixTextView.setTextColor(textColor);
                deductionSevenTextView.setTextColor(textColor);
                deductionEightTextView.setTextColor(textColor);
                if(!discipline.equals("TUM")) {
                    deductionNineTextView.setTextColor(textColor);
                    deductionTenTextView.setTextColor(textColor);
                }
            }
            deductionStabilityTextView.setTextColor(textColor);
        }
        ((Button)findViewById(R.id.oneButton)).setTextColor(textColor);
        ((Button)findViewById(R.id.twoButton)).setTextColor(textColor);
        ((Button)findViewById(R.id.threeButton)).setTextColor(textColor);
        ((Button)findViewById(R.id.fourButton)).setTextColor(textColor);
        ((Button)findViewById(R.id.fiveButton)).setTextColor(textColor);
        findViewById(R.id.oneButton).setEnabled(enabled);
        findViewById(R.id.twoButton).setEnabled(enabled);
        findViewById(R.id.threeButton).setEnabled(enabled);
        findViewById(R.id.fourButton).setEnabled(enabled);
        findViewById(R.id.fiveButton).setEnabled(enabled);
        if (interfaceType.equals("FullScore")) {
            ((Button)findViewById(R.id.sixButton)).setTextColor(textColor);
            ((Button)findViewById(R.id.sevenButton)).setTextColor(textColor);
            ((Button)findViewById(R.id.eightButton)).setTextColor(textColor);
            ((Button)findViewById(R.id.nineButton)).setTextColor(textColor);
            findViewById(R.id.sixButton).setEnabled(enabled);
            findViewById(R.id.sevenButton).setEnabled(enabled);
            findViewById(R.id.eightButton).setEnabled(enabled);
            findViewById(R.id.nineButton).setEnabled(enabled);
        } else {
            ((Button)findViewById(R.id.tenButton)).setTextColor(textColor);
            findViewById(R.id.tenButton).setEnabled(enabled);
        }
        ((Button)findViewById(R.id.zeroButton)).setTextColor(textColor);
        ((Button)findViewById(R.id.decimalButton)).setTextColor(textColor);
        ((ImageButton)findViewById(R.id.deleteButton)).setImageAlpha(imageAlpha);
        findViewById(R.id.zeroButton).setEnabled(enabled);
        findViewById(R.id.decimalButton).setEnabled(enabled);
        findViewById(R.id.deleteButton).setEnabled(enabled);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setBackground(getDrawable(R.drawable.submit_button_background));
        submitButton.setEnabled(enabled);
        submitButton.setTextColor(textColor);
        submitButton.setText("SUBMIT");
    }

    private void ShowCustomToast(int resource, ViewGroup viewGroup, String toastText, int toastLength) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(resource, viewGroup);
        TextView tv = (TextView)layout.findViewById(R.id.txtvw);
        tv.setTextSize(32);
        toast = new Toast(getApplicationContext());
        toast.setDuration(toastLength);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setView(layout);
        tv.setText(toastText);
        toast.show();
    }

    public void keypadPressed(View view){
        onTouchEvent(null);
        if(!inputAllowed) return;
        Button b = (Button)view;
        String buttonValue = b.getText().toString();
        if (interfaceType.equals("FullScore")){
            String scoreTextValue = scoreText.getText().toString();
            if(scoreTextValue.equals("") && buttonValue.equals(".")) return;
            if(scoreTextValue.contains(".") && buttonValue.equals("."))return;
            float value = buttonValue == "." ? Float.parseFloat(scoreTextValue) : Float.parseFloat(scoreTextValue + buttonValue);
            switch(roleType){
                case "HD" :
                case "CJP":
                    if(value > 10){
                        ShowCustomToast(R.layout.custom_toast_red,(ViewGroup)findViewById(R.id.custom_toast_layout_red),"Please enter a value between 0 - 10", Toast.LENGTH_SHORT);
                        return;
                    }
                    break;
                case "S":
                case "T":
                case "D":
                    if(value > 20) {
                        ShowCustomToast(R.layout.custom_toast_red,(ViewGroup)findViewById(R.id.custom_toast_layout_red),"Please enter a value between 0 - 20", Toast.LENGTH_SHORT);
                        return;
                    }
                    break;
            }
            String valueAfterDecimal = scoreTextValue.contains(".") ? scoreTextValue.substring(scoreTextValue.lastIndexOf('.') + 1) : null;
            switch(roleType){
                case "CJP":
                case "D":
                    if(valueAfterDecimal != null && valueAfterDecimal.length() > 0) return;
                    break;
                case "HD" :
                    if(valueAfterDecimal != null && valueAfterDecimal.length() > 1) return;
                    break;
                case "S":
                case "T":
                    if(valueAfterDecimal != null && valueAfterDecimal.length() > 2) return;
                    break;
            }
            scoreText.setText(scoreTextValue + buttonValue);
        }
        else {
            deductionsArray = getDeductions();
            int deductionsArrayCount = deductionsArray.length;
            int firstEmpty = find(deductionsArray, -1);
            if (firstEmpty == -1) return;
            if (firstEmpty != deductionsArrayCount - 1 && buttonValue.equals("10")) return;
            if (firstEmpty == deductionsArrayCount - 1 && buttonValue.equals("4")) return;
            if(!fullExercise && firstEmpty == elements) return;
            deductionsArray[firstEmpty] = tryParse(buttonValue);
            setDeductions(deductionsArray);
            UpdateScore(deductionsArray);
            UpdateActiveDeductionBox(deductionsArray);
        }
    }

    public void deductionTextViewPressed(View view){
        if(!inputAllowed) return;
        TextView tv = (TextView)view;
        tv.setText("");
        UpdateActiveDeductionBox(getDeductions());
        UpdateScore(getDeductions());
    }

    public void scoreBasePressed(View view){
        onTouchEvent(null);
        long clickTime = System.currentTimeMillis();
        if((clickTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA){
            Intent intent = new Intent(this, SettingsPreferenceActivity.class);
            startActivity(intent);
        }
        lastClickTime = clickTime;
    }

    private void UpdateScore(int[] deductionsArray) {
        int firstEmpty = find(deductionsArray, -1);
        if (firstEmpty != -1) {
            if((!fullExercise && firstEmpty != elements) || (fullExercise && firstEmpty != elements + 1)) {
                scoreText.setText("");
                return;
            }
        }

        float maxMark = 10.0f;
        //elements = 10;
        switch (interfaceType) {
            case "DMTDeduction":
                maxMark = elements == 0 ? 0.0f : elements + 8.0f;
                break;
            case "TUMDeduction":
                maxMark = elements == 0 ? 0.0f : elements + 2.0f;
                break;
            case "TRADeduction":
                maxMark = 1.0f * elements;
                break;
        }
        if(elements == 0) maxMark = 0.0f;
        float deduction = CalculateDeduction(deductionsArray);
        float total = maxMark - deduction;
        scoreText.setText(String.valueOf(total));
    }

    private int[] getDeductions() {
        if (discipline.equals("DMT")) {
            deductionsArray = new int[]{
                    tryParse(deductionOneTextView.getText().toString()),
                    tryParse(deductionTwoTextView.getText().toString()),
                    tryParse(deductionStabilityTextView.getText().toString())
            };
        }else if (discipline.equals("TUM")) {
            deductionsArray = new int[]{
                    tryParse(deductionOneTextView.getText().toString()),
                    tryParse(deductionTwoTextView.getText().toString()),
                    tryParse(deductionThreeTextView.getText().toString()),
                    tryParse(deductionFourTextView.getText().toString()),
                    tryParse(deductionFiveTextView.getText().toString()),
                    tryParse(deductionSixTextView.getText().toString()),
                    tryParse(deductionSevenTextView.getText().toString()),
                    tryParse(deductionEightTextView.getText().toString()),
                    tryParse(deductionStabilityTextView.getText().toString())
            };
        }else {
            deductionsArray = new int[]{
                    tryParse(deductionOneTextView.getText().toString()),
                    tryParse(deductionTwoTextView.getText().toString()),
                    tryParse(deductionThreeTextView.getText().toString()),
                    tryParse(deductionFourTextView.getText().toString()),
                    tryParse(deductionFiveTextView.getText().toString()),
                    tryParse(deductionSixTextView.getText().toString()),
                    tryParse(deductionSevenTextView.getText().toString()),
                    tryParse(deductionEightTextView.getText().toString()),
                    tryParse(deductionNineTextView.getText().toString()),
                    tryParse(deductionTenTextView.getText().toString()),
                    tryParse(deductionStabilityTextView.getText().toString())
            };
        }
        return deductionsArray;
    }

    private void setDeductions(int[] deductionsArray) {
        if(deductionOneTextView.getAlpha() == 1.0)deductionOneTextView.setText(deductionsArray[0] == -1 ? "" : String.valueOf(deductionsArray[0]));
        if(deductionTwoTextView.getAlpha() == 1.0)deductionTwoTextView.setText(deductionsArray[1] == -1 ? "" : String.valueOf(deductionsArray[1]));
        if (!discipline.equals("DMT")) {
            if(deductionThreeTextView.getAlpha() == 1.0) deductionThreeTextView.setText(deductionsArray[2] == -1 ? "" : String.valueOf(deductionsArray[2]));
            if(deductionFourTextView.getAlpha() == 1.0) deductionFourTextView.setText(deductionsArray[3] == -1 ? "" : String.valueOf(deductionsArray[3]));
            if(deductionFiveTextView.getAlpha() == 1.0) deductionFiveTextView.setText(deductionsArray[4] == -1 ? "" : String.valueOf(deductionsArray[4]));
            if(deductionSixTextView.getAlpha() == 1.0) deductionSixTextView.setText(deductionsArray[5] == -1 ? "" : String.valueOf(deductionsArray[5]));
            if(deductionSevenTextView.getAlpha() == 1.0) deductionSevenTextView.setText(deductionsArray[6] == -1 ? "" : String.valueOf(deductionsArray[6]));
            if(deductionEightTextView.getAlpha() == 1.0) deductionEightTextView.setText(deductionsArray[7] == -1 ? "" : String.valueOf(deductionsArray[7]));
            if (!discipline.equals("TUM")) {
                if (deductionNineTextView.getAlpha() == 1.0) deductionNineTextView.setText(deductionsArray[8] == -1 ? "" : String.valueOf(deductionsArray[8]));
                if (deductionTenTextView.getAlpha() == 1.0) deductionTenTextView.setText(deductionsArray[9] == -1 ? "" : String.valueOf(deductionsArray[9]));
            }
        }
        int deductionsArrayCount = deductionsArray.length;
        if(deductionStabilityTextView.getAlpha() == 1.0) deductionStabilityTextView.setText(deductionsArray[deductionsArrayCount-1] == -1 ? "" : String.valueOf(deductionsArray[deductionsArrayCount-1]));
    }

    private float CalculateDeduction(int[] deductionsArray){
        int total = 0;
        for (int i : deductionsArray) {
            {
                if (i != -1) {
                    total += i;
                }
            }
        }
        return total/10.0f;
    }

    public int tryParse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    // Function to find the index of an element in a primitive array in Java
    public static int find(int[] a, int target)
    {
        if(a == null) return 0;
        for (int i = 0; i < a.length; i++)
            if (a[i] == target)
                return i;

        return -1;
    }

    private void updateSettings(){
        panelNumber = SP.getString("panelNumber","0");
        roleType = SP.getString("roleType","1");
        discipline = SP.getString("discipline","TRA");
        ipAddressAssignMode = SP.getString("ipAddressAssignMode","Automatic");
        if(mService != null) {
            mService.mHubProxy.invoke("SetUserName", "P" + panelNumber + "|" + roleType);
        }

        if(ipAddressAssignMode.equals("Automatic")) {
            SharedPreferences.Editor editor = SP.edit();
            switch (panelNumber) {
                case "1":
                    editor.putString("ipAddress", "10.0.0.11");
                    break;
                case "2":
                    editor.putString("ipAddress", "10.0.0.12");
                    break;
                case "3":
                    editor.putString("ipAddress", "10.0.0.13");
                    break;
                case "4":
                    editor.putString("ipAddress", "10.0.0.14");
                    break;
                case "5":
                    editor.putString("ipAddress", "10.0.0.15");
                    break;
                default:
                    editor.putString("ipAddress", "10.0.0.11");
                    break;
            }
            editor.commit();
        }

        if(roleType.equals("HD") || roleType.equals("T") || roleType.equals("D") || roleType.equals("S") || roleType.equals("CJP")) {
            interfaceType = "FullScore";
            setContentView(R.layout.activity_main_full_score);
        }else if (discipline.equals("DMT")) {
            interfaceType = "DMTDeduction";
            setContentView(R.layout.activity_main_dmt);
        }else if (discipline.equals("TUM")) {
            interfaceType = "TUMDeduction";
            setContentView(R.layout.activity_main_tum);
        }else{
            interfaceType = "TRADeduction";
            setContentView(R.layout.activity_main_tra);
        }
        deductionOneTextView = findViewById(R.id.deductionOneTextView);
        deductionTwoTextView = findViewById(R.id.deductionTwoTextView);
        deductionThreeTextView = findViewById(R.id.deductionThreeTextView);
        deductionFourTextView = findViewById(R.id.deductionFourTextView);
        deductionFiveTextView = findViewById(R.id.deductionFiveTextView);
        deductionSixTextView = findViewById(R.id.deductionSixTextView);
        deductionSevenTextView = findViewById(R.id.deductionSevenTextView);
        deductionEightTextView = findViewById(R.id.deductionEightTextView);
        deductionNineTextView = findViewById(R.id.deductionNineTextView);
        deductionTenTextView = findViewById(R.id.deductionTenTextView);
        deductionStabilityTextView = findViewById(R.id.deductionStabilityTextView);
        panelAndRoleTextView = findViewById(R.id.panelAndRoleTextView);
        judgeNameTextView = findViewById(R.id.judgeNameTextView);
        scoreText = (TextView) findViewById(R.id.scoreTextView);
        scoreTextText = (TextView) findViewById(R.id.scoreTextTextView);
        panelAndRoleTextView.setText("P" + panelNumber + " | " + roleType);
        judgeNameTextView.setText(SP.getString("judgeName","SURNAME FirstName"));
        if(roleType.equals("CJP")) {
            scoreTextText.setText("PENALTY");
        } else {
            scoreTextText.setText("SCORE");
        }
        ClearScoreAndScoreText();
        ToggleInput(false);
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval){
            super(startTime,interval);
        }

        @Override
        public void onFinish() {
            showWallpaper = true;
            setContentView(R.layout.screensaver);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            final int height = metrics.heightPixels;
            final int width = metrics.widthPixels;
            final ImageView screensaverText = findViewById(R.id.screensaverTextImageView);
            final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(screensaverText, "alpha", 0);
            final ObjectAnimator fadeIn = ObjectAnimator.ofFloat(screensaverText, "alpha", 1);
            fadeOut.setDuration(750);
            fadeIn.setDuration(750);
            final Random random = new Random();
            fadeOut.setStartDelay(4000);
            final AnimatorSet animSet = new AnimatorSet();
            animSet.play(fadeIn).before(fadeOut);
            animSet.addListener(new AnimatorSet.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    screensaverText.setX(random.nextInt(width - screensaverText.getWidth()));
                    screensaverText.setY(random.nextInt(height - screensaverText.getHeight()));
                    animSet.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animSet.start();
        }

        @Override
        public void onTick(long millisUntilFinished){
        }
    }

    public class PopUpClass{

        TextView elementsTextView;
        public void showPopupWindow(final View view){

            LayoutInflater inflater = getLayoutInflater();
            final View popupLayout = inflater.inflate(R.layout.pop_up_layout, (ViewGroup)findViewById(R.id.custom_pop_up_layout));
            TextView tv = (TextView)popupLayout.findViewById(R.id.textTitle);
            elementsTextView = (TextView)popupLayout.findViewById(R.id.elements);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            final PopupWindow popupWindow = new PopupWindow(popupLayout, width - 100,LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(view,Gravity.CENTER,0,0);
            elementsTextView.setText(discipline.equals("DMT") ? "2" : discipline.equals("TUM") ? "8" : "10");

            ImageButton upButton = popupLayout.findViewById(R.id.upButton);
            upButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer elements = Integer.parseInt(elementsTextView.getText().toString());
                    if(elements == 2 && discipline.equals("DMT")) return;
                    if(elements == 8 && discipline.equals("TUM")) return;
                    if(elements == 10) return;
                    String newElements = String.valueOf(elements + 1);
                    elementsTextView.setText(newElements);
                }
            });

            ImageButton downButton = popupLayout.findViewById(R.id.downButton);
            downButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Integer elements = Integer.parseInt(elementsTextView.getText().toString());
                    if(elements == 0) return;
                    String newElements = String.valueOf(elements - 1);
                    elementsTextView.setText(newElements);
                }
            });

            Button submitButton = popupLayout.findViewById(R.id.submitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    mService.sendMessage("ElementsConfirmed," + elementsTextView.getText().toString());
                }
            });
        }
    }

}







