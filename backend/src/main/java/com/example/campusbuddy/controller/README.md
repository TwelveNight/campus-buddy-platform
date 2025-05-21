# controller

存放RESTful API控制器类，对应前端的接口请求。每个Controller负责处理特定领域的HTTP请求，调用Service层完成业务逻辑，并返回统一格式的响应。

已实现示例：
- UserController.java：用户相关接口，已实现用户列表查询接口。

后续可扩展如HelpInfoController、StudyGroupController等，实现互助信息、小组、资源等模块的API。
