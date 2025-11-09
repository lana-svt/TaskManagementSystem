package task.management.system.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import task.management.system.task.*;
import task.management.system.bd.TaskEntity;
import task.management.system.bd.TaskRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository repository;
    private static final int MAX_COUNT_IN_PROGRESS = 4;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository repository, TaskMapper taskMapper){
        this.repository = repository;
        this.taskMapper = taskMapper;
    }

    public Task createTask(Task task){
        if(task.id() != null){
            logger.warn("Task id must be empty");
            throw new IllegalArgumentException("Task id must be empty");
        }
        if(task.status() != null){
            logger.warn("Task status must be empty");
            throw new IllegalArgumentException("Task status must be empty");
        }
        if(task.deadlineDate() == null || task.assignedUserId() == null){
            logger.warn("Task deadline date and assignedUserId must be filled in");
            throw new IllegalArgumentException("Task deadline date and assignedUserId must be filled in");
        }

        TaskEntity resultTask = new TaskEntity(
                null,
                task.creatorId(),
                task.assignedUserId(),
                TaskStatus.CREATED,
                task.createDateTime(),
                task.deadlineDate(),
                task.priority()
        );
        TaskEntity savedEntity = repository.save(resultTask);
        logger.info("Task with id = {} created", resultTask.getId());
        return taskMapper.convertToTask(savedEntity);
    }

    @Transactional
    public Task transitionTaskInProgress(Long id){
        TaskEntity taskEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found task by id = " + id));
        if(taskEntity.getAssignedUserId() == null) {
            logger.warn("AssignedUserId must be fill in");
            throw new IllegalArgumentException("AssignedUserId must be fill in");
        }
        checkCountInProgressTasks(taskEntity.getAssignedUserId());

        taskEntity.setStatus(TaskStatus.IN_PROGRESS);
        repository.save(taskEntity);
        logger.info("Task with id = {} updated status to IN_PROGRESS", taskEntity.getId());
        return taskMapper.convertToTask(taskEntity);
    }

    public Task transitionTaskToDone(Long id){
        TaskEntity taskEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found task by id = " + id));
        if(taskEntity.getAssignedUserId() == null || taskEntity.getDeadlineDate() == null){
            logger.warn("Fields AssignedUserId and DeadlineDate must be fill in");
            throw new IllegalArgumentException("Fields AssignedUserId and DeadlineDate must be fill in");
        }
        taskEntity.setStatus(TaskStatus.DONE);
        taskEntity.setDoneDateTime(LocalDateTime.now());
        repository.save(taskEntity);
        logger.info("Task with id = {} updated status to DONE", taskEntity.getId());
        return taskMapper.convertToTask(taskEntity);
    }

    public Task getTaskById(Long id){
        TaskEntity taskEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found task by id = " + id));
        logger.info("Get task by id = {}", id);
        return taskMapper.convertToTask(taskEntity);
    }

    public List<Task> getAllTasksByFilter(TaskSearchFilter taskSearchFilter){
        int sizePage = taskSearchFilter.pageSize() != null ? taskSearchFilter.pageSize() : 10;
        int pageNumber = taskSearchFilter.pageNum() != null ? taskSearchFilter.pageNum() : 0;
        Pageable pageable = Pageable.ofSize(sizePage).withPage(pageNumber);

        List<TaskEntity> allTasks = repository.searchAllByFilter(
                taskSearchFilter.creatorId(),
                taskSearchFilter.assignedUserId(),
                taskSearchFilter.status(),
                taskSearchFilter.priority(),
                pageable
        );
        logger.info("Get list of all tasks by filter");
        return allTasks.stream().map(taskMapper::convertToTask).toList();
    }

    @Transactional
    public Task updateTask(Long id, Task task){
        TaskEntity taskEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found task by id = " + id));
        if(task.status() != TaskStatus.IN_PROGRESS && taskEntity.getStatus() == TaskStatus.DONE) {
            logger.warn("Can't update task with status DONE to status CREATED");
            throw new IllegalArgumentException("Can't update task with status DONE to status CREATED");
        }
        checkCountInProgressTasks(taskEntity.getAssignedUserId());

        TaskEntity updateTaskEntity = new TaskEntity(
                id,
                task.creatorId(),
                task.assignedUserId(),
                task.status(),
                task.createDateTime(),
                task.deadlineDate(),
                task.priority()
        );
        repository.save(updateTaskEntity);
        logger.info("Task with id = {} updated", id);
        return taskMapper.convertToTask(updateTaskEntity);
    }

    public void deleteTask(Long id){
        if(!repository.existsById(id)) {
            logger.warn("Not found task by id = {}", id);
            throw new EntityNotFoundException("Not found task by id = " + id);
        }
        repository.deleteById(id);
        logger.info("Task with id = {} deleted", id);
    }

    private void checkCountInProgressTasks(Long assignedUserId){
        Long countInProgress = repository.countInProgressTasksByUser(assignedUserId);
        if(countInProgress > 4) {
            logger.warn("User with ID {} has {} active tasks. Maximum allowed is {}",
                    assignedUserId, countInProgress, MAX_COUNT_IN_PROGRESS);
            throw new IllegalStateException(String.format("User with ID %d has %d active tasks. Maximum allowed is %d",
                    assignedUserId, countInProgress, MAX_COUNT_IN_PROGRESS));
        }
    }
}
