package com.simple.batch.config;

import com.simple.batch.domain.Pay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class JdbcCursorItemReaderJobConfiguration {

    private static final int chunkSize = 10;

    @Bean
    public Job jdbcCursorItemReaderJob(JobRepository jobRepository, Step jdbcCursorItemReaderStep) {
        return new JobBuilder("jdbcCursorItemReaderJob", jobRepository)
                .start(jdbcCursorItemReaderStep)
                .build();
    }

    @Bean
    public Step jdbcCursorItemReaderStep(JobRepository jobRepository, PlatformTransactionManager transactionManager,
                                         JdbcCursorItemReader<Pay> jdbcCursorItemReader, ItemWriter<Pay> jdbcCursorItemWriter) {
        return new StepBuilder("jdbcCursorItemReaderStep", jobRepository)
                .<Pay, Pay> chunk(chunkSize, transactionManager)
                .reader(jdbcCursorItemReader)
                .writer(jdbcCursorItemWriter)
                .build();
    }

    @Bean
    public ItemWriter<Pay> jdbcCursorItemWriter() {
        return list -> {
            for (Pay pay : list) {
                log.info("Current Pay = {}", pay);
            }
        };
    }

    @Bean
    public JdbcCursorItemReader<Pay> jdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Pay>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Pay.class))
                .sql("SELECT id, amount, tx_name, tx_date_time FROM pay")
                .name("jdbcCursorItemReader")
                .build();
    }
}
