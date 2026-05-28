import csv
import random

# 常见姓氏和名字
surnames = ["王", "李", "张", "刘", "陈", "杨", "黄", "赵", "周", "吴",
            "徐", "孙", "马", "朱", "胡", "郭", "林", "何", "高", "罗"]
names = ["伟", "芳", "娜", "敏", "静", "丽", "强", "磊", "军", "洋",
         "勇", "艳", "杰", "娟", "涛", "明", "超", "秀", "华", "平",
         "刚", "桂", "英", "鑫", "宇", "欣", "婷", "慧", "涛", "浩"]

schools = ["清华大学", "北京大学", "复旦大学", "浙江大学", "南京大学",
           "上海交通大学", "华中科技大学", "武汉大学", "中山大学", "四川大学",
           "哈尔滨工业大学", "西安交通大学", "同济大学", "北京航空航天大学",
           "东南大学", "天津大学", "南开大学", "电子科技大学", "华南理工大学",
           "湖南大学"]

majors = ["计算机科学与技术", "软件工程", "电子信息工程", "数据科学与大数据技术",
          "人工智能", "通信工程", "自动化", "信息安全", "网络工程", "物联网工程",
          "数学与应用数学", "统计学", "金融学", "经济学", "工商管理",
          "市场营销", "会计学", "法学", "英语", "机械工程"]

roles = ["student"] * 95 + ["admin"] * 5  # 95%学生，5%管理员

def generate_phone():
    prefix = random.choice(["138", "139", "137", "136", "135", "134", "150", "151", "152", "157", "158", "159", "182", "183", "187", "188"])
    suffix = ''.join([str(random.randint(0, 9)) for _ in range(8)])
    return prefix + suffix

def generate_user(user_id):
    username = f"user{user_id:03d}"
    real_name = random.choice(surnames) + random.choice(names)
    if random.random() > 0.5:
        real_name += random.choice(names)
    phone = generate_phone()
    email = f"{username}@example.com"
    role = random.choice(roles)
    status = 1
    # 密码统一用 bcrypt hash of "123456"
    # 这里用 Spring Security 的 BCryptPasswordEncoder 编码后的值
    password_hash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO"
    
    return [username, password_hash, real_name, phone, email, role, status]

def generate_csv(file_name="sample_users.csv", total=100):
    headers = ["username", "password_hash", "real_name", "phone", "email", "role", "status"]
    
    with open(file_name, "w", newline="", encoding="utf-8-sig") as f:
        writer = csv.writer(f)
        writer.writerow(headers)
        
        for i in range(1, total + 1):
            writer.writerow(generate_user(i))
    
    print(f"✅ 成功生成 {total} 个用户数据！")
    print(f"✅ 文件：{file_name}")

if __name__ == "__main__":
    generate_csv()
