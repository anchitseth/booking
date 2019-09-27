package com.nus.iss.eatngreet.booking.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.nus.iss.eatngreet.booking.entity.ItemEntity;
import com.nus.iss.eatngreet.booking.repository.ItemRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AWSUtils {

	private AmazonS3 s3Client;

	@Value("${aws.s3.bucketName}")
	private String bucketName;

	@Value("${thumbnail.height}")
	private Integer thumbnailHeight;

	@Value("${thumbnail.width}")
	private Integer thumbnailWidth;

	@Value("${aws.s3.accessKey}")
	private String accessKey;

	@Value("${aws.s3.secretKey}")
	private String secretKey;

	@Value("${aws.s3.itemFolderName}")
	private String itemFolderName;

	@Value("${aws.s3.thumbnailFolderName}")
	private String thumbnailFolderName;

	@Autowired
	ItemRepository itemRepository;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

		this.s3Client = AmazonS3ClientBuilder.standard().withRegion("us-east-1")
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}

	public void uploadToS3(MultipartFile multipartFile) {
		log.info("upload to S3 ");
		String fileName = "";
		File file, thumbnailFile;
		try {
			file = convertMultiPartToFile(multipartFile);
			if (file != null) {
				fileName = multipartFile.getOriginalFilename();
				s3Client.putObject(new PutObjectRequest(bucketName, itemFolderName + "/" + fileName, file)
						.withCannedAcl(CannedAccessControlList.PublicRead));
				thumbnailFile = getThumbnail(file);
				s3Client.putObject(
						new PutObjectRequest(bucketName, itemFolderName + "/" + thumbnailFolderName + "/" + fileName,
								thumbnailFile).withCannedAcl(CannedAccessControlList.PublicRead));
				file.delete();
				thumbnailFile.delete();
				ItemEntity fileInfo = new ItemEntity();
				fileInfo.setImageUrl(s3Client.getUrl(bucketName, itemFolderName + "/" + fileName).toString());
				fileInfo.setImageThumbnailUrl(s3Client
						.getUrl(bucketName, itemFolderName + "/" + thumbnailFolderName + "/" + fileName).toString());
				fileInfo.setIsActive(true);
				fileInfo.setIsDeleted(false);
				itemRepository.save(fileInfo);
			} else {
				log.error(
						"Unable to convert file from MultipartFile. File name: " + multipartFile.getOriginalFilename());
			}
		} catch (Exception e) {
			log.error("Exception occurred in uploading file to S3. Multipart File: "
					+ multipartFile.getOriginalFilename() + ". Exception message: " + e.getMessage() + ".");
		}

	}

	private File convertMultiPartToFile(MultipartFile file) {
		log.info("convertMultiPartToFile() of AWSUtils.");
		File convFile = null;
		try {
			convFile = new File(file.getOriginalFilename());
			FileOutputStream fos = new FileOutputStream(convFile);
			fos.write(file.getBytes());
			fos.close();
		} catch (Exception e) {
			log.error(
					"Exception occurred in convertMultiPartToFile() of AWSUtils. Exception message: " + e.getMessage());
		}
		return convFile;
	}

	private File getThumbnail(File file) {
		log.info("getThumbnail() of AWSUtils.");
		BufferedImage image = null;
		File output = null;
		try {
			image = ImageIO.read(file);
			Image tmp = image.getScaledInstance(thumbnailWidth, thumbnailHeight, Image.SCALE_SMOOTH);
			BufferedImage resized = new BufferedImage(thumbnailWidth, thumbnailHeight, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = resized.createGraphics();
			g2d.drawImage(tmp, 0, 0, null);
			g2d.dispose();
			output = new File("/tmp/resized.png");
			ImageIO.write(resized, "png", output);
		} catch (IOException e) {
			log.error("Exception occurred in creating image thumbnail (getThumbnail() of AWSUtils). Exception message: "
					+ e.getMessage());
		}
		return output;

	}

}
