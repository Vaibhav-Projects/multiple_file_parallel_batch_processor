package com.project.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;

public class MultiFileProcessorStepListener {

	private final Logger LOGGER = LoggerFactory.getLogger(MultiFileProcessorStepListener.class);

	@BeforeStep
	public void beforeStep(StepExecution exec) {
		LOGGER.info("Step Named - {} starting", exec.getStepName());
	}

	@AfterStep
	public ExitStatus afterStep(StepExecution exec) {
		LOGGER.info("Record Read - {}", exec.getReadCount());
		LOGGER.info("Record Written - {}", exec.getWriteCount());
		LOGGER.info("Records Skipped Durring Reading- {} ", exec.getReadSkipCount());
		LOGGER.info("Records Skipped Durring Processing- {} ", exec.getProcessSkipCount());
		LOGGER.info("Records Skipped During Writing - {}", exec.getWriteSkipCount());
		LOGGER.info("Step Named - {} ended ", exec.getStepName());
		return exec.getExitStatus();
	}
}
