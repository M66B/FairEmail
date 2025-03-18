package me.leolin.shortcutbadger.impl;

import java.util.ArrayList;
import java.util.List;

public class HuaweiHomeBadgerAlt extends HuaweiHomeBadger {
    @Override
    public List<String> getSupportLaunchers() {
        List<String> result = new ArrayList<>(super.getSupportLaunchers());
        result.add("com.hihonor.android.launcher");
        return result;
    }
}
