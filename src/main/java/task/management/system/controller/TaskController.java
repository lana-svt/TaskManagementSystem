package task.management.system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import task.management.system.service.TaskService;
import task.management.system.task.Task;
import task.management.system.task.TaskPriority;
import task.management.system.task.TaskSearchFilter;
import task.management.system.task.TaskStatus;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        logger.info("Called createTask");
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Task> transitionTaskInProgress(@PathVariable("id") Long id) {
        logger.info("Called transitionTaskInProgress");
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.transitionTaskInProgress(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Task> transitionTaskToDone(@PathVariable("id") Long id){
        logger.info("Called transitionTaskToDone");
        return ResponseEntity.ok(taskService.transitionTaskToDone(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable("id") Long id) {
        logger.info("Called getTask");
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(name = "creatorId", required = false) Long creatorId,
            @RequestParam(name = "assignedUserId", required = false) Long assignedUserId,
            @RequestParam(name = "status", required = false) TaskStatus status,
            @RequestParam(name = "priority", required = false) TaskPriority priority,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "pageNum", required = false) Integer pageNum

    ) {
        TaskSearchFilter taskSearchFilter = new TaskSearchFilter(
                creatorId,
                assignedUserId,
                status,
                priority,
                pageSize,
                pageNum
        );
        logger.info("Called getAllTasks");
        return ResponseEntity.ok(taskService.getAllTasksByFilter(taskSearchFilter));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable("id") Long id, @RequestBody Task task) {
        logger.info("Called updateTasks");
        return ResponseEntity.ok(taskService.updateTask(id, task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable("id") Long id) {
        logger.info("Called deleteTask");
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }
}
