package task.management.system.task;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Task(
        Long id,
        @NotNull
        Long creatorId,
        Long assignedUserId,
        TaskStatus status,
        LocalDateTime createDateTime,
        @NotNull
        @Future
        LocalDate deadlineDate,
        @NotNull
        TaskPriority priority,
        LocalDateTime doneDateTime
) {
}
