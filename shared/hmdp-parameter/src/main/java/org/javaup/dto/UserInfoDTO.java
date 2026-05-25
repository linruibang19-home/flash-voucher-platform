package org.javaup.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * @description: 用户详情 DTO，跨服务传输用（无持久化注解）
 * @maintainer: lrb
 **/
@Data
public class UserInfoDTO {

    /** 用户ID */
    private Long userId;

    /** 城市 */
    private String city;

    /** 个人介绍 */
    private String introduce;

    /** 粉丝数 */
    private Integer fans;

    /** 关注数 */
    private Integer followee;

    /** 性别，0：男，1：女 */
    private Boolean gender;

    /** 生日 */
    private LocalDate birthday;

    /** 积分 */
    private Integer credits;

    /** 会员等级，0~9，0 表示未开通 */
    private Integer level;
}
