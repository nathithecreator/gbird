package com.example.gbird;

import android.app.Application;

public class UserSettings extends Application {
    public static final String PREFERENCES = "preferences";
    public static final String CUSTOM_THEME = "customTheme";
    public static final String LIGHT_THEME = "lightTheme";
    public static final String DARK_THEME = "darkTheme";
    public static final String UNITS_PREFERENCE = "unitsPreference";
    public static final String KILOMETERS = "kilometers";
    public static final String MILES = "miles";

    private String customTheme;
    private String unitsPreference;

    public String getCustomTheme() {
        return customTheme;
    }

    public void setCustomTheme(String customTheme) {
        this.customTheme = customTheme;
    }

    public String getUnitsPreference() {
        return unitsPreference;
    }

    public void setUnitsPreference(String unitsPreference) {
        this.unitsPreference = unitsPreference;
    }
}
