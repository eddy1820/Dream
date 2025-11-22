package com.eddy.dream.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * Current page content
     */
    private List<T> content;
    
    /**
     * Current page number (0-based)
     */
    private int pageNumber;
    
    /**
     * Page size
     */
    private int pageSize;
    
    /**
     * Total number of elements
     */
    private long totalElements;
    
    /**
     * Total number of pages
     */
    private int totalPages;
    
    /**
     * Is this the first page?
     */
    private boolean first;
    
    /**
     * Is this the last page?
     */
    private boolean last;
    
    /**
     * Does it have next page?
     */
    private boolean hasNext;
    
    /**
     * Does it have previous page?
     */
    private boolean hasPrevious;
}

