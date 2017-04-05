package com.rockchip.echo.smartecho.nlu.aiapi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.rockchip.echo.smartecho.nlu.Nlu;
import com.rockchip.echo.smartecho.nlu.NluError;
import com.rockchip.echo.smartecho.nlu.NluListener;
import com.rockchip.echo.smartecho.nlu.NluResult;
import com.rockchip.echo.util.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.GsonFactory;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIEvent;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;

public class ApiaiNlu extends Nlu {

    private AIDataService aiDataService;

    public ApiaiNlu(Context context) {
        super(context);
        initApiaiNlu();
    }

    public ApiaiNlu(Context context, NluListener listener) {
        super(context, listener);
        initApiaiNlu();
    }

    @Override
    public void startProcess(String text) {
        requestApiaiNluProcess(text);
    }

    private void initApiaiNlu() {
        LanguageConfig selectedLanguage = Config.languages[0];
        final AIConfiguration.SupportedLanguages lang = AIConfiguration.SupportedLanguages.fromLanguageTag(selectedLanguage.getLanguageCode());
        final AIConfiguration config = new AIConfiguration(selectedLanguage.getAccessToken(),
                lang,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(mContext, config);
    }

    private void requestApiaiNluProcess(String text) {
        final AsyncTask<String, Void, AIResponse> task = new AsyncTask<String, Void, AIResponse>() {

            private AIError aiError;

            @Override
            protected AIResponse doInBackground(final String... params) {
                final AIRequest request = new AIRequest();
                String query = params[0];
                String event = params[1];

                if (!TextUtils.isEmpty(query))
                    request.setQuery(query);
                if (!TextUtils.isEmpty(event))
                    request.setEvent(new AIEvent(event));
                final String contextString = params[2];
                RequestExtras requestExtras = null;
                if (!TextUtils.isEmpty(contextString)) {
                    final List<AIContext> contexts = Collections.singletonList(new AIContext(contextString));
                    requestExtras = new RequestExtras(contexts, null);
                }

                try {
                    return aiDataService.request(request, requestExtras);
                } catch (final AIServiceException e) {
                    aiError = new AIError(e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(final AIResponse response) {
                if (response != null) {
                    onResult(response);
                } else {
                    onError(aiError);
                }
            }
        };
        task.execute(text, "", "");
    }

    private Gson gson = GsonFactory.getGson();
    private void onResult(final AIResponse response) {

        LogUtil.d("onResult");
        LogUtil.d(gson.toJson(response));
        LogUtil.d("Received success response");

        // this is example how to get different parts of result object
        final Status status = response.getStatus();
        LogUtil.d("Status code: " + status.getCode());
        LogUtil.d("Status type: " + status.getErrorType());

        final Result result = response.getResult();
        LogUtil.d("Resolved query: " + result.getResolvedQuery());

        LogUtil.d("Action: " + result.getAction());

        final String speech = result.getFulfillment().getSpeech();
        LogUtil.d("Speech: " + speech);

//        startTtsOutput(speech);
        if (mNluLinstener != null) {
            NluResult res = new NluResult();
            res.setIntent(NluResult.NluIntent.SPEAK_TEXT);
            Bundle resBundle = new Bundle();
            resBundle.putString("TEXT", speech);
            res.setData(resBundle);
            mNluLinstener.onResult(res);
        }

        final Metadata metadata = result.getMetadata();
        if (metadata != null) {
            LogUtil.d("Intent id: " + metadata.getIntentId());
            LogUtil.d("Intent name: " + metadata.getIntentName());
        }

        final HashMap<String, JsonElement> params = result.getParameters();
        if (params != null && !params.isEmpty()) {
            LogUtil.d("Parameters: ");
            for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                LogUtil.d(String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
            }
        }
    }

    private void onError(final AIError error) {
        LogUtil.d(error.toString());
        if (mNluLinstener != null) {
            mNluLinstener.onError(new NluError());
        }
    }
}
