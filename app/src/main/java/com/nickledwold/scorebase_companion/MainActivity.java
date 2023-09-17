package com.nickledwold.scorebase_companion;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.jakewharton.processphoenix.ProcessPhoenix;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.util.Base64;

import java.util.List;
import java.util.Random;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class MainActivity extends AppCompatActivity implements ContinuousHttpGet.OnHttpResultListener {

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

    private ImageView scorePanelImageView;
    private ImageView score2PanelImageView;
    private ImageView score3PanelImageView;
    private ImageView score4PanelImageView;
    private TextView score2Text;
    private TextView score2TextText;

    private TextView score3Text;
    private TextView score3TextText;

    private TextView score4Text;
    private TextView score4TextText;
    private String panelNumber;
    private String roleType;
    private String interimRoleType;
    private String interimScore;
    private String discipline;
    private String ipAddressAssignMode;
    private SharedPreferences SP;
    private static final String TAG = "MainActivity";
    private int[] deductionsArray;
    private String interfaceType = "TRADeduction";
    private final Context mContext = this;
    private boolean mBound = false;
    private Handler mHandler; // to display Toast message
    private int elements;
    private boolean fullExercise;
    private long lastClickTime;
    private long DOUBLE_CLICK_TIME_DELTA = 300;
    private Toast toast;
    private boolean inputAllowed = false;

    private long startTime = 5 * 60 * 1000; //5 minutes
    //private long startTime = 1 * 20 * 1000; //20 seconds
    private final long interval = 10 * 6 * 1000; //1 minute
    private CountDownTimer countDownTimer;
    private boolean showWallpaper;
    private ContinuousHttpGet continuousHttpGet;

    public String Status = null;

    private ElementsPopUpClass popUpClass;

    private SignOffPopUpClass signOffPopUpClass;


    private Boolean reEntryInProgress = false;

    private int currentScoreInput = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.splashScreenTheme);
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        popUpClass = new ElementsPopUpClass();
        signOffPopUpClass = new SignOffPopUpClass();

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
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        updateSettings();

        ToggleInput(false);
        countDownTimer = new MyCountDownTimer(startTime, interval);

        continuousHttpGet = new ContinuousHttpGet(this, SP);

    }

    @Override
    protected void onStart() {
        super.onStart();
        continuousHttpGet.startContinuousHttpGet();
    }

    @Override
    protected void onStop() {
        super.onStop();
        continuousHttpGet.stopContinuousHttpGet();
    }

    public void onHttpErrorOrException() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Unable to connect to the SCOREBASE API - please seek assistance", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onHttpResult(final String result) {
        ObjectMapper objectMapper = new ObjectMapper();
        ApiResponseObjects.GetCompetitionDataResponse response = null;
        try {
            response = objectMapper.readValue(result, ApiResponseObjects.GetCompetitionDataResponse.class);
        } catch (JsonProcessingException e) {
            System.out.println(e);
            return;
        }
        ApiResponseObjects.CompetitionData competitionData = response.getResult();
        if (competitionData == null) return;
        if (competitionData.getPanelNumber() == null) return;
        if (!competitionData.getPanelNumber().toString().equals(panelNumber)) return;
        Boolean judgeNeedsReEntering = DoesJudgeNeedReEntering(competitionData.getJudgeInformation());
        if (!competitionData.getStatus().equals(Status) || (judgeNeedsReEntering && !reEntryInProgress)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(result);
                            onTouchEvent(null);
                            if (competitionData.getStatus().equals("")) {
                                nameTextView = findViewById(R.id.nameTextView);
                                clubTextView = findViewById(R.id.clubTextView);
                                categoryTextView = findViewById(R.id.categoryTextView);
                                otherInfoTextView = findViewById(R.id.otherInfoTextView);
                                scoreTextText.setVisibility(View.VISIBLE);
                                if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
                                    score2TextText.setVisibility(View.VISIBLE);
                                    if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                                        score3TextText.setVisibility(View.VISIBLE);
                                        score4TextText.setVisibility(View.VISIBLE);
                                    }
                                }
                                nameTextView.setText("");
                                clubTextView.setText("");
                                categoryTextView.setText("");
                                otherInfoTextView.setText("");
                                ClearScores(true);
                                HideCompetitorSummary();
                                ReduceOpacityOfDeductionBoxes(interfaceType.equals("DMTDeduction") ? 2 : interfaceType.equals("TUMDeduction") ? 8 : 10);
                                inputAllowed = false;
                                ToggleInput(false);
                            }

                            if (competitionData.getStatus().equals("COMPETING") || competitionData.getStatus().equals("AWAITING ELEMENTS") || competitionData.getStatus().equals("ELEMENTS CONFIRMED") || competitionData.getStatus().equals("WAITING") || competitionData.getStatus().equals("FLIGHT COMPLETE")) {
                                ApiResponseObjects.CompetitorInformation competitorInfo = competitionData.getCompetitorInformation();
                                nameTextView = findViewById(R.id.nameTextView);
                                clubTextView = findViewById(R.id.clubTextView);
                                categoryTextView = findViewById(R.id.categoryTextView);
                                otherInfoTextView = findViewById(R.id.otherInfoTextView);
                                scoreTextText.setVisibility(View.VISIBLE);
                                if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
                                    score2TextText.setVisibility(View.VISIBLE);
                                    if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                                        score3TextText.setVisibility(View.VISIBLE);
                                        score4TextText.setVisibility(View.VISIBLE);
                                    }
                                }
                                nameTextView.setText(competitorInfo.getName());
                                clubTextView.setText(competitorInfo.getClub());
                                categoryTextView.setText(competitorInfo.getCategory());
                                String otherInfo = "Exercise " + competitorInfo.getExercise() + " | Flight " + competitorInfo.getFlight() + " |  No " + competitorInfo.getCompetitorNumber() + "/" + competitorInfo.getCompetitorCount();
                                otherInfoTextView.setText(otherInfo);
                                ClearScores(true);
                                HideCompetitorSummary();
                                ReduceOpacityOfDeductionBoxes(interfaceType.equals("DMTDeduction") ? 2 : interfaceType.equals("TUMDeduction") ? 8 : 10);
                                inputAllowed = false;
                                ToggleInput(false);
                            }
                            if (competitionData.getStatus().equals("ELEMENTS CONFIRMED") && !judgeNeedsReEntering) {
                                elements = competitionData.getCompetitorInformation().getElements();
                                fullExercise = discipline.equals("DMT") ? elements == 2 : discipline.equals("TUM") ? elements == 8 : elements == 10;
                                ReduceOpacityOfDeductionBoxes(elements);
                                inputAllowed = true;
                                if (elements > 0) {
                                    if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                                        scoreText.setText(String.valueOf(elements));
                                        currentScoreInput = 2;
                                        UpdateScoreInputOpacity();
                                    }
                                    ToggleInput(true);
                                    if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                                        currentScoreInput = 2;
                                    } else {
                                        currentScoreInput = 1;
                                    }
                                    UpdateScoreInputOpacity();
                                    ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Please enter your score", Toast.LENGTH_SHORT);
                                } else {
                                    currentScoreInput = 1;
                                    UpdateScoreInputOpacity();
                                    ToggleInput(false);
                                    ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Zero score", Toast.LENGTH_LONG);
                                }
                            }
                            if (judgeNeedsReEntering) {
                                ClearScores(true);
                                elements = competitionData.getCompetitorInformation().getElements();
                                fullExercise = discipline.equals("DMT") ? elements == 2 : discipline.equals("TUM") ? elements == 8 : elements == 10;
                                ReduceOpacityOfDeductionBoxes(elements);
                                inputAllowed = true;
                                reEntryInProgress = true;
                                ToggleInput(true);
                                if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                                    currentScoreInput = 2;
                                    scoreText.setText(String.valueOf(elements));
                                } else {
                                    currentScoreInput = 1;
                                }
                                UpdateScoreInputOpacity();
                                if (roleType.equals("HDT") || roleType.equals("HDS")) {
                                    interimRoleType = "HD";
                                    interimScore = "";
                                    scoreTextText.setText("HORIZONTAL DISPLACEMENT");
                                    scoreText.setTextSize(180);
                                }
                                ShowCustomToast(R.layout.custom_toast_amber, (ViewGroup) findViewById(R.id.custom_toast_layout_amber), "Please re-enter", Toast.LENGTH_LONG);
                            }
                            if (competitionData.getStatus().equals("FLIGHT COMPLETE")) {
                                ClearScores(false);
                                inputAllowed = false;
                                ToggleInput(false);
                                ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Flight complete", Toast.LENGTH_LONG);
                            }
                            if (competitionData.getStatus().equals("WAITING") || competitionData.getStatus().equals("FLIGHT COMPLETE")) {
                                scoreText.setText("");
                                scoreTextText.setVisibility(View.VISIBLE);
                                if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
                                    score2TextText.setVisibility(View.VISIBLE);
                                    if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                                        score3TextText.setVisibility(View.VISIBLE);
                                        score4TextText.setVisibility(View.VISIBLE);
                                    }
                                }
                                inputAllowed = false;
                                ToggleInput(false);
                                ShowCompetitorSummary(competitionData.getCompetitorInformation().getCompetitorSummary());
                            }
                            if (competitionData.getStatus().equals("AWAITING ELEMENTS") && roleType.equals("CJP")) {
                                if (popUpClass.popupWindowIsShowing) {
                                    popUpClass.closePopupWindow();
                                }
                                popUpClass.showPopupWindow((ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0));
                            }
                            if (!competitionData.getStatus().equals("AWAITING ELEMENTS") && roleType.equals("CJP") && popUpClass != null && popUpClass.popupWindowIsShowing) {
                                popUpClass.closePopupWindow();
                            }

                            if (competitionData.getStatus().equals("AWAITING SIGN OFF") && roleType.equals("CJP")) {
                                if (signOffPopUpClass.popupWindowIsShowing) {
                                    signOffPopUpClass.closePopupWindow();
                                }
                                signOffPopUpClass.showPopupWindow((ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0), competitionData.getSignOffCategory(), competitionData.getSignOffRound());
                            }
                            if (!competitionData.getStatus().equals("AWAITING SIGN OFF") && roleType.equals("CJP") && signOffPopUpClass != null && signOffPopUpClass.popupWindowIsShowing) {
                                signOffPopUpClass.closePopupWindow();
                            }
                            if (competitionData.getJudgeInformation().size() > 0) {
                                for (ApiResponseObjects.JudgeInformation judgeInformation : competitionData.getJudgeInformation()) {
                                    if (judgeInformation.getJudgeRole().equals(roleType) || (roleType.equals("HDT") && (judgeInformation.getJudgeRole().equals("HD") || judgeInformation.getJudgeRole().equals("T"))) || (roleType.equals("HDS") && (judgeInformation.getJudgeRole().equals("HD") || judgeInformation.getJudgeRole().equals("S")))) {
                                        judgeNameTextView = findViewById(R.id.judgeNameTextView);
                                        judgeNameTextView.setText(judgeInformation.getJudgeName());
                                        SharedPreferences.Editor editor = SP.edit();
                                        editor.putString("judgeName", judgeInformation.getJudgeName());
                                        editor.commit();
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
        Status = competitionData.getStatus();
    }

    private void UpdateScoreInputOpacity() {
        if (!interfaceType.equals("FullScore")) return;
        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add((ImageView) findViewById(R.id.scorePanelImageView));
        List<TextView> textViews = new ArrayList<>();
        textViews.add((TextView) findViewById(R.id.scoreTextView));
        textViews.add((TextView) findViewById(R.id.scoreTextTextView));
        if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || roleType.equals("D") && discipline.equals("TUM")) {
            imageViews.add((ImageView) findViewById(R.id.scorePanelImageView2));
            textViews.add((TextView) findViewById(R.id.score2TextView));
            textViews.add((TextView) findViewById(R.id.score2TextTextView));
            if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                imageViews.add((ImageView) findViewById(R.id.scorePanelImageView3));
                textViews.add((TextView) findViewById(R.id.score3TextView));
                textViews.add((TextView) findViewById(R.id.score3TextTextView));
                imageViews.add((ImageView) findViewById(R.id.scorePanelImageView4));
                textViews.add((TextView) findViewById(R.id.score4TextView));
                textViews.add((TextView) findViewById(R.id.score4TextTextView));
            }
        }
        if (imageViews.size() == 1) {
            for (ImageView imageView :
                    imageViews) {
                imageView.setAlpha(1.0f);
            }
            for (TextView textView :
                    textViews) {
                textView.setAlpha(1.0f);
            }
        }
        for (int i = 1; i <= imageViews.size(); i++) {
            if (i == currentScoreInput) {
                imageViews.get(i - 1).setAlpha(1.0f);
                textViews.get((i - 1) * 2).setAlpha(1.0f);
                textViews.get(((i - 1) * 2) + 1).setAlpha(1.0f);
            } else {
                imageViews.get(i - 1).setAlpha(0.4f);
                textViews.get((i - 1) * 2).setAlpha(0.4f);
                textViews.get(((i - 1) * 2) + 1).setAlpha(0.4f);
            }
        }
    }

    private void ResetScoreInputOpacity() {
        if (!interfaceType.equals("FullScore")) return;
        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add((ImageView) findViewById(R.id.scorePanelImageView));
        List<TextView> textViews = new ArrayList<>();
        textViews.add((TextView) findViewById(R.id.scoreTextView));
        textViews.add((TextView) findViewById(R.id.scoreTextTextView));
        if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || roleType.equals("D") && discipline.equals("TUM")) {
            imageViews.add((ImageView) findViewById(R.id.scorePanelImageView2));
            textViews.add((TextView) findViewById(R.id.score2TextView));
            textViews.add((TextView) findViewById(R.id.score2TextTextView));
            if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                imageViews.add((ImageView) findViewById(R.id.scorePanelImageView3));
                textViews.add((TextView) findViewById(R.id.score3TextView));
                textViews.add((TextView) findViewById(R.id.score3TextTextView));
                imageViews.add((ImageView) findViewById(R.id.scorePanelImageView4));
                textViews.add((TextView) findViewById(R.id.score4TextView));
                textViews.add((TextView) findViewById(R.id.score4TextTextView));
            }
        }

        for (ImageView imageView :
                imageViews) {
            imageView.setAlpha(1.0f);
        }
        for (TextView textView :
                textViews) {
            textView.setAlpha(1.0f);
        }

    }

    private Boolean DoesJudgeNeedReEntering(List<ApiResponseObjects.JudgeInformation> judgeInformationList) {
        List<ApiResponseObjects.JudgeInformation> trimmedJudgeInformationList = new ArrayList<>();
        for (ApiResponseObjects.JudgeInformation judgeInformation : judgeInformationList) {
            if ((judgeInformation.getJudgeRole().equals(roleType) || (roleType.equals("HDT") && (judgeInformation.getJudgeRole().equals("HD") || judgeInformation.getJudgeRole().equals("T"))) || (roleType.equals("HDS") && (judgeInformation.getJudgeRole().equals("HD") || judgeInformation.getJudgeRole().equals("S")))) && judgeInformation.isReEntryRequested()) {
                return true;
            }
        }
        return false;
    }

    private void ClearScoreAndScoreText() {
        scoreText.setText("");
        scoreTextText.setVisibility(View.INVISIBLE);
        scoreText.requestLayout();

        if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
            score2Text.setText("");
            score2TextText.setVisibility(View.INVISIBLE);
            score2Text.requestLayout();


            if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                score3Text.setText("");
                score4Text.setText("");
                score3TextText.setVisibility(View.INVISIBLE);
                score4TextText.setVisibility(View.INVISIBLE);
                score3Text.requestLayout();
                score4Text.requestLayout();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (showWallpaper) {
            updateSettings();
            showWallpaper = false;
        }
        countDownTimer.cancel();
        countDownTimer.start();
        if (toast != null)
            toast.cancel();
        return false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

    }


    private void HideCompetitorSummary() {
        if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
            scoreTextText.setText("Elements");
            score2TextText.setText("H");
            score3TextText.setText(discipline.equals("TRS") ? "S" : "T");
            score4TextText.setText("Penalty");
        } else if (roleType.equals("D") && discipline.equals("TUM")) {
            scoreTextText.setText("D");
            score2TextText.setText("Bonus");
        } else if (roleType.equals("CJP")) {
            scoreTextText.setText("PENALTY");
        } else if (roleType.startsWith("E")) {
            scoreTextText.setText("TOTAL DEDUCTIONS");
        } else if (roleType.equals("HDT") || roleType.equals("HDS")) {
            scoreTextText.setText("HORIZONTAL DISPLACEMENT");
            scoreText.setTextSize(180);
        } else {
            scoreTextText.setText("SCORE");
        }
    }

    private void ShowCompetitorSummary(ApiResponseObjects.CompetitorSummary competitorSummary) {
        if (roleType.startsWith("E")) {
            scoreText.setText(String.format("%.2f", competitorSummary.getExecution()));
            scoreText.setTextColor(Color.WHITE);
            scoreTextText.setText("TOTAL EXECUTION SCORE");
        }
    }

    private void ClearScores(boolean clearScoreText) {
        if (!interfaceType.equals("FullScore")) {
            deductionOneTextView.setText("");
            deductionTwoTextView.setText("");
            if (interfaceType.equals("TRADeduction") || interfaceType.equals("TUMDeduction")) {
                deductionThreeTextView.setText("");
                deductionFourTextView.setText("");
                deductionFiveTextView.setText("");
                deductionSixTextView.setText("");
                deductionSevenTextView.setText("");
                deductionEightTextView.setText("");
                if (interfaceType.equals("TRADeduction")) {
                    deductionNineTextView.setText("");
                    deductionTenTextView.setText("");
                }
            }
            deductionStabilityTextView.setText("");
        }
        if (!interfaceType.equals("FullScore")) {
            deductionsArray = getDeductions();
        }
        if (clearScoreText) {
            scoreText.setText("");
            if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
                score2Text.setText("");
                if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                    score3Text.setText("");
                    score4Text.setText("");
                }
            }
        }
    }

    private void UpdateActiveDeductionBox(int[] deductionsArray) {
        if (!inputAllowed) return;
        if (interfaceType.equals("FullScore")) return;

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add((ImageView) findViewById(R.id.deuctionOnePanelImageView));
        imageViews.add((ImageView) findViewById(R.id.deuctionTwoPanelImageView));
        imageViews.add((ImageView) findViewById(R.id.deuctionStabilityPanelImageView));
        if (interfaceType.equals("TRADeduction") || interfaceType.equals("TUMDeduction")) {
            imageViews.add(2, (ImageView) findViewById(R.id.deuctionThreePanelImageView));
            imageViews.add(3, (ImageView) findViewById(R.id.deuctionFourPanelImageView));
            imageViews.add(4, (ImageView) findViewById(R.id.deuctionFivePanelImageView));
            imageViews.add(5, (ImageView) findViewById(R.id.deuctionSixPanelImageView));
            imageViews.add(6, (ImageView) findViewById(R.id.deuctionSevenPanelImageView));
            imageViews.add(7, (ImageView) findViewById(R.id.deuctionEightPanelImageView));
            if (interfaceType.equals("TRADeduction")) {
                imageViews.add(8, (ImageView) findViewById(R.id.deuctionNinePanelImageView));
                imageViews.add(9, (ImageView) findViewById(R.id.deuctionTenPanelImageView));
            }
        }
        for (int i = 0; i < deductionsArray.length; i++) {
            imageViews.get(i).setImageDrawable(getDrawable(R.drawable.bluepanel));
        }
        int firstEmpty = find(deductionsArray, -1);
        if (firstEmpty == -1) return;
        if (!fullExercise && firstEmpty >= elements) return;
        imageViews.get(firstEmpty).setImageDrawable(getDrawable(R.drawable.bluepanel_lighter));
    }

    private void ReduceOpacityOfDeductionBoxes(int elementsInExercise) {
        if (interfaceType.equals("FullScore")) return;
        if (interfaceType.equals("DMTDeduction") && elementsInExercise > 1) elementsInExercise = 3;
        if (interfaceType.equals("TUMDeduction") && elementsInExercise > 7) elementsInExercise = 9;
        if (interfaceType.equals("TRADeduction") && elementsInExercise > 9) elementsInExercise = 11;

        List<ImageView> imageViews = new ArrayList<>();
        imageViews.add((ImageView) findViewById(R.id.deuctionOnePanelImageView));
        imageViews.add((ImageView) findViewById(R.id.deuctionTwoPanelImageView));
        imageViews.add((ImageView) findViewById(R.id.deuctionStabilityPanelImageView));
        List<TextView> textViews = new ArrayList<>();
        textViews.add((TextView) findViewById(R.id.deductionOneTextView));
        textViews.add((TextView) findViewById(R.id.deductionTwoTextView));
        textViews.add((TextView) findViewById(R.id.deductionStabilityTextView));

        if (interfaceType.equals("TRADeduction") || interfaceType.equals("TUMDeduction")) {
            imageViews.add(2, (ImageView) findViewById(R.id.deuctionThreePanelImageView));
            imageViews.add(3, (ImageView) findViewById(R.id.deuctionFourPanelImageView));
            imageViews.add(4, (ImageView) findViewById(R.id.deuctionFivePanelImageView));
            imageViews.add(5, (ImageView) findViewById(R.id.deuctionSixPanelImageView));
            imageViews.add(6, (ImageView) findViewById(R.id.deuctionSevenPanelImageView));
            imageViews.add(7, (ImageView) findViewById(R.id.deuctionEightPanelImageView));
            textViews.add(2, (TextView) findViewById(R.id.deductionThreeTextView));
            textViews.add(3, (TextView) findViewById(R.id.deductionFourTextView));
            textViews.add(4, (TextView) findViewById(R.id.deductionFiveTextView));
            textViews.add(5, (TextView) findViewById(R.id.deductionSixTextView));
            textViews.add(6, (TextView) findViewById(R.id.deductionSevenTextView));
            textViews.add(7, (TextView) findViewById(R.id.deductionEightTextView));

            if (interfaceType.equals("TRADeduction")) {
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


    public void deleteButtonpressed(View view) {
        onTouchEvent(null);
        if (!inputAllowed) return;
        if (interfaceType.equals("FullScore")) {
            if (currentScoreInput == 1) {
                int length = scoreText.getText().toString().length();
                if (length > 0) {
                    scoreText.setText(scoreText.getText().toString().substring(0, length - 1));
                }
            } else if (currentScoreInput == 2) {
                int length = score2Text.getText().toString().length();
                if (length > 0) {
                    score2Text.setText(score2Text.getText().toString().substring(0, length - 1));
                }
            } else if (currentScoreInput == 3) {
                int length = score3Text.getText().toString().length();
                if (length > 0) {
                    score3Text.setText(score3Text.getText().toString().substring(0, length - 1));
                }
            } else if (currentScoreInput == 4) {
                int length = score4Text.getText().toString().length();
                if (length > 0) {
                    score4Text.setText(score4Text.getText().toString().substring(0, length - 1));
                }
            }
        } else {
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
    public void onResume() {
        super.onResume();
        updateSettings();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void submitButtonpressed(View button) {
        String ipAddress = SP.getString("ipAddress", "10.0.0.11");
        String submitScoreUrl = "http://" + ipAddress + ":1337/submitScore";
        String requestReEntryUrl = "http://" + ipAddress + ":1337/requestReEntry";
        onTouchEvent(null);
        String buttonText = ((Button) button).getText().toString();
        if (buttonText.equals("RE-ENTER")) {
            ShowCustomToast(R.layout.custom_toast_amber, (ViewGroup) findViewById(R.id.custom_toast_layout_amber), "Re-entry requested\n\nPlease wait", Toast.LENGTH_SHORT);
            FormBody formBody = new FormBody.Builder()
                    .add("Role", roleType)
                    .build();
            NetworkUtils.performPostRequestWithRetry(requestReEntryUrl, formBody);
            submitButton.setEnabled(false);
            submitButton.setTextColor(Color.GRAY);
        }
        if (!inputAllowed) return;
        String score = "";
        if (interfaceType.equals("FullScore")) {
            if (currentScoreInput == 1) {
                String scoreTextValue = scoreText.getText().toString();
                if (TextUtils.isEmpty(scoreTextValue)) {
                    ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a score before submitting", Toast.LENGTH_SHORT);
                    return;
                }
            } else if (currentScoreInput == 2) {
                String scoreTextValue = score2Text.getText().toString();
                if (TextUtils.isEmpty(scoreTextValue)) {
                    ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a score before submitting", Toast.LENGTH_SHORT);
                    return;
                }
            } else if (currentScoreInput == 3) {
                String scoreTextValue = score3Text.getText().toString();
                if (TextUtils.isEmpty(scoreTextValue)) {
                    ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a score before submitting", Toast.LENGTH_SHORT);
                    return;
                }
            } else if (currentScoreInput == 4) {
                String scoreTextValue = score4Text.getText().toString();
                if (TextUtils.isEmpty(scoreTextValue)) {
                    ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a score before submitting", Toast.LENGTH_SHORT);
                    return;
                }
            }
            FormBody formBody = null;
            if (roleType.equals("HDS") || roleType.equals("HDT")) {
                score = scoreText.getText().toString();
                formBody = new FormBody.Builder()
                        .add("Role", interimRoleType)
                        .add("Score", score)
                        .build();
            } else {
                String subRole = "";
                if (currentScoreInput == 1) {
                    score = scoreText.getText().toString();
                    formBody = new FormBody.Builder()
                            .add("Role", roleType)
                            .add("Score", score)
                            .build();
                } else if (currentScoreInput == 2) {
                    score = score2Text.getText().toString();
                    if (roleType.equals("CJP")) {
                        subRole = "HD";
                    }
                    if (roleType.equals("D") && discipline.equals("TUM")) {
                        subRole = "B";
                    }
                    formBody = new FormBody.Builder()
                            .add("Role", roleType)
                            .add("Score", score)
                            .add("SubRole", subRole)
                            .build();
                } else if (currentScoreInput == 3) {
                    score = score3Text.getText().toString();
                    if (roleType.equals("CJP")) {
                        subRole = discipline.equals("TRS") ? "S" : "T";
                    }
                    formBody = new FormBody.Builder()
                            .add("Role", roleType)
                            .add("Score", score)
                            .add("SubRole", subRole)
                            .build();
                } else if (currentScoreInput == 4) {
                    score = score4Text.getText().toString();
                    formBody = new FormBody.Builder()
                            .add("Role", roleType)
                            .add("Score", score)
                            .build();
                }
            }
            NetworkUtils.performPostRequestWithRetry(submitScoreUrl, formBody);
            reEntryInProgress = false;
        } else {
            int firstEmpty = find(deductionsArray, -1);
            if (firstEmpty != -1) {
                if ((!fullExercise && firstEmpty != elements) || (fullExercise && firstEmpty != elements + 1)) {
                    ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter all deductions", Toast.LENGTH_SHORT);
                    return;
                }
            }
            if (elements == 0) return;
            for (int i = 0; i < deductionsArray.length; i++) {
                score += deductionsArray[i];
                if (i != deductionsArray.length - 1) score += ",";
            }
            FormBody formBody = new FormBody.Builder()
                    .add("Role", roleType)
                    .add("Deductions", score)
                    .build();
            NetworkUtils.performPostRequestWithRetry(submitScoreUrl, formBody);
        }

        if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
            if (currentScoreInput == 1) {
                currentScoreInput++;
                UpdateScoreInputOpacity();
            } else if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) && currentScoreInput > 1 && currentScoreInput < 4) {
                currentScoreInput++;
                UpdateScoreInputOpacity();
            } else {
                inputAllowed = false;
                ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Submitted", Toast.LENGTH_SHORT);
                ToggleInput(false);
                submitButton = findViewById(R.id.submitButton);
                submitButton.setBackground(getDrawable(R.drawable.reenter_button_background));
                submitButton.setText("RE-ENTER");
                submitButton.setEnabled(true);
                submitButton.setTextColor(Color.WHITE);
                currentScoreInput = 1;
                ResetScoreInputOpacity();
                reEntryInProgress = false;
            }
        } else if (roleType.equals("HDS") || roleType.equals("HDT")) {
            if (interimRoleType.equals("HD")) {
                interimRoleType = roleType.equals("HDS") ? "S" : "T";
                ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Submitted", Toast.LENGTH_SHORT);
                interimScore = scoreText.getText().toString();
                scoreTextText.setText(roleType.equals("HDS") ? "SYNCHRONISATION" : "TIME OF FLIGHT");
                scoreText.setText("");
            } else {
                scoreTextText.setText("SCORES");
                scoreText.setTextSize(90);
                scoreText.setText(interimScore + "    " + scoreText.getText().toString());
                inputAllowed = false;
                ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Submitted", Toast.LENGTH_SHORT);
                ToggleInput(false);
                submitButton = findViewById(R.id.submitButton);
                submitButton.setBackground(getDrawable(R.drawable.reenter_button_background));
                submitButton.setText("RE-ENTER");
                submitButton.setEnabled(true);
                submitButton.setTextColor(Color.WHITE);
                interimRoleType = "HD";
                reEntryInProgress = false;
            }
        } else {
            inputAllowed = false;
            ShowCustomToast(R.layout.custom_toast_green, (ViewGroup) findViewById(R.id.custom_toast_layout_green), "Submitted", Toast.LENGTH_SHORT);
            ToggleInput(false);
            submitButton = findViewById(R.id.submitButton);
            submitButton.setBackground(getDrawable(R.drawable.reenter_button_background));
            submitButton.setText("RE-ENTER");
            submitButton.setEnabled(true);
            submitButton.setTextColor(Color.WHITE);
            reEntryInProgress = false;
        }
    }

    private void ToggleInput(boolean enabled) {
        int textColor = enabled ? Color.WHITE : Color.GRAY;
        int imageAlpha = enabled ? 255 : 100;
        scoreText.setTextColor(textColor);
        if ((roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) || (roleType.equals("D") && discipline.equals("TUM"))) {
            score2Text.setTextColor(textColor);
            if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
                score3Text.setTextColor(textColor);
                score4Text.setTextColor(textColor);
            }
        }
        if (!interfaceType.equals("FullScore")) {
            deductionOneTextView.setTextColor(textColor);
            deductionTwoTextView.setTextColor(textColor);
            if (!discipline.equals("DMT")) {
                deductionThreeTextView.setTextColor(textColor);
                deductionFourTextView.setTextColor(textColor);
                deductionFiveTextView.setTextColor(textColor);
                deductionSixTextView.setTextColor(textColor);
                deductionSevenTextView.setTextColor(textColor);
                deductionEightTextView.setTextColor(textColor);
                if (!discipline.equals("TUM")) {
                    deductionNineTextView.setTextColor(textColor);
                    deductionTenTextView.setTextColor(textColor);
                }
            }
            deductionStabilityTextView.setTextColor(textColor);
        }
        ((Button) findViewById(R.id.oneButton)).setTextColor(textColor);
        ((Button) findViewById(R.id.twoButton)).setTextColor(textColor);
        ((Button) findViewById(R.id.threeButton)).setTextColor(textColor);
        ((Button) findViewById(R.id.fourButton)).setTextColor(textColor);
        ((Button) findViewById(R.id.fiveButton)).setTextColor(textColor);
        findViewById(R.id.oneButton).setEnabled(enabled);
        findViewById(R.id.twoButton).setEnabled(enabled);
        findViewById(R.id.threeButton).setEnabled(enabled);
        findViewById(R.id.fourButton).setEnabled(enabled);
        findViewById(R.id.fiveButton).setEnabled(enabled);
        if (interfaceType.equals("FullScore")) {
            ((Button) findViewById(R.id.sixButton)).setTextColor(textColor);
            ((Button) findViewById(R.id.sevenButton)).setTextColor(textColor);
            ((Button) findViewById(R.id.eightButton)).setTextColor(textColor);
            ((Button) findViewById(R.id.nineButton)).setTextColor(textColor);
            findViewById(R.id.sixButton).setEnabled(enabled);
            findViewById(R.id.sevenButton).setEnabled(enabled);
            findViewById(R.id.eightButton).setEnabled(enabled);
            findViewById(R.id.nineButton).setEnabled(enabled);
        } else {
            ((Button) findViewById(R.id.tenButton)).setTextColor(textColor);
            findViewById(R.id.tenButton).setEnabled(enabled);
        }
        ((Button) findViewById(R.id.zeroButton)).setTextColor(textColor);
        ((Button) findViewById(R.id.decimalButton)).setTextColor(textColor);
        ((ImageButton) findViewById(R.id.deleteButton)).setImageAlpha(imageAlpha);
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
        TextView tv = (TextView) layout.findViewById(R.id.txtvw);
        tv.setTextSize(32);
        toast = new Toast(getApplicationContext());
        toast.setDuration(toastLength);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setView(layout);
        tv.setText(toastText);
        toast.show();
    }

    public void keypadPressed(View view) {
        onTouchEvent(null);
        if (!inputAllowed) return;
        Button b = (Button) view;
        String buttonValue = b.getText().toString();
        if (interfaceType.equals("FullScore")) {
            String scoreTextValue = currentScoreInput == 4 ? score4Text.getText().toString() : currentScoreInput == 3 ? score3Text.getText().toString() : currentScoreInput == 2 ? score2Text.getText().toString() : scoreText.getText().toString();
            if (scoreTextValue.equals("") && buttonValue.equals(".")) return;
            if (scoreTextValue.contains(".") && buttonValue.equals(".")) return;
            float value = buttonValue.equals(".") ? Float.parseFloat(scoreTextValue) : Float.parseFloat(scoreTextValue + buttonValue);
            switch (interimRoleType) {
                case "HD":
                case "CJP":
                    if (discipline.equals("TRA") || discipline.equals("TRS")) {
                        if (currentScoreInput == 3) {
                            if (value > 20) {
                                ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a value between 0 - 20", Toast.LENGTH_SHORT);
                                return;
                            }
                        } else if (currentScoreInput == 4) {
                            if (value > 10) {
                                ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a value between 0 - 10", Toast.LENGTH_SHORT);
                                return;
                            }
                        } else {
                            if (value > 10) {
                                ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a value between 0 - 10", Toast.LENGTH_SHORT);
                                return;
                            }
                        }
                    } else if (value > 10) {
                        ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a value between 0 - 10", Toast.LENGTH_SHORT);
                        return;
                    }
                    break;
                case "S":
                case "T":
                case "D":
                    if (value > 20) {
                        ShowCustomToast(R.layout.custom_toast_red, (ViewGroup) findViewById(R.id.custom_toast_layout_red), "Please enter a value between 0 - 20", Toast.LENGTH_SHORT);
                        return;
                    }
                    break;
            }
            String valueAfterDecimal = scoreTextValue.contains(".") ? scoreTextValue.substring(scoreTextValue.lastIndexOf('.') + 1) : null;
            switch (interimRoleType) {
                case "CJP":
                    if(currentScoreInput == 2 || currentScoreInput == 3){
                        if (valueAfterDecimal != null && valueAfterDecimal.length() > 1) return;
                    }else{
                        if (valueAfterDecimal != null && valueAfterDecimal.length() > 0) return;
                    }
                    break;
                case "D":
                    if (valueAfterDecimal != null && valueAfterDecimal.length() > 0) return;
                    break;
                case "HD":
                case "S":
                case "T":
                    if (valueAfterDecimal != null && valueAfterDecimal.length() > 1) return;
                    break;
            }
            switch (currentScoreInput) {
                case 1:
                    scoreText.setText(scoreTextValue + buttonValue);
                    break;
                case 2:
                    score2Text.setText(scoreTextValue + buttonValue);
                    break;
                case 3:
                    score3Text.setText(scoreTextValue + buttonValue);
                    break;
                case 4:
                    score4Text.setText(scoreTextValue + buttonValue);
                    break;
            }

        } else {
            deductionsArray = getDeductions();
            int deductionsArrayCount = deductionsArray.length;
            int firstEmpty = find(deductionsArray, -1);
            if (firstEmpty == -1) return;
            if (firstEmpty != deductionsArrayCount - 1 && buttonValue.equals("10")) return;
            if (firstEmpty == deductionsArrayCount - 1 && buttonValue.equals("4")) return;
            if (!fullExercise && firstEmpty == elements) return;
            deductionsArray[firstEmpty] = tryParse(buttonValue);
            setDeductions(deductionsArray);
            UpdateScore(deductionsArray);
            UpdateActiveDeductionBox(deductionsArray);
        }
    }

    public void deductionTextViewPressed(View view) {
        if (!inputAllowed) return;
        TextView tv = (TextView) view;
        tv.setText("");
        UpdateActiveDeductionBox(getDeductions());
        UpdateScore(getDeductions());
    }

    public void scoreBasePressed(View view) {
        onTouchEvent(null);
        long clickTime = System.currentTimeMillis();
        if ((clickTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA) {
            Intent intent = new Intent(this, SettingsPreferenceActivity.class);
            startActivity(intent);
        }
        lastClickTime = clickTime;
    }

    public void panelAndRoleTextViewPressed(View view) {
        onTouchEvent(null);
        long clickTime = System.currentTimeMillis();
        if ((clickTime - lastClickTime) < DOUBLE_CLICK_TIME_DELTA) {
            ProcessPhoenix.triggerRebirth(getApplicationContext());
        }
        lastClickTime = clickTime;
    }

    private void UpdateScore(int[] deductionsArray) {
        float deduction = CalculateDeduction(deductionsArray);
        scoreText.setText(String.valueOf(deduction));
    }

    private int[] getDeductions() {
        if (discipline.equals("DMT")) {
            deductionsArray = new int[]{
                    tryParse(deductionOneTextView.getText().toString()),
                    tryParse(deductionTwoTextView.getText().toString()),
                    tryParse(deductionStabilityTextView.getText().toString())
            };
        } else if (discipline.equals("TUM")) {
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
        } else {
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
        if (deductionOneTextView.getAlpha() == 1.0)
            deductionOneTextView.setText(deductionsArray[0] == -1 ? "" : String.valueOf(deductionsArray[0]));
        if (deductionTwoTextView.getAlpha() == 1.0)
            deductionTwoTextView.setText(deductionsArray[1] == -1 ? "" : String.valueOf(deductionsArray[1]));
        if (!discipline.equals("DMT")) {
            if (deductionThreeTextView.getAlpha() == 1.0)
                deductionThreeTextView.setText(deductionsArray[2] == -1 ? "" : String.valueOf(deductionsArray[2]));
            if (deductionFourTextView.getAlpha() == 1.0)
                deductionFourTextView.setText(deductionsArray[3] == -1 ? "" : String.valueOf(deductionsArray[3]));
            if (deductionFiveTextView.getAlpha() == 1.0)
                deductionFiveTextView.setText(deductionsArray[4] == -1 ? "" : String.valueOf(deductionsArray[4]));
            if (deductionSixTextView.getAlpha() == 1.0)
                deductionSixTextView.setText(deductionsArray[5] == -1 ? "" : String.valueOf(deductionsArray[5]));
            if (deductionSevenTextView.getAlpha() == 1.0)
                deductionSevenTextView.setText(deductionsArray[6] == -1 ? "" : String.valueOf(deductionsArray[6]));
            if (deductionEightTextView.getAlpha() == 1.0)
                deductionEightTextView.setText(deductionsArray[7] == -1 ? "" : String.valueOf(deductionsArray[7]));
            if (!discipline.equals("TUM")) {
                if (deductionNineTextView.getAlpha() == 1.0)
                    deductionNineTextView.setText(deductionsArray[8] == -1 ? "" : String.valueOf(deductionsArray[8]));
                if (deductionTenTextView.getAlpha() == 1.0)
                    deductionTenTextView.setText(deductionsArray[9] == -1 ? "" : String.valueOf(deductionsArray[9]));
            }
        }
        int deductionsArrayCount = deductionsArray.length;
        if (deductionStabilityTextView.getAlpha() == 1.0)
            deductionStabilityTextView.setText(deductionsArray[deductionsArrayCount - 1] == -1 ? "" : String.valueOf(deductionsArray[deductionsArrayCount - 1]));
    }

    private float CalculateDeduction(int[] deductionsArray) {
        int total = 0;
        for (int i : deductionsArray) {
            {
                if (i != -1) {
                    total += i;
                }
            }
        }
        return total / 10.0f;
    }

    public int tryParse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static int find(int[] a, int target) {
        if (a == null) return 0;
        for (int i = 0; i < a.length; i++)
            if (a[i] == target)
                return i;

        return -1;
    }

    private void updateSettings() {
        panelNumber = SP.getString("panelNumber", "0");
        roleType = SP.getString("roleType", "1");
        interimRoleType = (roleType.equals("HDS") || roleType.equals("HDT")) ? "HD" : roleType;
        discipline = SP.getString("discipline", "TRA");
        ipAddressAssignMode = SP.getString("ipAddressAssignMode", "Automatic");

        if (ipAddressAssignMode.equals("Automatic")) {
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
                case "6":
                    editor.putString("ipAddress", "10.0.0.16");
                    break;
                case "7":
                    editor.putString("ipAddress", "10.0.0.17");
                    break;
                case "8":
                    editor.putString("ipAddress", "10.0.0.18");
                    break;
                case "9":
                    editor.putString("ipAddress", "10.0.0.19");
                    break;
                case "10":
                    editor.putString("ipAddress", "10.0.0.20");
                    break;
                default:
                    editor.putString("ipAddress", "10.0.0.11");
                    break;
            }
            editor.commit();
        }


        if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
            interfaceType = "FullScore";
            setContentView(R.layout.activity_main_four_score);
        } else if (roleType.equals("D") && discipline.equals("TUM")) {
            interfaceType = "FullScore";
            setContentView(R.layout.activity_main_two_score);
        } else if (roleType.equals("HD") || roleType.equals("HDT") || roleType.equals("HDS") || roleType.equals("T") || roleType.equals("D") || roleType.equals("S") || roleType.equals("CJP")) {
            interfaceType = "FullScore";
            setContentView(R.layout.activity_main_full_score);
        } else if (discipline.equals("DMT")) {
            interfaceType = "DMTDeduction";
            setContentView(R.layout.activity_main_dmt);
        } else if (discipline.equals("TUM")) {
            interfaceType = "TUMDeduction";
            setContentView(R.layout.activity_main_tum);
        } else {
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
        scorePanelImageView = (ImageView) findViewById(R.id.scorePanelImageView);
        score2PanelImageView = (ImageView) findViewById(R.id.scorePanelImageView2);
        score3PanelImageView = (ImageView) findViewById(R.id.scorePanelImageView3);
        score4PanelImageView = (ImageView) findViewById(R.id.scorePanelImageView4);
        score2Text = (TextView) findViewById(R.id.score2TextView);
        score2TextText = (TextView) findViewById(R.id.score2TextTextView);
        score3Text = (TextView) findViewById(R.id.score3TextView);
        score3TextText = (TextView) findViewById(R.id.score3TextTextView);
        score4Text = (TextView) findViewById(R.id.score4TextView);
        score4TextText = (TextView) findViewById(R.id.score4TextTextView);
        panelAndRoleTextView.setText("P" + panelNumber + " | " + roleType);
        judgeNameTextView.setText(SP.getString("judgeName", "SURNAME FirstName"));

        if (roleType.equals("CJP") && (discipline.equals("TRA") || discipline.equals("TRS"))) {
            scoreTextText.setText("Elements");
            score2TextText.setText("H");
            if (discipline.equals("TRA")) {
                score3TextText.setText("T");
            } else {
                score3TextText.setText("S");
            }
            score4TextText.setText("Penalty");
        } else if (roleType.equals("D") && discipline.equals("TUM")) {
            scoreTextText.setText("D");
            score2TextText.setText("Bonus");
        } else if (roleType.equals("CJP")) {
            scoreTextText.setText("PENALTY");
        } else if (roleType.startsWith("E")) {
            scoreTextText.setText("TOTAL DEDUCTIONS");
        } else if (roleType.equals("HDT") || roleType.equals("HDS")) {
            scoreTextText.setText("HORIZONTAL DISPLACEMENT");
            scoreText.setTextSize(180);
        } else {
            scoreTextText.setText("SCORE");
        }
        ClearScoreAndScoreText();
        ToggleInput(false);
    }

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
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
        public void onTick(long millisUntilFinished) {
        }
    }

    public class ElementsPopUpClass {

        PopupWindow popupWindow = null;
        Boolean popupWindowIsShowing = false;
        TextView elementsTextView;

        public void showPopupWindow(final View view) {

            LayoutInflater inflater = getLayoutInflater();
            final View popupLayout = inflater.inflate(R.layout.pop_up_layout, (ViewGroup) findViewById(R.id.custom_pop_up_layout));
            TextView tv = (TextView) popupLayout.findViewById(R.id.textTitle);
            elementsTextView = (TextView) popupLayout.findViewById(R.id.elements);
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            popupWindow = new PopupWindow(popupLayout, width - 100, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            popupWindowIsShowing = true;
            elementsTextView.setText(discipline.equals("DMT") ? "2" : discipline.equals("TUM") ? "8" : "10");

            final Button zeroButton = popupLayout.findViewById(R.id.zeroButton2);
            zeroButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(zeroButton.getText());
                }
            });
            final Button oneButton = popupLayout.findViewById(R.id.oneButton2);
            oneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(oneButton.getText());
                }
            });
            final Button twoButton = popupLayout.findViewById(R.id.twoButton2);
            twoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(twoButton.getText());
                }
            });
            final Button threeButton = popupLayout.findViewById(R.id.threeButton2);
            if (discipline.equals("DMT")) {
                threeButton.setEnabled(false);
                threeButton.setTextColor(Color.GRAY);
            }
            threeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(threeButton.getText());
                }
            });
            final Button fourButton = popupLayout.findViewById(R.id.fourButton2);
            if (discipline.equals("DMT")) {
                fourButton.setEnabled(false);
                fourButton.setTextColor(Color.GRAY);
            }
            fourButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(fourButton.getText());
                }
            });
            final Button fiveButton = popupLayout.findViewById(R.id.fiveButton2);
            if (discipline.equals("DMT")) {
                fiveButton.setEnabled(false);
                fiveButton.setTextColor(Color.GRAY);
            }
            fiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(fiveButton.getText());
                }
            });
            final Button sixButton = popupLayout.findViewById(R.id.sixButton2);
            if (discipline.equals("DMT")) {
                sixButton.setEnabled(false);
                sixButton.setTextColor(Color.GRAY);
            }
            sixButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(sixButton.getText());
                }
            });
            final Button sevenButton = popupLayout.findViewById(R.id.sevenButton2);
            if (discipline.equals("DMT")) {
                sevenButton.setEnabled(false);
                sevenButton.setTextColor(Color.GRAY);
            }
            sevenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(sevenButton.getText());
                }
            });
            final Button eightButton = popupLayout.findViewById(R.id.eightButton2);
            if (discipline.equals("DMT")) {
                eightButton.setEnabled(false);
                eightButton.setTextColor(Color.GRAY);
            }
            eightButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(eightButton.getText());
                }
            });
            final Button nineButton = popupLayout.findViewById(R.id.nineButton2);
            if (discipline.equals("DMT") || discipline.equals("TUM")) {
                nineButton.setEnabled(false);
                nineButton.setTextColor(Color.GRAY);
            }
            nineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(nineButton.getText());
                }
            });
            final Button tenButton = popupLayout.findViewById(R.id.tenButton2);
            if (discipline.equals("DMT") || discipline.equals("TUM")) {
                tenButton.setEnabled(false);
                tenButton.setTextColor(Color.GRAY);
            }
            tenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    elementsTextView.setText(tenButton.getText());
                }
            });

            Button submitButton = popupLayout.findViewById(R.id.submitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    popupWindowIsShowing = false;
                    FormBody formBody = new FormBody.Builder()
                            .add("Elements", elementsTextView.getText().toString())
                            .build();
                    String ipAddress = SP.getString("ipAddress", "10.0.0.11");
                    String url = "http://" + ipAddress + ":1337/confirmElements";

                    NetworkUtils.performPostRequestWithRetry(url, formBody);
                }
            });
        }

        public void closePopupWindow() {
            popupWindow.dismiss();
        }
    }

    public class SignOffPopUpClass {

        PopupWindow popupWindow = null;
        Boolean popupWindowIsShowing = false;

        public void showPopupWindow(final View view, String signOffCategory, String signOffRound) {

            LayoutInflater inflater = getLayoutInflater();
            final View popupLayout = inflater.inflate(R.layout.results_sign_off_layout, (ViewGroup) findViewById(R.id.results_sign_off_layout));
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            popupWindow = new PopupWindow(popupLayout, width - 100, LinearLayout.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            popupWindowIsShowing = true;

            Button submitButton = popupLayout.findViewById(R.id.submitButton);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                    popupWindowIsShowing = false;
                    SignaturePad signaturePad = popupLayout.findViewById(R.id.signature_pad);
                    Bitmap signatureBitmap = signaturePad.getSignatureBitmap();
                    sendSignature(signatureBitmap, signOffCategory, signOffRound);
                }
            });
        }

        private void sendSignature(Bitmap signatureBitmap, String signOffCategory, String signOffRound) {
            // Convert the Bitmap to a byte array or other suitable format
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] signatureBytes = byteArrayOutputStream.toByteArray();
            String base64String = Base64.encodeToString(signatureBytes, Base64.DEFAULT);

            FormBody formBody = new FormBody.Builder()
                    .add("SignatureBytesBase64", base64String)
                    .add("SignOffCategory", signOffCategory)
                    .add("SignOffRound", signOffRound)
                    .build();
            String ipAddress = SP.getString("ipAddress", "10.0.0.11");
            String url = "http://" + ipAddress + ":1337/resultsSignOff";

            NetworkUtils.performPostRequestWithRetry(url, formBody);

        }

        public void closePopupWindow() {
            popupWindow.dismiss();
        }
    }

}







