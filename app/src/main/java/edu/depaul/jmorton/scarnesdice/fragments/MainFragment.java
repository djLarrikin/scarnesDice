package edu.depaul.jmorton.scarnesdice.fragments;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import edu.depaul.jmorton.scarnesdice.R;
import edu.depaul.jmorton.scarnesdice.databinding.FragmentMainBinding;
import edu.depaul.jmorton.scarnesdice.models.DiceModel;

public class MainFragment extends Fragment {

    private String TAG = "MainFragment";

    private final int WINNING_SCORE_THRESHOLD = 100;
    private final int COMPUTER_TURN_DELAY = 500; //milliseconds
    private final int ANIMATION_LENGTH = COMPUTER_TURN_DELAY - 300; //milliseconds

    private int playersTotalScore = 0;
    private int computersTotalScore = 0;
    private int playersTurnScore = 0;
    private int computersTurnScore = 0;
    private final Handler handler = new Handler();
    private Runnable computerTurnRunnable;

    private boolean isPlayersTurn = true;

    private final int DICE_NUMBER_OF_SIDES = 6;
    private DiceModel dice;

    private FragmentMainBinding binding;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dice = new DiceModel(DICE_NUMBER_OF_SIDES);

        computerTurnRunnable = new Runnable() {
            @Override
            public void run() {
                computersTurn();
            }
        };

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        View view = binding.getRoot();

        setDiceImage();
        binding.computersScoreTextView.setText("" + computersTotalScore);
        binding.playersScoreTextView.setText("" + playersTotalScore);

        Button rollButton = binding.rollButton;
        rollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollDice();
                playersTurn();
            }
        });

        Button holdButton = binding.holdButton;
        holdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playersTotalScore = playersTotalScore + playersTurnScore;
                playersTurnScore = 0;
                binding.playersScoreTextView.setText("" + playersTotalScore);
                checkForWinner();
                computersTurn();
            }
        });

        Button resetButton = binding.resetButton;
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playersTotalScore = 0;
                computersTotalScore = 0;
                playersTurnScore = 0;
                computersTurnScore = 0;
                binding.playersScoreTextView.setText("" + playersTotalScore);
                binding.computersScoreTextView.setText("" + computersTotalScore);

                isPlayersTurn = true;
                disableEnablePlayersControl();
            }
        });

        return view;
    }


    private void rollDice() {
        dice.roll();
        setDiceImage();
    }

    private void setDiceImage() {
        final ImageView diceImageView = binding.diceImageView;

        AnimatorSet tossAnimatorSet = new AnimatorSet();
        ValueAnimator rotateXAnim = ObjectAnimator.ofFloat(diceImageView, "rotationX", 0f, 360f);
        rotateXAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        ValueAnimator rotateYAnim = ObjectAnimator.ofFloat(diceImageView, "rotationY", 0f, 360f);
        rotateYAnim.setInterpolator(new AccelerateDecelerateInterpolator());


        tossAnimatorSet.playTogether(rotateXAnim, rotateYAnim);
        tossAnimatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                String imageFileName = "dice";
                int diceFace = dice.getDiceFace();
                Log.d(TAG, "currently setting die to: " + diceFace);
                int diceResId = getResources().getIdentifier(imageFileName + diceFace, "drawable", getActivity().getPackageName());
                diceImageView.setImageResource(diceResId);

                binding.holdButton.setEnabled(false);
                binding.rollButton.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                disableEnablePlayersControl();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        tossAnimatorSet.setDuration(ANIMATION_LENGTH);
        tossAnimatorSet.start();

    }

    private void checkForWinner() {
        if (playersTotalScore + playersTurnScore >= WINNING_SCORE_THRESHOLD) {
            Toast.makeText(getActivity(), "You win!", Toast.LENGTH_SHORT).show();
            isPlayersTurn = false;
            disableEnablePlayersControl();
        } else if (computersTotalScore + computersTurnScore >= WINNING_SCORE_THRESHOLD) {
            Toast.makeText(getActivity(), "You LOSE!", Toast.LENGTH_SHORT).show();
            isPlayersTurn = false;
            disableEnablePlayersControl();
        }
    }

    private void playersTurn() {
        if (dice.getDiceFace() != 1) {
            playersTurnScore = playersTurnScore + dice.getDiceFace();
        } else {
            playersTurnScore = 0;
            handler.postDelayed(computerTurnRunnable, COMPUTER_TURN_DELAY);
        }
    }

    private void disableEnablePlayersControl() {
        binding.holdButton.setEnabled(isPlayersTurn);
        binding.rollButton.setEnabled(isPlayersTurn);
        if (isPlayersTurn) {
            binding.playersScoreLabelTextView.setTypeface(Typeface.DEFAULT_BOLD);
            binding.computersScoreLabelTextView.setTypeface(Typeface.DEFAULT);
        } else {
            binding.playersScoreLabelTextView.setTypeface(Typeface.DEFAULT);
            binding.computersScoreLabelTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }

    }

    private void computersTurn() {
        rollDice();
        if (dice.getDiceFace() != 1) {
            isPlayersTurn = false;
            disableEnablePlayersControl();
            computersTurnScore += dice.getDiceFace();

            if (computersTurnScore < 20) {
                handler.postDelayed(computerTurnRunnable, COMPUTER_TURN_DELAY);
            } else {
                computersTotalScore += computersTurnScore;
                computersTurnScore = 0;
                binding.computersScoreTextView.setText("" + computersTotalScore);
                checkForWinner();
                isPlayersTurn = true;
            }
        } else {
            isPlayersTurn = true;
        }

        disableEnablePlayersControl();
    }

}
