package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaginatedDTO<T> {
    @JsonProperty("total-count")
    public long totalCount;
    @JsonProperty
    public List<T> objects;

    @SuppressWarnings("unused")
    public PaginatedDTO() {}

    public PaginatedDTO(long totalCount, List<T> objects) {
        this.totalCount = totalCount;
        this.objects = objects;
    }
}
