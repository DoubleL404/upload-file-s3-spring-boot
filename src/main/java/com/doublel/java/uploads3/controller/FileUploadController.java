package com.doublel.java.uploads3.controller;

import com.doublel.java.uploads3.service.AWSS3Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

    private AWSS3Service s3Service;

    public FileUploadController(AWSS3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/api/upload")
    @ResponseStatus(HttpStatus.OK)
    public String uploadFile(@RequestParam("file") MultipartFile file) {

        return s3Service.uploadFileToBucket(file);
    }
}
