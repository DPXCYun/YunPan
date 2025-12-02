package com.example.yunpiyuanpan.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.yunpiyuanpan.exception.FileException;
import com.example.yunpiyuanpan.exception.FileStatus;
import com.example.yunpiyuanpan.mapper.YPFileMapper;
import com.example.yunpiyuanpan.pojo.YPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FileService {

    @Autowired
    YPFileMapper mapper;

    @Value("${storage-lib}")
    String storageLib;

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    /**
     * 将文件数据存入数据库
     * @param ypFile
     */
    public void saveYPFile(YPFile ypFile, boolean isExist) {
        if(isExist){
            QueryWrapper<YPFile> wrapper = new QueryWrapper();
            wrapper.eq("file_name", ypFile.getFileName());
            wrapper.eq("virtual_path", ypFile.getVirtualPath());
            wrapper.eq("user_id", ypFile.getUserId());
            mapper.delete(wrapper);
        }
        mapper.insert(ypFile);
    }

    public boolean checkFileExist(YPFile ypFile){
        QueryWrapper<YPFile> wrapper = new QueryWrapper();
        wrapper.eq("file_name", ypFile.getFileName());
        wrapper.eq("virtual_path", ypFile.getVirtualPath());
        wrapper.eq("user_id", ypFile.getUserId());
        List<YPFile> list = mapper.selectList(wrapper);
        if(list.isEmpty()){
            return false;
        }
        return true;
    }

    //将文件封装为Resource类
    public Resource loadFileAsResource(Long userId, Long id) throws FileException {
        return loadFileAsResource(userId, id, true);
    }

    public Resource loadFileAsResource(Long userId, Long id, boolean checkUser) throws FileException {
        YPFile ypFile = mapper.selectById(id);
        if(ypFile==null) {
            logger.error(String.format("file not found:%d", id));
            throw new FileException(FileStatus.FILE_NOT_FOUND);
        }
        if(checkUser){
            if(!userId.equals(ypFile.getUserId())) {
                logger.error(String.format("userID=%d, file not found:%d", userId, id));
                throw new FileException(FileStatus.FILE_NOT_FOUND);
            }
        }


        // ✅ 使用 Path API 安全拼接路径（自动处理 / 和 \）
        Path basePath = Paths.get(storageLib);           // e.g. D:\YPFile\YPFiles
        Path userDir = basePath.resolve(userId.toString()); // → D:\YPFile\YPFiles\1987860564375650306
        Path filePath = userDir.resolve(ypFile.getPhysicalPath()); // → ...\1762778601440.config.txt

//        String physicalPath = String.format("%s%d/%s", storageLib, userId, ypFile.getPhysicalPath());
//        Path path = Paths.get(physicalPath).toAbsolutePath().normalize();
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if(!resource.exists()){
                logger.error("❌ PHYSICAL FILE MISSING! DB record exists but file not found on disk.");
                logger.error("   userId: {}, fileId: {}, db.physicalPath: {}", userId, id, ypFile.getPhysicalPath());
                logger.error("   resolved path: {}", filePath);
                throw new FileException(FileStatus.FILE_GOT_LOST);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new FileException(FileStatus.FILE_OPEN_FAIL);
        }
    }

    public void updateDownloadCount(Long fileId) {
        // 假设你有一个文件实体类 FileEntity，其中包含 downloadCount 字段
        YPFile ypFile = mapper.selectById(fileId);
        if(ypFile==null) {
            logger.error(String.format("file not fount:%d", fileId));
            throw new FileException(FileStatus.FILE_NOT_FOUND);
        }

        // 更新下载次数
        ypFile.setDownloadTimes(ypFile.getDownloadTimes() + 1);

        // 保存到数据库
        mapper.updateById(ypFile);
    }


    //直接通过文件id返回文件
    public File getFileByFileId(Long fileId){
        YPFile ypFile = mapper.selectById(fileId);
        //当没有查出结果时
        if(ypFile==null) {
            logger.error(String.format("file not found:%d", fileId));
            throw new FileException(FileStatus.FILE_NOT_FOUND);
        }
        Long userId = ypFile.getUserId();
        String physicalPath = String.format("%s%d/%s", storageLib, userId, ypFile.getPhysicalPath());
        File file = new File(physicalPath);
        if(!file.exists()){return file;}//当文件不存在时，返回给上一级进行处理
        File filetemp = new File(String.format("%s%d/%s", storageLib, userId, ypFile.getFileName()));
        file.renameTo(filetemp);
        return file;
    }



    //检测用户是否拥有某文件
    public boolean checkOwnership(Long userId, Long id){
        YPFile ypFile = mapper.selectById(id);
        return ypFile==null || userId.equals(ypFile.getUserId());
    }
}
