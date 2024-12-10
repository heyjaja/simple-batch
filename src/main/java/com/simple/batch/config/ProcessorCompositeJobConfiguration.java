package com.simple.batch.config;

import com.simple.batch.domain.Teacher;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ProcessorCompositeJobConfiguration {
    public static final String JOB_NAME = "processorCompositeBatch";
    public static final String BEAN_PREFIX = JOB_NAME + "_";

    @Value("${chunkSize:1000}")
    private int chunkSize;

    @Bean(JOB_NAME)
    public Job job(JobRepository jobRepository, @Qualifier(BEAN_PREFIX + "step") Step step) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .preventRestart()
                .start(step)
                .build();
    }

    @Bean(BEAN_PREFIX+"step")
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                     @Qualifier(BEAN_PREFIX+"reader") JpaPagingItemReader<Teacher> reader,
                     @Qualifier(BEAN_PREFIX+"processor") CompositeItemProcessor<Teacher, String> processor) {
        return new StepBuilder(BEAN_PREFIX+"step", jobRepository)
                .<Teacher, String> chunk(chunkSize, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer())
                .build();
    }

    @Bean(BEAN_PREFIX+"reader")
    public JpaPagingItemReader<Teacher> reader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Teacher>()
                .name(BEAN_PREFIX + "reader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT t FROM Teacher t")
                .build();
    }

    @Bean(BEAN_PREFIX+"processor")
    public CompositeItemProcessor<Teacher, String> compositeItemProcessor() {
        List<ItemProcessor<?, String>> delegates = new ArrayList<>(2);
        delegates.add(processor1());
        delegates.add(processor2());

        CompositeItemProcessor<Teacher, String> processor = new CompositeItemProcessor<>();

        processor.setDelegates(delegates);

        return processor;
    }

    public ItemProcessor<Teacher, String> processor1() {
        return Teacher::getName;
    }

    public ItemProcessor<String, String> processor2() {
        return name -> "안녕하세요. " + name + "입니다.";
    }


    private ItemWriter<String> writer() {
        return chunk -> {
            log.info(">>>>>> Item Writer");
            for (String item : chunk.getItems()) {
                log.info("Teacher Name={}", item);
            }
        };
    }
}
