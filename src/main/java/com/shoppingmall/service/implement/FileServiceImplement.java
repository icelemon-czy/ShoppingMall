package com.shoppingmall.service.implement;

import com.google.common.collect.Lists;
import com.shoppingmall.service.IFileService;
import com.shoppingmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
@Service("iFileService")
public class FileServiceImplement implements IFileService {
    private Logger logger = LoggerFactory.getLogger(FileServiceImplement.class);

    @Override
    public String upload(MultipartFile file,String path){
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        // User wants to upload different file with same name :
        // For example: File A : abc.jpg  File B : abc.jpg
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("Start to upload file,filename: {},filepath: {},new filename: {}",fileName,path,uploadFileName);

        // Make sure File dictionary exists
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);
        try {
            // File transfer success!
            file.transferTo(targetFile);

            // Upload to FTP server
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));

            //Delete original file
            targetFile.delete();
        }catch (IOException e){
            logger.error("Upload Exception",e);
            return null;
        }
        return targetFile.getName();
    }

}
