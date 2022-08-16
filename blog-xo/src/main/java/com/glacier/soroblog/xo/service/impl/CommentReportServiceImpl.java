package com.glacier.soroblog.xo.service.impl;

import com.glacier.soroblog.commons.entity.CommentReport;
import com.glacier.soroblog.xo.mapper.CommentReportMapper;
import com.glacier.soroblog.xo.service.CommentReportService;
import com.glacier.soroblog.base.serviceImpl.SuperServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 评论举报表 服务实现类
 *
 * @author 陌溪
 * @date 2020年1月12日15:47:47
 */
@Service
public class CommentReportServiceImpl extends SuperServiceImpl<CommentReportMapper, CommentReport> implements CommentReportService {

}
