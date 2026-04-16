package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class TaskGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private TaskService taskService;

    @Test
    void getTasks_ShouldReturnTasks() {
        Task task = new Task(UUID.randomUUID(), "Title", "Desc", TaskType.ERRAND, LocalDateTime.now(), TaskStatus.OPEN, UUID.randomUUID(), null);
        when(taskService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(task)));

        String query = """
                query {
                  getTasks(page: 0, size: 10) {
                    id
                    title
                    status
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getTasks")
                .entityList(Task.class)
                .hasSize(1);
    }

    @Test
    void createTask_ShouldReturnCreatedTask() {
        UUID patientId = UUID.randomUUID();
        Task task = new Task(UUID.randomUUID(), "Title", "Desc", TaskType.ERRAND, LocalDateTime.of(2023, 10, 27, 10, 0), TaskStatus.OPEN, patientId, null);
        when(taskService.create(any(Task.class))).thenReturn(task);

        String mutation = """
                mutation($title: String!, $description: String!, $taskType: TaskType!, $neededBy: String!, $patientId: ID!) {
                  createTask(title: $title, description: $description, taskType: $taskType, neededBy: $neededBy, patientId: $patientId) {
                    id
                    title
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("title", "Title")
                .variable("description", "Desc")
                .variable("taskType", "ERRAND")
                .variable("neededBy", "2023-10-27T10:00:00")
                .variable("patientId", patientId)
                .execute()
                .errors()
                .verify()
                .path("createTask")
                .entity(Task.class)
                .matches(t -> t.getTitle().equals("Title"));
    }
}
