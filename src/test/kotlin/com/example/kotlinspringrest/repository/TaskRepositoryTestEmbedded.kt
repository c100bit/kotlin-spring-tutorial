package com.example.kotlinspringrest.repository

import com.example.kotlinspringrest.data.Task
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql

@DataJpaTest(properties = ["spring.jpa.properties.javax.persistence.validation.mode=none"])
internal class TaskRepositoryTestEmbedded {
    @Autowired
    private lateinit var objectUnderTest: TaskRepository

    private val numberOfRecordsInTestDataSql = 3
    private val numberOfClosedTasksInTestDataSql = 2
    private val numberOfOpenTasksInTestDataSql = 1

    @Test
    @Sql("classpath:test-data.sql")
    fun `when task saved through SQL file then check if it's not null`() {
        val task = objectUnderTest.findTaskById(111)
        assertThat(task).isNotNull
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `check for number of tasks`() {
        val tasks = objectUnderTest.findAll()
        assertThat(tasks.size).isEqualTo(numberOfRecordsInTestDataSql)
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when task saved through SQL file then remove it by id`() {
        objectUnderTest.deleteById(112)
        val tasks: List<Task> = objectUnderTest.findAll()
        assertThat(tasks.size).isEqualTo(numberOfRecordsInTestDataSql - 1)
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when task saved through SQL file then check for the number of open tasks`() {
        val tasks: List<Task> = objectUnderTest.queryAllOpenTasks()
        assertThat(tasks.size).isEqualTo(numberOfOpenTasksInTestDataSql)
    }

    @Test
    @Sql("classpath:test-data.sql")
    fun `when description is queried then check if descriptions already exists`() {
        val isDescriptionAlreadyGiven1 = objectUnderTest.doesDescriptionExist("first test todo")
        val isDescriptionAlreadyGiven2 = objectUnderTest.doesDescriptionExist("any todo")

        assertThat(isDescriptionAlreadyGiven1).isTrue
        assertThat(isDescriptionAlreadyGiven2).isFalse
    }

}
