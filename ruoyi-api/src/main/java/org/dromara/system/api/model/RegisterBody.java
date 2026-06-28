package org.dromara.system.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.domain.model.LoginBody;
import org.hibernate.validator.constraints.Length;

/**
 * 用户注册对象
 *
 * @author Lion Li
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RegisterBody extends LoginBody {

    /**
     * 用户名
     */
    @NotBlank(message = "{user.username.not.blank}")
    @Length(min = 2, max = 128, message = "{user.username.length.valid}")
    private String username;

    /**
     * 邮箱
     */
    @Email(message = "{user.email.not.valid}")
    @Length(max = 128, message = "{user.email.length.valid}")
    private String email;

    /**
     * 邮箱验证码
     */
    private String emailCode;

    /**
     * 用户密码
     */
    @NotBlank(message = "{user.password.not.blank}")
    @Length(min = 5, max = 30, message = "{user.password.length.valid}")
//    @Pattern(regexp = RegexConstants.PASSWORD, message = "{user.password.format.valid}")
    private String password;

    /**
     * 用户类型
     */
    private String userType;

}
