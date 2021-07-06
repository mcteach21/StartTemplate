package mc.apps.rxandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;

public class StartActivity extends AppCompatActivity {
    ImageView logo;
    ConstraintLayout root;
    //val TAG = "network"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_begin);

        logo = findViewById(R.id.logo);
        root = findViewById(R.id.root);

        getSupportActionBar().hide();
        startAnimation();

        logo.setOnClickListener((e)->open());
    }
    private void startAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        ValueAnimator fadeAnim = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f);
        fadeAnim.setDuration(500);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animateLogo();
            }
        });

        animatorSet.play(fadeAnim);
        animatorSet.start();
    }

    private void animateLogo() {
        ConstraintSet finishingConstraintSet = new ConstraintSet();
        finishingConstraintSet.clone(this, R.layout.activity_start);
        Transition transition = new ChangeBounds();
        transition.setDuration(1000);
        transition.setInterpolator(new BounceInterpolator());

        transition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                open();
            }
        });

        TransitionManager.beginDelayedTransition(root, transition);
        finishingConstraintSet.applyTo(root);
    }

    private void open() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}