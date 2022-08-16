package com.glacier.soroblog.admin.restapi;


import com.glacier.soroblog.admin.annotion.AuthorityVerify.AuthorityVerify;
import com.glacier.soroblog.admin.annotion.AvoidRepeatableCommit.AvoidRepeatableCommit;
import com.glacier.soroblog.admin.annotion.OperationLogger.OperationLogger;
import com.glacier.soroblog.utils.ResultUtil;
import com.glacier.soroblog.xo.service.CommentService;
import com.glacier.soroblog.xo.vo.CommentVO;
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

import java.util.List;

/**
 * 评论表 RestApi
 *
 * @author 陌溪
 * @since 2020年1月20日16:44:25
 */
@Api(value = "用户评论相关接口", tags = {"用户评论相关接口"})
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentRestApi {

    @Autowired
    CommentService commentService;

    @AuthorityVerify
    @ApiOperation(value = "获取评论列表", notes = "获取评论列表", response = String.class)
    @PostMapping(value = "/getList")
    public String getList(@Validated({GetList.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("获取评论列表: {}", commentVO);
        return ResultUtil.successWithData(commentService.getPageList(commentVO));
    }

    @AvoidRepeatableCommit
    @AuthorityVerify
    @ApiOperation(value = "增加评论", notes = "增加评论", response = String.class)
    @PostMapping("/add")
    public String add(@Validated({Insert.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("新增评论: {}", commentVO);
        return commentService.addComment(commentVO);
    }

    @AuthorityVerify
    @ApiOperation(value = "编辑评论", notes = "编辑评论", response = String.class)
    @PostMapping("/edit")
    public String edit(@Validated({Update.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("编辑评论: {}", commentVO);
        return commentService.editComment(commentVO);
    }

    @AuthorityVerify
    @ApiOperation(value = "删除评论", notes = "删除评论", response = String.class)
    @PostMapping("/delete")
    public String delete(@Validated({Delete.class}) @RequestBody CommentVO commentVO, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        log.info("删除评论: {}", commentVO);
        return commentService.deleteComment(commentVO);
    }

    @AuthorityVerify
    @OperationLogger(value = "删除选中评论")
    @ApiOperation(value = "删除选中评论", notes = "删除选中评论", response = String.class)
    @PostMapping("/deleteBatch")
    public String deleteBatch(@Validated({Delete.class}) @RequestBody List<CommentVO> commentVoList, BindingResult result) {

        // 参数校验
        ThrowableUtils.checkParamArgument(result);
        return commentService.deleteBatchComment(commentVoList);
    }


}

