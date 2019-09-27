package com.nus.iss.eatngreet.booking.restcontroller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDTO;
import com.nus.iss.eatngreet.booking.service.AWSS3Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value=AWSS3RestController.SECURE_BASE)
public class AWSS3RestController {

	public static final String SECURE_BASE = "/storage";

    @Autowired
    private AWSS3Service awsS3Service;

    @PostMapping("/upload-item")
    public CommonResponseDTO uploadItem(@RequestParam(value = "images") MultipartFile[] images) {
    	log.info("uploadFile() of S3BucketRestController");
        return awsS3Service.uploadItem(images);
    }

//    @DeleteMapping("/delete-item")
//    public CommonResponseDTO deleteItem(@RequestPart(value = "url") String fileUrl) {
//    	log.info("deleteFile() of S3BucketRestController");
//        return awsS3Service.deleteFile(fileUrl);
//    }
//    
//    @PostMapping("/get-all-image-urls")
//    public CommonResponseDTO getAllItems() {
//    	log.info("getAllImages() of S3BucketRestController");
//        return awsS3Service.getAllImages();
//    }
	
}
