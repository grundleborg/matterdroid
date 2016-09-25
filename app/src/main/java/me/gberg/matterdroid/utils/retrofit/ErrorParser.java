package me.gberg.matterdroid.utils.retrofit;

import java.io.IOException;
import java.lang.annotation.Annotation;

import me.gberg.matterdroid.model.APIError;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ErrorParser {

    private Retrofit retrofit;

    public ErrorParser(Retrofit retrofit) {
        this.retrofit = retrofit;
    }

    public APIError parseError(Response<?> response) {
        Converter<ResponseBody, APIError> converter =
                retrofit.responseBodyConverter(APIError.class, new Annotation[0]);

        APIError error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            // FIXME: Should this instead return an invalid APIError()?
            return null;
        }

        return error;
    }
}
