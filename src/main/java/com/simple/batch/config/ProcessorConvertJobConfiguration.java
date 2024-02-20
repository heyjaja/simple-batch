package com.simple.batch.config;

import com.simple.batch.domain.Teacher;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class ProcessorConvertJobConfiguration {
    public static final String JOB_NAME = "processorConvertBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job job(JobRepository jobRepository, @Qualifier(BEAN_PREFIX+"step") Step step) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(step)
                .build();
    }

    @Bean(BEAN_PREFIX + "step")
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     JpaPagingItemReader<Teacher> processorConvertBatchReader, ItemProcessor<Teacher, String> processorConvertBatchProcessor) {
        return new StepBuilder(BEAN_PREFIX+"step", jobRepository)
                .<Teacher, String> chunk(chunkSize, transactionManager)
                .reader(processorConvertBatchReader)
                .processor(processorConvertBatchProcessor)
                .writer(writer())
                .build();
    }

    @Bean
    public JpaPagingItemReader<Teacher> processorConvertBatchReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX+"reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean
    public ItemProcessor<Teacher, String> processorConvertBatchProcessor() {
        return Teacher::getName;
    }

    private ItemWriter<String> writer() {
        return chunk -> {
            for (String item : chunk.getItems()) {
                log.info("Teacher Name = {}", item);
            }
        };
    }
}
