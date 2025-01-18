package com.example.LifeMaster_BE.Community.Post;

import com.example.LifeMaster_BE.Community.Post.Like.PostLikeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
public class PostEntity {

    @Id
    @GeneratedValue
    private Long id;

    // 작성자

    @OneToMany(mappedBy = "post")
    private List<PostLikeEntity> likes = new ArrayList<>();

    // 게시글 유형
    private String type;

    private String title;
    private String content;
    private String image;
}
