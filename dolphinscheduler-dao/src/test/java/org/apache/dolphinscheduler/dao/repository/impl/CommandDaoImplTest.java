/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.dao.repository.impl;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskDependType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.mapper.CommandMapper;
import org.apache.dolphinscheduler.dao.repository.CommandDao;

import org.apache.commons.lang3.RandomUtils;

import java.util.List;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

class CommandDaoImplTest extends BaseDaoTest {

    @Autowired
    private CommandDao commandDao;

    @Autowired
    private CommandMapper commandMapper;

    @RepeatedTest(value = 100)
    void fetchCommandByIdSlot() {
        // clear all commands
        commandMapper.delete(new QueryWrapper<Command>().ge("id", -1));

        int totalSlot = RandomUtils.nextInt(1, 10);
        int currentSlotIndex = RandomUtils.nextInt(0, totalSlot);
        int fetchSize = RandomUtils.nextInt(10, 100);
        int idStep = RandomUtils.nextInt(1, 5);
        int commandSize = RandomUtils.nextInt(currentSlotIndex, 1000);
        // Generate commandSize commands
        int id = 0;
        for (int j = 0; j < commandSize; j++) {
            Command command = generateCommand(CommandType.START_PROCESS, 0);
            command.setId(id);
            id += idStep;
            commandDao.insert(command);
        }

        List<Command> commands = commandDao.queryCommandByIdSlot(currentSlotIndex, totalSlot, idStep, fetchSize);
        assertFalse(commands.isEmpty(),
                "Commands should not be empty, currentSlotIndex: " + currentSlotIndex +
                        ", totalSlot: " + totalSlot +
                        ", idStep: " + idStep +
                        ", fetchSize: " + fetchSize +
                        ", total command size: " + commandSize +
                        ", total commands: " + commandDao.queryAll());
        assertThat(commands.size())
                .isEqualTo(commandDao.queryAll()
                        .stream()
                        .filter(command -> (command.getId() / idStep) % totalSlot == currentSlotIndex)
                        .limit(fetchSize)
                        .count());

    }

    private Command generateCommand(CommandType commandType, int processDefinitionCode) {
        Command command = new Command();
        command.setCommandType(commandType);
        command.setProcessDefinitionCode(processDefinitionCode);
        command.setExecutorId(4);
        command.setCommandParam("test command param");
        command.setTaskDependType(TaskDependType.TASK_ONLY);
        command.setFailureStrategy(FailureStrategy.CONTINUE);
        command.setWarningType(WarningType.ALL);
        command.setWarningGroupId(1);
        command.setScheduleTime(DateUtils.stringToDate("2019-12-29 12:10:00"));
        command.setProcessInstancePriority(Priority.MEDIUM);
        command.setStartTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setUpdateTime(DateUtils.stringToDate("2019-12-29 10:10:00"));
        command.setWorkerGroup(Constants.DEFAULT_WORKER_GROUP);
        command.setProcessInstanceId(0);
        command.setProcessDefinitionVersion(0);
        return command;
    }
}
