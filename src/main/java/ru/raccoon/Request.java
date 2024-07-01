package ru.raccoon;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.http.*;
import org.apache.http.client.utils.URLEncodedUtils;


public class Request {

    private final String requestMethod;
    private final String requestPath;
    private final List<NameValuePair> requestParams;

    public Request(String requestMethod, String requestPath) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
        this.requestParams = URLEncodedUtils.parse(requestPath.substring(requestPath.indexOf('?') + 1), Charset.defaultCharset());
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath.substring(0, requestPath.indexOf('?'));
    }

    public List<NameValuePair> getQueryParams() {
        return requestParams;
    }

    public String getQueryParam(String name) {

        List<NameValuePair> nameValuePairList = getQueryParams();
        for (NameValuePair pair : nameValuePairList) {
            if (pair.getName().equals(name)) {
                return pair.toString();
            }
        }
        return "Параметра " + name + " нет в Query";
    }
}
