//package com.carebridge.backend.oldgraphqls;
//
//import com.carebridge.backend.task.TaskService;
//import com.carebridge.backend.task.model.Task;
//import com.carebridge.backend.task.model.TaskStatus;
//import com.carebridge.backend.task.model.TaskType;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
//import org.springframework.stereotype.Controller;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//public class TaskGraphController {
//
//    private final TaskService taskService;
//
//    public TaskGraphController(TaskService taskService) {
//        this.taskService = taskService;
//    }
//
//    @QueryMapping
//    public List<Task> getTasks(@Argument int page, @Argument int size) {
//        return taskService.findAll(PageRequest.of(page, size)).getContent();
//    }
//
//    @MutationMapping
//    public Task createTask(
//        @Argument String title, @Argument String description,
//        @Argument TaskType taskType, @Argument String neededBy,
//        @Argument UUID patientId) {
//
//        Task task = new Task(UUID.randomUUID(), title, description, taskType,
//            LocalDateTime.parse(neededBy), TaskStatus.OPEN, patientId, null);
//        return taskService.create(task);
//    }
//
//    @MutationMapping
//    public Task updateTaskStatus(@Argument UUID taskId, @Argument TaskStatus status) {
//        Task task = taskService.getById(taskId);
//        task.setStatus(status);
//        return taskService.update(taskId, task);
//    }
//
//    @SubscriptionMapping
//    public Flux<Task> onTaskCreated(@Argument UUID patientId) {
//        return taskService.getNewTaskStream(patientId);
//    }
//}
