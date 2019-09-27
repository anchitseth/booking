package com.nus.iss.eatngreet.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDTO;

@Service
public interface AWSS3Service {

	public CommonResponseDTO uploadItem(MultipartFile[] images);

//	public CommonResponseDTO deleteFile(String fileUrl);

	public CommonResponseDTO getAllImages();

}
