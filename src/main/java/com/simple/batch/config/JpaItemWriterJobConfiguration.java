package com.simple.batch.config;

import com.simple.batch.domain.Pay;
import com.simple.batch.domain.Pay2;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class JpaItemWriterJobConfiguration {
    private static final int chunkSize = 10;

    @Bean
    public Job jpaItemWriterJob(JobRepository jobRepository, Step jpaItemWriterStep) {
        return new JobBuilder("jpaItemWriterJob", jobRepository)
                .start(jpaItemWriterStep)
                .build();
    }

    @Bean
    public Step jpaItemWriterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                  ItemReader<? extends Pay> jpaItemWriterReader, ItemProcessor<? super Pay, ? extends Pay2> jpaItemProcessor,
                                  ItemWriter<? super Pay2> jpaItemWriter) {
        return new StepBuilder("jpaItemWriterStep", jobRepository)
                .<Pay, Pay2> chunk(chunkSize, transactionManager)
                .reader(jpaItemWriterReader)
                .processor(jpaItemProcessor)
                .writer(jpaItemWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> jpaItemWriterReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name("jpaItemWriterReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    @Bean
    public ItemProcessor<Pay, Pay2> jpaItemProcessor() {
        return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime());
    }

    @Bean
    public JpaItemWriter<Pay2> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Pay2> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
