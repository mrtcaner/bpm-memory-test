/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.jbpm.thread;

import org.camunda.bpm.engine.OptimisticLockingException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;


/**
 * @author Joram Barrez
 */
public class CompleteTasksThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(CompleteTasksThread.class);

    private boolean running;
    private TaskService taskService;
    private String user;
    private int maxDelaySecondsBetweenTaskChecks;
    private Random random = new Random();

    public CompleteTasksThread(ProcessEngine processEngine, String user, int maxDelaySecondsBetweenTaskChecks) {
        this.taskService = processEngine.getTaskService();
        this.user = user;
        this.maxDelaySecondsBetweenTaskChecks = maxDelaySecondsBetweenTaskChecks;
    }

    public void run() {
        running = true;
        while (running) {

            List<Task> tasks = taskService.createTaskQuery().taskAssignee(user).list();
            try {
                for (Task task : tasks) {
                    taskService.complete(task.getId());
                }
            } catch (OptimisticLockingException e) {
                // Ignore it, we'll complete the tasks next time
            } catch (Exception e) {
                logger.warn("Got exception, retrtrying later: " + e);
            }


            try {
                Thread.sleep(random.nextInt(maxDelaySecondsBetweenTaskChecks) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void halt() {
        running = false;
    }

}
