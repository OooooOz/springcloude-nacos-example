package com.example.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.contant.FileConstant;
import com.example.model.BusinessException;
import com.example.model.dto.FileUploadDTO;
import com.example.model.vo.FileUploadVo;
import com.example.service.FileService;
import com.example.service.helper.ExecutorHelper;
import com.example.service.helper.FileHelper;
import com.example.service.strategy.SliceUploadFactory;
import com.example.service.strategy.SliceUploadStrategy;
import com.example.task.FileCallable;
import com.example.util.FileMD5Util;
import com.example.util.FileUtils;
import com.example.util.RedisUtil;

import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private FileHelper fileHelper;

    @Resource(name = "fileUploadExecutor")
    private Executor executor;

    @Autowired
    private ExecutorHelper executorHelper;

    @Value("${upload.mode}")
    private String mode;

    @Autowired
    private SliceUploadFactory sliceUploadFactory;

    @Override
    public FileUploadVo upload(FileUploadDTO param) throws IOException {

        if (Objects.isNull(param.getFile())) {
            throw new RuntimeException("file can not be empty");
        }
        param.setPath(FileUtils.withoutHeadAndTailDiagonal(param.getPath()));
        String md5 = FileMD5Util.getFileMD5(param.getFile());

        param.setMd5(md5);

        String filePath = fileHelper.getPath(param);
        File targetFile = new File(filePath);
        if (!targetFile.exists()) {
            targetFile.mkdirs();
        }
        String path = filePath + FileConstant.FILE_SEPARATORCHAR + param.getFile().getOriginalFilename();
        FileOutputStream out = new FileOutputStream(path);
        out.write(param.getFile().getBytes());
        out.flush();
        IoUtil.close(out);

        redisUtil.hset(FileConstant.FILE_UPLOAD_STATUS, md5, "true");

        return FileUploadVo.builder().uploadComplete(true).build();
    }

    @Override
    public FileUploadVo sliceUpload(FileUploadDTO dto) {
        SliceUploadStrategy strategy = sliceUploadFactory.getStrategyByMode(mode);
        CompletionService<FileUploadVo> completionService = executorHelper.getFileUploadCompletionService(executor);
        completionService.submit(new FileCallable(strategy, dto));
        try {
            FileUploadVo fileUploadVo = completionService.take().get();
            return fileUploadVo;
        } catch (Exception e) {
            throw BusinessException.failMsg("sliceUpload fail：" + e.getMessage());
        }
    }

    @Override
    public FileUploadVo checkFileMd5(FileUploadDTO dto) throws IOException {
        return null;
    }

}
