package com.javahog.mail_clone;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final GridFsTemplate gridFsTemplate;

    public AttachmentController(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> getAttachment(
            @PathVariable String fileId) {
        try {
            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    query(where("_id").is(new ObjectId(fileId))));

            if (gridFSFile == null) {
                return ResponseEntity.notFound().build();
            }

            GridFsResource resource = gridFsTemplate
                    .getResource(gridFSFile);

            byte[] data = StreamUtils.copyToByteArray(
                    resource.getInputStream());

            String contentType = gridFSFile.getMetadata() != null
                    ? gridFSFile.getMetadata().getString("_contentType")
                    : "application/octet-stream";

            String filename = gridFSFile.getFilename();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(data);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Upload image from composer — store in GridFS, return URL
    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        try {
            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType());
            return ResponseEntity.ok(Map.of(
                    "fileId", fileId.toString(),
                    "url", "/api/attachments/" + fileId.toString()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Delete attachment from GridFS
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable String fileId) {
        try {
            gridFsTemplate.delete(
                    query(where("_id").is(new ObjectId(fileId))));
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}