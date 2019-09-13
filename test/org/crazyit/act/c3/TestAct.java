package org.crazyit.act.c3;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.MembershipEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipInputStream;


public class TestAct {

    private RepositoryService repositoryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private HistoryService historyService;
    private IdentityService identityService;

    /**
     * 获取服务
     */
    @Before
    public void addService(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        this.repositoryService = engine.getRepositoryService();
        this.runtimeService = engine.getRuntimeService();
        this.taskService = engine.getTaskService();
        this.historyService = engine.getHistoryService();
        this.identityService = engine.getIdentityService();
    }

    /**
     * 部署文件
     * @throws Exception
     */
    @Test
    public void deployment() throws Exception{
        //第一种：导入bpmn文件
       Deployment deployment1 = repositoryService.createDeployment().addClasspathResource("setVariable.bpmn").deploy();
        //第二种：导入压缩包
        InputStream inputStream = new FileInputStream(new File("resource/testZip.zip"));
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        //Deployment deployment2 = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
        //第三种：通过代码动态部署流程
        //Deployment deployment3 = repositoryService.createDeployment().addBpmnModel("My Porcess001",createProcessModel()).deploy();

        //部署bpmn文件时会验证bpmn文件是否满足规范，不满足会报错
        //DeploymentBuilder db =  repositoryService.createDeployment().addClasspathResource("error.bpmn");
        //下面的方法可以取消验证
        //db.disableSchemaValidation().deploy();


    }

    /**
     * 启动流程
     */
    @Test
    public void startPorcess(){

        ProcessInstance pi = runtimeService.startProcessInstanceByKey("variable","uuid0012");

    }

    /**
     * 指定用户才能启动流程
     */
    @Test
    public void startProcessByAssignUser(){

        User user = identityService.newUser(UUID.randomUUID().toString());
        user.setFirstName("linrenjia");
        identityService.saveUser(user);
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionId("commonApprove:1:27504").singleResult();
        //流程定义和用户的关系存放在act_ru_identitylink
        repositoryService.addCandidateStarterUser(definition.getId(),user.getId());
        //查询到用户可以启动哪些流程定义
        List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().startableByUser(user.getId()).list();

    }


    /**
     * 查询任务
     */
    @Test
    public void queryTask(){
        //act_ru_task表的查询器 命名规则：createXXXXQuery
        TaskQuery taskQuery = taskService.createTaskQuery();
        //可以通过链式编程设置条件，这些条件的方法规则：XXX字段名，以该表某些字段做为条件
        taskQuery = taskQuery.processInstanceId("10001").taskId("10005");
        //还可以设置排序的方式
        taskQuery = taskQuery.orderByTaskName().desc();
        //获取结果
            //如果只有一条，可用singleResult
        Task task = taskQuery.singleResult();
            //如果结果有多条，必须用list
        List<Task> tasks = taskQuery.list();

        //使用原生的sql查询
        NativeTaskQuery nativeTaskQuery = taskService.createNativeTaskQuery();
        nativeTaskQuery = nativeTaskQuery.sql("select * from act_ru_task where id_ = #{id} ").parameter("id","10005");
        Task task1 = nativeTaskQuery.singleResult();

    }

    /**
     * 完成任务
     */
    @Test
    public void completeTask(){
        Task task = taskService.createTaskQuery().processInstanceId("2501").singleResult();

        taskService.complete(task.getId());


    }

    /**
     * 从数据库中获取资源文件
     * @throws Exception
     */
    @Test
    public void findResourceFile() throws Exception{
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("myProcess_1").singleResult();

        //从数据库中获取bpmn文件
        InputStream in = repositoryService.getProcessModel(definition.getId());
        OutputStream out = new FileOutputStream(new File("resource/outFile.bpmn"));
        //IOUtils.copy(in,out);
        in.close();
        out.close();

        //从数据库中获取bpmn图片
        InputStream in1 = repositoryService.getProcessDiagram(definition.getId());
        BufferedImage image = ImageIO.read(in1);
        //ImageIO.write(image,"png",new File("resource/outImage.png"));
        in1.close();

        //从数据库中读取其他文件
        InputStream in2 = repositoryService.getResourceAsStream("12501","2.txt");
        OutputStream out2 = new FileOutputStream(new File("resource/outText.txt"));
        //IOUtils.copy(in2,out2);
        in2.close();
        out2.close();


    }

    /**
     * 删除资源数据
     */
    @Test
    public void deleteDeployData(){
        //因为act_re_deployment是顶级表，删除它就可以删除其他资源
        Deployment deployment = repositoryService.createDeploymentQuery().deploymentId("1").singleResult();
        //1、级联 ：删除所有相关的资源数据
        //2、不级联：只删除身份数据，流程定义数据，流程资源数据，部署数据，条件：不能存在运行时的数据，否则删除失败
        repositoryService.deleteDeployment(deployment.getId(),false);

    }

    /**
     * 中止和激活流程
     */
    @Test
    public void suspendAndActivateProcessDefination(){
        //中止流程后，流程无法再启动
       // repositoryService.suspendProcessDefinitionByKey("commonApprove");
        //如果要启动流程，可激活流程
        repositoryService.activateProcessDefinitionByKey("commonApprove");
    }

