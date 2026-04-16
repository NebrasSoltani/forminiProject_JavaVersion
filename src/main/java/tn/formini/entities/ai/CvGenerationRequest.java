package tn.formini.entities.ai;

public class CvGenerationRequest {
    private CvData cvData;
    private String templateStyle = "professionnel";
    private String format = "markdown";
    private String targetJob;

    public CvGenerationRequest() {}

    public CvGenerationRequest(CvData cvData) {
        this.cvData = cvData;
    }

    public CvData getCvData() { return cvData; }
    public void setCvData(CvData cvData) { this.cvData = cvData; }
    public String getTemplateStyle() { return templateStyle; }
    public void setTemplateStyle(String templateStyle) { this.templateStyle = templateStyle; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getTargetJob() { return targetJob; }
    public void setTargetJob(String targetJob) { this.targetJob = targetJob; }

    public void valider() {
        if (cvData == null) throw new IllegalArgumentException("Les données CV sont obligatoires.");
        cvData.valider();
    }
}
