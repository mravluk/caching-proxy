package com.roadmaphs.caching_proxy.dto;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class CachedResponse {
    private byte[] body;
    private int responseCode;
    private Map<String, String> headers;
}
