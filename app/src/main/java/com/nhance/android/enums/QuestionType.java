package com.nhance.android.enums;

import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.utils.SQLDBUtil;

public enum QuestionType {
    SCQ {

        @Override
        public String descText() {

            return "Single Choice Question";
        }

        @Override
        public boolean isCorrect(String correctAnswer, String answerGiven) {

            boolean isCorrect = false;
            try {
                isCorrect = correctAnswer.trim().equalsIgnoreCase(answerGiven.trim());
            } catch (Exception e) {}
            return isCorrect;
        }
    },
    MCQ {

        @Override
        public String descText() {

            return "Multiple Choice Question";
        }

        @Override
        public boolean isCorrect(String correctAnswer, String answerGiven) {

            boolean isCorrect = false;
            try {
                List<String> correctAns = Arrays.asList(TextUtils.split(correctAnswer,
                        SQLDBUtil.SEPARATOR));
                List<String> ansGiven = Arrays.asList(TextUtils.split(answerGiven,
                        SQLDBUtil.SEPARATOR));
                int correctCount = 0;
                for (String ansGvn : ansGiven) {
                    isCorrect = correctAns.contains(ansGvn);
                    if (!isCorrect) {
                        break;
                    }
                    correctCount++;
                }
                isCorrect = isCorrect && correctAns.size() == correctCount;

            } catch (Exception e) {}
            return isCorrect;
        }
    },
    NUMERIC {

        @Override
        public String descText() {

            return "Numerical Question";
        }

        @Override
        public boolean isCorrect(String correctAnswer, String answerGiven) {

            try {
                Log.d(this.getClass().getSimpleName(),
                        correctAnswer.length() - correctAnswer.indexOf(".") + "");
                int correctNumChars = correctAnswer.length() - correctAnswer.indexOf(".");
                int answeredNumChars = answerGiven.length() - answerGiven.indexOf(".");
                int numChars = 0;
                if (correctAnswer.contains(".") && answerGiven.contains(".")) {
                    numChars = Math.max(answeredNumChars, correctNumChars) - 1;
                    if (answeredNumChars == correctNumChars) {
                        numChars = answeredNumChars;
                    }
                } else if (!correctAnswer.contains(".") && answerGiven.contains(".")) {
                    numChars = answeredNumChars - 1;
                } else if (correctAnswer.contains(".") && !answerGiven.contains(".")) {
                    numChars = correctNumChars - 1;
                } else if (!correctAnswer.contains(".") && !answerGiven.contains(".")) {
                    numChars = 1;
                }

                Double dCorrect = Double.parseDouble(correctAnswer);
                Double dAnswered = Double.parseDouble(answerGiven);
                Log.d(this.getClass().getSimpleName(), "dcorrect: " + dCorrect + " dAnswered: "
                        + dAnswered);
                if (dCorrect == dAnswered) {
                    return true;
                }
                if ((Math.signum(dCorrect) * dCorrect) > (Math.signum(dAnswered) * dAnswered)) {
                    return false;
                }
                Double tolerance = Double.valueOf(1)
                        / Math.pow(Double.valueOf(10), Math.max(numChars, 1));
                Log.d(this.getClass().getSimpleName(), "tolerance: " + tolerance);
                Log.d(this.getClass().getSimpleName(), "dAnswered-dCorrect: "
                        + (dAnswered - dCorrect - tolerance));
                if (Math.abs(dAnswered) - Math.abs(dCorrect) - tolerance <= Double.valueOf(1)
                        / Math.pow(Double.valueOf(10), 9)) {
                    return true;
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage(), e);
            }
            return false;
        }
    },
    MATRIX {

        @Override
        public String descText() {

            return "Matrix Match Question";
        }

        @Override
        public boolean isCorrect(String correctAnswer, String answerGiven) {

            boolean isCorrect = false;
            try {
                isCorrect = correctAnswer.trim().equalsIgnoreCase(answerGiven.trim());
            } catch (Exception e) {}
            return isCorrect;
        }
    };

    public abstract String descText();

    public abstract boolean isCorrect(String correctAnswer, String answerGiven);

    @SuppressLint("DefaultLocale")
    public static QuestionType valueOfKey(String key) {

        QuestionType qType = null;
        try {
            qType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return qType;
    }
}
