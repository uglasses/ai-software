from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_resume_parse_returns_skills():
    response = client.post(
        "/ai/resume/parse",
        json={"resumeText": "熟悉 Java、Python 与 MySQL。"},
    )

    body = response.json()
    assert response.status_code == 200
    assert "Java" in body["skills"]
    assert body["education"]


def test_resume_suggestion_returns_keywords():
    response = client.post(
        "/ai/resume/suggestion",
        json={"resumeText": "Java Spring Boot", "targetJob": "Java开发"},
    )

    body = response.json()
    assert response.status_code == 200
    assert body["targetJob"] == "Java开发"
    assert body["suggestions"]
