package com.example.kotlinspringrest.service

import com.example.kotlinspringrest.data.Task
import com.example.kotlinspringrest.data.model.TaskCreateRequest
import com.example.kotlinspringrest.data.model.TaskDto
import com.example.kotlinspringrest.data.model.TaskUpdateRequest
import com.example.kotlinspringrest.exception.BadRequestException
import com.example.kotlinspringrest.exception.TaskNotFoundException
import com.example.kotlinspringrest.repository.TaskRepository
import org.springframework.stereotype.Service
import org.springframework.util.ReflectionUtils
import java.util.stream.Collectors
import kotlin.reflect.full.memberProperties

@Service
class TaskService(private val repository: TaskRepository) {
    private fun convertEntityToDto(task: Task): TaskDto {
        return TaskDto(
                task.id,
                task.description,
                task.isReminderSet,
                task.isTaskOpen,
                task.createdOn,
                task.priority
        )
    }

    private fun assignValueToEntity(task: Task, taskCreateRequest: TaskCreateRequest) {
        task.description = taskCreateRequest.description
        task.isReminderSet = taskCreateRequest.isReminderSet
        task.isTaskOpen = taskCreateRequest.isTaskOpen
        task.createdOn = taskCreateRequest.createdOn
        task.priority = taskCreateRequest.priority
    }

    private fun checkForTaskId(id: Long) {
        if (!repository.existsById(id))
            throw TaskNotFoundException("Task with id $id does not exist!")
    }

    fun getAllTasks(): List<TaskDto> = repository
            .findAll()
            .stream()
            .map(this::convertEntityToDto)
            .collect(Collectors.toList())

    fun getAllOpenTasks(): List<TaskDto> = repository
            .queryAllOpenTasks()
            .stream()
            .map(this::convertEntityToDto)
            .collect(Collectors.toList())

    fun getAllClosedTasks(): List<TaskDto> = repository
            .queryAllClosedTasks()
            .stream()
            .map(this::convertEntityToDto)
            .collect(Collectors.toList())

    fun getTaskById(id: Long): TaskDto {
        checkForTaskId(id)
        val task = repository.findTaskById(id)
        return convertEntityToDto(task)
    }

    fun createTask(createRequest: TaskCreateRequest): TaskDto {
        if (repository.doesDescriptionExist(createRequest.description)) {
            throw BadRequestException("There is already a task with desk = ${createRequest.description}")
        }

        val task = Task()
        assignValueToEntity(task, createRequest)
        val savedTask = repository.save(task)
        return convertEntityToDto(savedTask)
    }

    fun updateTask(id: Long, updateRequest: TaskUpdateRequest): TaskDto {
        checkForTaskId(id)
        val existingTask = repository.findTaskById(id)

        for (prop in TaskUpdateRequest::class.memberProperties) {
            if (prop.get(updateRequest) != null) {
                val field = ReflectionUtils.findField(Task::class.java, prop.name)
                field?.let {
                    it.isAccessible = true
                    ReflectionUtils.setField(it, existingTask, prop.get(updateRequest))
                }
            }
        }

        val savedTask = repository.save(existingTask)
        return convertEntityToDto(savedTask)
    }

    fun deleteTask(id: Long): String {
        checkForTaskId(id)
        repository.deleteById(id)
        return "Task with id id$ has been deleted"
    }
}
