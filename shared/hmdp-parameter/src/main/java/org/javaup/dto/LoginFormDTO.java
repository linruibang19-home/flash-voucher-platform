package org.javaup.dto;

import lombok.Data;

/**
 * @description: 鐧诲綍-鍏ュ弬
 * @maintainer: lrb
 **/
@Data
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}
