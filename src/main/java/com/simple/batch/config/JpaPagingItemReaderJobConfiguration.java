package com.simple.batch.config;

import com.simple.batch.domain.Pay;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class JpaPagingItemReaderJobConfiguration {
    private static final int chunkSize = 10;

    @Bean
    public Job jpaPagingItemReaderJob(JobRepository jobRepository, Step jpaPagingItemReaderStep) {
        return new JobBuilder("jpaPagingItemReaderJob", jobRepository)
                .start(jpaPagingItemReaderStep)
                .build();
    }

    @Bean
    public Step jpaPagingItemReaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                        JpaPagingItemReader<Pay> jpaPagingItemReader, ItemWriter<Pay> jpaPagingItemWriter) {
        return new StepBuilder("jpaPagingItemReaderStep", jobRepository)
                .<Pay, Pay> chunk(chunkSize, transactionManager)
                .reader(jpaPagingItemReader)
                .writer(jpaPagingItemWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> jpaPagingItemReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name("jpaPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(chunkSize)
                .queryString("SELECT p FROM Pay p WHERE amount >= 2000")
                .build();
    }

    @Bean
    public ItemWriter<Pay> jpaPagingItemWriter() {
        return list -> {
            for (Pay pay : list) {
                log.info("Current Pay = {}", pay);
            }
        };
    }

}
