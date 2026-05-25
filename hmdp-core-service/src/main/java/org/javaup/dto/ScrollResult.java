package org.javaup.dto;

import lombok.Data;

import java.util.List;

/**
 * @description: 滚动-结果
 * @maintainer: lrb
 **/
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
