package client.gui;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;


public class LocaleManager {
    public enum SupportedLocale {
        RU("Русский", new Locale("ru")),
        EN("English (UK)", new Locale("en")),
        TR("Türkçe", new Locale("tr")),
        LT("Lietuvių", new Locale("lt"));

        public final String displayName;
        public final Locale locale;

        SupportedLocale(String displayName, Locale locale) {
            this.displayName = displayName;
            this.locale = locale;
        }
    }

    private static LocaleManager instance;
    private SupportedLocale currentLocale = SupportedLocale.RU;
    private ResourceBundle bundle;
    private final List<Runnable> changeListeners = new ArrayList<>();

    private LocaleManager() {loadBundle();}

    public static LocaleManager getInstance() {
        if (instance == null) instance = new LocaleManager();
        return instance;
    }

    private void loadBundle(){
        String suffix = switch (currentLocale) {
            case RU -> "ru";
            case EN -> "en";
            case TR -> "tr";
            case LT -> "lt";
        };
        try (var is = getClass().getClassLoader().getResourceAsStream("messages_" + suffix + ".properties")) {
            if (is != null) {
                bundle = new PropertyResourceBundle(new InputStreamReader(is, StandardCharsets.UTF_8));
            } else {
                bundle = ResourceBundle.getBundle("messages", currentLocale.locale);
            }
        } catch (Exception e) {
            bundle = new ListResourceBundle() {
                @Override
                protected Object[][] getContents() { return new Object[0][]; }
            };
        }
    }
    public void setLocale(SupportedLocale locale){
        this.currentLocale = locale;
        loadBundle();
        changeListeners.forEach(Runnable::run);
    }

    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }
    public SupportedLocale getCurrentLocale() {
        return currentLocale;
    }
    public Locale getJavaLocale() {
        return currentLocale.locale;
    }
    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
    public String formatNumber(Number number) {
        if (number == null) return "";
        return NumberFormat.getInstance(getJavaLocale()).format(number);
    }

    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getJavaLocale());
        return dateTime.format(fmt);
    }

    public String formatDateShort(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(getJavaLocale());
        return dateTime.format(fmt);
    }

    public String formatPrice(Long price) {
        if (price == null) return "";
        NumberFormat nf = NumberFormat.getCurrencyInstance(getJavaLocale());
        return nf.format(price);
    }

}
