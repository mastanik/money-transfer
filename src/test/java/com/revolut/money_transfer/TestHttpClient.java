package com.revolut.money_transfer;

import com.google.common.io.CharStreams;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestHttpClient {

    public static class HttpClientResponse {
        private String body;
        private int status;

        public HttpClientResponse(String body, int status) {
            this.body = body;
            this.status = status;
        }

        public String getBody() {
            return body;
        }

        public int getStatus() {
            return status;
        }
    }

    public HttpClientResponse get(String requestUrl) {
        String response = null;
        int responseCode = -1;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.connect();

            responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode == 200 ? connection.getInputStream() : connection.getErrorStream();
            try (final Reader reader = new InputStreamReader(inputStream)) {
                response = CharStreams.toString(reader);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HttpClientResponse(response, responseCode);
    }

    public HttpClientResponse post(String requestUrl, String payload) {
        byte[] postData = payload.getBytes();
        String response = null;
        int responseCode = -1;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.write(postData);
            }
            responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode == 200 ? connection.getInputStream() : connection.getErrorStream();
            try (final Reader reader = new InputStreamReader(inputStream)) {
                response = CharStreams.toString(reader);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HttpClientResponse(response, responseCode);
    }

}
