import csv
import random
from datetime import datetime, timedelta

# 职位名称池
job_names = [
    "Java开发工程师", "Python开发工程师", "前端开发工程师", "后端开发工程师",
    "测试工程师", "产品经理", "数据分析师", "算法工程师", "运维工程师",
    "UI设计师", "大数据开发工程师", "Golang工程师", "HRBP", "运营专员",
    "市场营销", "财务专员", "软件测试", "iOS开发", "安卓开发", "实习生"
]

# 公司名称
companies = [
    "腾讯科技", "阿里巴巴", "字节跳动", "百度", "美团", "京东", "网易",
    "小米", "华为", "携程", "滴滴", "拼多多", "科大讯飞", "商汤科技",
    "贝壳找房", "顺丰科技", "快手", "B站", "猿辅导", "好未来"
]

# 城市
cities = ["北京", "上海", "广州", "深圳", "杭州", "成都", "南京", "武汉", "西安", "苏州"]

# 学历
educations = ["大专", "本科", "硕士", "不限"]

# 经验
experiences = ["不限", "1年以内", "1-3年", "3-5年", "5-10年"]

# 技能标签
skills = [
    "Java,MySQL,Spring",
    "Python,机器学习,深度学习",
    "Vue,React,JavaScript",
    "测试,自动化测试,Jmeter",
    "产品设计,Axure,需求分析",
    "大数据,Hadoop,Spark",
    "运维,Linux,Docker",
    "HR,招聘,培训"
]

# 生成一条招聘数据
def generate_one_job():
    job_name = random.choice(job_names)
    company_name = random.choice(companies)
    city = random.choice(cities)
    
    # 薪资随机
    salary_min = random.randint(6, 15) * 1000
    salary_max = salary_min + random.randint(2, 10) * 1000
    
    education = random.choice(educations)
    experience = random.choice(experiences)
    skill_tags = random.choice(skills)
    
    # 发布时间 2026年
    days = random.randint(0, 120)
    publish_time = (datetime(2026, 1, 1) + timedelta(days=days)).strftime("%Y-%m-%d")
    
    return [
        job_name, company_name, city,
        salary_min, salary_max, education, experience,
        skill_tags, publish_time
    ]

# 生成 600 条数据
def generate_csv(file_name="sample_jobs.csv", total=600):
    headers = [
        "job_name", "company_name", "city",
        "salary_min", "salary_max", "education",
        "experience", "skill_tags", "publish_time"
    ]

    with open(file_name, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        
        for i in range(total):
            writer.writerow(generate_one_job())
    
    print(f"✅ 成功生成 {total} 条数据！")
    print(f"✅ 文件：{file_name}")

if __name__ == "__main__":
    generate_csv()
