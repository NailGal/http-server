package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream body;
    private final Map<String, List<String>> queryParams;

    public Request(String method, String path, Map<String, String> headers, InputStream body, String query) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));
        this.body = body;
        this.queryParams = parseQuery(query);
    }

    private Map<String, List<String>>parseQuery(String query) {
        Map<String, List<String>> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        for (NameValuePair pair : pairs) {
            params.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                    .add(pair.getValue());
        }
        return params;
    }

    public List<String> getQueryParam(String name) {
        return queryParams.getOrDefault(name, Collections.emptyList());
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;

    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getBody() {
        return body;
    }
}

