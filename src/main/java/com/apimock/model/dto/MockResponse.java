package com.apimock.model.dto;

import lombok.Data;

@Data
public class MockResponse<T> {

    private int code;
    private String message;
    private T data;

    public static <T> MockResponse<T> ok(T data) {
        MockResponse<T> resp = new MockResponse<>();
        resp.setCode(200);
        resp.setMessage("success");
        resp.setData(data);
        return resp;
    }

    public static <T> MockResponse<T> ok() {
        return ok(null);
    }

    public static <T> MockResponse<T> error(int code, String message) {
        MockResponse<T> resp = new MockResponse<>();
        resp.setCode(code);
        resp.setMessage(message);
        return resp;
    }
}
