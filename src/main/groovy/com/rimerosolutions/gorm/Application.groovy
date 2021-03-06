/*
 * Copyright 2013 Rimero Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rimerosolutions.gorm

import com.rimerosolutions.gorm.domain.Person
import com.rimerosolutions.gorm.service.PersonService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.MessageSource
import org.springframework.validation.FieldError
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.SpringApplication
import org.springframework.boot.CommandLineRunner
import org.springframework.beans.factory.annotation.Autowired

@EnableAutoConfiguration
@ComponentScan
class Application implements CommandLineRunner {
  private static final Logger LOG = LoggerFactory.getLogger(Application.class)

  @Autowired
  private ApplicationContext ctx
  
  @Autowired
  private PersonService personService

  @Autowired
  private MessageSource messageSource

  @Override
  public void run(String... args) throws Exception {
    // Dummy user objects to persist
    def persons = [
      new Person("firstName":"Yves", "lastName":"Zoundi"),
      new Person("firstName":"Rimero", "lastName":"Solutions")
    ]

    LOG.info("About to load users: ${persons}")

    // Save person if validation constraints are met
    persons.each { person ->
      if (personService.validate(person)) {
        personService.save(person)
        LOG.info("Successfully saved ${person}")
      }
      else {
        // print validation errors
        person.errors.allErrors.each { FieldError error ->
          LOG.error(messageSource.getMessage(error, Locale.getDefault()))
        }
      }
    }

    LOG.info ("\n\n1. All Persons:  ${personService.findAll()}")
    Person p = personService.findAll()[0]

    LOG.info "\n\nWe try to update the firstName to Ludovic but it should reset to Rimero1"
    // Will be set to reset to Rimero1 by beforeUpdate closure in domain
    // because we check if it is set to Ludovic
    p.firstName = "Ludovic"
    personService.save(p)

    LOG.info ("\n\n2. All Persons:  ${personService.findAll()}")

    ((ConfigurableApplicationContext) ctx).close()
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Application.class)
  }
}
