package com.glacier.soroblog.picture.form;

import com.glacier.soroblog.base.vo.FileVO;
import lombok.Data;

@Data
public class SearchPictureForm extends FileVO {
    private String searchKey;
    private Integer count;
}
