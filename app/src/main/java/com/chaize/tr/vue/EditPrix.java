package com.chaize.tr.vue;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.chaize.tr.R;
import com.chaize.tr.controleur.Controle;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Some note <br/>
 * <li>Always use locale US instead of default to make DecimalFormat work well in all language</li>
 */
public class EditPrix extends androidx.appcompat.widget.AppCompatEditText {
    private static String prefix = "€ ";
    private static final int MAX_DECIMAL = 2;
    private static final int MAX_INT = 3;

    public EditPrix(Context context) {
        this(context, null);
    }

    public EditPrix(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public EditPrix(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        this.setHint(prefix);
        this.setFilters(new InputFilter[]{new DigitsInputFilter(MAX_INT, MAX_DECIMAL, 1000)});
    }
    public Float getPrix() {
        String str = this.getText().toString();
        String cleanString = str.replaceAll("[^0-9?!\\.]", "");
        Float prix = new Float(cleanString);
        return prix;
    }

    public class DigitsInputFilter implements InputFilter {

        private final String DOT = ".";

        private int mMaxIntegerDigitsLength;
        private int mMaxDigitsAfterLength;
        private double mMax;


        public DigitsInputFilter(int maxDigitsBeforeDot, int maxDigitsAfterDot, double maxValue) {
            mMaxIntegerDigitsLength = maxDigitsBeforeDot;
            mMaxDigitsAfterLength = maxDigitsAfterDot;
            mMax = maxValue;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String allText = getAllText(source, dest, dstart);
            String onlyDigitsText = getOnlyDigitsPart(allText);

            if (allText.isEmpty()) {
                return null;
            } else {
                double enteredValue;
                try {
                    enteredValue = Double.parseDouble(onlyDigitsText);
                } catch (NumberFormatException e) {
                    return "";
                }
                return checkMaxValueRule(enteredValue, onlyDigitsText);
            }
        }


        private CharSequence checkMaxValueRule(double enteredValue, String onlyDigitsText) {
            if (enteredValue > mMax) {
                return "";
            } else {
                return handleInputRules(onlyDigitsText);
            }
        }

        private CharSequence handleInputRules(String onlyDigitsText) {
            if (isDecimalDigit(onlyDigitsText)) {
                return checkRuleForDecimalDigits(onlyDigitsText);
            } else {
                return checkRuleForIntegerDigits(onlyDigitsText.length());
            }
        }

        private boolean isDecimalDigit(String onlyDigitsText) {
            return onlyDigitsText.contains(DOT);
        }

        private CharSequence checkRuleForDecimalDigits(String onlyDigitsPart) {
            String afterDotPart = onlyDigitsPart.substring(onlyDigitsPart.indexOf(DOT), onlyDigitsPart.length() - 1);
            if (afterDotPart.length() > mMaxDigitsAfterLength) {
                return "";
            }
            return null;
        }

        private CharSequence checkRuleForIntegerDigits(int allTextLength) {
            if (allTextLength > mMaxIntegerDigitsLength) {
                return "";
            }
            return null;
        }

        private String getOnlyDigitsPart(String text) {
            return text.replaceAll("[^0-9?!\\.]", "");
        }

        private String getAllText(CharSequence source, Spanned dest, int dstart) {
            String allText = "";
            if (!dest.toString().isEmpty()) {
                if (source.toString().isEmpty()) {
                    allText = deleteCharAtIndex(dest, dstart);
                } else {
                    allText = new StringBuilder(dest).insert(dstart, source).toString();
                }
            }
            return allText;
        }

        private String deleteCharAtIndex(Spanned dest, int dstart) {
            StringBuilder builder = new StringBuilder(dest);
            builder.deleteCharAt(dstart);
            return builder.toString();
        }
    }

}