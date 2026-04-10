package ru.practicum.shareit.client;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class BaseClient {

    protected final RestTemplate restTemplate;
    protected final String serverUrl;

    public BaseClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.serverUrl = System.getenv().getOrDefault("SHAREIT_SERVER_URL", "http://localhost:9090");
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