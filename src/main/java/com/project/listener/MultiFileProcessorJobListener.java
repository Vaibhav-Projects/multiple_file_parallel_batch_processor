package com.project.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;

public class MultiFileProcessorJobListener implements JobExecutionListener {

	private final Logger LOGGER = LoggerFactory.getLogger(MultiFileProcessorJobListener.class);

	@BeforeJob
	public void beforeJob(JobExecution exec) {
		LOGGER.info("Job Named - {} starting", exec.getJobInstance().getJobName());
	}

	@AfterJob
	public void afterJob(JobExecution exec) {
		LOGGER.info("Job Named - {} ended with status - {}", exec.getJobInstance().getJobName(), exec.getExitStatus());
	}

}
