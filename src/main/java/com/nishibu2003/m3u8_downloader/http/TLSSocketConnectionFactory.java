package com.nishibu2003.m3u8_downloader.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class TLSSocketConnectionFactory extends SSLSocketFactory {

	// This implementation uses JSSE with a permissive TrustManager that captures
	// the server certificate chain. It removes dependency on the old
	// BouncyCastle TLS API so the project can upgrade to BC 1.70.

	@Override
	public Socket createSocket(Socket socket, final String host, int port, boolean autoClose)
			throws IOException {
		if (socket == null) {
			socket = new Socket();
		}
		if (!socket.isConnected()) {
			socket.connect(new InetSocketAddress(host, port));
		}

		try {
			final CaptureTrustManager tm = new CaptureTrustManager();
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, new TrustManager[] { tm }, new SecureRandom());
			SSLSocketFactory factory = sc.getSocketFactory();
			SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, host, port, autoClose);

			// set SNI hostname
			try {
				SSLParameters params = sslSocket.getSSLParameters();
				params.setServerNames(java.util.Collections.<SNIServerName>singletonList(new SNIHostName(host)));
				sslSocket.setSSLParameters(params);
			} catch (Throwable ignored) {
				// ignore if SNI not supported on target JVM
			}

			return sslSocket;
		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return ((SSLSocketFactory)SSLSocketFactory.getDefault()).getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return ((SSLSocketFactory)SSLSocketFactory.getDefault()).getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return SSLSocketFactory.getDefault().createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return SSLSocketFactory.getDefault().createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
			throws IOException, UnknownHostException {
		return SSLSocketFactory.getDefault().createSocket(host, port, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
			throws IOException {
		return SSLSocketFactory.getDefault().createSocket(address, port, localAddress, localPort);
	}

	// Simple permissive TrustManager that captures server certificate chain.
	private static class CaptureTrustManager implements X509TrustManager {
		private volatile java.security.cert.X509Certificate[] peerCerts;

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
			// no-op
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
			this.peerCerts = chain; // capture chain; do not throw to allow unknown certs
		}

		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[0];
		}

		public java.security.cert.X509Certificate[] getPeerCertificates() {
			return peerCerts;
		}
	}

}