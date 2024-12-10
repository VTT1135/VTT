package com.example.store.Controller.UserController;

import com.example.store.Entity.Comment;
import com.example.store.Service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin("*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{productId}/ratings")
    public ResponseEntity<Map<Integer, Long>> getRatingsCountByProduct(@PathVariable Long productId) {
        Map<Integer, Long> ratingCounts = commentService.getRatingsCountByProduct(productId);
        return ResponseEntity.ok(ratingCounts);
    }

    // Thêm bình luận với nhiều ảnh
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @RequestParam("productId") Long productId,
            @RequestParam("commentText") String commentText,
            @RequestParam("rating") int rating,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        Comment comment = commentService.addComment(productId, commentText, rating, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    // Lấy bình luận theo sản phẩm
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<Comment>> getCommentsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> comments = commentService.getCommentsByProduct(productId, pageable);
        return ResponseEntity.ok(comments);
    }

    // Xóa bình luận
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
