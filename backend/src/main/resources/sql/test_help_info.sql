-- 添加互助任务测试数据
-- 课程辅导类互助
INSERT INTO help_info (
        publisher_id,
        type,
        title,
        description,
        expected_time,
        expected_location,
        contact_method,
        reward_amount,
        status
    )
VALUES (
        1,
        'COURSE_TUTORING',
        '高等数学辅导',
        '期末考试前需要高数辅导，重点是微积分和线性代数部分',
        '每周二、四晚上7-9点',
        '图书馆二楼自习室',
        '微信: math123',
        50.00,
        'OPEN'
    ),
    (
        2,
        'COURSE_TUTORING',
        'Java编程指导',
        '需要有经验的Java程序员指导完成一个简单的学生管理系统',
        '周末下午',
        '计算机学院实验室',
        'QQ: 12345678',
        100.00,
        'OPEN'
    );
-- 技能学习类互助
INSERT INTO help_info (
        publisher_id,
        type,
        title,
        description,
        expected_time,
        expected_location,
        contact_method,
        reward_amount,
        status
    )
VALUES (
        1,
        'SKILL_LEARNING',
        '求大神教吉他入门',
        '想学习基本的吉他弹唱，有无大神带一下，谢谢！',
        '工作日晚上或周末',
        '音乐教室或宿舍',
        '电话: 13812345678',
        80.00,
        'OPEN'
    ),
    (
        3,
        'SKILL_LEARNING',
        '寻找PS技术指导',
        '需要学习照片后期处理，主要是人像美化和场景合成',
        '时间灵活，可商量',
        '线上指导即可',
        '邮箱: ps@example.com',
        60.00,
        'OPEN'
    );
-- 物品借用类互助
INSERT INTO help_info (
        publisher_id,
        type,
        title,
        description,
        expected_time,
        expected_location,
        contact_method,
        status
    )
VALUES (
        2,
        'ITEM_LEND',
        '借用单反相机三天',
        '社团活动需要借用单反相机拍摄，保证完好归还，可押金',
        '5月25日-5月28日',
        '校内交接',
        '微信: camera2023',
        'OPEN'
    ),
    (
        3,
        'ITEM_LEND',
        '短期借用绘图板',
        '需要完成设计作业，借用绘图板2-3天',
        '本周内',
        '设计学院附近',
        'QQ: 87654321',
        'OPEN'
    );
-- 物品交换类互助
INSERT INTO help_info (
        publisher_id,
        type,
        title,
        description,
        expected_location,
        contact_method,
        status
    )
VALUES (
        1,
        'ITEM_EXCHANGE',
        '教材交换：高数换英语',
        '手里有高等数学第七版教材，想换大学英语教材',
        '教学楼一楼大厅',
        '电话: 13987654321',
        'OPEN'
    );
-- 组队类互助
INSERT INTO help_info (
        publisher_id,
        type,
        title,
        description,
        expected_time,
        expected_location,
        contact_method,
        status
    )
VALUES (
        2,
        'TEAM_UP',
        '找队友参加编程比赛',
        '校园黑客马拉松比赛找队友，需要2名前端+1名后端，有经验优先',
        '比赛时间：6月15-16日',
        '计算机学院',
        '微信群：扫码进群',
        'OPEN'
    ),
    (
        3,
        'TEAM_UP',
        '期末小组项目队友招募',
        '市场营销课程小组作业，需要3-4名队友，一起完成市场调研项目',
        '本学期末前',
        '线上或面对面都可',
        '邮箱: market@example.com',
        'OPEN'
    );
-- 已完成的互助
INSERT INTO help_info (
        publisher_id,
        type,
        title,
        description,
        expected_time,
        expected_location,
        contact_method,
        reward_amount,
        status
    )
VALUES (
        1,
        'COURSE_TUTORING',
        'Python基础辅导',
        '需要Python基础知识辅导，主要是数据结构和函数',
        '已完成',
        '图书馆',
        '微信: py123',
        40.00,
        'RESOLVED'
    );