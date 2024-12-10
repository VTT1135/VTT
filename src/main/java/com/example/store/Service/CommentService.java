package com.example.store.Service;

import com.example.store.Entity.Comment;
import com.example.store.Entity.CommentImage;
import com.example.store.Entity.Product;
import com.example.store.Entity.User;
import com.example.store.Repository.CommentImageRepository;
import com.example.store.Repository.CommentRepository;
import com.example.store.Repository.ProductRepository;
import com.example.store.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentImageRepository commentImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${upload.path}")
    private String uploadPath;


    public Map<Integer, Long> getRatingsCountByProduct(Long productId) {
        List<Object[]> results = commentRepository.countRatingsByProductId(productId);
        Map<Integer, Long> ratingCounts = new HashMap<>();

        for (Object[] result : results) {
            Integer rating = (Integer) result[0];
            Long count = (Long) result[1];
            ratingCounts.put(rating, count);
        }

        return ratingCounts;
    }

    // Thêm bình luận
    @Transactional
    public Comment addComment(Long productId, String commentText, int rating, List<MultipartFile> images) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;

        if (authentication != null && authentication.isAuthenticated()) {
            userId = ((User) authentication.getPrincipal()).getId();
        }

        if (userId == null) {
            throw new IllegalArgumentException("Người dùng chưa đăng nhập");
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("Người dùng không tồn tại");
        }

        Product product = productOptional.get();
        User user = userOptional.get();

        Comment comment = new Comment();
        comment.setProduct(product);
        comment.setUser(user);
        comment.setCommentText(commentText);
        comment.setRating(rating);
        Comment savedComment = commentRepository.save(comment);

        // Lưu ảnh nếu có
        if (images != null && !images.isEmpty()) {
            List<CommentImage> commentImages = new ArrayList<>();
            for (MultipartFile image : images) {
                String imageUrl = saveImage(image);
                CommentImage commentImage = new CommentImage();
                commentImage.setComment(savedComment);
                commentImage.setImageUrl(imageUrl);
                commentImages.add(commentImage);
            }
            commentImageRepository.saveAll(commentImages);
            savedComment.setCommentImages(commentImages);
        }

        return savedComment;
    }

    private String saveImage(MultipartFile image) {
        try {
            String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path uploadDir = Paths.get(uploadPath);

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            Path filePath = uploadDir.resolve(fileName);
            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + image.getOriginalFilename(), e);
        }
    }

    public Page<Comment> getCommentsByProduct(Long productId, Pageable pageable) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại");
        }
        return commentRepository.findByProductId(productId, pageable);
    }

    // Xóa bình luận
    @Transactional
    public void deleteComment(Long commentId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (!commentOptional.isPresent()) {
            throw new IllegalArgumentException("Bình luận không tồn tại");
        }

        // Xóa ảnh liên quan
        List<CommentImage> commentImages = commentImageRepository.findByCommentId(commentId);
        if (commentImages != null && !commentImages.isEmpty()) {
            for (CommentImage commentImage : commentImages) {
                String imageUrl = commentImage.getImageUrl();
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1); // Lấy tên tệp từ URL

                Path filePath = Paths.get(uploadPath, fileName); // Tạo đường dẫn đầy đủ đến tệp
                try {
                    Files.deleteIfExists(filePath); // Xóa ảnh từ hệ thống tệp
                } catch (IOException e) {
                    throw new RuntimeException("Không thể xóa ảnh: " + fileName, e);
                }
            }
        }
        // Xóa ảnh liên quan
        commentImageRepository.deleteByCommentId(commentId);

        // Xóa bình luận
        commentRepository.deleteById(commentId);
    }
}
