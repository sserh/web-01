package ru.raccoon;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.http.*;
import org.apache.http.client.utils.URLEncodedUtils;


public class Request {

    private final String requestMethod;
    private final String requestPath;

    public Request(String requestMethod, String requestPath) {
        this.requestMethod = requestMethod;
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestPath() {
        return requestPath.substring(0, requestPath.indexOf('?'));
    }

    public List<NameValuePair> getQueryParams() {
        return URLEncodedUtils.parse(requestPath.substring(requestPath.indexOf('?') + 1), Charset.defaultCharset());
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
