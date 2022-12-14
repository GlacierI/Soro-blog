package com.glacier.soroblog.admin.restapi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.glacier.soroblog.admin.global.RedisConf;
import com.glacier.soroblog.admin.global.MessageConf;
import com.glacier.soroblog.admin.global.SQLConf;
import com.glacier.soroblog.admin.global.SysConf;
import com.glacier.soroblog.commons.config.jwt.Audience;
import com.glacier.soroblog.commons.config.jwt.JwtTokenUtil;
import com.glacier.soroblog.commons.entity.Admin;
import com.glacier.soroblog.commons.entity.CategoryMenu;
import com.glacier.soroblog.commons.entity.OnlineAdmin;
import com.glacier.soroblog.commons.entity.Role;
import com.glacier.soroblog.commons.feign.PictureFeignClient;
import com.glacier.soroblog.utils.*;
import com.moxi.mogublog.utils.*;
import com.glacier.soroblog.xo.service.AdminService;
import com.glacier.soroblog.xo.service.CategoryMenuService;
import com.glacier.soroblog.xo.service.RoleService;
import com.glacier.soroblog.xo.service.WebConfigService;
import com.glacier.soroblog.xo.utils.WebUtil;
import com.glacier.soroblog.base.enums.EMenuType;
import com.glacier.soroblog.base.enums.EStatus;
import com.glacier.soroblog.base.global.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * ???????????? RestApi????????????????????????security???????????????????????????AuthRestApi??????
 *
 * @author limbo
 * @date 2018-10-14
 */
@RestController
@RefreshScope
@RequestMapping("/auth")
@Api(value = "??????????????????", tags = {"??????????????????"})
@Slf4j
public class LoginRestApi {

    @Autowired
    private WebUtil webUtil;
    @Autowired
    private AdminService adminService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private CategoryMenuService categoryMenuService;
    @Autowired
    private Audience audience;
    @Value(value = "${tokenHead}")
    private String tokenHead;
    @Value(value = "${isRememberMeExpiresSecond}")
    private int isRememberMeExpiresSecond;
    @Autowired
    private RedisUtil redisUtil;
    @Resource
    private PictureFeignClient pictureFeignClient;
    @Autowired
    private WebConfigService webConfigService;

