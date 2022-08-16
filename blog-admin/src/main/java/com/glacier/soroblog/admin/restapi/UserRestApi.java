package com.glacier.soroblog.admin.restapi;


import com.glacier.soroblog.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.glacier.soroblog.admin.annotion.OperationLogger.OperationLogger;
import com.glacier.soroblog.utils.ResultUtil;
import com.glacier.soroblog.xo.service.UserService;
import com.glacier.soroblog.xo.vo.UserVO;
import com.glacier.soroblog.base.exception.ThrowableUtils;
import com.glacier.soroblog.base.validator.group.Delete;
import com.glacier.soroblog.base.validator.group.GetList;
import com.glacier.soroblog.base.validator.group.Insert;
import com.glacier.soroblog.base.validator.group.Update;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户表 RestApi
 *
 * @author 陌溪
 * @date 2020年1月4日21:29:09
 */
@RestController
@Api(value = "用户相关接口", tags = {"用户相关接口"})
@RequestMapping("/user")
@Slf4j
public class UserRestApi {

    @Autowired
    private UserService userService;

    @AuthorityVerify
    @ApiOperation(value = "获取用户列表", notes = "获取用户列表", response = String.class)
    @PostMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("获取用户列表: {}", userVO);
        return ResultUtil.successWithData(userService.getPageList(userVO));
    }

    @AuthorityVerify
    @OperationLogger(value = "新增用户")
    @ApiOperation(value = "新增用户", notes = "新增用户", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("新增用户: {}", userVO);
        return userService.addUser(userVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑用户")
    @ApiOperation(value = "编辑用户", notes = "编辑用户", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody UserVO userVO, BindingResult result) {
        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("编辑用户: {}", userVO);
        return userService.editUser(userVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除用户")
    @ApiOperation(value = "删除用户", notes = "删除用户", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("删除用户: {}", userVO);
        return userService.deleteUser(userVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "重置用户密码")
    @ApiOperation(value = "重置用户密码", notes = "重置用户密码", response = String.class)
    @PostMapping("/resetUserPassword")
    public String resetUserPassword(@Validated({Delete.class}) @RequestBody UserVO userVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("重置用户密码: {}", userVO);
        return userService.resetUserPassword(userVO);
    }
}