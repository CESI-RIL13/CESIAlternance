package fr.cesi.alternance;

public class Constants {

	public static final String DEBUG_ACCOUNT_LOGIN  		= "intervenant1@via-cesi.fr";
	public static final String DEBUG_ACCOUNT_PASSWD 		= "intervenant1";
	public static final String DEBUG_CALENDAR_REFERENCE		= "7j4qlfm3fssh6crmv3bf7smv8s";
	public static final String DEBUG_APP_AUTH_TOKEN			= "52d7dccf53f4d";
	
	public static final boolean DEBUG						= true;
	
	public static final String APP_NAME 					= "CESI Alternance";
	public static final String APP_PACKAGE 					= "fr.cesi.alternance";
	public static final String APP_VERSION 					= "1.0.0 (alpha)";
	public static final String APP_VERSION_CODE				= "1";
	public static final String APP_NAMESPACE				= "http://schemas.android.com/apk/res/" + APP_PACKAGE;

	public static final String BASE_URL						= "http://cesi.kolapsis.com/cesi_alternance";
	public static final String BASE_API_URL					= BASE_URL + "/api/v1";

	public static final int RESULT_NEW_ACCOUNT				= 1;
	public static final int RESULT_PREFERENCES				= 2;

    public static final String ACCOUNT_LABEL 				= "CESI Alternance";
    public static final String ACCOUNT_TYPE 				= APP_PACKAGE + ".account";
    public static final String ACCOUNT_TOKEN_TYPE 			= APP_PACKAGE + ".account.token";
    
    public static final String CALLER_IS_SYNCADAPTER 		= "caller_is_syncadapter";
    public static final String PROVIDER_CALENDAR			= "com.android.calendar";
    
}
