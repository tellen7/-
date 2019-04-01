## 网络大数据同步分流系统

springboot + druid + maven + thymeleaf + bootstrap + JWT

其主要实现界面化的可配置的不同数据库的数据跨网路同步。主要研发点

- 定时任务: 数据出库与压缩（bcp，select into file等）
- 大文件的网络传输(多线程下载,断点续传，下载熔断) 
- 数据的解压缩与入库（load data、bulk等）
- 热切换数据源
- 用户交互功能（请求同步与审批等）
- 数据样例加载与同步日志搜集

### 项目截图

客户端主页
![](https://github.com/tellen7/DataSync/blob/master/screenshot/clientIndex2.png)

客户端查看表
![](https://github.com/tellen7/DataSync/blob/master/screenshot/clientTable2.png)

服务端主页
![](https://github.com/tellen7/DataSync/blob/master/screenshot/serverIndex2.png)

服务端配置表
![](https://github.com/tellen7/DataSync/blob/master/screenshot/serverTableConfig2.png)

服务端配置用户
![](https://github.com/tellen7/DataSync/blob/master/screenshot/serverUserConfig.png)

数据源配置
![](https://github.com/tellen7/DataSync/blob/master/screenshot/serverDataSourceConfig.png)


### 项目用途

- 用于不同公司间商业数据同步买卖。例如，A地的甲公司(使用mysql数据库)希望购买B地的乙公司(使用oracle数据库)的某个表数据(数据压缩之后有几百兆字节) ,如果认购成功（审批成功），这个项目就能直接把mysql中的数据通过网络传输同步到oracle中。
- 用于备份数据于数据迁移。


### TODO

1. 支持oracle数据库
2. 整理解决问题与技术文档 
3. 系统数据序列化存储
