package com.nickledwold.scorebase_companion;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

public class ApiResponseObjects {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GetCompetitionDataResponse {
        private CompetitionData Result;

        public CompetitionData getResult() {
            return Result;
        }

        public void setResult(CompetitionData result) {
            Result = result;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompetitionData {
        private Integer PanelNumber;
        private String Status;
        private String SignOffCategory;
        private String SignOffRound;
        private CompetitorInformation CompetitorInformation;
        private List<JudgeInformation> JudgeInformation;

        public Integer getPanelNumber() {
            return PanelNumber;
        }

        public void setPanelNumber(Integer panelNumber) {
            PanelNumber = panelNumber;
        }

        public String getStatus() {
            return Status;
        }
        public String getSignOffCategory() {
            return SignOffCategory;
        }

        public String getSignOffRound() {
            return SignOffRound;
        }

        public void setStatus(String status) {
            Status = status;
        }
        public void setSignOffCategory(String signOffCategory) {
            SignOffCategory = signOffCategory;
        }

        public void setSignOffRound(String signOffRound) {
            SignOffRound = signOffRound;
        }

        public CompetitorInformation getCompetitorInformation() {
            return (CompetitorInformation != null) ? CompetitorInformation : new CompetitorInformation();
        }

        public void setCompetitorInformation(CompetitorInformation competitorInformation) {
            CompetitorInformation = competitorInformation;
        }

        public List<JudgeInformation> getJudgeInformation() {
            return JudgeInformation;
        }

        public void setJudgeInformation(List<JudgeInformation> judgeInformation) {
            JudgeInformation = judgeInformation;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JudgeInformation {
        private String JudgeName;
        private String JudgeRole;
        private String JudgeNation;
        private boolean ReEntryRequested;

        public String getJudgeName() {
            return JudgeName;
        }

        public static String getNameUpToFirstSpace(String str) {
            if (str == null || str.isEmpty()) {
                return ""; // or handle as needed
            }
            int spaceIndex = str.indexOf(' ');
            if (spaceIndex != -1) {
                return str.substring(0, spaceIndex);
            } else {
                return str; // No space found, return the entire string
            }
        }

        public static String getNameAfterFirstSpace(String str) {
            if (str == null || str.isEmpty()) {
                return ""; // or handle as needed
            }
            int spaceIndex = str.indexOf(' ');
            if (spaceIndex != -1) {
                return str.substring(spaceIndex + 1);
            } else {
                return ""; // No space found, return an empty string
            }
        }
        public String getJudgeFirstname() {
            return (JudgeName != null) ? getNameAfterFirstSpace(JudgeName) : "";
        }
        public String getJudgeSurname() {
            return (JudgeName != null) ? getNameUpToFirstSpace(JudgeName) : "";
        }

        public void setJudgeName(String judgeName) {
            JudgeName = judgeName;
        }

        public String getJudgeRole() {
            return JudgeRole;
        }

        public void setJudgeRole(String judgeRole) {
            JudgeRole = judgeRole;
        }

        public String getJudgeNation() {
            return JudgeNation;
        }

        public void setJudgeNation(String judgeNation) {
            JudgeNation = judgeNation;
        }

        public boolean isReEntryRequested() {
            return ReEntryRequested;
        }

        public void setReEntryRequested(boolean reEntryRequested) {
            ReEntryRequested = reEntryRequested;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompetitorInformation {
        private int CompetitorId;
        private String Name;
        private String Club;
        private String Category;
        private int Exercise;
        private int Elements;
        private int Flight;
        private int CompetitorNumber;
        private int CompetitorCount;
        private CompetitorSummary CompetitorSummary;

        public int getCompetitorId() {
            return CompetitorId;
        }

        public void setCompetitorId(int competitorId) {
            CompetitorId = competitorId;
        }
        public static String getNameUpToFirstSpace(String str) {
            if (str == null || str.isEmpty()) {
                return ""; // or handle as needed
            }
            int spaceIndex = str.indexOf(' ');
            if (spaceIndex != -1) {
                return str.substring(0, spaceIndex);
            } else {
                return str; // No space found, return the entire string
            }
        }

        public static String getNameAfterFirstSpace(String str) {
            if (str == null || str.isEmpty()) {
                return ""; // or handle as needed
            }
            int spaceIndex = str.indexOf(' ');
            if (spaceIndex != -1) {
                return str.substring(spaceIndex + 1);
            } else {
                return ""; // No space found, return an empty string
            }
        }
        public String getFirstName() {
            return (Name != null) ? getNameAfterFirstSpace(Name) : "";
        }
        public String getSurname() {
            return (Name != null) ? getNameUpToFirstSpace(Name) : "";
        }

        public void setName(String name) {
            Name = name;
        }

        public String getClub() {
            return (Club != null) ? Club : "";
        }

        public void setClub(String club) {
            Club = club;
        }

        public String getCategory() {
            return (Category != null) ? Category : "";
        }

        public void setCategory(String category) {
            Category = category;
        }

        public int getExercise() {
            return Exercise;
        }

        public void setExercise(int exercise) {
            Exercise = exercise;
        }

        public int getElements() {
            return Elements;
        }

        public void setElements(int elements) {
            Elements = elements;
        }

        public int getFlight() {
            return Flight;
        }

        public void setFlight(int flight) {
            Flight = flight;
        }

        public int getCompetitorNumber() {
            return CompetitorNumber;
        }

        public void setCompetitorNumber(int competitorNumber) {
            CompetitorNumber = competitorNumber;
        }

        public int getCompetitorCount() {
            return CompetitorCount;
        }

        public void setCompetitorCount(int competitorCount) {
            CompetitorCount = competitorCount;
        }

        public CompetitorSummary getCompetitorSummary() {
            return CompetitorSummary;
        }

        public void setCompetitorSummary(CompetitorSummary competitorSummary) {
            CompetitorSummary = competitorSummary;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompetitorSummary {
        private double TotalScore;
        private double Execution;
        private double HorizonalDisplacement;
        private double Difficulty;
        private double TimeOfFlight;
        private double Synchronisation;
        private double Penalty;

        public double getTotalScore() {
            return TotalScore;
        }

        public void setTotalScore(double totalScore) {
            TotalScore = totalScore;
        }

        public double getExecution() {
            return Execution;
        }

        public void setExecution(double execution) {
            Execution = execution;
        }

        public double getHorizonalDisplacement() {
            return HorizonalDisplacement;
        }

        public void setHorizonalDisplacement(double horizonalDisplacement) {
            HorizonalDisplacement = horizonalDisplacement;
        }

        public double getDifficulty() {
            return Difficulty;
        }

        public void setDifficulty(double difficulty) {
            Difficulty = difficulty;
        }

        public double getTimeOfFlight() {
            return TimeOfFlight;
        }

        public void setTimeOfFlight(double timeOfFlight) {
            TimeOfFlight = timeOfFlight;
        }

        public double getSynchronisation() {
            return Synchronisation;
        }

        public void setSynchronisation(double synchronisation) {
            Synchronisation = synchronisation;
        }

        public double getPenalty() {
            return Penalty;
        }

        public void setPenalty(double penalty) {
            Penalty = penalty;
        }
    }


}
