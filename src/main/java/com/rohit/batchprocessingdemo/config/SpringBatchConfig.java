package com.rohit.batchprocessingdemo.config;

import com.rohit.batchprocessingdemo.entity.Customer;
import com.rohit.batchprocessingdemo.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;



@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {



    private CustomerRepository customerRepository;

    @Bean
    public FlatFileItemReader<Customer> reader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("D:/springboot/batch-processing-demo/src/main/resources/customers.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());

        return itemReader;

    }



    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;

    }


    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

//  @Bean
//  public FlatFileItemWriter<Customer> writer()
//  {
//      //Create writer instance
//      FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();
//
//      //Set output file location
//
//      writer.setResource(new FileSystemResource("D:/springboot/batch-processing-demo/src/main/resources/output.csv"));
//      //All job repetitions should "append" to same output file
//      writer.setAppendAllowed(true);
//
//      //Name field values sequence based on object properties
//      writer.setLineAggregator(new DelimitedLineAggregator<>() {
//
//          {
//              setDelimiter(",");
//              setFieldExtractor(new BeanWrapperFieldExtractor<>() {
//                  {
//                      setNames(new String[] {"id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob" });
//                  }
//              });
//          }
//      });
//      return writer;
//  }




    @Bean
    public RepositoryItemWriter<Customer> writer() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("csv-step",jobRepository)
                .<Customer, Customer>chunk(1,transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job runJob(JobRepository jobRepository,PlatformTransactionManager transactionManager) {
        return new JobBuilder("importCustomers",jobRepository)
                .flow(step1(jobRepository,transactionManager)).end().build();

    }




}
