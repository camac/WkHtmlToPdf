package com.gregorbyte.xsp.wkhtmltopdf.library;

import com.ibm.xsp.library.AbstractXspLibrary;

public class WkHtmlToPdfXspLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = "com.gregorbyte.xsp.wkhtmltopdf.library";
	
	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}

	@Override
	public boolean isGlobalScope() {
		return true;
	}

	
	
}
