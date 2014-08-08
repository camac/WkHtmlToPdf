package com.gregorbyte.xsp.wkhtmltopdf.job;

public class PdfRequest {

	private String viewId;
	private String sessionId;
	private String ltpaToken;
	
	private String url;
	
	public PdfRequest(String viewId, String url) {
		
		// Steal the sessionId or ltpaToken
		
		// Figure out the view id
		this.viewId = viewId;
		this.url = url;
		
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getViewId() {
		return viewId;
	}
	
}
