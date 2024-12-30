package com.example.model.dto;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
public class FileUploadDTO {

    /**
     * 上传文件到指定目录
     */
    private String path;

    /**
     * 上传文件的文件名称
     */
    private String name;

    /**
     * 任务id
     */
    private String id;

    /**
     * 总分片数量
     */
    private Integer chunks;

    /**
     * 当前为第几块分片
     */
    private Integer chunk;

    /**
     * 按多大的文件粒度进行分片
     */
    private Long chunkSize;

    /**
     * 二进制文件
     */
    private MultipartFile file;
    /**
     * 文件MD5
     */
    private String md5;

    /**
     * 文件大小
     */
    private Long size = 0L;

    /**
     * 上传后的本地文件
     */
    private File successFile;

}
