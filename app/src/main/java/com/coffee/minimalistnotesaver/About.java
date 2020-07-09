package com.coffee.minimalistnotesaver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.coffee.minimalistnotesaver.databinding.AboutBinding;

import java.util.Objects;

public class About extends AppCompatActivity {

    AboutBinding aboutBinding;
    Animation rotate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aboutBinding = AboutBinding.inflate(getLayoutInflater());
        View view = aboutBinding.getRoot();
        setContentView(view);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        aboutBinding.appVersion.setText(getString(R.string.version, BuildConfig.VERSION_NAME));
        setOnClick();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnClick() {

        aboutBinding.gotoTNC.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), TermsAndConditions.class);
            startActivity(i);
        });

        aboutBinding.gotoPrPc.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), PrivacyPolicy.class);
            startActivity(i);
        });

        aboutBinding.sendFeedback.setOnClickListener(v -> {
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.mail_feedback_email)});
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_feedback_subject));
            intent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.mail_feedback_message));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(intent, getString(R.string.send_feedback)));
            } else {
                Toast.makeText(getApplicationContext(), "No Applications Found. If possible please send your feedback on the given email. ;)", Toast.LENGTH_LONG).show();
            }
        });

        aboutBinding.appIcon.setOnClickListener(v -> aboutBinding.appIcon.startAnimation(rotate));
    }
}