    @ApiOperation(value = "????????????", notes = "????????????")
    @PostMapping("/login")
    public String login(HttpServletRequest request,
                        @ApiParam(name = "username", value = "??????????????????????????????") @RequestParam(name = "username", required = false) String username,
                        @ApiParam(name = "password", value = "??????") @RequestParam(name = "password", required = false) String password,
                        @ApiParam(name = "isRememberMe", value = "????????????????????????") @RequestParam(name = "isRememberMe", required = false, defaultValue = "false") Boolean isRememberMe) {

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            return ResultUtil.result(SysConf.ERROR, "???????????????????????????");
        }
        String ip = IpUtils.getIpAddr(request);
        String limitCount = redisUtil.get(RedisConf.LOGIN_LIMIT + RedisConf.SEGMENTATION + ip);
        if (StringUtils.isNotEmpty(limitCount)) {
            Integer tempLimitCount = Integer.valueOf(limitCount);
            if (tempLimitCount >= Constants.NUM_FIVE) {
                return ResultUtil.result(SysConf.ERROR, "????????????????????????,????????????30??????");
            }
        }
        Boolean isEmail = CheckUtils.checkEmail(username);
        Boolean isMobile = CheckUtils.checkMobileNumber(username);
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        if (isEmail) {
            queryWrapper.eq(SQLConf.EMAIL, username);
        } else if (isMobile) {
            queryWrapper.eq(SQLConf.MOBILE, username);
        } else {
            queryWrapper.eq(SQLConf.USER_NAME, username);
        }
        queryWrapper.last(SysConf.LIMIT_ONE);
        queryWrapper.eq(SysConf.STATUS, EStatus.ENABLE);
        Admin admin = adminService.getOne(queryWrapper);
        if (admin == null) {
            // ????????????????????????
            log.error("?????????????????????");
            return ResultUtil.result(SysConf.ERROR, String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request)));
        }
        // ??????????????????????????????????????????SHA-256 + ??????????????????????????? + ???????????????????????????
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        boolean isPassword = encoder.matches(password, admin.getPassWord());
        if (!isPassword) {
            //???????????????????????????
            log.error("?????????????????????");
            return ResultUtil.result(SysConf.ERROR, String.format(MessageConf.LOGIN_ERROR, setLoginCommit(request)));
        }
        List<String> roleUids = new ArrayList<>();
        roleUids.add(admin.getRoleUid());
        List<Role> roles = (List<Role>) roleService.listByIds(roleUids);

        if (roles.size() <= 0) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.NO_ROLE);
        }
        String roleNames = null;
        for (Role role : roles) {
            roleNames += (role.getRoleName() + Constants.SYMBOL_COMMA);
        }
        String roleName = roleNames.substring(0, roleNames.length() - 2);
        long expiration = isRememberMe ? isRememberMeExpiresSecond : audience.getExpiresSecond();
        String jwtToken = jwtTokenUtil.createJWT(admin.getUserName(),
                admin.getUid(),
                roleName,
                audience.getClientId(),
                audience.getName(),
                expiration * 1000,
                audience.getBase64Secret());
        String token = tokenHead + jwtToken;
        Map<String, Object> result = new HashMap<>(Constants.NUM_ONE);
        result.put(SysConf.TOKEN, token);

        //????????????????????????
        Integer count = admin.getLoginCount() + 1;
        admin.setLoginCount(count);
        admin.setLastLoginIp(IpUtils.getIpAddr(request));
        admin.setLastLoginTime(new Date());
        admin.updateById();
        // ??????token???validCode???????????????????????????
        admin.setValidCode(token);
        // ??????tokenUid????????????????????????token???????????????token???????????????????????????????????????
        admin.setTokenUid(StringUtils.getUUID());
        admin.setRole(roles.get(0));
        // ?????????????????????Redis???????????????????????????
        adminService.addOnlineAdmin(admin, expiration);
        return ResultUtil.result(SysConf.SUCCESS, result);
    }

    @ApiOperation(value = "????????????", notes = "????????????", response = String.class)
    @GetMapping(value = "/info")
    public String info(HttpServletRequest request,
                       @ApiParam(name = "token", value = "token??????", required = false) @RequestParam(name = "token", required = false) String token) {

        Map<String, Object> map = new HashMap<>(Constants.NUM_THREE);
        if (request.getAttribute(SysConf.ADMIN_UID) == null) {
            return ResultUtil.result(SysConf.ERROR, "token????????????");
        }
        Admin admin = adminService.getById(request.getAttribute(SysConf.ADMIN_UID).toString());
        map.put(SysConf.TOKEN, token);
        //????????????
        if (StringUtils.isNotEmpty(admin.getAvatar())) {
            String pictureList = this.pictureFeignClient.getPicture(admin.getAvatar(), SysConf.FILE_SEGMENTATION);
            List<String> list = webUtil.getPicture(pictureList);
            if (list.size() > 0) {
                map.put(SysConf.AVATAR, list.get(0));
            } else {
                map.put(SysConf.AVATAR, "https://gitee.com/moxi159753/wx_picture/raw/master/picture/favicon.png");
            }
        }

        List<String> roleUid = new ArrayList<>();
        roleUid.add(admin.getRoleUid());
        Collection<Role> roleList = roleService.listByIds(roleUid);
        map.put(SysConf.ROLES, roleList);
        return ResultUtil.result(SysConf.SUCCESS, map);
    }

    @ApiOperation(value = "???????????????????????????", notes = "???????????????????????????", response = String.class)
    @GetMapping(value = "/getMenu")
    public String getMenu(HttpServletRequest request) {

        Collection<CategoryMenu> categoryMenuList = new ArrayList<>();
        Admin admin = adminService.getById(request.getAttribute(SysConf.ADMIN_UID).toString());

        List<String> roleUid = new ArrayList<>();
        roleUid.add(admin.getRoleUid());
        Collection<Role> roleList = roleService.listByIds(roleUid);
        List<String> categoryMenuUids = new ArrayList<>();
        roleList.forEach(item -> {
            String caetgoryMenuUids = item.getCategoryMenuUids();
            String[] uids = caetgoryMenuUids.replace("[", "").replace("]", "").replace("\"", "").split(",");
            categoryMenuUids.addAll(Arrays.asList(uids));
        });
        categoryMenuList = categoryMenuService.listByIds(categoryMenuUids);

        // ?????????????????????????????? ????????????
        List<CategoryMenu> buttonList = new ArrayList<>();
        Set<String> secondMenuUidList = new HashSet<>();
        categoryMenuList.forEach(item -> {
            // ??????????????????
            if (item.getMenuType() == EMenuType.MENU && item.getMenuLevel() == SysConf.TWO) {
                secondMenuUidList.add(item.getUid());
            }
            // ???????????????????????????????????????
            if (item.getMenuType() == EMenuType.BUTTON && StringUtils.isNotEmpty(item.getParentUid())) {
                // ??????????????????
                secondMenuUidList.add(item.getParentUid());
                // ??????????????????
                buttonList.add(item);
            }
        });

        Collection<CategoryMenu> childCategoryMenuList = new ArrayList<>();
        Collection<CategoryMenu> parentCategoryMenuList = new ArrayList<>();
        List<String> parentCategoryMenuUids = new ArrayList<>();

        if (secondMenuUidList.size() > 0) {
            childCategoryMenuList = categoryMenuService.listByIds(secondMenuUidList);
        }

        childCategoryMenuList.forEach(item -> {
            //???????????????????????????
            if (item.getMenuLevel() == SysConf.TWO) {

                if (StringUtils.isNotEmpty(item.getParentUid())) {
                    parentCategoryMenuUids.add(item.getParentUid());
                }
            }
        });

        if (parentCategoryMenuUids.size() > 0) {
            parentCategoryMenuList = categoryMenuService.listByIds(parentCategoryMenuUids);
        }

        List<CategoryMenu> list = new ArrayList<>(parentCategoryMenuList);

        //???parent????????????
        Map<String, Object> map = new HashMap<>(Constants.NUM_THREE);
        Collections.sort(list);
        map.put(SysConf.PARENT_LIST, list);
        map.put(SysConf.SON_LIST, childCategoryMenuList);
        map.put(SysConf.BUTTON_LIST, buttonList);
        return ResultUtil.result(SysConf.SUCCESS, map);
    }

    @ApiOperation(value = "??????????????????", notes = "??????????????????", response = String.class)
    @GetMapping(value = "/getWebSiteName")
    public String getWebSiteName() {
        return ResultUtil.successWithData(webConfigService.getWebSiteName());
    }


    @ApiOperation(value = "????????????", notes = "????????????", response = String.class)
    @PostMapping(value = "/logout")
    public String logout() {
        ServletRequestAttributes attribute = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attribute.getRequest();
        String token = request.getAttribute(SysConf.TOKEN).toString();
        if (StringUtils.isEmpty(token)) {
            return ResultUtil.result(SysConf.ERROR, MessageConf.OPERATION_FAIL);
        } else {
            // ????????????????????????
            String adminJson = redisUtil.get(RedisConf.LOGIN_TOKEN_KEY + RedisConf.SEGMENTATION + token);
            if (StringUtils.isNotEmpty(adminJson)) {
                OnlineAdmin onlineAdmin = JsonUtils.jsonToPojo(adminJson, OnlineAdmin.class);
                String tokenUid = onlineAdmin.getTokenId();
                // ??????Redis??????TokenUid
                redisUtil.delete(RedisConf.LOGIN_UUID_KEY + RedisConf.SEGMENTATION + tokenUid);
            }
            // ??????Redis????????????
            redisUtil.delete(RedisConf.LOGIN_TOKEN_KEY + RedisConf.SEGMENTATION + token);
            SecurityContextHolder.clearContext();
            return ResultUtil.result(SysConf.SUCCESS, MessageConf.OPERATION_SUCCESS);
        }
    }

    /**
     * ???????????????????????????????????????
     * ?????????????????????????????????30??????
     *
     * @param request
     */
    private Integer setLoginCommit(HttpServletRequest request) {
        String ip = IpUtils.getIpAddr(request);
        String count = redisUtil.get(RedisConf.LOGIN_LIMIT + RedisConf.SEGMENTATION + ip);
        Integer surplusCount = 5;
        if (StringUtils.isNotEmpty(count)) {
            Integer countTemp = Integer.valueOf(count) + 1;
            surplusCount = surplusCount - countTemp;
            redisUtil.setEx(RedisConf.LOGIN_LIMIT + RedisConf.SEGMENTATION + ip, String.valueOf(countTemp), 30, TimeUnit.MINUTES);
        } else {
            surplusCount = surplusCount - 1;
            redisUtil.setEx(RedisConf.LOGIN_LIMIT + RedisConf.SEGMENTATION + ip, Constants.STR_ONE, 30, TimeUnit.MINUTES);
        }
        return surplusCount;
    }

}
