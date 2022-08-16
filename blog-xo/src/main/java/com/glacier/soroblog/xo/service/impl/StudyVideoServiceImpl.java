package com.glacier.soroblog.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.glacier.soroblog.commons.entity.ResourceSort;
import com.glacier.soroblog.commons.entity.StudyVideo;
import com.glacier.soroblog.commons.feign.PictureFeignClient;
import com.glacier.soroblog.utils.ResultUtil;
import com.glacier.soroblog.utils.StringUtils;
import com.glacier.soroblog.xo.global.SQLConf;
import com.glacier.soroblog.xo.global.SysConf;
import com.glacier.soroblog.xo.service.ResourceSortService;
import com.glacier.soroblog.xo.service.StudyVideoService;
import com.glacier.soroblog.xo.utils.WebUtil;
import com.glacier.soroblog.xo.vo.StudyVideoVO;
import com.glacier.soroblog.xo.global.MessageConf;
import com.glacier.soroblog.xo.mapper.StudyVideoMapper;
import com.glacier.soroblog.base.enums.EStatus;
import com.glacier.soroblog.base.serviceImpl.SuperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 学习视频表 服务实现类
 *
 * @author 陌溪
 * @date 2018-09-04
 */
@Service
public class StudyVideoServiceImpl extends SuperServiceImpl<StudyVideoMapper, StudyVideo> implements StudyVideoService {

    @Autowired
    private WebUtil webUtil;

    @Autowired
    private StudyVideoService studyVideoService;

    @Autowired
    private ResourceSortService resourceSortService;

    @Resource
    private PictureFeignClient pictureFeignClient;

    @Override
    public IPage<StudyVideo> getPageList(StudyVideoVO studyVideoVO) {
        QueryWrapper<StudyVideo> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(studyVideoVO.getKeyword()) && !StringUtils.isEmpty(studyVideoVO.getKeyword().trim())) {
            queryWrapper.like(SQLConf.NAME, studyVideoVO.getKeyword().trim());
        }

        Page<StudyVideo> page = new Page<>();
        page.setCurrent(studyVideoVO.getCurrentPage());
        page.setSize(studyVideoVO.getPageSize());
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.CREATE_TIME);
        IPage<StudyVideo> pageList = studyVideoService.page(page, queryWrapper);
        List<StudyVideo> list = pageList.getRecords();

        final StringBuffer fileUids = new StringBuffer();
        list.forEach(item -> {
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                fileUids.append(item.getFileUid() + SysConf.FILE_SEGMENTATION);
            }
        });
        String pictureResult = null;
        Map<String, String> pictureMap = new HashMap<>();
        if (fileUids != null) {
            pictureResult = this.pictureFeignClient.getPicture(fileUids.toString(), SysConf.FILE_SEGMENTATION);
        }
        List<Map<String, Object>> picList = webUtil.getPictureMap(pictureResult);
        picList.forEach(item -> {
            pictureMap.put(item.get(SysConf.UID).toString(), item.get(SysConf.URL).toString());
        });

        for (StudyVideo item : list) {
            //获取分类资源
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SysConf.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();
                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }

            if (StringUtils.isNotEmpty(item.getResourceSortUid())) {
                ResourceSort resourceSort = resourceSortService.getById(item.getResourceSortUid());
                item.setResourceSort(resourceSort);
            }
        }
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public String addStudyVideo(StudyVideoVO studyVideoVO) {
        StudyVideo studyVideo = new StudyVideo();
        studyVideo.setName(studyVideoVO.getName());
        studyVideo.setSummary(studyVideoVO.getSummary());
        studyVideo.setContent(studyVideoVO.getContent());
        studyVideo.setFileUid(studyVideoVO.getFileUid());
        studyVideo.setBaiduPath(studyVideoVO.getBaiduPath());
        studyVideo.setResourceSortUid(studyVideoVO.getResourceSortUid());
        studyVideo.setClickCount(SysConf.ZERO);
        studyVideo.setUpdateTime(new Date());
        studyVideo.insert();
        return ResultUtil.successWithMessage(MessageConf.INSERT_SUCCESS);
    }

    @Override
    public String editStudyVideo(StudyVideoVO studyVideoVO) {
        StudyVideo studyVideo = studyVideoService.getById(studyVideoVO.getUid());
        studyVideo.setName(studyVideoVO.getName());
        studyVideo.setSummary(studyVideoVO.getSummary());
        studyVideo.setContent(studyVideoVO.getContent());
        studyVideo.setFileUid(studyVideoVO.getFileUid());
        studyVideo.setBaiduPath(studyVideoVO.getBaiduPath());
        studyVideo.setResourceSortUid(studyVideoVO.getResourceSortUid());
        studyVideo.setUpdateTime(new Date());
        studyVideo.updateById();
        return ResultUtil.successWithMessage(MessageConf.UPDATE_SUCCESS);
    }

    @Override
    public String deleteBatchStudyVideo(List<StudyVideoVO> studyVideoVOList) {
        if (studyVideoVOList.size() <= 0) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        List<String> uids = new ArrayList<>();
        studyVideoVOList.forEach(item -> {
            uids.add(item.getUid());
        });
        Collection<StudyVideo> blogSortList = studyVideoService.listByIds(uids);
        blogSortList.forEach(item -> {
            item.setUpdateTime(new Date());
            item.setStatus(EStatus.DISABLED);
        });

        Boolean save = studyVideoService.updateBatchById(blogSortList);

        if (save) {
            return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
        } else {
            return ResultUtil.errorWithMessage(MessageConf.DELETE_FAIL);
        }
    }
}
