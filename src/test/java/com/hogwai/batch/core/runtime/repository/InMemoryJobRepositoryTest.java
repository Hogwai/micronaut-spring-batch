package com.hogwai.batch.core.runtime.repository;

import com.hogwai.batch.core.config.JobParameters;
import com.hogwai.batch.core.runtime.BatchStatus;
import com.hogwai.batch.core.runtime.JobExecution;
import com.hogwai.batch.core.runtime.JobInstance;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InMemoryJobRepositoryTest {

    @Test
    void shouldCreateJobInstanceWithIncrementingIds() {
        InMemoryJobRepository repo = new InMemoryJobRepository();
        JobParameters params = JobParameters.builder().toJobParameters();

        JobInstance i1 = repo.createJobInstance("job1", params);
        JobInstance i2 = repo.createJobInstance("job2", params);

        assertThat(i1.id()).isLessThan(i2.id());
        assertThat(i1.jobName()).isEqualTo("job1");
    }

    @Test
    void shouldCreateJobExecutionWithStartedStatus() {
        InMemoryJobRepository repo = new InMemoryJobRepository();
        JobParameters params = JobParameters.builder().toJobParameters();
        JobInstance instance = repo.createJobInstance("job", params);

        JobExecution exec = repo.createJobExecution(instance, params);

        assertThat(exec.getStatus()).isEqualTo(BatchStatus.STARTED);
        assertThat(exec.getStartTime()).isNotNull();
        assertThat(exec.getJobInstance()).isEqualTo(instance);
    }

    @Test
    void shouldRetrieveJobExecutionById() {
        InMemoryJobRepository repo = new InMemoryJobRepository();
        JobParameters params = JobParameters.builder().toJobParameters();
        JobInstance instance = repo.createJobInstance("job", params);
        JobExecution exec = repo.createJobExecution(instance, params);

        assertThat(repo.getJobExecution(exec.getId())).isSameAs(exec);
    }
}
