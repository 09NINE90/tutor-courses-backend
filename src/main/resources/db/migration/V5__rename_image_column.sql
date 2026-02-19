ALTER TABLE courses
    RENAME COLUMN image_url TO image_s3_key;

UPDATE courses
SET image_s3_key = REGEXP_REPLACE(
        image_s3_key,
        '^.+/([^/]+)$',
        '\1'
                   )
WHERE image_s3_key IS NOT NULL
  AND image_s3_key != ''
  AND image_s3_key ~ '^.+/[^/]+\.[a-zA-Z0-9]+$';

UPDATE courses
SET image_s3_key = NULL
WHERE image_s3_key IS NOT NULL
  AND (image_s3_key = '' OR image_s3_key !~ '[0-9a-f-]+\.(png|jpg|jpeg|webp|gif)$');

ALTER TABLE courses
ALTER
COLUMN image_s3_key TYPE VARCHAR(50);

COMMENT
ON COLUMN courses.image_s3_key IS 'Ключ для поулучения изображения из хранилища';