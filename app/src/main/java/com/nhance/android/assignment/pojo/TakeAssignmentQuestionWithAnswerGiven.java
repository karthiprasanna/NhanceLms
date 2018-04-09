package com.nhance.android.assignment.pojo;

import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.QuestionType;

import java.io.Serializable;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class TakeAssignmentQuestionWithAnswerGiven implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public enum AssignementAttemptStatus {
        RESET, SKIP, REVIEW, SAVED;
    }

    public QuestionType type;
    public String qId;                // _id corresponding to question table
    public int            orgKeyId;
    public String answerGiven;
    public String correctMatrixAnswer;
    public String correctAnswer;
    public boolean        correct;
    // status will only be used while take assignment
    private AssignementAttemptStatus status;
    private int           score;
    private boolean       analysed;
    private float           timeTaken;          // seconds taken to solve this question;
    private int           maxMarks;
    private int           qusNo;
    private boolean       synced;

    public TakeAssignmentQuestionWithAnswerGiven() {

    }

    public TakeAssignmentQuestionWithAnswerGiven(int orgKeyId, String qId, String answerGiven,
                                                 String correctAnswer, boolean correct) {

        this.orgKeyId = orgKeyId;
        this.qId = qId;
        this.answerGiven = answerGiven;
        this.correctAnswer = correctAnswer;
        this.correct = correct;
        this.status = AssignementAttemptStatus.RESET;
    }

    public AssignementAttemptStatus getStatus() {

        return status;
    }

    public void setStatus(AssignementAttemptStatus status) {

        this.status = status;
    }

    public int getScore() {

        return score;
    }

    public void setScore(int score) {

        this.score = score;
    }

    public float getTimeTaken() {

        return timeTaken;
    }

    public void setTimeTaken(float timeTaken) {

        this.timeTaken = timeTaken;
    }

    public boolean isAnalysed() {

        return analysed;
    }

    public void setAnalysed(boolean analysed) {

        this.analysed = analysed;
    }

    public int getMaxMarks() {

        return maxMarks;
    }

    public void setMaxMarks(int maxMarks) {

        this.maxMarks = maxMarks;
    }

    public int getQusNo() {

        return qusNo;
    }

    public void setQusNo(int qusNo) {

        this.qusNo = qusNo;
    }

    public boolean isSynced() {

        return synced;
    }

    public void setSynced(boolean synced) {

        this.synced = synced;
    }

    public Question getQuestion(String userId, ContentDataManager cDataManager) {

        return cDataManager.getQuestion(qId, userId, orgKeyId);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{qId:").append(qId).append(", qusNo:").append(qusNo)
                .append(", answerGiven:").append(answerGiven).append(", correctAnswer:")
                .append(correctAnswer).append(", correct:").append(correct).append(", status:")
                .append(status).append("}");
        return builder.toString();
    }

}
