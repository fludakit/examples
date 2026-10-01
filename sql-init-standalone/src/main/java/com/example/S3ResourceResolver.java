package com.example;

import io.github.fludakit.sqlinit.resource.Resource;
import io.github.fludakit.sqlinit.resource.ResourceResolver;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Resource resolver that loads SQL migrations from AWS S3.
 * Location format: s3://bucket-name/prefix
 */
public class S3ResourceResolver implements ResourceResolver {

    private final S3Client s3Client;

    public S3ResourceResolver(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String protocol() {
        return "s3";
    }

    @Override
    public Resource getResource(String location) {
        String[] parts = parseLocation(location);
        String bucket = parts[0];
        String key = parts[1];

        return new S3Resource(bucket, key);
    }

    @Override
    public List<Resource> getResources(String locationPattern) {
        String[] parts = parseLocation(locationPattern);
        String bucket = parts[0];
        String prefix = parts[1];

        List<Resource> resources = new ArrayList<>();
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        for (S3Object obj : response.contents()) {
            if (obj.key().endsWith(".sql")) {
                resources.add(new S3Resource(bucket, obj.key()));
            }
        }
        return resources;
    }

    private String[] parseLocation(String location) {
        String withoutProtocol = location.startsWith("s3://") ? location.substring(5) : location;
        int slashIndex = withoutProtocol.indexOf('/');
        if (slashIndex < 0) {
            throw new IllegalArgumentException("Invalid S3 location (expected s3://bucket/key): " + location);
        }
        String bucket = withoutProtocol.substring(0, slashIndex);
        String key = withoutProtocol.substring(slashIndex + 1);
        return new String[]{bucket, key};
    }

    private class S3Resource implements Resource {
        private final String bucket;
        private final String key;

        S3Resource(String bucket, String key) {
            this.bucket = bucket;
            this.key = key;
        }

        @Override
        public InputStream getInputStream() {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            ResponseBytes<GetObjectResponse> bytes = s3Client.getObjectAsBytes(request);
            return new ByteArrayInputStream(bytes.asByteArray());
        }

        @Override
        public String getFilename() {
            int lastSlash = key.lastIndexOf('/');
            return lastSlash >= 0 ? key.substring(lastSlash + 1) : key;
        }

        @Override
        public long contentLength() {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            return s3Client.headObject(request).contentLength();
        }

        @Override
        public URL getURL() {
            try {
                return new URL("s3://" + bucket + "/" + key);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create URL for S3 object: " + bucket + "/" + key, e);
            }
        }

        @Override
        public boolean exists() {
            try {
                contentLength();
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
