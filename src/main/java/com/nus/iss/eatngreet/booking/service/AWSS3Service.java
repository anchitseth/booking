package com.nus.iss.eatngreet.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nus.iss.eatngreet.booking.responsedto.CommonResponseDto;

@Service
public interface AWSS3Service {

	public CommonResponseDto uploadItem(MultipartFile[] images);

//	public CommonResponseDTO deleteFile(String fileUrl);

	public CommonResponseDto getAllImages();

}
