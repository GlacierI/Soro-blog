package com.glacier.soroblog.xo.service;

import com.glacier.soroblog.commons.entity.SystemConfig;
import com.glacier.soroblog.xo.vo.SystemConfigVO;
import com.glacier.soroblog.base.service.SuperService;

import java.util.List;

/**
 * 系统配置表 服务类
 *
 * @author 陌溪
 * @datge 2020年1月21日09:05:53
 */
public interface SystemConfigService extends SuperService<SystemConfig> {

    /**
     * 获取系统配置
     *
     * @return
     */
    public SystemConfig getConfig();

    /**
     * 通过Key前缀清空Redis缓存
     *
     * @param key
     * @return
     */
    public String cleanRedisByKey(List<String> key);

    /**
     * 修改系统配置
     *
     * @param systemConfigVO
     * @return
     */
    public String editSystemConfig(SystemConfigVO systemConfigVO);

    /**
     * 获取系统配置中的搜索模式
     * @return
     */
    public String getSearchModel();

}
