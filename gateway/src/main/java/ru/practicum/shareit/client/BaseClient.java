package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

public abstract class BaseClient {

    protected final RestTemplate restTemplate;

    @Value("${shareit-server.url:http://localhost:9090}")
    protected String serverUrl;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private ResponseEntity<Object> execute(RequestEntity<?> request) {
        try {
            return restTemplate.exchange(request, Object.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        }
    }

    protected ResponseEntity<Object> get(String path) {
        RequestEntity<Void> request = RequestEntity
                .get(serverUrl + path)
                .build();
        return execute(request);
    }

    protected ResponseEntity<Object> post(String path, Object body) {
        RequestEntity<Object> request = RequestEntity
                .post(serverUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
        return execute(request);
    }

    protected ResponseEntity<Object> patch(String path, Object body) {
        RequestEntity<Object> request = RequestEntity
                .method(HttpMethod.PATCH, serverUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
        return execute(request);
    }

    protected ResponseEntity<Object> delete(String path) {
        RequestEntity<Void> request = RequestEntity
                .delete(serverUrl + path)
                .build();
        return execute(request);
    }

    protected ResponseEntity<Object> getWithHeader(String path, Long userId) {
        RequestEntity<Void> request = RequestEntity
                .get(serverUrl + path)
                .header("X-Sharer-User-Id", userId.toString())
                .build();
        return execute(request);
    }

    protected ResponseEntity<Object> getWithParam(String path) {
        return get(path);
    }

    protected ResponseEntity<Object> postWithHeader(String path, Object body, Long userId) {
        RequestEntity<Object> request = RequestEntity
                .post(serverUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Sharer-User-Id", userId.toString())
                .body(body);
        return execute(request);
    }

    protected ResponseEntity<Object> patchWithHeader(String path, Object body, Long userId) {
        RequestEntity<Object> request = RequestEntity
                .method(HttpMethod.PATCH, serverUrl + path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Sharer-User-Id", userId.toString())
                .body(body);
        return execute(request);
    }

    protected ResponseEntity<Object> deleteWithHeader(String path, Long userId) {
        RequestEntity<Void> request = RequestEntity
                .delete(serverUrl + path)
                .header("X-Sharer-User-Id", userId.toString())
                .build();
        return execute(request);
    }
}