    /**
     * 任务指定用户
     */
    @Test
    public void taskAssignUser(){

        User user = identityService.newUser(UUID.randomUUID().toString());
        user.setFirstName("ouzuqi");
       // identityService.saveUser(user);

        Group group = identityService.newGroup(UUID.randomUUID().toString());
        group.setName("myGroup");
        //identityService.saveGroup(group);
        user.setId("cf036437-c248-46c7-8624-23da2000bb85");
        group.setId("a9e67736-d357-4eae-9d28-f33d23066e77");
        

        Task task2 = taskService.createTaskQuery().taskId("30005").singleResult();
        //设置任务的代理人，两者的关系在act_ru_task
        taskService.setAssignee(task2.getId(),user.getId());
        //获取该代理人的任务，
        List<Task> list2 = taskService.createTaskQuery().taskAssignee(user.getId()).list();




    }

    /**
     * 任务类型
     */
    public void taskType(){
        //1、个人任务：act_ru_task中的assign!=null   代理人和任务的关系在act_ru_task
        //2、组任务：act_ru_task中的assign=null
            //候选用户：张三，李四
            //候选组：组1（王五，张六）
            //候选用户和任务的关系，候选组和任务的关系存放在act_ru_identityLink
        //任务的完成并不需要条件，直接调用taskService.complete(task.getId())即可，但是一般都是把该任务变成个人任务后再去执行
        //当任务为组任务时，候选人要拾取任务，将组任务变为个人任务，变为个人任务后，所有候选人都无法查询到任务，
        //如果个人任务要更换代理人，要先退回任务，再由其他人拾取,否则会报错。

        //候选人
        User user = identityService.newUser(UUID.randomUUID().toString());
        //候选组及其组中的成员
        Group group = identityService.newGroup(UUID.randomUUID().toString());
        User groupUser1 = identityService.newUser(UUID.randomUUID().toString());
        User groupUser2 = identityService.newUser(UUID.randomUUID().toString());
        identityService.createMembership(groupUser1.getId(),group.getId());
        identityService.createMembership(groupUser2.getId(),group.getId());

        Task task1 = taskService.createTaskQuery().taskId("30005").singleResult();
        //设置任务的候选人
        taskService.addCandidateUser(task1.getId(),user.getId());
        //获取该候选人的任务
        List<Task> list1= taskService.createTaskQuery().taskCandidateUser(user.getId()).list();

        Task task2 = taskService.createTaskQuery().taskId("30005").singleResult();
        //设置任务的候选组
        taskService.addCandidateGroup(task2.getId(),group.getId());
        //获取该候选组的任务
        List<Task> list2 = taskService.createTaskQuery().taskCandidateGroup(group.getId()).list();

        Task task3 = taskService.createTaskQuery().taskId("30005").singleResult();
        //设置任务的代理人
        taskService.setAssignee(task3.getId(),user.getId());
        //获取该代理人的任务，
        List<Task> list3 = taskService.createTaskQuery().taskAssignee(user.getId()).list();

        //退回任务
            //第一种
        taskService.setAssignee(task3.getId(),null);
            //第二种
        taskService.unclaim(task3.getId());

        //拾取任务
            //第一种
        taskService.claim(task3.getId(),user.getId());
            //第二种
        taskService.setAssignee(task3.getId(),user.getId());

    }






    /**
     * 代码编写流程
     * @return
     */
    public BpmnModel createProcessModel(){

        BpmnModel model = new BpmnModel();
        Process process = new Process();
        model.addProcess(process);
        process.setId("MyProcess_lrj");
        process.setName("My Process");

        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent001");
        process.addFlowElement(startEvent);

        UserTask userTask= new UserTask();
        userTask.setName("User Task");
        userTask.setId("userTask001");
        process.addFlowElement(userTask);

        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent001");
        process.addFlowElement(endEvent);

        process.addFlowElement(new SequenceFlow("startEvent001","userTask001"));
        process.addFlowElement(new SequenceFlow("userTask001","endEvent001"));
        return model;

    }

    /**
     * 设置流程变量
     */
    @Test
    public void setVariable(){
        Map<String,Object> map =  new HashMap<>();
        map.put("condition",5);

    /*    Task task = taskService.createTaskQuery().processInstanceId("82501").singleResult();

        //这4种是设置全局变量，act_ru_variable表的task_id都是为null，重复设置相同变量，记录不会增加，版本号增加
        //启动流程时设置变量
        runtimeService.startProcessInstanceByKey("variable","uuid01",map);
        //完成任务时设置变量
        taskService.complete(task.getId(),map);
        //taskService设置变量
        taskService.setVariable(task.getId(),"act",999);
        //runtimeService设置变量
        runtimeService.setVariable("82501","condition",78);//执行流的id

        //这两种设置本地变量，act_ru_variable表的task_id是有值的，
        taskService.setVariableLocal(task.getId(),"act",66);
        runtimeService.setVariableLocal("82501","condition",78);
*/
       //List<Task> task = taskService.createTaskQuery().processInstanceId("115001").list();
       // taskService.complete(task.getId());
        runtimeService.setVariableLocal("115002","condition",78);
    }




    public void entity(){
        //服务可以创建实体
        //服务可以创建关系


        Deployment deployment; //act_re_deployment
        ProcessDefinition processDefinition; //act_re_procdef
        ProcessInstance processInstance; //act_hi_procinst

        Task task;  //act_ru_task
        HistoricTaskInstance historicTaskInstance;//act_hi_taskinst

        IdentityLink identityLink;  //act_ru_identityLink


        Resource resource; //act_ge_bytearray
        historyService.createHistoricTaskInstanceQuery().singleResult();

    }





















































}
