package com.kolapsis.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import com.kolapsis.utils.FileUtils.CountingMultiPartEntity;

import android.util.Log;

public class HttpData extends Thread {

	private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	public static String generateBoundary() {
		StringBuilder buffer = new StringBuilder();
		Random rand = new Random();
		int count = rand.nextInt(11) + 30; // a random size from 30 to 40
		for (int i = 0; i < count; i++) {
			buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		return buffer.toString();
	}

	public static final class HttpDataException extends Exception {
		private static final long serialVersionUID = 4248039827913459855L;
		public HttpDataException(String string) {
			super(string);
		}
		public HttpDataException(String string, StackTraceElement[] stack) {
			super(string);
			super.setStackTrace(stack);
		}
	}
	
	public static interface ProgressListener {
		public void transferred(long transferred);
	}

	private static AbstractHttpClient getDefaultHttpClient() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, false);
		return new DefaultHttpClient(params);
	}

	private final String url;
	private boolean loadWithRedirect = false;
	private final ArrayList<BasicHeader> headers;
	private ArrayList<NameValuePair> data;
	private final HashMap<String, File> files;
	private JSONObject jsonData;

	private AbstractHttpClient client;
	private HttpResponse response;
	private HttpUriRequest request;
	private HttpEntity entity;
	private InputStream stream;
	private String content;
	private JSONObject json;
	private ProgressListener mListener;
	
	public HttpData(String url) {
		this(getDefaultHttpClient(), url);
	}
	public HttpData(AbstractHttpClient client, String url) {
		this.client = client;
		this.url = url;
		this.headers = new ArrayList<BasicHeader>();
		this.headers.add(new BasicHeader("Cache-Control", "no-cache"));
		this.data = new ArrayList<NameValuePair>();
		this.files = new HashMap<String, File>();
	}
	
	public void logData() {
		for (int i = 0; i < data.size(); i++) {
			Log.v("Log data http", ""+data.get(i).getName() + "="+data.get(i).getValue());
		}
	}

	public void consume() {
		CloseUtils.closeQuietly(stream);
		if (entity != null) {
			try { entity.consumeContent(); }
			catch (IOException e) {}
		}
		if (request != null) request.abort();
	}
	
	public HttpData setProgressListener(ProgressListener l) {
		mListener = l;
		return this;
	}

	public HttpData header(String name, String value) {
		this.headers.add(new BasicHeader(name,value));
		return this;
	}

	public Header header(String name) {
		if (this.response == null) return null;
		Header header = this.response.getFirstHeader(name);
		return header;
	}
	public Header[] headers() {
		if (this.response != null) return this.response.getAllHeaders();
		else {
			Header[] headers = new Header[0];
			headers = this.headers.toArray(headers);
			return headers;
		}
	}

	public HttpData data(String name, String value) {
		this.data.add(new BasicNameValuePair(name,value));
		return this;
	}
	public HttpData data(ArrayList<NameValuePair> value) {
		this.data = value;
		return this;
	}
	public HttpData data(JSONObject value) {
		this.jsonData = value;
		return this;
	}
	
	public String data(String name) {
		for (NameValuePair dt : this.data) {
			if (dt.getName().equals(name)) return dt.getValue();
		}
		return null;
	}

	public HttpData file(File fl) {
		return file(fl.getName(), fl);
	}
	public HttpData file(String name, File fl) {
		this.files.put(name, fl);
		return this;
	}
	
	public HttpData head() throws HttpDataException {
		return head(60000);
	}
	public HttpData head(final int timeOut) throws HttpDataException {
		return head(timeOut, null);
	}
	public HttpData head(final HttpContext context) throws HttpDataException {
		return head(60000, context);
	}
	public HttpData head(final int timeOut, final HttpContext context) throws HttpDataException {
		HttpHead request = new HttpHead(this.url);
		doQuery(context, request, timeOut);
		return this;
	}

	public HttpData get() throws HttpDataException {
		return get(null);
	}
	public HttpData get(final HttpContext context) throws HttpDataException {
		return get(context, false);
	}
	public HttpData get(final HttpContext context, final boolean redirect) throws HttpDataException {
		//System.out.print(".get()\n");
		this.loadWithRedirect = redirect;
		this.headers.add(new BasicHeader("Content-Type","application/x-www-form-urlencoded"));
		this.headers.add(new BasicHeader("X-Requested-With","XMLHttpRequest"));
		String finalUrl = this.url.toString();
		for (NameValuePair dt : this.data) {
			int index = finalUrl.indexOf("?");
			finalUrl += (index == -1 ? "?" : "&") + dt.getName() + "=" + dt.getValue();
		}
		HttpGet request = new HttpGet(finalUrl);
		doQuery(context, request);
		this.loadWithRedirect = false;
		return this;
	}

	public HttpData post() throws HttpDataException {
		return post(null);
	}
	public HttpData post(final HttpContext context) throws HttpDataException {
		return post(context, false);
	}
	public HttpData post(final HttpContext context, final boolean redirect) throws HttpDataException {
		this.loadWithRedirect = redirect;
		this.headers.add(new BasicHeader("X-Requested-With","XMLHttpRequest"));
		HttpPost request = new HttpPost(this.url);
		try {
			if (this.files.size() > 0) {
				String boundary = generateBoundary();
				Charset chars = Charset.forName(HTTP.UTF_8);
				CountingMultiPartEntity reqEntity = new CountingMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, boundary, chars, mListener);
				if (this.data.size() > 0) {
					for (NameValuePair pair : this.data) {
						reqEntity.addPart(pair.getName(), new StringBody(pair.getValue()));
					}
				}
				for (String key : this.files.keySet()) {
					File file = this.files.get(key);
					String name = file.getName();
					//String ext = MimeTypeMap.getFileExtensionFromUrl(name);

					String mimeType = StringUtils.getMimeType(name); //MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

					FileBody fb = new FileBody(file, mimeType);
					//Log.v(TAG, "key: "+key+", name: "+fb.getFilename()+", mediatype: "+fb.getMediaType()+", length: "+fb.getContentLength());
					reqEntity.addPart(key, fb);
				}
				request.setEntity(reqEntity);
			} else if (jsonData != null) {
				this.headers.add(new BasicHeader("Content-Type","application/json; charset=utf-8"));
				request.setEntity(new StringEntity(jsonData.toString(), HTTP.UTF_8));
			} else if (this.data.size() > 0) {
				this.headers.add(new BasicHeader("Content-Type","application/x-www-form-urlencoded; charset=utf-8"));
				//request.setEntity(new StringEntity(dataToJSON().toString(), HTTP.UTF_8));
				request.setEntity(new StringEntity(URLEncodedUtils.format(this.data, HTTP.UTF_8)));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		}
		doQuery(context, request);
		this.loadWithRedirect = false;
		return this;
	}

	public HttpData put() throws HttpDataException {
		return put(null);
	}
	public HttpData put(final HttpContext context) throws HttpDataException {
		return put(context, false);
	}
	public HttpData put(final HttpContext context, final boolean redirect) throws HttpDataException {
		this.loadWithRedirect = redirect;
		this.headers.add(new BasicHeader("X-Requested-With","XMLHttpRequest"));
		HttpPut request = new HttpPut(this.url);
		try {
			if (jsonData != null) {
				this.headers.add(new BasicHeader("Content-Type","application/json; charset=utf-8"));
				request.setEntity(new StringEntity(jsonData.toString(), HTTP.UTF_8));
			} else if (this.data.size() > 0) {
				this.headers.add(new BasicHeader("Content-Type","application/x-www-form-urlencoded; charset=utf-8"));
				request.setEntity(new UrlEncodedFormEntity(this.data, HTTP.UTF_8));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
		}
		doQuery(context, request);
		this.loadWithRedirect = false;
		return this;
	}

	public HttpData delete() throws HttpDataException {
		return delete(null);
	}
	public HttpData delete(final HttpContext context) throws HttpDataException {
		return delete(context, false);
	}
	public HttpData delete(final HttpContext context, final boolean redirect) throws HttpDataException {
		this.loadWithRedirect = redirect;
		this.headers.add(new BasicHeader("X-Requested-With","XMLHttpRequest"));
		HttpDelete request = new HttpDelete(this.url);
		doQuery(context, request);
		this.loadWithRedirect = false;
		return this;
	}

	private void doQuery(final HttpContext context, HttpUriRequest request) throws HttpDataException {
		doQuery(context, request, 60000);
	}
	private void doQuery(final HttpContext context, HttpUriRequest request, int timeOut) throws HttpDataException {
		try {
			//Log.v(TAG, "doQuery url: " + this.url);
			if (this.url.startsWith("https://")) client = (AbstractHttpClient) sslClient(client);
			HttpConnectionParams.setConnectionTimeout(client.getParams(), timeOut);
			for (BasicHeader header : this.headers) request.addHeader(header);
			this.request = request;
			this.response = client.execute(this.request, context);
			if (this.loadWithRedirect) {
				String base = this.url.substring(0, this.url.lastIndexOf("/")+1);
				while (isRedirect()) {
					String redirect = this.response.getFirstHeader("Location").getValue();
					if (!StringUtils.isEmpty(redirect)) {
						this.consume();
						if (!redirect.startsWith("http")) redirect = base + redirect;
						//Log.v(TAG, "-> redirect: " + redirect);
						this.request = new HttpGet(redirect);
						for (BasicHeader header : this.headers) this.request.addHeader(header);
						this.response = client.execute(this.request, context);
					}
				}
			}
			//Log.v(TAG, " -> status: " + this.response.getStatusLine());
		} catch (OutOfMemoryError e) {
			throw new HttpDataException("OutOfMemoryError on doQuery");
		} catch (ClientProtocolException e) {
			throw new HttpDataException("ClientProtocolException on doQuery", e.getStackTrace());
		} catch (UnknownHostException e) {
			throw new HttpDataException("UnknownHostException on doQuery", e.getStackTrace());
		} catch (Exception e) {
			throw new HttpDataException("Exception on doQuery", e.getStackTrace());
		}
	}

	public boolean isRedirect() {
		int status = this.response.getStatusLine().getStatusCode();
		return (status == HttpStatus.SC_MOVED_PERMANENTLY || status == HttpStatus.SC_MOVED_TEMPORARILY) && this.response.containsHeader("Location");
	}
	
	public boolean isHttpOK() {
		return this.response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
	}
	public boolean isHttpUnauthorized() {
		int status = this.response.getStatusLine().getStatusCode();
		return status == HttpStatus.SC_UNAUTHORIZED || status == HttpStatus.SC_FORBIDDEN;
	}

	public HttpResponse asResponse() {
		return this.response;
	}
	public HttpEntity asHttpEntity() {
		if (entity == null) this.entity = this.asResponse().getEntity();
		return entity;
	}
	public InputStream asInputStream() {
		if (stream == null) {
			try {
				this.stream = this.asHttpEntity().getContent();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return stream;
	}

	public String asString() {
		return asString(HTTP.UTF_8);
	}
	public String asString(String charset) {
		//Log.v(TAG, "asString " + charset + ", content: " + (content != null));
		if (content == null) {
			try {
				BufferedReader r = new BufferedReader(new InputStreamReader(this.asInputStream(), charset));
				StringBuilder total = new StringBuilder(this.asInputStream().available());
				String line;
				while ((line = r.readLine()) != null) {
					//Log.v(TAG, line);
					total.append(line).append("\n");
				}
				this.content = total.toString().trim();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return content;
	}
	public JSONObject asJSONObject() {
		if (this.json == null) {
			try {
				this.json = new JSONObject();
				if (this.asString().startsWith("{") && this.asString().endsWith("}")) this.json = new JSONObject(this.asString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.json;
	}

	public List<Cookie> getCookies() {
		return this.client.getCookieStore().getCookies();
	}

	public List<Cookie> cookies() {
		return this.client.getCookieStore().getCookies();
	}
	public HttpData cookies(List<Cookie> cookies) {
		for (Cookie cookie: cookies){
			this.client.getCookieStore().addCookie(cookie);
		}
		return this;
	}

	public String cookie(String name) {
		String cookieId = null;
		List<Cookie> cookies = this.client.getCookieStore().getCookies();
		for (Cookie cookie: cookies){
			if (cookie.getName().equals(name)){
				cookieId = cookie.getValue();
			}
		}
		return cookieId;
	}
	public String cookie(String name, HttpContext context) {
		CookieStore cookies = (CookieStore) context.getAttribute(ClientContext.COOKIE_STORE);
		for (Cookie c : cookies.getCookies()) {
			//Log.v(TAG, "--> " + c.getName() + " = " + c.getValue());
			if (name.equals(c.getName())) return c.getValue();
		}
		return null;
	}
	
	public HttpData cookie(String name, String value) {
		this.headers.add(new BasicHeader("Cookie", name+"="+value));
		return this;
	}
	
	private HttpClient sslClient(HttpClient client) {
	    try {
	        X509TrustManager tm = new X509TrustManager() { 
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };
	        SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[]{tm}, null);
	        SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
	        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        ClientConnectionManager ccm = client.getConnectionManager();
	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("https", ssf, 443));
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;
	    }
	}
	
	public class MySSLSocketFactory extends SSLSocketFactory {
	     SSLContext sslContext = SSLContext.getInstance("TLS");

	     public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	         super(truststore);

	         TrustManager tm = new X509TrustManager() {
	             public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	             }

	             public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	             }

	             public X509Certificate[] getAcceptedIssuers() {
	                 return null;
	             }
	         };

	         sslContext.init(null, new TrustManager[] { tm }, null);
	     }

	     public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
	        super(null);
	        sslContext = context;
	     }

	     @Override
	     public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	         return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	     }

	     @Override
	     public Socket createSocket() throws IOException {
	         return sslContext.getSocketFactory().createSocket();
	     }
	}
}
