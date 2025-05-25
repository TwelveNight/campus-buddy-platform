-- 清空互助申请表和互助任务表
-- 先将help_info表中的accepted_application_id设置为NULL
UPDATE help_info SET accepted_application_id = NULL;

-- 然后删除help_info和help_application表中的数据
DELETE FROM help_application;
DELETE FROM help_info;

-- 重置自增ID
ALTER TABLE help_application AUTO_INCREMENT = 1;
ALTER TABLE help_info AUTO_INCREMENT = 1;

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
        '高等数学辅导需求',
        '期末考试前需要高数辅导，重点是微积分和线性代数部分，希望能提高30分以上。',
        '每周二、四晚上7-9点',
        '图书馆二楼自习室',
        '微信: math123',
        50.00,
        'OPEN'
    ),
    (
        2,
        'COURSE_TUTORING',
        'Python数据分析辅导',
        '需要有经验的Python工程师指导完成数据分析项目，熟悉Pandas和Matplotlib优先',
        '周末下午',
        '计算机学院实验室',
        'QQ: 12345678',
        120.00,
        'OPEN'
    ),
    (
        3,
        'COURSE_TUTORING',
        'Web前端开发指导',
        '希望有人指导React和TypeScript开发，完成一个简单的电商网站前端',
        '工作日晚上',
        '线上或线下均可',
        '电话: 13899999999',
        150.00,
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
        2,
        'SKILL_LEARNING',
        '求大神教吉他入门',
        '想学习基本的吉他弹唱，有无大神带一下，最好能在一个月内学会一首完整的歌曲',
        '工作日晚上或周末',
        '音乐教室或宿舍',
        '电话: 13812345678',
        80.00,
        'OPEN'
    ),
    (
        1,
        'SKILL_LEARNING',
        '寻找PS技术指导',
        '需要学习照片后期处理，主要是人像美化和场景合成，希望能教会基本操作',
        '时间灵活，可商量',
        '线上指导即可',
        '邮箱: ps@example.com',
        60.00,
        'OPEN'
    ),
    (
        3,
        'SKILL_LEARNING',
        '学习视频剪辑',
        '想学习短视频制作和剪辑技巧，有经验的同学请联系我',
        '周末全天',
        '传媒学院或线上',
        '微信: video_edit',
        90.00,
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
        1,
        'ITEM_LEND',
        '借用单反相机两天',
        '社团活动需要借用单反相机拍摄，保证完好归还，可押金',
        '本周末',
        '校内交接',
        '微信: camera2023',
        'OPEN'
    ),
    (
        3,
        'ITEM_LEND',
        '短期借用绘图板',
        '需要完成设计作业，借用绘图板2-3天，保证不损坏',
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
        expected_time,
        expected_location,
        contact_method,
        status
    )
VALUES (
        2,
        'ITEM_EXCHANGE',
        '交换二手教材',
        '有《数据结构》第三版，想换《算法导论》或其他计算机类教材',
        '随时可以',
        '教学楼附近',
        '电话: 13766666666',
        'OPEN'
    ),
    (
        1,
        'ITEM_EXCHANGE',
        '交换小说书籍',
        '手上有《三体》全套，想交换《百年孤独》或其他文学作品',
        '下周内',
        '图书馆门口',
        '微信: book_lover',
        'OPEN'
    );

-- 组队互助
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
        3,
        'TEAM_UP',
        '软件工程课程项目组队',
        '寻找3-4名队友一起完成软件工程课程的校园APP项目，有后端开发经验优先',
        '本学期内',
        '线上+线下结合',
        '邮箱: team@example.com',
        'OPEN'
    ),
    (
        2,
        'TEAM_UP',
        '创新创业比赛寻找队友',
        '准备参加校级创新创业比赛，项目方向是智能家居，需要电子和软件背景的队友',
        '即日起到比赛截止',
        '创新创业中心',
        'QQ群: 666777888',
        'OPEN'
    );
