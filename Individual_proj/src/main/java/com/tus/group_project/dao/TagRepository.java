package com.tus.group_project.dao;

import com.tus.group_project.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name); // ✅ Find tags by name
}
