package com.project.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.poi.PoiItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.project.domain.Order;
import com.project.listener.MultiFileProcessorJobListener;
import com.project.listener.MultiFileProcessorStepListener;
import com.project.mapper.FileDataMapper;
import com.project.processor.FileDataProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	private JobBuilderFactory jobBuilder;

	@Autowired
	private StepBuilderFactory stepBuilder;

	@Bean(name = "multiFileProcessorJob")
	public Job multiFileProcessorJob() throws Exception {
		return jobBuilder.get("multiFileProcessorJob").start(multiFileProcessorStep()).listener(jobListener()).build();
	}

	@Bean
	public Step multiFileProcessorStep() throws Exception {
		return stepBuilder.get("multiFileProcessorStep").listener(stepListener())
				.partitioner("multiFileProcessorSlaveStep", partitioner()).step(multiFileProcessorSlaveStep())
				.taskExecutor(taskExecutor()).build();
	}

	@Bean
	public Step multiFileProcessorSlaveStep() throws Exception {
		return stepBuilder.get("multiFileProcessorSlaveStep").<Order, Order>chunk(1).reader(dataReader(null))
				.processor(dataProcessor()).writer(dataWriter(null)).build();

	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(5);
		taskExecutor.setMaxPoolSize(5);
		taskExecutor.setQueueCapacity(5);
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Bean
	public MultiResourcePartitioner partitioner() {
		MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			partitioner.setResources(resolver.getResources("file:src/main/resources/input/*.xlsx"));
		} catch (IOException e) {
			throw new RuntimeException("I/O problems when resolving the input file pattern.", e);
		}
		return partitioner;
	}

	@Bean()
	@StepScope
	public ItemReader<Order> dataReader(@Value("#{stepExecutionContext['fileName']}") String file) throws Exception {
		PoiItemReader<Order> reader = new PoiItemReader<Order>();
		reader.setLinesToSkip(1);
		reader.setResource(new UrlResource(file));
		reader.setRowMapper(dataMapper());
		reader.afterPropertiesSet();
		reader.open(new ExecutionContext());
		return reader;
	}

	@Bean
	@StepScope
	public JdbcBatchItemWriter<Order> dataWriter(DataSource dataSource) {
		return new JdbcBatchItemWriterBuilder<Order>()
				.itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Order>())
				.sql("INSERT INTO ORDER_DETAIL (order_code, customer_name, customer_address, cutomer_number, product_id, product_name, product_price) VALUES (:orderCode, :customerName, :customerAddress, :cutomerNumber, :productId, :productName, :productPrice)")
				.dataSource(dataSource).build();
	}

	@Bean
	public JdbcTemplate jdbcTemplate(DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}

	@Bean
	@StepScope
	public ItemProcessor<Order, Order> dataProcessor() {
		return new FileDataProcessor();
	}

	@Bean
	@StepScope
	public RowMapper<Order> dataMapper() {
		return new FileDataMapper();
	}

	@Bean
	public MultiFileProcessorJobListener jobListener() {
		return new MultiFileProcessorJobListener();
	}

	@Bean
	public MultiFileProcessorStepListener stepListener() {
		return new MultiFileProcessorStepListener();
	}

	private ResourcelessTransactionManager getTransactionManager() {
		return new ResourcelessTransactionManager();
	}

	@Bean
	public DataSource dataSource() {
		EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
		return builder.setType(EmbeddedDatabaseType.H2)
				.addScript("classpath:org/springframework/batch/core/schema-drop-h2.sql")
				.addScript("classpath:org/springframework/batch/core/schema-h2.sql").build();
	}

	public JobRepository getJobRepository() throws Exception {
		JobRepositoryFactoryBean jobRepoFactory = new JobRepositoryFactoryBean();
		jobRepoFactory.setDataSource(dataSource());
		jobRepoFactory.setTransactionManager(getTransactionManager());
		jobRepoFactory.afterPropertiesSet();
		return jobRepoFactory.getObject();
	}

	public JobLauncher getJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(getJobRepository());
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}
}
