package com.kolapsis.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

public class FileUtils {

	public static void copyFile(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);
		copyStream(in, out);
		CloseUtils.closeQuietly(in);
		CloseUtils.closeQuietly(out);
	}

	public static void copyStream(InputStream in, OutputStream out) throws IOException {
		byte[] data = new byte[8 * 1024];
		int numBytes;
		while ((numBytes = in.read(data)) > 0) {
			out.write(data, 0, numBytes);
		}
	}
	
	public static abstract class CountingOutputStream extends FilterOutputStream {

		private OutputStream wrappedOutputStream;

		public CountingOutputStream(final OutputStream out) {
			super(out);
			wrappedOutputStream = out;
		}

		public void write(byte[] b, int off, int len) throws IOException {
			wrappedOutputStream.write(b,off,len);
			onWrite(len);
		}

		public void write(int b) throws IOException {
			super.write(b);
		}
		
		@Override
		public void close() throws IOException {
			wrappedOutputStream.close();
			super.close();
		}
		
		public abstract void onWrite(int len);
	}
	
	public static class CountingMultiPartEntity extends MultipartEntity {

		private HttpData.ProgressListener mListener;
		private CountingOutputStream mOutputStream;
		private OutputStream mLastOutputStream;

		public CountingMultiPartEntity(HttpData.ProgressListener listener) {
			super(HttpMultipartMode.BROWSER_COMPATIBLE);
			mListener = listener;
		}
		public CountingMultiPartEntity(HttpMultipartMode mode, String boundary, Charset chars, HttpData.ProgressListener listener) {
			super(mode, boundary, chars);
			mListener = listener;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			// If we have yet to create the CountingOutputStream, or the
			// OutputStream being passed in is different from the OutputStream used
			// to create the current CountingOutputStream
			if ((mLastOutputStream == null) || (mLastOutputStream != out)) {
				mLastOutputStream = out;
				mOutputStream = new CountingOutputStream(out){
					@Override
					public void onWrite(int len) {
						mListener.transferred(len);
					}
				};
			}
			super.writeTo(mOutputStream);
		}
	}
}
