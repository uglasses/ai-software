from app.services.resume_service import parse_resume, suggest_resume_updates


def test_parse_resume_extracts_skills():
    result = parse_resume("熟悉 Java、Spring Boot 与 redis")

    assert "Java" in result["skills"]
    assert "Spring Boot" in result["skills"]
    assert result["education"] == "本科"
    assert result["projects"]


def test_suggest_resume_updates_includes_target_job_and_suggestions():
    result = suggest_resume_updates("Java Vue", "后端开发")

    assert result["targetJob"] == "后端开发"
    assert "Java" in result["keywords"]
    assert len(result["suggestions"]) >= 1
