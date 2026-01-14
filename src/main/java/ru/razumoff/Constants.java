package ru.razumoff;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    @UtilityClass
    public class ApiDocs {
        public static final String COURSES_TAG_NAME = "Курсы";
        public static final String COURSES_TAG_DESCRIPTION = "Управление курсами (получение, создание, редактирование)";
    }

    @UtilityClass
    public class Minio {
        public static final String PUBLIC_READ_POLICY_TEMPLATE = """
            {
              "Version": "2012-10-17",
              "Statement": [{
                "Effect": "Allow",
                "Principal": "*",
                "Action": ["s3:GetObject"],
                "Resource": ["arn:aws:s3:::%s/*"]
              }]
            }
            """;
    }
}
