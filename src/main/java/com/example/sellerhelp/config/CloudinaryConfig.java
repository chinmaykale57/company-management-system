package com.example.sellerhelp.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dlpadzxub",
                "api_key", "799693695882277",
                "api_secret", "oMJ-vUJ4-WuBnTh1BVKOMpVIK_c",
                "secure", true
        ));
    }
}