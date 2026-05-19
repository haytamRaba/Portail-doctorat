package com.doctorat.suividoctorat.dto;

public class ProgressDTO {
    private int articleCount;
    private int conferenceCount;
    private int totalTrainingHours;
    private boolean articlesRequirementMet;
    private boolean conferencesRequirementMet;
    private boolean trainingRequirementMet;
    private boolean readyForDefense;

    // Constructors
    public ProgressDTO() {}

    public ProgressDTO(int articleCount, int conferenceCount, int totalTrainingHours,
                       boolean articlesRequirementMet, boolean conferencesRequirementMet,
                       boolean trainingRequirementMet, boolean readyForDefense) {
        this.articleCount = articleCount;
        this.conferenceCount = conferenceCount;
        this.totalTrainingHours = totalTrainingHours;
        this.articlesRequirementMet = articlesRequirementMet;
        this.conferencesRequirementMet = conferencesRequirementMet;
        this.trainingRequirementMet = trainingRequirementMet;
        this.readyForDefense = readyForDefense;
    }

    // Getters and Setters
    public int getArticleCount() { return articleCount; }
    public void setArticleCount(int articleCount) { this.articleCount = articleCount; }

    public int getConferenceCount() { return conferenceCount; }
    public void setConferenceCount(int conferenceCount) { this.conferenceCount = conferenceCount; }

    public int getTotalTrainingHours() { return totalTrainingHours; }
    public void setTotalTrainingHours(int totalTrainingHours) { this.totalTrainingHours = totalTrainingHours; }

    public boolean isArticlesRequirementMet() { return articlesRequirementMet; }
    public void setArticlesRequirementMet(boolean articlesRequirementMet) { this.articlesRequirementMet = articlesRequirementMet; }

    public boolean isConferencesRequirementMet() { return conferencesRequirementMet; }
    public void setConferencesRequirementMet(boolean conferencesRequirementMet) { this.conferencesRequirementMet = conferencesRequirementMet; }

    public boolean isTrainingRequirementMet() { return trainingRequirementMet; }
    public void setTrainingRequirementMet(boolean trainingRequirementMet) { this.trainingRequirementMet = trainingRequirementMet; }

    public boolean isReadyForDefense() { return readyForDefense; }
    public void setReadyForDefense(boolean readyForDefense) { this.readyForDefense = readyForDefense; }


    public int getArticlesRequired() { return 2; }
    public int getConferencesRequired() { return 2; }
    public int getTrainingHoursRequired() { return 200; }

    public int getArticleProgressPercent() {
        return Math.min(100, (articleCount * 100) / getArticlesRequired());
    }

    public int getConferenceProgressPercent() {
        return Math.min(100, (conferenceCount * 100) / getConferencesRequired());
    }

    public int getTrainingProgressPercent() {
        return Math.min(100, (totalTrainingHours * 100) / getTrainingHoursRequired());
    }

    public int getOverallProgressPercent() {
        int sum = getArticleProgressPercent() + getConferenceProgressPercent() + getTrainingProgressPercent();
        return sum / 3;
    }
    public String getArticleBarClass() {
        if (articlesRequirementMet) return "progress-green";
        if (articleCount > 0) return "progress-orange";
        return "progress-red";
    }

    public String getConferenceBarClass() {
        if (conferencesRequirementMet) return "progress-green";
        if (conferenceCount > 0) return "progress-orange";
        return "progress-red";
    }

    public String getTrainingBarClass() {
        if (trainingRequirementMet) return "progress-green";
        if (totalTrainingHours > 0) return "progress-orange";
        return "progress-red";
    }
}