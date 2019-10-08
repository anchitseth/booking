package com.nus.iss.eatngreet.booking.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nus.iss.eatngreet.booking.entity.ItemEntity;
import com.nus.iss.eatngreet.booking.repository.ItemRepository;
import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;
import com.nus.iss.eatngreet.booking.responsedto.DataResponseDto;
import com.nus.iss.eatngreet.booking.service.AWSS3Service;
import com.nus.iss.eatngreet.booking.util.AWSUtils;
import com.nus.iss.eatngreet.booking.util.ResponseUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AWSS3ServiceImpl implements AWSS3Service {

	@Value("${aws.s3.bucketName}")
	private String bucketName;

	@Value("${aws.s3.itemFolderName}")
	private String itemsFolderName;

	@Autowired
	AWSUtils awsUtils;

	@Autowired
	ItemRepository itemRepository;

	public synchronized CommonResponseDto uploadItem(MultipartFile[] multipartFiles) {
		log.info("uploadFile of AWSS3ServiceImpl");
		CommonResponseDto response = new CommonResponseDto();
		List<String> allowedExtensions = new ArrayList<>();
		allowedExtensions.add("png");
		allowedExtensions.add("jpg");
		allowedExtensions.add("jpeg");
		allowedExtensions.add("bmp");
		for (MultipartFile multipartFile : multipartFiles) {
			if (!multipartFile.isEmpty()) {
				String extension = FilenameUtils.getExtension(multipartFile.getOriginalFilename());
				if (!allowedExtensions.contains(extension)) {
					ResponseUtil.prepareResponse(response,
							"Invalid file/s extension. Files with extension jpg, jpeg, bmp and png are allowed.",
							"FAILURE",
							"Invalid file/s extension. Files with extension jpg, jpeg, bmp and png are allowed.",
							false);
					return response;
				}
			} else {
				log.error("Multipart File is empty.");
			}
		}
		ResponseUtil.prepareResponse(response, "Successfully uploaded file to S3.", "SUCCESS",
				"Successfully uploaded file to S3.", true);
		for (MultipartFile multipartFile : multipartFiles) {
			awsUtils.uploadToS3(multipartFile);
		}
		return response;
	}

	@Override
	public CommonResponseDto getAllImages() {
		DataResponseDto response = new DataResponseDto();
		try {
			Iterable<ItemEntity> allImageData = itemRepository.findAll();
			List<Map<String, Object>> imagesURLList = new ArrayList<Map<String, Object>>();
			for (ItemEntity image : allImageData) {
				Map<String, Object> imageUrl = new HashMap<>();
				imageUrl.put("url", image.getImageUrl());
				imageUrl.put("thumbnailUrl", image.getImageThumbnailUrl());
				imageUrl.put("id", image.getItemId());
				imagesURLList.add(imageUrl);
			}
			Map<String, Object> imageURLMap = new HashMap<>();
			imageURLMap.put("imageURLs", imagesURLList);
			response.setData(imageURLMap);
			ResponseUtil.prepareResponse(response, "Images URL fetched successfully.", "SUCCESS",
					"Images URL fetched successfully.", true);
		} catch (Exception e) {
			ResponseUtil.prepareResponse(response, "Couldn't fetch images URL.", "FAILURE",
					"Couldn't fetch images URL.", false);
		}
		return response;
	}

}
