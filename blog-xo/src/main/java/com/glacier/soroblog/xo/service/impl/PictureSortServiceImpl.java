package com.glacier.soroblog.xo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.glacier.soroblog.commons.entity.Picture;
import com.glacier.soroblog.commons.entity.PictureSort;
import com.glacier.soroblog.commons.feign.PictureFeignClient;
import com.glacier.soroblog.utils.ResultUtil;
import com.glacier.soroblog.utils.StringUtils;
import com.glacier.soroblog.xo.global.SQLConf;
import com.glacier.soroblog.xo.global.SysConf;
import com.glacier.soroblog.xo.mapper.PictureSortMapper;
import com.glacier.soroblog.xo.service.PictureSortService;
import com.glacier.soroblog.xo.utils.WebUtil;
import com.glacier.soroblog.xo.vo.PictureSortVO;
import com.glacier.soroblog.xo.global.MessageConf;
import com.glacier.soroblog.xo.service.PictureService;
import com.glacier.soroblog.base.enums.EStatus;
import com.glacier.soroblog.base.serviceImpl.SuperServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 图片分类表 服务实现类
 *
 * @author 陌溪
 * @since 2018-09-04
 */
@Service
public class PictureSortServiceImpl extends SuperServiceImpl<PictureSortMapper, PictureSort> implements PictureSortService {

    @Autowired
    private WebUtil webUtil;
    @Autowired
    private PictureSortService pictureSortService;
    @Autowired
    private PictureService pictureService;
    @Resource
    private PictureFeignClient pictureFeignClient;

    @Override
    public IPage<PictureSort> getPageList(PictureSortVO pictureSortVO) {
        QueryWrapper<PictureSort> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(pictureSortVO.getKeyword()) && !StringUtils.isEmpty(pictureSortVO.getKeyword().trim())) {
            queryWrapper.like(SQLConf.NAME, pictureSortVO.getKeyword().trim());
        }

        if (pictureSortVO.getIsShow() != null) {
            queryWrapper.eq(SQLConf.IS_SHOW, SysConf.ONE);
        }
        Page<PictureSort> page = new Page<>();
        page.setCurrent(pictureSortVO.getCurrentPage());
        page.setSize(pictureSortVO.getPageSize());
        queryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        queryWrapper.orderByDesc(SQLConf.SORT);
        IPage<PictureSort> pageList = pictureSortService.page(page, queryWrapper);
        List<PictureSort> list = pageList.getRecords();

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

        for (PictureSort item : list) {
            //获取图片
            if (StringUtils.isNotEmpty(item.getFileUid())) {
                List<String> pictureUidsTemp = StringUtils.changeStringToString(item.getFileUid(), SysConf.FILE_SEGMENTATION);
                List<String> pictureListTemp = new ArrayList<>();
                pictureUidsTemp.forEach(picture -> {
                    pictureListTemp.add(pictureMap.get(picture));
                });
                item.setPhotoList(pictureListTemp);
            }
        }
        pageList.setRecords(list);
        return pageList;
    }

    @Override
    public String addPictureSort(PictureSortVO pictureSortVO) {
        PictureSort pictureSort = new PictureSort();
        pictureSort.setName(pictureSortVO.getName());
        pictureSort.setParentUid(pictureSortVO.getParentUid());
        pictureSort.setSort(pictureSortVO.getSort());
        pictureSort.setFileUid(pictureSortVO.getFileUid());
        pictureSort.setStatus(EStatus.ENABLE);
        pictureSort.setIsShow(pictureSortVO.getIsShow());
        pictureSort.setUpdateTime(new Date());
        pictureSort.insert();
        return ResultUtil.successWithMessage(MessageConf.INSERT_SUCCESS);
    }

    @Override
    public String editPictureSort(PictureSortVO pictureSortVO) {
        PictureSort pictureSort = pictureSortService.getById(pictureSortVO.getUid());
        pictureSort.setName(pictureSortVO.getName());
        pictureSort.setParentUid(pictureSortVO.getParentUid());
        pictureSort.setSort(pictureSortVO.getSort());
        pictureSort.setFileUid(pictureSortVO.getFileUid());
        pictureSort.setIsShow(pictureSortVO.getIsShow());
        pictureSort.setUpdateTime(new Date());
        pictureSort.updateById();
        return ResultUtil.successWithMessage(MessageConf.UPDATE_SUCCESS);
    }

    @Override
    public String deletePictureSort(PictureSortVO pictureSortVO) {
        // 判断要删除的分类，是否有图片
        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.eq(SQLConf.STATUS, EStatus.ENABLE);
        pictureQueryWrapper.eq(SQLConf.PICTURE_SORT_UID, pictureSortVO.getUid());
        Integer pictureCount = pictureService.count(pictureQueryWrapper);
        if (pictureCount > 0) {
            return ResultUtil.errorWithMessage(MessageConf.PICTURE_UNDER_THIS_SORT);
        }

        PictureSort pictureSort = pictureSortService.getById(pictureSortVO.getUid());
        pictureSort.setStatus(EStatus.DISABLED);
        pictureSort.setUpdateTime(new Date());
        pictureSort.updateById();
        return ResultUtil.successWithMessage(MessageConf.DELETE_SUCCESS);
    }

    @Override
    public String stickPictureSort(PictureSortVO pictureSortVO) {
        PictureSort pictureSort = pictureSortService.getById(pictureSortVO.getUid());
        //查找出最大的那一个
        QueryWrapper<PictureSort> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(SQLConf.SORT);
        Page<PictureSort> page = new Page<>();
        page.setCurrent(0);
        page.setSize(1);
        IPage<PictureSort> pageList = pictureSortService.page(page, queryWrapper);
        List<PictureSort> list = pageList.getRecords();
        PictureSort maxSort = list.get(0);
        if (StringUtils.isEmpty(maxSort.getUid())) {
            return ResultUtil.errorWithMessage(MessageConf.PARAM_INCORRECT);
        }
        if (maxSort.getUid().equals(pictureSort.getUid())) {
            return ResultUtil.errorWithMessage(MessageConf.THIS_SORT_IS_TOP);
        }
        Integer sortCount = maxSort.getSort() + 1;
        pictureSort.setSort(sortCount);
        pictureSort.setUpdateTime(new Date());
        pictureSort.updateById();
        return ResultUtil.successWithMessage(MessageConf.OPERATION_SUCCESS);
    }
}
