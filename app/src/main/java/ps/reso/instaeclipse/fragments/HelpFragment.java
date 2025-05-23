package ps.reso.instaeclipse.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;

import ps.reso.instaeclipse.R;

public class HelpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout once
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // Find the GitHub button
        MaterialCardView githubCard = view.findViewById(R.id.github_card);

        // Find the Telegram button
        MaterialCardView telegramCard = view.findViewById(R.id.telegram_card);


        // Set the click listener for the GitHub button
        githubCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ReSo7200/InstaEclipse"));
            startActivity(intent);
        });

        // Set the click listener for the Telegram button
        telegramCard.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/InstaEclipse"));
            startActivity(intent);
        });

        return view;
    }
}
