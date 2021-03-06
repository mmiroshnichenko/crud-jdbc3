package org.crud.repository.impl;

import org.crud.model.Label;
import org.crud.model.Post;
import org.crud.model.PostStatus;
import org.crud.model.Writer;
import org.crud.repository.LabelRepository;
import org.crud.repository.PostRepository;
import org.crud.utils.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcPostRepositoryImpl implements PostRepository {
    private LabelRepository labelRepository = new JdbcLabelRepositoryImpl();

    @Override
    public Post getById(Long id) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement(
                    "SELECT p.id, p.writerId, w.firstName, w.lastName, p.content, p.created, p.updated, p.status FROM post p " +
                            "INNER JOIN writer w ON p.writerId = w.id " +
                            "WHERE p.id = ?")) {

            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Post post = new Post();
                post.setId(resultSet.getLong(1));
                post.setContent(resultSet.getString(5));
                post.setCreated(resultSet.getDate(6));
                post.setUpdated(resultSet.getDate(7));
                post.setStatus(PostStatus.valueOf(resultSet.getString(8)));

                Writer writer = new Writer();
                writer.setId(resultSet.getLong(2));
                writer.setFirstName(resultSet.getString(3));
                writer.setLastName(resultSet.getString(4));
                post.setWriter(writer);

                List<Label> labels = labelRepository.getByPostId(id);
                post.setLabels(labels);
                return post;
            }

        } catch (SQLException e) {
            System.err.println("Error: post(id: " + id + ") has not found");
        }

        return null;
    }

    @Override
    public List<Post> getAll() {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement(
                    "SELECT p.id, p.writerId, w.firstName, w.lastName, p.content, p.created, p.updated, p.status FROM post p " +
                            "INNER JOIN writer w ON p.writerId = w.id ")) {

            ResultSet resultSet = preparedStatement.executeQuery();
            List<Post> posts = new ArrayList<>();

            while (resultSet.next()) {
                Post post = new Post();
                post.setId(resultSet.getLong(1));
                post.setContent(resultSet.getString(5));
                post.setCreated(resultSet.getDate(6));
                post.setUpdated(resultSet.getDate(7));
                post.setStatus(PostStatus.valueOf(resultSet.getString(8)));

                Writer writer = new Writer();
                writer.setId(resultSet.getLong(2));
                writer.setFirstName(resultSet.getString(3));
                writer.setLastName(resultSet.getString(4));
                post.setWriter(writer);
                posts.add(post);
            }
            return posts;

        } catch (SQLException e) {
            System.err.println("Error: Can't get posts");
        }

        return null;
    }

    @Override
    public Post save(Post post) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatementWithGeneratedKeys(
                "INSERT INTO post(writerId, content, created, updated, status) VALUES(?, ?, ?, ?, ?)")) {

            preparedStatement.setLong(1, post.getWriter().getId());
            preparedStatement.setString(2, post.getContent());
            preparedStatement.setDate(3, new java.sql.Date(post.getCreated().getTime()));
            preparedStatement.setDate(4, new java.sql.Date(post.getUpdated().getTime()));
            preparedStatement.setString(5, post.getStatus().name());
            preparedStatement.executeUpdate();

            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                }
                else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            for (Label label: post.getLabels()) {
                savePostLabel(post, label);
            }
            return post;

        } catch (SQLException e) {
            System.err.println("Error: new post has not saved");
        }

        return null;
    }

    private void savePostLabel(Post post, Label label) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement(
                    "INSERT INTO post_label(postId, labelId) VALUES(?, ?)")) {

            preparedStatement.setLong(1, post.getId());
            preparedStatement.setLong(2, label.getId());
            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            System.err.println("Error: post label link has not saved");
        }
    }

    private void deletePostLabels(Post post) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement(
                    "DELETE FROM post_label WHERE postId = ?")) {

            preparedStatement.setLong(1, post.getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error: post label link (postId: " + post.getId() + ")has not deleted");
        }
    }

    @Override
    public Post update(Post post) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement(
                    "UPDATE post SET writerId = ?, content = ?, updated = ?, status = ? WHERE id = ?")) {

            preparedStatement.setLong(1, post.getWriter().getId());
            preparedStatement.setString(2, post.getContent());
            preparedStatement.setDate(3, new java.sql.Date(System.currentTimeMillis()));
            preparedStatement.setString(4, post.getStatus().name());
            preparedStatement.executeUpdate();

            deletePostLabels(post);
            for (Label label: post.getLabels()) {
                savePostLabel(post, label);
            }
            return post;

        } catch (SQLException e) {
            System.err.println("Error: post(id: " + post.getId() + ") has not updated");
        }

        return null;
    }

    @Override
    public void deleteById(Long id) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement("DELETE FROM post WHERE id = ?")) {

            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error: post (id: " + id + ")has not deleted");
        }
    }

    @Override
    public List<Post> getAllByWriter(Writer writer) {
        try(PreparedStatement preparedStatement = JdbcUtils.getPreparedStatement(
                    "SELECT id, content, created, updated, status FROM post  " +
                            " WHERE writerId = ?")) {

            preparedStatement.setLong(1, writer.getId());
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Post> posts = new ArrayList<>();

            while (resultSet.next()) {
                Post post = new Post();
                post.setId(resultSet.getLong(1));
                post.setContent(resultSet.getString(5));
                post.setCreated(resultSet.getDate(6));
                post.setUpdated(resultSet.getDate(7));
                post.setStatus(PostStatus.valueOf(resultSet.getString(8)));

                post.setWriter(writer);
                posts.add(post);
            }
            return posts;

        } catch (SQLException e) {
            System.err.println("Error: Can't get posts");
        }

        return null;
    }
}
