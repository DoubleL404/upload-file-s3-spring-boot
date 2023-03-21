package com.doublel.java.uploads3.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum ImageTypeSupportedEnum {
    PNG("png", "image/png"),
    JPEG("jpeg", "image/jpeg"),
    JPG("jpg", "image/jpeg");

    private String extension;
    private String contentType;

    private static final String FILENAME_PATTERN = ".+(\\.(" + extensionsToDelimitedString("|") + "))$";

    ImageTypeSupportedEnum(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    /**
     * Get supported extensions
     * @return List<String>
     */
    public static List<String> supportedExtensions() {
        return Stream.of(values()).map(ImageTypeSupportedEnum::getExtension)
                .collect(Collectors.toList());
    }

    /**
     * Convert list extensions to delimited string
     * @param delimiter Delimiter to separate extensions
     * @return String
     */
    public static String extensionsToDelimitedString(String delimiter) {
        return StringUtils.join(supportedExtensions(), delimiter);
    }

    /**
     * Check valid extension file
     * @param fileName
     * @return boolean True if file extension is valid, False if not
     */
    public static boolean isValidExtensionFile(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return false;
        }
        return fileName.toLowerCase().matches(FILENAME_PATTERN);
    }

    /**
     * Find by extension
     * @param extension
     * @return Optional<ImageTypeSupportedEnum>
     */
    public static Optional<ImageTypeSupportedEnum> findByExtension(String extension) {
        if (StringUtils.isEmpty(extension)) {
            return Optional.empty();
        }

        return Stream.of(values())
                .filter(item -> item.extension.equalsIgnoreCase(extension))
                .findFirst();
    }
}
