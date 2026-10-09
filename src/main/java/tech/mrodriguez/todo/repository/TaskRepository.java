package tech.mrodriguez.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.mrodriguez.todo.model.entity.Task;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser_Id(Long userId);
    Optional<Task> findByIdAndUser_Id(Long taskId, Long userId);
}
