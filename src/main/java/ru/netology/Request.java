package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
    private final Map<String, List<String>> postParams;
    private final byte[] bodyBytes;

    public Request(String method, String path, Map<String, String> headers, InputStream body, String query) {
        this.method = method;
        this.path = path;
        this.headers = Collections.unmodifiableMap(new HashMap<>(headers));

        try {
            this.bodyBytes = body.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Error reading request body", e);
        }
        this.body = new ByteArrayInputStream(bodyBytes);
        this.queryParams = parseQuery(query);
        this.postParams = parseFormData();
    }

    private Map<String, List<String>> parseQuery(String query) {
        Map<String, List<String>> params = new HashMap<>();
        //TODO Данные строки удаляем
        /*if (query == null || query.isEmpty()) {
            return params;
        }*/

        List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        for (NameValuePair pair : pairs) {
            params.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                    .add(pair.getValue());
        }
        return params;
    }

    private Map<String, List<String>> parseFormData() {
        Map<String, List<String>> params = new HashMap<>();
        String contentType = headers.get("content-type");
        if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            String bodyStr = new String(bodyBytes, StandardCharsets.UTF_8);
            List<NameValuePair> pairs = URLEncodedUtils.parse(bodyStr, StandardCharsets.UTF_8);
            for (NameValuePair pair : pairs) {
                params.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                        .add(pair.getValue());
            }
        }
        return params;
    }

    public List<String> getQueryParam(String name) {
        return queryParams.getOrDefault(name, Collections.emptyList());
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public List<String> getPostParam(String name) {
        return postParams.getOrDefault(name, Collections.emptyList());
    }

    public Map<String, List<String>> getPostParams() {
        return Collections.unmodifiableMap(postParams);
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

