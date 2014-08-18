package com.kolapsis.utils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.protocol.HTTP;

import android.webkit.MimeTypeMap;

public class StringUtils {
	
	public static final String escapeColumnName(String name) {
		if (!name.startsWith("`")) name = "`" + name;
		if (!name.endsWith("`")) name = name + "`";
		return name;
	}
	public static final String unescapeColumnName(String name) {
		if (name.startsWith("`")) name = name.substring(1);
		if (name.endsWith("`")) name = name.substring(0, name.length()-1);
		return name;
	}

	public static String decode(String str) {
		try {
			str = URLDecoder.decode(str, HTTP.UTF_8);
			return str;
		} catch (Exception e) {
			return str;
		}
	}
	public static String encode(String str) {
		try {
			return URLEncoder.encode(str, HTTP.UTF_8);
		} catch (Exception e) {
			return str;
		}
	}

	public static boolean isURL(String url) {
		return url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("magnet:?"));
	}
	public static boolean isLocalTorrent(String url) {
		return url != null && (url.startsWith("file:///") && url.endsWith(".torrent"));
	}
	public static boolean isLocalNBZ(String url) {
		return url != null && (url.startsWith("file:///") && url.endsWith(".nbz"));
	}
	public static boolean isAcceptableURL(String url) {
		return isURL(url) || isLocalTorrent(url);
	}
	
	public static boolean isFreeboxURL (String url) {
		try {
			if (url.startsWith("http://")) url = url.substring(7);
			else if (url.startsWith("https://")) url = url.substring(8);
			if (url.endsWith("/")) url = url.substring(0, url.lastIndexOf("/"));
			if (url.indexOf(":") > -1) {
				String[] tmp = url.split(":");
				url = tmp[0];
				if (!isInteger(tmp[1])) return false;
			}
			return isIpAddress(url) || isHostName(url);
		} catch (Exception e){}
		return false;
	}


	/**
	 * @param double speed
	 * @return String to represent speed.
	 */
	 public static String speed(double speed){
		return speed(speed,false,"b");
	}
	public static String speed(double speed, boolean si){
		return speed(speed,si,"b");
	}
	public static String speed(double speed, boolean si, String mode){
		int unit = si ? 1000 : 1024;
		if (mode.equals("octet")) mode = "o";
		String str = " " + mode + "/s";
		if (speed > unit){speed /= unit; str = " K"+mode+"/s";}
		if (speed > unit){speed /= unit; str = " M"+mode+"/s";}
		if (speed > unit){speed /= unit; str = " G"+mode+"/s";}
		if (speed > unit){speed /= unit; str = " T"+mode+"/s";}
		NumberFormat formater = new DecimalFormat("#.##");
		str = formater.format(speed) + str;
		return str;
	}

	public static String percent(float pct) {
		String str = String.format("%2.02f", pct);
		String sep = ".";
		if (str.indexOf(",") > -1) sep = ",";
		if (str.endsWith(sep+"00")) str = str.replace(sep+"00", "");
		return str + "%";
	}

	/**
	 * @param double speed
	 * @return String to represent speed.
	 */
	public static String size(double size){
		return size(size,false);
	}
	public static String size(double size, boolean si){
		return size(size,false,"b");
	}
	public static String size(double size, boolean si, String mode){
		int unit = si ? 1000 : 1024;
		if (mode.equals("octet")) mode = "o";
		String str = " " + mode + "/s";
		//"+(si?"i":"")+"
		if (size > unit){size /= unit; str = " K"+mode;}
		if (size > unit){size /= unit; str = " M"+mode;}
		if (size > unit){size /= unit; str = " G"+mode;}
		if (size > unit){size /= unit; str = " T"+mode;}
		NumberFormat formater = new DecimalFormat("#.##");
		str = formater.format(size) + str;
		return str;
	}

	static public String join(List<String> list, String sep) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String item : list) {
			if (first) first = false;
			else sb.append(sep);
			sb.append(item);
		}
		return sb.toString();
	}

	public static String dateUTCToLocalDate(String date) {
		if (StringUtils.isEmpty(date)) return date;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			Date myDate = df.parse(date);
			df.setTimeZone(TimeZone.getDefault());
			date = df.format(myDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     * */
	public static String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";

		int hours = (int)( milliseconds / (1000*60*60));
		int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		if(hours > 0){
			finalTimerString = hours + ":";
		}
		if(seconds < 10){
			secondsString = "0" + seconds;
		}else{
			secondsString = "" + seconds;}
		finalTimerString = finalTimerString + minutes + ":" + secondsString;
		return finalTimerString;
	}

	/**
	 * @param String time style "hh:mm:ss"
	 * @return long duration in milliseconds.
	 */
	public static long getDuration(String time) {
		String[] tokens = time.split(":");
		int hours = Integer.parseInt(tokens[0]);
		int minutes = Integer.parseInt(tokens[1]);
		int seconds = Integer.parseInt(tokens[2]);
		return (3600 * hours + 60 * minutes + seconds) * 1000;
	}

	/**
	 * @param long millisecond
	 * @return String to represent duration.
	 */
	public static String getDuration(long millis) {
		return getDuration(millis, false);
	}
	public static String getDuration(long millis, boolean shorted) {
		if(millis < 0) {
			throw new IllegalArgumentException("Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		if (days > 0) {
			sb.append(days);
			sb.append("j ");
		}
		if (hours > 0) {
			sb.append(hours);
			sb.append("h ");
		}
		if (minutes > 0) {
			sb.append(minutes);
			sb.append("m ");
		}
		if (!shorted || minutes == 0) {
			sb.append(seconds);
			sb.append("s");
		}
		return sb.toString();
	}
	
	public static String getDayTime(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd à HH:mm", Locale.FRANCE);
		return sdf.format(new Date(date));
	}

	public static String getDates(long begin, long end) {
		return getDates(begin, end, false);
	}
	public static String getDates(long begin, long end, boolean small) {
		return "Du " + getDate(begin, small) + " au " + getDate(end, small);
	}
	public static String getDate(long date, boolean small) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd "+(small?"MMM":"MMMM"), Locale.FRANCE);
		return sdf.format(new Date(date));
	}
	
	public static long getDate(String str) {
		long date = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
		try {
			date = sdf.parse(str).getTime();
		} catch (ParseException e) {}
		return date;
	}
	public static long getDateTime(String str) {
		long date = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE);
		try {
			date = sdf.parse(str).getTime();
		} catch (ParseException e) {}
		return date;
	}

	/**
	 * @param url
	 * @return
	 */
	public static String getHostFromURL(String url) {
		String host = "";
		try {
			URI uri = new URI(url);
			host = uri.getHost();
		} catch (URISyntaxException e) {
			host = url.toString().substring(url.indexOf("://")+3);
			if (host.indexOf("/") > -1) host = host.substring(0,host.indexOf("/"));
		}
		return host;
	}

	/**
	 * @param url
	 * @return
	 */
	public static String getFilenameFromPath(String url) {
		int li = url.lastIndexOf("/");
		if (li > -1) return url.substring(li+1);
		return url;
	}

	/**
	 * @return True if the rtsp is an magnet: rtsp.
	 */
	public static boolean isMagnetUrl(String url) {
		return (null != url) &&
				(url.length() > 7) &&
				url.substring(0, 7).equalsIgnoreCase("magnet:");
	}

	public static String getExtension(String name) {
		String ext = null;
		if (name.indexOf(".") > -1) ext = name.substring(name.lastIndexOf(".")+1).toLowerCase();
		return ext;
	}

	/**
	 * Valide une adresse IP
	 * @param ip
	 * @return boolean
	 */
	public static boolean isIpAddress(String ip) {
		String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();
	}

	/**
	 * Valide une adresse dns
	 * @param ip
	 * @return boolean
	 */
	public static boolean isHostName(String dns) {
		String HOSTNAME_PATTERN = "^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])(\\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9]))*$";
		Pattern pattern = Pattern.compile(HOSTNAME_PATTERN);
		Matcher matcher = pattern.matcher(dns);
		return matcher.matches();
	}

	public static boolean isFrenchNumber(String number) {
		String PHONE_PATTERN = "^(0|\\+33)[1-9]([\\-. ]?[0-9]{2}){4}$";
		Pattern pattern = Pattern.compile(PHONE_PATTERN);
		Matcher matcher = pattern.matcher(number);
		return matcher.matches();
	}

	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch(Exception e) {
			return false;
		}
	}

	public static boolean isEmpty(Object input) {
		if (input == null) return true;
		if (input instanceof String) {
			if ("{}".equals(input)) input = "";
			return !isValideSize((String) input, 0);
		} else {
			return false;
		}
	}
	public static boolean isValideSize(String input, int size) {
		if (input == null) return false;
		if (input.length() > size) return true;
		return false;
	}

	public static String getMimieType(String name) {
		String ext = getExtension(name);
		//Log.i("Freebox", "ext: "+ext);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
		if (mimeType == null) mimeType = getMimeTypeByExtension(ext);
		return mimeType;
	}
	public static String getMimieType(String name, boolean complete) {
		String mimeType = getMimieType(name);
		if (!complete && mimeType != null) mimeType = mimeType.substring(0,mimeType.indexOf("/"))+"/*";
		return mimeType;
	}

	private static String getMimeTypeByExtension(String ext) {
		if (MIME_TYPES.containsKey(ext)) return MIME_TYPES.get(ext);
		return "";
	}

	public static String getMimieType(File file) {
		return getMimieType(file.getName());
	}
	public static String getMimieType(File file, boolean complete) {
		return getMimieType(file.getName(), complete);
	}

	public static String capitalizeFirstLetters( String s ) {
		for (int i = 0; i < s.length(); i++) {
			if (i == 0) {
				// Capitalize the first letter of the string.
				s = String.format( "%s%s",
						Character.toUpperCase(s.charAt(0)),
						s.substring(1) );
			}

			// Is this character a non-letter or non-digit?  If so
			// then this is probably a word boundary so let's capitalize
			// the next character in the sequence.
			if (!Character.isLetterOrDigit(s.charAt(i))) {
				if (i + 1 < s.length()) {
					s = String.format( "%s%s%s",
							s.subSequence(0, i+1),
							Character.toUpperCase(s.charAt(i + 1)),
							s.substring(i+2) );
				}
			}
		}
		return s;

	}

	public static String stripTags(String text) {
		String strippedHtml = text.replaceAll("<(.|\n)*?>", "");
		return strippedHtml.trim();
	}

	private static HashMap<String,String> htmlEntities;
	static {
		htmlEntities = new HashMap<String,String>();
		htmlEntities.put("&lt;","<")    ; htmlEntities.put("&gt;",">");
		htmlEntities.put("&amp;","&")   ; htmlEntities.put("&quot;","\"");
		htmlEntities.put("&agrave;","à"); htmlEntities.put("&Agrave;","À");
		htmlEntities.put("&acirc;","â") ; htmlEntities.put("&auml;","ä");
		htmlEntities.put("&Auml;","Ä")  ; htmlEntities.put("&Acirc;","Â");
		htmlEntities.put("&aring;","å") ; htmlEntities.put("&Aring;","Å");
		htmlEntities.put("&aelig;","æ") ; htmlEntities.put("&AElig;","Æ" );
		htmlEntities.put("&ccedil;","ç"); htmlEntities.put("&Ccedil;","Ç");
		htmlEntities.put("&eacute;","é"); htmlEntities.put("&Eacute;","É" );
		htmlEntities.put("&egrave;","è"); htmlEntities.put("&Egrave;","È");
		htmlEntities.put("&ecirc;","ê") ; htmlEntities.put("&Ecirc;","Ê");
		htmlEntities.put("&euml;","ë")  ; htmlEntities.put("&Euml;","Ë");
		htmlEntities.put("&iuml;","ï")  ; htmlEntities.put("&Iuml;","�?");
		htmlEntities.put("&ocirc;","ô") ; htmlEntities.put("&Ocirc;","Ô");
		htmlEntities.put("&ouml;","ö")  ; htmlEntities.put("&Ouml;","Ö");
		htmlEntities.put("&oslash;","ø") ; htmlEntities.put("&Oslash;","Ø");
		htmlEntities.put("&szlig;","ß") ; htmlEntities.put("&ugrave;","ù");
		htmlEntities.put("&Ugrave;","Ù"); htmlEntities.put("&ucirc;","û");
		htmlEntities.put("&Ucirc;","Û") ; htmlEntities.put("&uuml;","ü");
		htmlEntities.put("&Uuml;","Ü")  ; htmlEntities.put("&nbsp;"," ");
		htmlEntities.put("&copy;","\u00a9");
		htmlEntities.put("&reg;","\u00ae");
		htmlEntities.put("&euro;","\u20a0");
	}

	public static final String unescapeHTML(String source) {
		int i, j;

		boolean continueLoop;
		int skip = 0;
		do {
			continueLoop = false;
			i = source.indexOf("&", skip);
			if (i > -1) {
				j = source.indexOf(";", i);
				if (j > i) {
					String entityToLookFor = source.substring(i, j + 1);
					String value = htmlEntities.get(entityToLookFor);
					if (value != null) {
						source = source.substring(0, i)
								+ value + source.substring(j + 1);
						continueLoop = true;
					}
					else if (value == null){
						skip = i+1;
						continueLoop = true;
					}
				}
			}
		} while (continueLoop);
		return source;
	}

	public static String lpad(String value, String add, int count) {
		while (value.length() < count) value = add + value;
		return value;
	}
	public static String rpad(String value, String add, int count) {
		while (value.length() < count) value = value + add;
		return value;
	}

	public static String minutesToHours(final int minutes) {
		final int hours = minutes / 60;
		final int min = (int) Math.ceil(minutes % 60);
		String format = "%02d minute"+(min>1?"s":"");
		if (hours > 0) format = "%d heure"+(hours>1?"s":"")+" "+format;
		return hours > 0 ? String.format(format, hours, min) : String.format(format, min);
	}

	public static String secondsToMinutes(final int seconds) {
		final int min = seconds / 60;
		final int sec = (int) Math.ceil(seconds % 60);
		String format = "%02d seconde"+(sec>1?"s":"");
		if (min > 0) format = "%d minute"+(min>1?"s":"")+" "+format;
		return min > 0 ? String.format(format, min, sec) : String.format(format, sec);
	}


	public static final HashMap<String, String> MIME_TYPES = new HashMap<String, String>();
	static {
		MIME_TYPES.put("ez", "application/andrew-inset");
		MIME_TYPES.put("tsp", "application/dsptype");
		MIME_TYPES.put("spl", "application/futuresplash");
		MIME_TYPES.put("hta", "application/hta");
		MIME_TYPES.put("hqx", "application/mac-binhex40");
		MIME_TYPES.put("cpt", "application/mac-compactpro");
		MIME_TYPES.put("nb", "application/mathematica");
		MIME_TYPES.put("mdb", "application/msaccess");
		MIME_TYPES.put("oda", "application/oda");
		MIME_TYPES.put("ogg", "application/ogg");
		MIME_TYPES.put("pdf", "application/pdf");
		MIME_TYPES.put("key", "application/pgp-keys");
		MIME_TYPES.put("pgp", "application/pgp-signature");
		MIME_TYPES.put("prf", "application/pics-rules");
		MIME_TYPES.put("rar", "application/rar");
		MIME_TYPES.put("rdf", "application/rdf+xml");
		MIME_TYPES.put("rss", "application/rss+xml");
		MIME_TYPES.put("zip", "application/zip");
		MIME_TYPES.put("apk", "application/vnd.android.package-archive");
		MIME_TYPES.put("cdy", "application/vnd.cinderella");
		MIME_TYPES.put("stl", "application/vnd.ms-pki.stl");
		MIME_TYPES.put("odb", "application/vnd.oasis.opendocument.database");
		MIME_TYPES.put("odf", "application/vnd.oasis.opendocument.formula");
		MIME_TYPES.put("odg", "application/vnd.oasis.opendocument.graphics");
		MIME_TYPES.put("otg", "application/vnd.oasis.opendocument.graphics-template");
		MIME_TYPES.put("odi", "application/vnd.oasis.opendocument.image");
		MIME_TYPES.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		MIME_TYPES.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
		MIME_TYPES.put("odt", "application/vnd.oasis.opendocument.text");
		MIME_TYPES.put("odm", "application/vnd.oasis.opendocument.text-master");
		MIME_TYPES.put("ott", "application/vnd.oasis.opendocument.text-template");
		MIME_TYPES.put("oth", "application/vnd.oasis.opendocument.text-web");
		MIME_TYPES.put("doc", "application/msword");
		MIME_TYPES.put("dot", "application/msword");
		MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		MIME_TYPES.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
		MIME_TYPES.put("xls", "application/vnd.ms-excel");
		MIME_TYPES.put("xlt", "application/vnd.ms-excel");
		MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		MIME_TYPES.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
		MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
		MIME_TYPES.put("pot", "application/vnd.ms-powerpoint");
		MIME_TYPES.put("pps", "application/vnd.ms-powerpoint");
		MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
		MIME_TYPES.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
		MIME_TYPES.put("ppsx","application/vnd.openxmlformats-officedocument.presentationml.slideshow");
		MIME_TYPES.put("cod", "application/vnd.rim.cod");
		MIME_TYPES.put("mmf", "application/vnd.smaf");
		MIME_TYPES.put("sdc", "application/vnd.stardivision.calc");
		MIME_TYPES.put("sda", "application/vnd.stardivision.draw");
		MIME_TYPES.put("sdd", "application/vnd.stardivision.impress");
		MIME_TYPES.put("sdp", "application/vnd.stardivision.impress");
		MIME_TYPES.put("smf", "application/vnd.stardivision.math");
		MIME_TYPES.put("sdw", "application/vnd.stardivision.writer");
		MIME_TYPES.put("vor", "application/vnd.stardivision.writer");
		MIME_TYPES.put("sgl", "application/vnd.stardivision.writer-global");
		MIME_TYPES.put("sxc", "application/vnd.sun.xml.calc");
		MIME_TYPES.put("stc", "application/vnd.sun.xml.calc.template");
		MIME_TYPES.put("sxd", "application/vnd.sun.xml.draw");
		MIME_TYPES.put("std", "application/vnd.sun.xml.draw.template");
		MIME_TYPES.put("sxi", "application/vnd.sun.xml.impress");
		MIME_TYPES.put("sti", "application/vnd.sun.xml.impress.template");
		MIME_TYPES.put("sxm", "application/vnd.sun.xml.math");
		MIME_TYPES.put("sxw", "application/vnd.sun.xml.writer");
		MIME_TYPES.put("sxg", "application/vnd.sun.xml.writer.global");
		MIME_TYPES.put("stw", "application/vnd.sun.xml.writer.template");
		MIME_TYPES.put("vsd", "application/vnd.visio");
		MIME_TYPES.put("abw", "application/x-abiword");
		MIME_TYPES.put("dmg", "application/x-apple-diskimage");
		MIME_TYPES.put("bcpio", "application/x-bcpio");
		MIME_TYPES.put("torrent", "application/x-bittorrent");
		MIME_TYPES.put("cdf", "application/x-cdf");
		MIME_TYPES.put("vcd", "application/x-cdlink");
		MIME_TYPES.put("pgn", "application/x-chess-pgn");
		MIME_TYPES.put("cpio", "application/x-cpio");
		MIME_TYPES.put("deb", "application/x-debian-package");
		MIME_TYPES.put("udeb", "application/x-debian-package");
		MIME_TYPES.put("dcr", "application/x-director");
		MIME_TYPES.put("dir", "application/x-director");
		MIME_TYPES.put("dxr", "application/x-director");
		MIME_TYPES.put("dms", "application/x-dms");
		MIME_TYPES.put("wad", "application/x-doom");
		MIME_TYPES.put("dvi", "application/x-dvi");
		MIME_TYPES.put("flac", "application/x-flac");
		MIME_TYPES.put("pfa", "application/x-font");
		MIME_TYPES.put("pfb", "application/x-font");
		MIME_TYPES.put("gsf", "application/x-font");
		MIME_TYPES.put("pcf", "application/x-font");
		MIME_TYPES.put("pcf.Z", "application/x-font");
		MIME_TYPES.put("mm", "application/x-freemind");
		MIME_TYPES.put("spl", "application/x-futuresplash");
		MIME_TYPES.put("gnumeric", "application/x-gnumeric");
		MIME_TYPES.put("sgf", "application/x-go-sgf");
		MIME_TYPES.put("gcf", "application/x-graphing-calculator");
		MIME_TYPES.put("gtar", "application/x-gtar");
		MIME_TYPES.put("tgz", "application/x-gtar");
		MIME_TYPES.put("taz", "application/x-gtar");
		MIME_TYPES.put("hdf", "application/x-hdf");
		MIME_TYPES.put("ica", "application/x-ica");
		MIME_TYPES.put("ins", "application/x-internet-signup");
		MIME_TYPES.put("isp", "application/x-internet-signup");
		MIME_TYPES.put("iii", "application/x-iphone");
		MIME_TYPES.put("iso", "application/x-iso9660-image");
		MIME_TYPES.put("jmz", "application/x-jmol");
		MIME_TYPES.put("chrt", "application/x-kchart");
		MIME_TYPES.put("kil", "application/x-killustrator");
		MIME_TYPES.put("skp", "application/x-koan");
		MIME_TYPES.put("skd", "application/x-koan");
		MIME_TYPES.put("skt", "application/x-koan");
		MIME_TYPES.put("skm", "application/x-koan");
		MIME_TYPES.put("kpr", "application/x-kpresenter");
		MIME_TYPES.put("kpt", "application/x-kpresenter");
		MIME_TYPES.put("ksp", "application/x-kspread");
		MIME_TYPES.put("kwd", "application/x-kword");
		MIME_TYPES.put("kwt", "application/x-kword");
		MIME_TYPES.put("latex", "application/x-latex");
		MIME_TYPES.put("lha", "application/x-lha");
		MIME_TYPES.put("lzh", "application/x-lzh");
		MIME_TYPES.put("lzx", "application/x-lzx");
		MIME_TYPES.put("frm", "application/x-maker");
		MIME_TYPES.put("maker", "application/x-maker");
		MIME_TYPES.put("frame", "application/x-maker");
		MIME_TYPES.put("fb", "application/x-maker");
		MIME_TYPES.put("book", "application/x-maker");
		MIME_TYPES.put("fbdoc", "application/x-maker");
		MIME_TYPES.put("mif", "application/x-mif");
		MIME_TYPES.put("wmd", "application/x-ms-wmd");
		MIME_TYPES.put("wmz", "application/x-ms-wmz");
		MIME_TYPES.put("msi", "application/x-msi");
		MIME_TYPES.put("pac", "application/x-ns-proxy-autoconfig");
		MIME_TYPES.put("nwc", "application/x-nwc");
		MIME_TYPES.put("o", "application/x-object");
		MIME_TYPES.put("oza", "application/x-oz-application");
		MIME_TYPES.put("p12", "application/x-pkcs12");
		MIME_TYPES.put("p7r", "application/x-pkcs7-certreqresp");
		MIME_TYPES.put("crl", "application/x-pkcs7-crl");
		MIME_TYPES.put("qtl", "application/x-quicktimeplayer");
		MIME_TYPES.put("shar", "application/x-shar");
		MIME_TYPES.put("swf", "application/x-shockwave-flash");
		MIME_TYPES.put("sit", "application/x-stuffit");
		MIME_TYPES.put("sv4cpio", "application/x-sv4cpio");
		MIME_TYPES.put("sv4crc", "application/x-sv4crc");
		MIME_TYPES.put("tar", "application/x-tar");
		MIME_TYPES.put("texinfo", "application/x-texinfo");
		MIME_TYPES.put("texi", "application/x-texinfo");
		MIME_TYPES.put("t", "application/x-troff");
		MIME_TYPES.put("roff", "application/x-troff");
		MIME_TYPES.put("man", "application/x-troff-man");
		MIME_TYPES.put("ustar", "application/x-ustar");
		MIME_TYPES.put("src", "application/x-wais-source");
		MIME_TYPES.put("wz", "application/x-wingz");
		MIME_TYPES.put("webarchive", "application/x-webarchive");
		MIME_TYPES.put("crt", "application/x-x509-ca-cert");
		MIME_TYPES.put("crt", "application/x-x509-user-cert");
		MIME_TYPES.put("xcf", "application/x-xcf");
		MIME_TYPES.put("fig", "application/x-xfig");
		MIME_TYPES.put("xhtml", "application/xhtml+xml");
		MIME_TYPES.put("3gpp", "audio/3gpp");
		MIME_TYPES.put("amr", "audio/amr");
		MIME_TYPES.put("snd", "audio/basic");
		MIME_TYPES.put("mid", "audio/midi");
		MIME_TYPES.put("midi", "audio/midi");
		MIME_TYPES.put("kar", "audio/midi");
		MIME_TYPES.put("xmf", "audio/midi");
		MIME_TYPES.put("mxmf", "audio/mobile-xmf");
		MIME_TYPES.put("mpga", "audio/mpeg");
		MIME_TYPES.put("mpega", "audio/mpeg");
		MIME_TYPES.put("mp2", "audio/mpeg");
		MIME_TYPES.put("mp3", "audio/mpeg");
		MIME_TYPES.put("m4a", "audio/mpeg");
		MIME_TYPES.put("m3u", "audio/x-mpegurl");
		MIME_TYPES.put("sid", "audio/prs.sid");
		MIME_TYPES.put("aif", "audio/x-aiff");
		MIME_TYPES.put("aiff", "audio/x-aiff");
		MIME_TYPES.put("aifc", "audio/x-aiff");
		MIME_TYPES.put("gsm", "audio/x-gsm");
		MIME_TYPES.put("wma", "audio/x-ms-wma");
		MIME_TYPES.put("wax", "audio/x-ms-wax");
		MIME_TYPES.put("ra", "audio/x-pn-realaudio");
		MIME_TYPES.put("rm", "audio/x-pn-realaudio");
		MIME_TYPES.put("ram", "audio/x-pn-realaudio");
		MIME_TYPES.put("ra", "audio/x-realaudio");
		MIME_TYPES.put("pls", "audio/x-scpls");
		MIME_TYPES.put("sd2", "audio/x-sd2");
		MIME_TYPES.put("wav", "audio/x-wav");
		MIME_TYPES.put("bmp", "image/bmp");
		MIME_TYPES.put("gif", "image/gif");
		MIME_TYPES.put("cur", "image/ico");
		MIME_TYPES.put("ico", "image/ico");
		MIME_TYPES.put("ief", "image/ief");
		MIME_TYPES.put("jpeg", "image/jpeg");
		MIME_TYPES.put("jpg", "image/jpeg");
		MIME_TYPES.put("jpe", "image/jpeg");
		MIME_TYPES.put("pcx", "image/pcx");
		MIME_TYPES.put("png", "image/png");
		MIME_TYPES.put("svg", "image/svg+xml");
		MIME_TYPES.put("svgz", "image/svg+xml");
		MIME_TYPES.put("tiff", "image/tiff");
		MIME_TYPES.put("tif", "image/tiff");
		MIME_TYPES.put("djvu", "image/vnd.djvu");
		MIME_TYPES.put("djv", "image/vnd.djvu");
		MIME_TYPES.put("wbmp", "image/vnd.wap.wbmp");
		MIME_TYPES.put("ras", "image/x-cmu-raster");
		MIME_TYPES.put("cdr", "image/x-coreldraw");
		MIME_TYPES.put("pat", "image/x-coreldrawpattern");
		MIME_TYPES.put("cdt", "image/x-coreldrawtemplate");
		MIME_TYPES.put("cpt", "image/x-corelphotopaint");
		MIME_TYPES.put("ico", "image/x-icon");
		MIME_TYPES.put("art", "image/x-jg");
		MIME_TYPES.put("jng", "image/x-jng");
		MIME_TYPES.put("bmp", "image/x-ms-bmp");
		MIME_TYPES.put("psd", "image/x-photoshop");
		MIME_TYPES.put("pnm", "image/x-portable-anymap");
		MIME_TYPES.put("pbm", "image/x-portable-bitmap");
		MIME_TYPES.put("pgm", "image/x-portable-graymap");
		MIME_TYPES.put("ppm", "image/x-portable-pixmap");
		MIME_TYPES.put("rgb", "image/x-rgb");
		MIME_TYPES.put("xbm", "image/x-xbitmap");
		MIME_TYPES.put("xpm", "image/x-xpixmap");
		MIME_TYPES.put("xwd", "image/x-xwindowdump");
		MIME_TYPES.put("igs", "model/iges");
		MIME_TYPES.put("iges", "model/iges");
		MIME_TYPES.put("msh", "model/mesh");
		MIME_TYPES.put("mesh", "model/mesh");
		MIME_TYPES.put("silo", "model/mesh");
		MIME_TYPES.put("ics", "text/calendar");
		MIME_TYPES.put("icz", "text/calendar");
		MIME_TYPES.put("csv", "text/comma-separated-values");
		MIME_TYPES.put("css", "text/css");
		MIME_TYPES.put("htm", "text/html");
		MIME_TYPES.put("html", "text/html");
		MIME_TYPES.put("text/h323", "323");
		MIME_TYPES.put("uls", "text/iuls");
		MIME_TYPES.put("mml", "text/mathml");
		// add it first so it will be the default for ExtensionFromMimeType
		MIME_TYPES.put("txt", "text/plain");
		MIME_TYPES.put("asc", "text/plain");
		MIME_TYPES.put("text", "text/plain");
		MIME_TYPES.put("diff", "text/plain");
		MIME_TYPES.put("po", "text/plain");     // reserve "pot" for vnd.ms-powerpoint
		MIME_TYPES.put("rtx", "text/richtext");
		MIME_TYPES.put("rtf", "text/rtf");
		MIME_TYPES.put("ts", "text/texmacs");
		MIME_TYPES.put("phps", "text/text");
		MIME_TYPES.put("tsv", "text/tab-separated-values");
		MIME_TYPES.put("xml", "text/xml");
		MIME_TYPES.put("bib", "text/x-bibtex");
		MIME_TYPES.put("boo", "text/x-boo");
		MIME_TYPES.put("text/x-c++hdr", "h++");
		MIME_TYPES.put("hpp", "text/x-c++hdr");
		MIME_TYPES.put("hxx", "text/x-c++hdr");
		MIME_TYPES.put("hh", "text/x-c++hdr");
		MIME_TYPES.put("text/x-c++src", "c++");
		MIME_TYPES.put("cpp", "text/x-c++src");
		MIME_TYPES.put("cxx", "text/x-c++src");
		MIME_TYPES.put("h", "text/x-chdr");
		MIME_TYPES.put("htc", "text/x-component");
		MIME_TYPES.put("csh", "text/x-csh");
		MIME_TYPES.put("c", "text/x-csrc");
		MIME_TYPES.put("d", "text/x-dsrc");
		MIME_TYPES.put("hs", "text/x-haskell");
		MIME_TYPES.put("java", "text/x-java");
		MIME_TYPES.put("lhs", "text/x-literate-haskell");
		MIME_TYPES.put("moc", "text/x-moc");
		MIME_TYPES.put("p", "text/x-pascal");
		MIME_TYPES.put("pas", "text/x-pascal");
		MIME_TYPES.put("gcd", "text/x-pcs-gcd");
		MIME_TYPES.put("etx", "text/x-setext");
		MIME_TYPES.put("tcl", "text/x-tcl");
		MIME_TYPES.put("tex", "text/x-tex");
		MIME_TYPES.put("ltx", "text/x-tex");
		MIME_TYPES.put("sty", "text/x-tex");
		MIME_TYPES.put("cls", "text/x-tex");
		MIME_TYPES.put("vcs", "text/x-vcalendar");
		MIME_TYPES.put("vcf", "text/x-vcard");
		MIME_TYPES.put("3gpp", "video/3gpp");
		MIME_TYPES.put("3gp", "video/3gpp");
		MIME_TYPES.put("3g2", "video/3gpp");
		MIME_TYPES.put("dl", "video/dl");
		MIME_TYPES.put("dif", "video/dv");
		MIME_TYPES.put("dv", "video/dv");
		MIME_TYPES.put("fli", "video/fli");
		MIME_TYPES.put("m4v", "video/m4v");
		MIME_TYPES.put("mpeg", "video/mpeg");
		MIME_TYPES.put("mpg", "video/mpeg");
		MIME_TYPES.put("mpe", "video/mpeg");
		MIME_TYPES.put("mp4", "video/mp4");
		MIME_TYPES.put("vob", "video/mpeg");
		MIME_TYPES.put("qt", "video/quicktime");
		MIME_TYPES.put("mov", "video/quicktime");
		MIME_TYPES.put("mxu", "video/vnd.mpegurl");
		MIME_TYPES.put("lsf", "video/x-la-asf");
		MIME_TYPES.put("lsx", "video/x-la-asf");
		MIME_TYPES.put("mng", "video/x-mng");
		MIME_TYPES.put("asf", "video/x-ms-asf");
		MIME_TYPES.put("asx", "video/x-ms-asf");
		MIME_TYPES.put("wm", "video/x-ms-wm");
		MIME_TYPES.put("wmv", "video/x-ms-wmv");
		MIME_TYPES.put("wmx", "video/x-ms-wmx");
		MIME_TYPES.put("wvx", "video/x-ms-wvx");
		MIME_TYPES.put("avi", "video/x-msvideo");
		MIME_TYPES.put("movie", "video/x-sgi-movie");
		MIME_TYPES.put("ice", "x-conference/x-cooltalk");
		MIME_TYPES.put("sisx", "x-epoc/x-sisx-app");
		// Some more mime-pairs
		MIME_TYPES.put("rmvb", "video/vnd.rn-realmedia");
		MIME_TYPES.put("rm", "video/vnd.rn-realmedia");
		MIME_TYPES.put("rv", "video/vnd.rn-realvideo");
		MIME_TYPES.put("flv", "video/x-flv");
		MIME_TYPES.put("hlv", "video/x-flv");
		MIME_TYPES.put("mkv", "video/x-matroska");
		MIME_TYPES.put("ra", "audio/vnd.rn-realaudio");
		MIME_TYPES.put("ram", "audio/vnd.rn-realaudio");
		MIME_TYPES.put("lrc", "text/plain");
	}
}

