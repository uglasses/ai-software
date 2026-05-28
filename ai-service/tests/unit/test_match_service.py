import pytest

from app.services.match_service import calculate_match


@pytest.mark.parametrize(
    "resume_text,job_description,expected_missing",
    [
        (
            "熟悉 Java、Spring Boot、Vue 和 MySQL。",
            "要求掌握 Java、Spring Boot、MySQL，了解 Redis。",
            ["Redis"],
        ),
        (
            "Python SQL Excel",
            "",
            [],
        ),
    ],
)
def test_calculate_match_score_and_missing_skills(resume_text, job_description, expected_missing):
    result = calculate_match(resume_text, job_description)

    assert result["score"] >= 60
    assert result["missingSkills"] == expected_missing
    assert result["suggestion"]


def test_calculate_match_empty_job_description_defaults_score_to_60():
    result = calculate_match("Java MySQL", "")

    assert result["score"] == 60
    assert result["matchedSkills"] == []
    assert result["missingSkills"] == []
