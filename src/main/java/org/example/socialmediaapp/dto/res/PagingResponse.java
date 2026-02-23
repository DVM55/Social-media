package org.example.socialmediaapp.dto.res;

import java.util.List;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagingResponse<T> {
    private int code;
    private String message;
    private long totalElements;
    private List<T> data;
}
