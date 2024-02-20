package com.simple.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class StepNextConditionalJobConfiguration {


    @Bean
    public Job stepNextConditionalJob(JobRepository jobRepository, Step conditionalJobStep1, Step conditionalJobStep2, Step conditionalJobStep3) {
        return new JobBuilder("stepNextConditionalJob", jobRepository)
                .start(conditionalJobStep1)
                    .on("FAILED") // step1이 FAILED 일 경우
                    .to(conditionalJobStep3) // step3으로 이동
                    .on("*") // step3의 결과와 관계 없이
                    .end() // step3을 수행하면 Flow 종료
                .from(conditionalJobStep1) // step1으로부터
                    .on("*") // FAILED 외 모든 경우
                    .to(conditionalJobStep2) // step2로 이동
                    .next(conditionalJobStep3) // step2가 정상 종료되면 step3으로 이동
                    .on("*") // step3의 결과와 관계 없이
                    .end() // flow 종료
                .end() // Job 종료
                .build();
    }

    @Bean
    public Step conditionalJobStep1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("conditionalJobStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>> This is stepNextConditionalJob Step1");

                    // ExitStatus를 FAILED로 지정. 해당 status를 보고 flow 진행
//                    contribution.setExitStatus(ExitStatus.FAILED);
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep2(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("conditionalJobStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>> This is stepNextConditionalJob Step2");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step conditionalJobStep3(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("conditionalJobStep3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>> This is stepNextConditionalJob Step3");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
