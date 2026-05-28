import csv
import random
import json

# 简历名称池
resume_names = [
    "Java开发简历", "前端开发简历", "Python开发简历", "数据分析简历",
    "产品经理简历", "算法工程师简历", "测试工程师简历", "运维工程师简历",
    "UI设计师简历", "大数据开发简历"
]

# 简历文本池
resume_texts = [
    "熟悉Java编程，掌握Spring Boot、MySQL、Redis等技术栈。有3年后端开发经验。",
    "精通Vue.js、React框架，熟悉HTML5/CSS3/JavaScript。有2年前端开发经验。",
    "熟练掌握Python，熟悉机器学习、深度学习算法。有1年AI开发经验。",
    "熟练使用SQL、Excel、Python进行数据分析。熟悉Tableau、PowerBI等可视化工具。",
    "有2年产品经理经验，熟悉Axure、墨刀等原型设计工具。具备良好的需求分析能力。",
    "精通Java、Python，熟悉分布式系统架构。有5年开发经验，带领过10人团队。",
    "熟悉Linux系统运维，掌握Docker、K8s容器化技术。有3年运维经验。",
    "熟悉UI/UX设计，精通Sketch、Figma、PS等设计工具。有2年设计经验。",
    "熟悉Hadoop、Spark、Flink等大数据技术栈。有2年大数据开发经验。",
    "熟悉软件测试流程，掌握自动化测试框架。有2年测试经验。"
]

# 学校
schools = ["清华大学", "北京大学", "复旦大学", "浙江大学", "南京大学",
           "上海交通大学", "华中科技大学", "武汉大学", "中山大学", "四川大学"]

# 专业
majors = ["计算机科学与技术", "软件工程", "电子信息工程", "数据科学与大数据技术",
          "人工智能", "通信工程", "自动化", "信息安全", "网络工程", "数学与应用数学"]

# 技能池
skills_pool = [
    ["Java", "Spring Boot", "MySQL", "Redis"],
    ["Vue.js", "React", "HTML5", "CSS3", "JavaScript"],
    ["Python", "TensorFlow", "PyTorch", "机器学习"],
    ["SQL", "Excel", "Python", "Tableau"],
    ["Axure", "墨刀", "需求分析", "原型设计"],
    ["Linux", "Docker", "K8s", "Prometheus"],
    ["Hadoop", "Spark", "Flink", "Hive"],
    ["Sketch", "Figma", "Photoshop", "UI设计"],
    ["测试用例", "自动化测试", "Selenium", "JMeter"],
    ["Golang", "微服务", "分布式系统", "RPC"]
]

def generate_resume(resume_id, user_id):
    resume_name = random.choice(resume_names)
    resume_text = random.choice(resume_texts)
    target_position = resume_name.replace("简历", "")
    file_type = random.choice(["pdf", "docx"])
    file_url = f"/uploads/resume_{resume_id}.{file_type}"
    parse_status = random.choice([0, 1, 2])  # 0=未解析, 1=解析中, 2=已解析
    is_default = 1 if random.random() > 0.7 else 0
    
    return [
        user_id, resume_name, file_url, file_type, resume_text,
        target_position, parse_status, is_default
    ]

def generate_parse_result(resume_id):
    parsed_name = random.choice(["张伟", "李娜", "王芳", "刘洋", "陈静", "杨磊", "黄强", "赵敏"])
    parsed_education = random.choice(["本科", "硕士", "博士"])
    parsed_school = random.choice(schools)
    parsed_major = random.choice(majors)
    parsed_skills = json.dumps(random.choice(skills_pool), ensure_ascii=False)
    model_name = random.choice(["deepseek-chat", "gpt-4", "mock-model"])
    
    return [
        resume_id, parsed_name, parsed_education, parsed_school,
        parsed_major, parsed_skills, model_name
    ]

def generate_csv(total=120):
    resume_headers = [
        "user_id", "resume_name", "file_url", "file_type", "resume_text",
        "target_position", "parse_status", "is_default"
    ]
    
    parse_headers = [
        "resume_id", "parsed_name", "parsed_education", "parsed_school",
        "parsed_major", "parsed_skills_json", "model_name"
    ]
    
    # 为每个用户生成1-2份简历
    resume_rows = []
    parse_rows = []
    resume_id = 1
    
    for user_id in range(1, 101):  # 100个用户
        num_resumes = random.randint(1, 2)
        for _ in range(num_resumes):
            resume_rows.append([resume_id] + generate_resume(resume_id, user_id))
            # 只有已解析的简历才生成解析结果
            if resume_rows[-1][7] == 2:  # parse_status
                parse_rows.append(generate_parse_result(resume_id))
            resume_id += 1
    
    # 写入简历CSV
    with open("sample_resumes.csv", "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(["id"] + resume_headers)
        writer.writerows(resume_rows)
    
    # 写入解析结果CSV
    with open("sample_resume_parse.csv", "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(parse_headers)
        writer.writerows(parse_rows)
    
    print(f"✅ 成功生成 {len(resume_rows)} 份简历！")
    print(f"✅ 其中 {len(parse_rows)} 份已解析！")
    print(f"✅ 文件：sample_resumes.csv, sample_resume_parse.csv")

if __name__ == "__main__":
    generate_csv()
