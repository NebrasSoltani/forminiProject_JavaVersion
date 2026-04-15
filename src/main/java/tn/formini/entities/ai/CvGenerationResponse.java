package tn.formini.entities.ai;

public class CvGenerationResponse {
    private String cvContent;
    private String format;
    private String style;
    private boolean success;
    private String errorMessage;
    private long generationTimeMs;
    private String suggestions;

    public CvGenerationResponse() {}

    public String getCvContent() { return cvContent; }
    public void setCvContent(String cvContent) { this.cvContent = cvContent; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public long getGenerationTimeMs() { return generationTimeMs; }
    public void setGenerationTimeMs(long generationTimeMs) { this.generationTimeMs = generationTimeMs; }
    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }

    public static CvGenerationResponse success(String content, String format, String style, long timeMs) {
        CvGenerationResponse response = new CvGenerationResponse();
        response.setSuccess(true);
        response.setCvContent(content);
        response.setFormat(format);
        response.setStyle(style);
        response.setGenerationTimeMs(timeMs);
        return response;
    }

    public static CvGenerationResponse error(String message) {
        CvGenerationResponse response = new CvGenerationResponse();
        response.setSuccess(false);
        response.setErrorMessage(message);
        return response;
    }
}
