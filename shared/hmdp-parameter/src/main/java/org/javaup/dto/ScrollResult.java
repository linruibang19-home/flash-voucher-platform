package org.javaup.dto;

import lombok.Data;

import java.util.List;

/**
 * @description: 婊氬姩-缁撴灉
 * @maintainer: lrb
 **/
@Data
public class ScrollResult {
    private List<?> list;
    private Long minTime;
    private Integer offset;
}
