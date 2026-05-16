package com.campus2hand.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private int page;
    private int size;
    private long total;
    private int pages;
    private List<T> list;

    public static <T> PageResponse<T> of(List<T> list, long total, int page, int size) {
        int pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResponse<>(page, size, total, pages, list);
    }
}