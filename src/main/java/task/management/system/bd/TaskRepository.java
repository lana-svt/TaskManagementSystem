package task.management.system.bd;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import task.management.system.task.TaskPriority;
import task.management.system.task.TaskStatus;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.assignedUserId = :assignedUserId AND t.status = 'IN_PROGRESS'")
    Long countInProgressTasksByUser(@Param("assignedUserId") Long assignedUserId);

    @Query("SELECT t FROM TaskEntity t WHERE " +
            "(:creatorId IS NULL OR t.creatorId = :creatorId) AND " +
            "(:assignedUserId IS NULL OR t.assignedUserId = :assignedUserId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority)")
    List<TaskEntity> searchAllByFilter(
            @Param("creatorId") Long creatorId,
            @Param("assignedUserId") Long assignedUserId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            Pageable pageable
    );
}
