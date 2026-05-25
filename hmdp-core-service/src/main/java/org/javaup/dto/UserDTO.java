package org.javaup.dto;

import lombok.Data;
/**
 * @description: 用户-入参
 * @maintainer: lrb
 **/
@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}
