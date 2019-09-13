package org.crazyit.act.c3;


import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;

public class FirstAct {

    public void deployment(){
        //流程引擎
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        //资源服务
        RepositoryService rs = engine.getRepositoryService();
        //运行服务
        RuntimeService runService = engine.getRuntimeService();
        //任务服务
        TaskService taskService = engine.getTaskService();
        //部署流程文件
        //Deployment deployment = rs.createDeployment().addClasspathResource("first.bpmn").deploy();
    }

    public static void main(String[] args) throws Exception{
        //流程引擎
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        //资源服务
        RepositoryService rs = engine.getRepositoryService();
        //运行服务
        RuntimeService runService = engine.getRuntimeService();
        //任务服务
        TaskService taskService = engine.getTaskService();
        //部署流程文件
        //Deployment deployment = rs.createDeployment().addClasspathResource("first.bpmn").deploy();

        //这里的key是指流程定义的key
        ProcessInstance pi = runService.startProcessInstanceByKey("myProcess_1");

        engine.close();
    }

}
