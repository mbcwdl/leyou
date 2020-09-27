package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 4, max = 12, message = "用户名长度为4-12位")
    private String username;// 用户名

    @Size(min = 8, max = 16, message = "密码长度为8-16位")
    @JsonIgnore
    private String password;// 密码


    @Pattern(regexp = "^1[3456789]\\d{9}$", message = "手机号格式错误")
    private String phone;// 电话

    @Past
    private Date created;// 创建时间

    @JsonIgnore
    private String salt;// 密码的盐值
}