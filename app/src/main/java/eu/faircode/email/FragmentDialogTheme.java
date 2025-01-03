package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2025 by Marcel Bokhorst (M66B)
*/

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class FragmentDialogTheme extends FragmentDialogBase {
    private RadioGroup rgTheme;
    private SwitchCompat swReverse;
    private RadioButton rbThemeYou;
    private RadioButton rbThemeYouMono;
    private TextView tvYou;
    private RadioGroup rgThemeOptions;
    private TextView tvSystem;
    private SwitchCompat swBlack;
    private SwitchCompat swHtmlLight;
    private SwitchCompat swComposerLight;
    private Button btnMore;
    private TextView tvMore;
    private Group grpDebug;

    private void eval() {
        int checkedId = rgTheme.getCheckedRadioButtonId();
        boolean grey = (checkedId == R.id.rbThemeGrey);
        boolean solarized = (checkedId == R.id.rbThemeSolarized);
        boolean blank = (checkedId == R.id.rbThemeBlank);
        boolean bw = (checkedId == R.id.rbThemeBlackOrWhite);
        boolean mono = (checkedId == R.id.rbThemeYouMono);
        boolean you = (checkedId == R.id.rbThemeYou || mono);
        boolean colored = (grey || bw || solarized || you ||
                checkedId == R.id.rbThemeBlueOrange ||
                checkedId == R.id.rbThemeRedGreen ||
                checkedId == R.id.rbThemeYellowPurple);
        int optionId = rgThemeOptions.getCheckedRadioButtonId();

        swReverse.setEnabled(colored && !grey && !solarized && !bw && !mono);

        rgThemeOptions.setEnabled(colored);
        for (int i = 0; i < rgThemeOptions.getChildCount(); i++)
            rgThemeOptions.getChildAt(i).setEnabled(colored);

        tvSystem.setEnabled(colored && optionId == R.id.rbThemeSystem);

        swBlack.setEnabled(colored && !grey && !bw && !solarized && optionId != R.id.rbThemeLight);

        swHtmlLight.setEnabled(colored ? optionId != R.id.rbThemeLight : !blank);
        swComposerLight.setEnabled(colored ? optionId != R.id.rbThemeLight : !blank);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean settings = (args != null && args.getBoolean("settings"));

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString("theme", "blue_orange_system");
        boolean default_light = prefs.getBoolean("default_light", false);
        boolean composer_light = prefs.getBoolean("composer_light", false);
        boolean debug = prefs.getBoolean("debug", false);

        View dview = LayoutInflater.from(context).inflate(R.layout.dialog_theme, null);
        rgTheme = dview.findViewById(R.id.rgTheme);
        rbThemeYou = dview.findViewById(R.id.rbThemeYou);
        rbThemeYouMono = dview.findViewById(R.id.rbThemeYouMono);
        tvYou = dview.findViewById(R.id.tvYou);
        swReverse = dview.findViewById(R.id.swReverse);
        rgThemeOptions = dview.findViewById(R.id.rgThemeOptions);
        tvSystem = dview.findViewById(R.id.tvSystem);
        swBlack = dview.findViewById(R.id.swBlack);
        swHtmlLight = dview.findViewById(R.id.swHtmlLight);
        swComposerLight = dview.findViewById(R.id.swComposerLight);
        btnMore = dview.findViewById(R.id.btnMore);
        tvMore = dview.findViewById(R.id.tvMore);
        grpDebug = dview.findViewById(R.id.grpDebug);

        rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                eval();
            }
        });

        rbThemeYou.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        rbThemeYouMono.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);
        tvYou.setVisibility(Build.VERSION.SDK_INT < Build.VERSION_CODES.S ? View.GONE : View.VISIBLE);

        tvYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
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
                (theme.contains("reversed") ||
                        theme.startsWith("orange_blue") ||
                        theme.startsWith("purple_yellow") ||
                        theme.startsWith("green_red"));
        boolean dark = (theme.endsWith("dark") || theme.equals("black"));
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
        swHtmlLight.setChecked(default_light);
        swComposerLight.setChecked(composer_light);

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

            case "grey_system":
            case "grey_light":
            case "grey_dark":
                rgTheme.check(R.id.rbThemeGrey);
                break;

            case "solarized":
            case "solarized_light":
            case "solarized_dark":
            case "solarized_system":
                rgTheme.check(R.id.rbThemeSolarized);
                break;

            case "blank":
                rgTheme.check(R.id.rbThemeBlank);
                break;

            case "black":
            case "white":
            case "bw_system":
                rgTheme.check(R.id.rbThemeBlackOrWhite);
                break;
            case "black_and_white":
                rgTheme.check(R.id.rbThemeBlackAndWhite);
                break;

            case "you_light":
            case "you_dark":
            case "you_black":
            case "you_system":
            case "you_system_black":
            case "you_reversed_light":
            case "you_reversed_dark":
            case "you_reversed_black":
            case "you_reversed_system":
            case "you_reversed_system_black":
                rgTheme.check(R.id.rbThemeYou);
                break;

            case "you_mono_light":
            case "you_mono_dark":
            case "you_mono_black":
            case "you_mono_system":
            case "you_mono_system_black":
            case "you_mono_reversed_light":
            case "you_mono_reversed_dark":
            case "you_mono_reversed_black":
            case "you_mono_reversed_system":
            case "you_mono_reversed_system_black":
                rgTheme.check(R.id.rbThemeYouMono);
                break;
        }

        tvMore.setPaintFlags(tvMore.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Helper.viewFAQ(v.getContext(), 164);
            }
        });

        btnMore.setVisibility(settings ? View.GONE : View.VISIBLE);
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                v.getContext().startActivity(new Intent(v.getContext(), ActivitySetup.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .putExtra("tab", "display"));
            }
        });

        if (grpDebug != null)
            grpDebug.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

        return new AlertDialog.Builder(context)
                .setView(dview)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getIntent().putExtra("tab", "display");

                        int optionId = rgThemeOptions.getCheckedRadioButtonId();
                        boolean reverse = (swReverse.isEnabled() && swReverse.isChecked());
                        boolean dark = (rgThemeOptions.isEnabled() && optionId == R.id.rbThemeDark);
                        boolean system = (rgThemeOptions.isEnabled() && optionId == R.id.rbThemeSystem);
                        boolean black = (swBlack.isEnabled() && swBlack.isChecked());

                        SharedPreferences.Editor editor = prefs.edit();

                        editor.remove("highlight_color");

                        int checkedRadioButtonId = rgTheme.getCheckedRadioButtonId();
                        if (checkedRadioButtonId == R.id.rbThemeBlueOrange) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "orange_blue_system" : "blue_orange_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "orange_blue" : "blue_orange") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeRedGreen) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "green_red_system" : "red_green_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "green_red" : "red_green") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeYellowPurple) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "purple_yellow_system" : "yellow_purple_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "purple_yellow" : "yellow_purple") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeGrey) {
                            if (system)
                                editor.putString("theme", "grey_system").apply();
                            else
                                editor.putString("theme",
                                        "grey" + (dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeSolarized) {
                            if (system)
                                editor.putString("theme", "solarized_system").apply();
                            else
                                editor.putString("theme",
                                        "solarized" + (dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeBlank)
                            editor.putString("theme", "blank").apply();
                        else if (checkedRadioButtonId == R.id.rbThemeBlackOrWhite) {
                            if (system)
                                editor.putString("theme", "bw_system").apply();
                            else
                                editor.putString("theme", (dark ? "black" : "white")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeBlackAndWhite) {
                            editor.putString("theme", "black_and_white").apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeYou) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "you_reversed_system" : "you_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "you_reversed" : "you") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        } else if (checkedRadioButtonId == R.id.rbThemeYouMono) {
                            if (system)
                                editor.putString("theme",
                                        (reverse ? "you_mono_reversed_system" : "you_mono_system") +
                                                (black ? "_black" : "")).apply();
                            else
                                editor.putString("theme",
                                        (reverse ? "you_mono_reversed" : "you_mono") +
                                                (black ? "_black" : dark ? "_dark" : "_light")).apply();
                        }

                        editor.putBoolean("default_light", swHtmlLight.isChecked());
                        editor.putBoolean("composer_light", swComposerLight.isChecked());

                        editor.apply();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    static int getTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString("theme", "blue_orange_system");
        boolean composer_light = prefs.getBoolean("composer_light", false);

        boolean night = Helper.isNight(context);
        boolean light = (composer_light && context instanceof ActivityCompose);
        if (light)
            night = false;
        EntityLog.log(context, "Activity theme=" + theme + " light=" + light + " night=" + night);

        switch (theme) {
            // Light
            case "light":
            case "blue_orange_light":
                return R.style.AppThemeBlueOrangeLight;
            case "orange_blue_light":
                return R.style.AppThemeOrangeBlueLight;

            case "yellow_purple_light":
                return R.style.AppThemeYellowPurpleLight;
            case "purple_yellow_light":
                return R.style.AppThemePurpleYellowLight;

            case "red_green_light":
                return R.style.AppThemeRedGreenLight;
            case "green_red_light":
                return R.style.AppThemeGreenRedLight;

            // Dark
            case "dark":
            case "blue_orange_dark":
                if (light)
                    return R.style.AppThemeBlueOrangeLight;
                else
                    return R.style.AppThemeBlueOrangeDark;
            case "orange_blue_dark":
                if (light)
                    return R.style.AppThemeOrangeBlueLight;
                else
                    return R.style.AppThemeOrangeBlueDark;

            case "yellow_purple_dark":
                if (light)
                    return R.style.AppThemeYellowPurpleLight;
                else
                    return R.style.AppThemeYellowPurpleDark;
            case "purple_yellow_dark":
                if (light)
                    return R.style.AppThemePurpleYellowLight;
                else
                    return R.style.AppThemePurpleYellowDark;

            case "red_green_dark":
                if (light)
                    return R.style.AppThemeRedGreenLight;
                else
                    return R.style.AppThemeRedGreenDark;
            case "green_red_dark":
                if (light)
                    return R.style.AppThemeGreenRedLight;
                else
                    return R.style.AppThemeGreenRedDark;

                // Black
            case "blue_orange_black":
                if (light)
                    return R.style.AppThemeBlueOrangeLight;
                else
                    return R.style.AppThemeBlueOrangeBlack;
            case "orange_blue_black":
                if (light)
                    return R.style.AppThemeOrangeBlueLight;
                else
                    return R.style.AppThemeOrangeBlueBlack;
            case "yellow_purple_black":
                if (light)
                    return R.style.AppThemeYellowPurpleLight;
                else
                    return R.style.AppThemeYellowPurpleBlack;
            case "purple_yellow_black":
                if (light)
                    return R.style.AppThemePurpleYellowLight;
                else
                    return R.style.AppThemePurpleYellowBlack;
            case "red_green_black":
                if (light)
                    return R.style.AppThemeRedGreenLight;
                else
                    return R.style.AppThemeRedGreenBlack;
            case "green_red_black":
                if (light)
                    return R.style.AppThemeGreenRedLight;
                else
                    return R.style.AppThemeGreenRedBlack;

                // Grey
            case "grey_light":
                return R.style.AppThemeGreySteelBlueLight;
            case "grey_dark":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeGreySteelBlueDark;

                // Solarized
            case "solarized_light":
                return R.style.AppThemeSolarizedLight;
            case "solarized":
            case "solarized_dark":
                if (light)
                    return R.style.AppThemeSolarizedLight;
                else
                    return R.style.AppThemeSolarizedDark;

                // Black
            case "blank":
                return R.style.AppThemeBlank;

            case "black":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeBlack;

            case "white":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeWhite;

            case "black_and_white":
                if (light)
                    return R.style.AppThemeGreySteelBlueLight;
                else
                    return R.style.AppThemeBlackAndWhite;

                // System
            case "system":
            case "blue_orange_system":
                return (night
                        ? R.style.AppThemeBlueOrangeDark : R.style.AppThemeBlueOrangeLight);
            case "blue_orange_system_black":
                return (night
                        ? R.style.AppThemeBlueOrangeBlack : R.style.AppThemeBlueOrangeLight);
            case "orange_blue_system":
                return (night
                        ? R.style.AppThemeOrangeBlueDark : R.style.AppThemeOrangeBlueLight);
            case "orange_blue_system_black":
                return (night
                        ? R.style.AppThemeOrangeBlueBlack : R.style.AppThemeOrangeBlueLight);
            case "yellow_purple_system":
                return (night
                        ? R.style.AppThemeYellowPurpleDark : R.style.AppThemeYellowPurpleLight);
            case "yellow_purple_system_black":
                return (night
                        ? R.style.AppThemeYellowPurpleBlack : R.style.AppThemeYellowPurpleLight);
            case "purple_yellow_system":
                return (night
                        ? R.style.AppThemePurpleYellowDark : R.style.AppThemePurpleYellowLight);
            case "purple_yellow_system_black":
                return (night
                        ? R.style.AppThemePurpleYellowBlack : R.style.AppThemePurpleYellowLight);
            case "red_green_system":
                return (night
                        ? R.style.AppThemeRedGreenDark : R.style.AppThemeRedGreenLight);
            case "red_green_system_black":
                return (night
                        ? R.style.AppThemeRedGreenBlack : R.style.AppThemeRedGreenLight);
            case "green_red_system":
                return (night
                        ? R.style.AppThemeGreenRedDark : R.style.AppThemeGreenRedLight);
            case "green_red_system_black":
                return (night
                        ? R.style.AppThemeGreenRedBlack : R.style.AppThemeGreenRedLight);
            case "grey_system":
                return (night
                        ? R.style.AppThemeGreySteelBlueDark : R.style.AppThemeGreySteelBlueLight);
            case "solarized_system":
                return (night
                        ? R.style.AppThemeSolarizedDark : R.style.AppThemeSolarizedLight);
            case "bw_system":
                return (night
                        ? R.style.AppThemeBlack : R.style.AppThemeWhite);

            // Material You

            case "you_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouLight;
            case "you_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouLight : R.style.AppThemeYouDark);
            case "you_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouLight : R.style.AppThemeYouBlack);
            case "you_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouDark : R.style.AppThemeYouLight);
            case "you_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouBlack : R.style.AppThemeYouLight);

            case "you_reversed_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouReversedLight;
            case "you_reversed_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouReversedLight : R.style.AppThemeYouReversedDark);
            case "you_reversed_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouReversedLight : R.style.AppThemeYouReversedBlack);
            case "you_reversed_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouReversedDark : R.style.AppThemeYouReversedLight);
            case "you_reversed_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouReversedBlack : R.style.AppThemeYouReversedLight);

                // Material You monochrome

            case "you_mono_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouMonoLight;
            case "you_mono_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoLight : R.style.AppThemeYouMonoDark);
            case "you_mono_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoLight : R.style.AppThemeYouMonoBlack);
            case "you_mono_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoDark : R.style.AppThemeYouMonoLight);
            case "you_mono_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoBlack : R.style.AppThemeYouMonoLight);

            case "you_mono_reversed_light":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return R.style.AppThemeYouMonoReversedLight;
            case "you_mono_reversed_dark":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoReversedLight : R.style.AppThemeYouMonoReversedDark);
            case "you_mono_reversed_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (light ? R.style.AppThemeYouMonoReversedLight : R.style.AppThemeYouMonoReversedBlack);
            case "you_mono_reversed_system":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoReversedDark : R.style.AppThemeYouMonoReversedLight);
            case "you_mono_reversed_system_black":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                    return (night ? R.style.AppThemeYouMonoReversedBlack : R.style.AppThemeYouMonoReversedLight);

            default:
                if (!theme.startsWith("you_") && !theme.startsWith("you_mono_"))
                    Log.e("Unknown theme=" + theme);
                return R.style.AppThemeBlueOrangeLight;
        }
    }

    static void setBackground(Context context, View view, boolean compose) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean cards = prefs.getBoolean("cards", true);
        boolean beige = prefs.getBoolean("beige", true);
        boolean tabular_card_bg = prefs.getBoolean("tabular_card_bg", false);
        String theme = prefs.getString("theme", "blue_orange_system");
        boolean dark = Helper.isDarkTheme(context);
        boolean black = (theme.endsWith("black") || "black_and_white".equals(theme));
        boolean solarized = theme.startsWith("solarized");
        boolean you = theme.startsWith("you_");

        Integer color = null;
        if (cards) {
            if (you && !black && Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (!dark && beige)
                    color = ContextCompat.getColor(context, R.color.lightColorBackground_cards_beige);
                else
                    color = ContextCompat.getColor(context, dark
                            ? android.R.color.system_background_dark
                            : android.R.color.system_background_light);
            } else {
                if (compose) {
                    if (!dark || solarized)
                        color = Helper.resolveColor(context, R.attr.colorCardBackground);
                } else {
                    if (!dark && !solarized)
                        color = ContextCompat.getColor(context, beige
                                ? R.color.lightColorBackground_cards_beige
                                : R.color.lightColorBackground_cards);
                }
            }
        } else {
            if (tabular_card_bg)
                color = Helper.resolveColor(context, R.attr.colorCardBackground);
        }

        if (color == null)
            if (dark && black)
                color = Color.BLACK;
            else {
                TypedValue a = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
                if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT)
                    color = a.data;
                else
                    color = Color.parseColor(dark ? "#121316" : "#FAF9FD");
            }

        view.setBackgroundColor(color);
    }
}
