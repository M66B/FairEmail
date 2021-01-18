package eu.faircode.email;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.PreferenceManager;

public class FragmentDialogTheme extends FragmentDialogBase {
    private ImageButton itten;
    private RadioGroup rgTheme;
    private SwitchCompat swReverse;
    private RadioGroup rgThemeOptions;
    private SwitchCompat swBlack;
    private TextView tvSystem;

    private void eval() {
        int checkedId = rgTheme.getCheckedRadioButtonId();
        boolean grey = (checkedId == R.id.rbThemeGrey);
        boolean colored = (grey ||
                checkedId == R.id.rbThemeBlueOrange ||
                checkedId == R.id.rbThemeYellowPurple ||
                checkedId == R.id.rbThemeRedGreen);
        int optionId = rgThemeOptions.getCheckedRadioButtonId();

        swReverse.setEnabled(colored && !grey);

        rgThemeOptions.setEnabled(colored);
        for (int i = 0; i < rgThemeOptions.getChildCount(); i++)
            rgThemeOptions.getChildAt(i).setEnabled(colored);

        swBlack.setEnabled(colored && !grey && optionId != R.id.rbThemeLight);

        tvSystem.setEnabled(colored && optionId == R.id.rbThemeSystem);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dview = LayoutInflater.from(getContext()).inflate(R.layout.dialog_theme, null);
        itten = dview.findViewById(R.id.itten);
        rgTheme = dview.findViewById(R.id.rgTheme);
        swReverse = dview.findViewById(R.id.swReverse);
        rgThemeOptions = dview.findViewById(R.id.rgThemeOptions);
        swBlack = dview.findViewById(R.id.swBlack);
        tvSystem = dview.findViewById(R.id.tvSystem);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String theme = prefs.getString("theme", "blue_orange_system");

        itten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://en.wikipedia.org/wiki/Johannes_Itten");
                Helper.view(getContext(), uri, false);
            }
        });

        rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                eval();
            }
        });

        swReverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eval();
            }
        });

        rgThemeOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                eval();
            }
        });

        swBlack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                eval();
            }
        });

        boolean reversed =
                (theme.startsWith("orange_blue") ||
                        theme.startsWith("purple_yellow") ||
                        theme.startsWith("green_red"));
        boolean dark = theme.endsWith("dark");
        boolean system = (theme.endsWith("system") || theme.endsWith("system_black"));
        boolean black = (!"black".equals(theme) && theme.endsWith("black"));

        swReverse.setChecked(reversed);

        if (system)
            rgThemeOptions.check(R.id.rbThemeSystem);
        else if (dark || black)
            rgThemeOptions.check(R.id.rbThemeDark);
        else
            rgThemeOptions.check(R.id.rbThemeLight);

        swBlack.setChecked(black);

        switch (theme) {
            case "light":
            case "dark":
            case "system":
            case "blue_orange_system":
            case "blue_orange_system_black":
            case "blue_orange_light":
            case "blue_orange_dark":
            case "blue_orange_black":
            case "orange_blue_system":
            case "orange_blue_system_black":
            case "orange_blue_light":
            case "orange_blue_dark":
            case "orange_blue_black":
                rgTheme.check(R.id.rbThemeBlueOrange);
                break;
            case "yellow_purple_system":
            case "yellow_purple_system_black":
            case "yellow_purple_light":
            case "yellow_purple_dark":
            case "yellow_purple_black":
            case "purple_yellow_system":
            case "purple_yellow_system_black":
            case "purple_yellow_light":
            case "purple_yellow_dark":
            case "purple_yellow_black":
                rgTheme.check(R.id.rbThemeYellowPurple);
                break;
            case "red_green_system":
            case "red_green_system_black":
            case "red_green_light":
            case "red_green_dark":
            case "red_green_black":
            case "green_red_system":
            case "green_red_system_black":
            case "green_red_light":
            case "green_red_dark":
            case "green_red_black":
                rgTheme.check(R.id.rbThemeRedGreen);
                break;
            case "grey_system":
            case "grey_light":
            case "grey_dark":
                rgTheme.check(R.id.rbThemeGrey);
                break;

            case "black":
                rgTheme.check(R.id.rbThemeBlack);
                break;
            case "black_and_white":
                rgTheme.check(R.id.rbThemeBlackAndWhite);
                break;
        }

        return new AlertDialog.Builder(getContext())
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getIntent().putExtra("tab", "display");

                        ContactInfo.clearCache(getContext());

                        int optionId = rgThemeOptions.getCheckedRadioButtonId();
                        boolean reverse = (swReverse.isEnabled() && swReverse.isChecked());
                        boolean dark = (rgThemeOptions.isEnabled() && optionId == R.id.rbThemeDark);
                        boolean system = (rgThemeOptions.isEnabled() && optionId == R.id.rbThemeSystem);
                        boolean black = (swBlack.isEnabled() && swBlack.isChecked());

                        SharedPreferences.Editor editor = prefs.edit();

                        editor.remove("highlight_color");

                        switch (rgTheme.getCheckedRadioButtonId()) {
                            case R.id.rbThemeBlueOrange:
                                if (system)
                                    editor.putString("theme",
                                            (reverse ? "orange_blue_system" : "blue_orange_system") +
                                                    (black ? "_black" : "")).apply();
                                else
                                    editor.putString("theme",
                                            (reverse ? "orange_blue" : "blue_orange") +
                                                    (black ? "_black" : dark ? "_dark" : "_light")).apply();
                                break;
                            case R.id.rbThemeYellowPurple:
                                if (system)
                                    editor.putString("theme",
                                            (reverse ? "purple_yellow_system" : "yellow_purple_system") +
                                                    (black ? "_black" : "")).apply();
                                else
                                    editor.putString("theme",
                                            (reverse ? "purple_yellow" : "yellow_purple") +
                                                    (black ? "_black" : dark ? "_dark" : "_light")).apply();
                                break;
                            case R.id.rbThemeRedGreen:
                                if (system)
                                    editor.putString("theme",
                                            (reverse ? "green_red_system" : "red_green_system") +
                                                    (black ? "_black" : "")).apply();
                                else
                                    editor.putString("theme",
                                            (reverse ? "green_red" : "red_green") +
                                                    (black ? "_black" : dark ? "_dark" : "_light")).apply();
                                break;
                            case R.id.rbThemeGrey:
                                if (system)
                                    editor.putString("theme", "grey_system").apply();
                                else
                                    editor.putString("theme",
                                            "grey" + (dark ? "_dark" : "_light")).apply();
                                break;
                            case R.id.rbThemeBlack:
                                editor.putString("theme", "black").apply();
                                break;
                            case R.id.rbThemeBlackAndWhite:
                                editor.putString("theme", "black_and_white").apply();
                                break;
                        }

                        editor.apply();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
