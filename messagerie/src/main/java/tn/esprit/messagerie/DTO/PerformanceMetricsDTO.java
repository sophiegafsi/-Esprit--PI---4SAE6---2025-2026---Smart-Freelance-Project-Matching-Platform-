package tn.esprit.messagerie.DTO;

public class PerformanceMetricsDTO {
    // PCS - Professional Communication Score
    private double pcs;

    // Components of PCS
    private double responseSpeedScore;
    private double readRateScore;
    private double resolutionRate;
    private double professionalismScore;

    // Advanced Metrics
    private double ewma; // Exponentially Weighted Moving Average of response time
    private double cei; // Conversation Engagement Index
    private double riskScore;
    private double trustIndex;
    private double volatility;
    private double per; // Productivity Efficiency Ratio
    private double slaCompliance;

    // Getters and Setters
    public double getPcs() {
        return pcs;
    }

    public void setPcs(double pcs) {
        this.pcs = pcs;
    }

    public double getResponseSpeedScore() {
        return responseSpeedScore;
    }

    public void setResponseSpeedScore(double responseSpeedScore) {
        this.responseSpeedScore = responseSpeedScore;
    }

    public double getReadRateScore() {
        return readRateScore;
    }

    public void setReadRateScore(double readRateScore) {
        this.readRateScore = readRateScore;
    }

    public double getResolutionRate() {
        return resolutionRate;
    }

    public void setResolutionRate(double resolutionRate) {
        this.resolutionRate = resolutionRate;
    }

    public double getProfessionalismScore() {
        return professionalismScore;
    }

    public void setProfessionalismScore(double professionalismScore) {
        this.professionalismScore = professionalismScore;
    }

    public double getEwma() {
        return ewma;
    }

    public void setEwma(double ewma) {
        this.ewma = ewma;
    }

    public double getCei() {
        return cei;
    }

    public void setCei(double cei) {
        this.cei = cei;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public double getTrustIndex() {
        return trustIndex;
    }

    public void setTrustIndex(double trustIndex) {
        this.trustIndex = trustIndex;
    }

    public double getVolatility() {
        return volatility;
    }

    public void setVolatility(double volatility) {
        this.volatility = volatility;
    }

    public double getPer() {
        return per;
    }

    public void setPer(double per) {
        this.per = per;
    }

    public double getSlaCompliance() {
        return slaCompliance;
    }

    public void setSlaCompliance(double slaCompliance) {
        this.slaCompliance = slaCompliance;
    }
}
