package com.simple.batch.job;

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
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class CustomItemWriterJobConfiguration {
    private static final int chunkSize = 10;

    @Bean
    public Job customItemWriterJob(JobRepository jobRepository, Step customItemWriterStep) {
        return new JobBuilder("customItemWriterJob", jobRepository)
                .start(customItemWriterStep)
                .build();
    }

    @Bean
    public Step customItemWriterStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                     ItemReader<? extends Pay> customItemWriterReader,
                                     ItemProcessor<? super Pay, ? extends Pay2> customItemWriterProcessor,
                                     ItemWriter<? super Pay2> customItemWriter) {
        return new StepBuilder("customItemWriterStep", jobRepository)
                .<Pay, Pay2> chunk(chunkSize, transactionManager)
                .reader(customItemWriterReader)
                .processor(customItemWriterProcessor)
                .writer(customItemWriter)
                .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> customItemWriterReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Pay>()
                .name("customItemWriterReader")
                .pageSize(chunkSize)
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT p FROM Pay p")
                .build();
    }

    @Bean
    public ItemProcessor<Pay, Pay2> customItemWriterProcessor() {
        return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime());
    }

    @Bean
    public ItemWriter<Pay2> customItemWriter() {
        return chunk -> {
            for (Pay2 pay2 : chunk.getItems()) {
                log.info("Current pay = {}", pay2);
            }
        };
    }


}
