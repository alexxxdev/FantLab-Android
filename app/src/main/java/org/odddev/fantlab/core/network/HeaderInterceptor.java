package org.odddev.fantlab.core.network;

import org.odddev.fantlab.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Developer: Ivan Zolotarev
 * Date: 15.09.16
 */

public class HeaderInterceptor implements Interceptor {

    private final static String APP_VERSION = "Android/" + BuildConfig.VERSION_NAME;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("User-Agent", APP_VERSION)
                .method(original.method(), original.body());
        return chain.proceed(builder.build());
    }
}