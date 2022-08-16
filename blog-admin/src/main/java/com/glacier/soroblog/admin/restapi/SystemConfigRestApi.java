package com.glacier.soroblog.admin.restapi;


import com.glacier.soroblog.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.glacier.soroblog.admin.annotion.OperationLogger.OperationLogger;
import com.glacier.soroblog.utils.ResultUtil;
import com.glacier.soroblog.xo.service.SystemConfigService;
import com.glacier.soroblog.xo.vo.SystemConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置表 RestApi
 *
 * @author 陌溪
 * @date 2020年1月21日09:24:37
 */
@Api(value = "系统配置相关接口", tags = {"系统配置相关接口"})
@RestController
@RequestMapping("/systemConfig")
@Slf4j
public class SystemConfigRestApi {

    @Autowired
    private SystemConfigService systemConfigService;

    @AuthorityVerify
    @ApiOperation(value = "获取系统配置", notes = "获取系统配置")
    @GetMapping("/getSystemConfig")
    public String getSystemConfig() {
        return ResultUtil.successWithData(systemConfigService.getConfig());
    }

    @AuthorityVerify
    @ApiOperation(value = "通过Key前缀清空Redis缓存", notes = "通过Key前缀清空Redis缓存")
    @PostMapping("/cleanRedisByKey")
    public String cleanRedisByKey(@RequestBody List<String> key) {
        return systemConfigService.cleanRedisByKey(key);
    }

    @AuthorityVerify
    @OperationLogger(value = "修改系统配置")
    @ApiOperation(value = "修改系统配置", notes = "修改系统配置")
    @PostMapping("/editSystemConfig")
    public String editSystemConfig(@RequestBody SystemConfigVO systemConfigVO) {
        return systemConfigService.editSystemConfig(systemConfigVO);
    }
}

