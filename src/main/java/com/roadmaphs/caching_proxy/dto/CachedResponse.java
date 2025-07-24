package com.roadmaphs.caching_proxy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CachedResponse {
    private byte[] body;
    private int responseCode;
    private Map<String, String> headers;
}
