package com.glacier.soroblog.admin.restapi;

import com.glacier.soroblog.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.glacier.soroblog.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.glacier.soroblog.admin.annotion.OperationLogger.OperationLogger;
import com.glacier.soroblog.utils.ResultUtil;
import com.glacier.soroblog.xo.service.WebNavbarService;
import com.glacier.soroblog.xo.vo.WebNavbarVO;
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
import org.springframework.web.bind.annotation.*;

/**
 * 门户导航栏管理
 *
 * @author 陌溪
 * @date 2021年2月22日16:30:38
 */
@RestController
@RequestMapping("/webNavbar")
@Api(value = "门户导航栏管理", tags = {"门户导航栏相关接口"})
@Slf4j
public class WebNavbarRestApi {

    @Autowired
    private WebNavbarService webNavbarService;

    @AuthorityVerify
    @ApiOperation(value = "获取门户导航栏列表", notes = "获取门户导航栏列表", response = String.class)
    @GetMapping("/getList")
    public String getList(@Validated({GetList.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return ResultUtil.successWithData(webNavbarService.getPageList(webNavbarVO));
    }

    @ApiOperation(value = "获取门户导航栏所有列表", notes = "获取门户导航栏所有列表", response = String.class)
    @GetMapping("/getAllList")
    public String getAllList() {
        return ResultUtil.successWithData(webNavbarService.getAllList());
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @OperationLogger(value = "增加门户导航栏")
    @ApiOperation(value = "增加门户导航栏", notes = "增加门户导航栏", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("增加门户导航栏");
        return webNavbarService.addWebNavbar(webNavbarVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "编辑门户导航栏")
    @ApiOperation(value = "编辑门户导航栏", notes = "编辑门户导航栏", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("编辑门户导航栏");
        return webNavbarService.editWebNavbar(webNavbarVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除门户导航栏")
    @ApiOperation(value = "删除门户导航栏", notes = "删除门户导航栏", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody WebNavbarVO webNavbarVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("批量删除门户导航栏");
        return webNavbarService.deleteWebNavbar(webNavbarVO);
    }
}

