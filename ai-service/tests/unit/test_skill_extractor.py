import pytest

from app.services.skill_extractor import KNOWN_SKILLS, extract_skills


def test_extract_skills_is_case_insensitive():
    # 避免 "mysql" 子串误匹配 KNOWN_SKILLS 中的 "SQL"
    assert extract_skills("熟悉 java 与 redis 开发") == ["Java", "Redis"]


def test_extract_skills_returns_empty_when_no_keywords():
    assert extract_skills("无相关技术关键词") == []


def test_extract_skills_preserves_known_skills_order():
    text = "Redis Docker Python Java Spring Boot"
    assert extract_skills(text) == [
        skill for skill in KNOWN_SKILLS if skill in {"Java", "Spring Boot", "Redis", "Docker", "Python"}
    ]
