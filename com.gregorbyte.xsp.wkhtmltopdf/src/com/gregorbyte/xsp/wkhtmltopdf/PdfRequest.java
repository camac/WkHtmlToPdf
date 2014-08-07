package com.gregorbyte.xsp.wkhtmltopdf;

public class PdfRequest {

	private String viewId;
	private String sessionId;
	private String ltpaToken;
	
	private String url;
	
	public PdfRequest(String url) {
		
		// Steal the sessionId or ltpaToken
		
		// Figure out the view id
		this.viewId = "temp";		
		this.url = url;
		
	}
	
	public String getUrl() {
		return url;
	}
	
}
