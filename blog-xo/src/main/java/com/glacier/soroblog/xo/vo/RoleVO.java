package com.glacier.soroblog.xo.vo;

import com.glacier.soroblog.base.validator.annotion.NotBlank;
import com.glacier.soroblog.base.validator.group.Insert;
import com.glacier.soroblog.base.validator.group.Update;
import com.glacier.soroblog.base.vo.BaseVO;
import lombok.Data;

/**
 * <p>
 * RoleVO
 * </p>
 *
 * @author 陌溪
 * @since 2019年12月20日16:47:02
 */
@Data
public class RoleVO extends BaseVO<RoleVO> {


    /**
     * 角色名称
     */
    @NotBlank(groups = {Insert.class, Update.class})
    private String roleName;

    /**
     * 介绍
     */
    private String summary;

    /**
     * 该角色所能管辖的区域
     */
    private String categoryMenuUids;

}
