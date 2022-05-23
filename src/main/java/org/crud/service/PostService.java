package org.crud.service;

import org.crud.model.Label;
import org.crud.model.Post;
import org.crud.model.PostStatus;
import org.crud.model.Writer;
import org.crud.repository.PostRepository;
import org.crud.repository.impl.MysqlPostRepositoryImpl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class PostService {
    private final PostRepository postRepository = new MysqlPostRepositoryImpl();

    public List<Post> getList() {
        return postRepository.getAll();
    }

    public Post save(String content, Writer writer, List<Label> labels) {
        Post post = new Post();
        post.setWriter(writer);
        post.setContent(content);
        post.setLabels(labels);
        post.setStatus(PostStatus.UNDER_REVIEW);
        post.setCreated(new Date());
        post.setUpdated(new Date());
        postRepository.save(post);

        return post;
    }

    public Post getById(Long id) {
        return postRepository.getById(id);
    }

    public Post updateStatus(Post post, PostStatus postStatus) {
        post.setStatus(postStatus);
        post.setUpdated(new Date());
        return postRepository.update(post);
    }

    public void delete(Long id) throws IOException {
        postRepository.deleteById(id);
    }
}
