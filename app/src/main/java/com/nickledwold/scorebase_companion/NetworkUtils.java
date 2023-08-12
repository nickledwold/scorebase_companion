package com.nickledwold.scorebase_companion;
import java.io.IOException;

import okhttp3.*;

public class NetworkUtils {

    private static final int MAX_RETRY_ATTEMPTS = 3; // Number of retry attempts

    public static void performPostRequestWithRetry(String url, FormBody formBody) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        performRequestWithRetry(client, request, 0);
    }

    private static void performRequestWithRetry(final OkHttpClient client, final Request request, final int retryCount) {
        if (retryCount >= MAX_RETRY_ATTEMPTS) {
            // Retry limit reached, stop retrying
            return;
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle failure
                e.printStackTrace();
                // Retry the request with exponential backoff
                int exponentialBackoffTime = (int) Math.pow(2, retryCount) * 1000;
                try {
                    Thread.sleep(exponentialBackoffTime);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                performRequestWithRetry(client, request, retryCount + 1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Handle response
                if (!response.isSuccessful()) {
                    performRequestWithRetry(client, request, retryCount + 1);
                }
            }
        });
    }
}

