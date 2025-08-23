package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class PaginatedDTO<T> {
    @JsonProperty("total-count")
    public long totalCount;
    @JsonProperty
    public List<T> objects;
}
