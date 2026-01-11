import json
from datetime import datetime
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path


def sql_escape(value: str) -> str:
    return value.replace("'", "''")


def format_credit(value) -> str:
    d = Decimal(str(value)).quantize(Decimal("0.0"), rounding=ROUND_HALF_UP)
    return format(d, "f")


def main() -> None:
    base_dir = Path(__file__).resolve().parent
    json_path = base_dir / "courses.json"
    out_path = base_dir / "seed_courses_generated.sql"

    with json_path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    categories = data.get("课程类型表", [])
    courses = data.get("课程信息表", [])

    existing_ids: set[str] = set()
    synthetic_base = 9000000000
    synthetic_counter = 0

    def unique_course_id(raw) -> str:
        nonlocal synthetic_counter
        raw_str = str(raw).strip() if raw is not None else ""
        if raw_str == "" or raw_str == "-" or raw_str in existing_ids:
            synthetic_counter += 1
            new_id = str(synthetic_base + synthetic_counter)
        else:
            new_id = raw_str
        existing_ids.add(new_id)
        return new_id

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    lines: list[str] = []
    lines.append(f"-- Auto-generated from courses.json at {now}")
    lines.append("-- Safe to run multiple times (uses ON DUPLICATE KEY UPDATE)")
    lines.append("")

    # course_categories
    lines.append("-- 1) course_categories")
    lines.append("INSERT INTO course_categories (type_code, type_name, credit_requirement) VALUES")
    cat_values: list[str] = []
    for c in categories:
        type_code = str(c.get("课程类型编码", "")).strip()
        type_name = str(c.get("课程类型名称", "")).strip()
        if not type_code or not type_name:
            continue
        cat_values.append(
            f"('{sql_escape(type_code)}', '{sql_escape(type_name)}', 0.00)"
        )
    if not cat_values:
        raise ValueError("No categories found in courses.json")
    lines.append(",\n".join(cat_values) + "\nON DUPLICATE KEY UPDATE type_name=VALUES(type_name), credit_requirement=VALUES(credit_requirement);")
    lines.append("")

    # teachers (keep existing + add a few)
    lines.append("-- 2) teacher (existing + added)")
    lines.append("INSERT INTO teacher (teacher_id, name, title, email, password) VALUES")
    teacher_values = [
        "('T001', '陈教授', '教授', 'chen@university.edu', '123456')",
        "('T002', '刘副教授', '副教授', 'liu@university.edu', '123456')",
        "('T003', '赵讲师', '讲师', 'zhao@university.edu', '123456')",
        "('T004', '孙老师', '讲师', 'sun@university.edu', '123456')",
        "('T005', '周老师', '副教授', 'zhou@university.edu', '123456')",
        "('T006', '吴老师', '教授', 'wu@university.edu', '123456')",
    ]
    lines.append(",\n".join(teacher_values) + "\nON DUPLICATE KEY UPDATE name=VALUES(name), title=VALUES(title), email=VALUES(email), password=VALUES(password);")
    lines.append("")

    # course
    lines.append("-- 3) course")
    lines.append("-- max_students default: 180")
    lines.append("-- teacher_id temporary: T001 (will be re-assigned by UPDATE below)")
    lines.append("INSERT INTO course (course_id, course_name, credit, max_students, teacher_id, type_code) VALUES")

    course_values: list[str] = []
    skipped = 0
    for item in courses:
        raw_id = item.get("课程编号")
        course_id = unique_course_id(raw_id)

        course_name = str(item.get("课程名称（中文）", "")).strip()
        if not course_name:
            skipped += 1
            continue

        type_code = str(item.get("课程类型编码", "")).strip() or None
        credit_raw = item.get("学分")
        if credit_raw is None:
            skipped += 1
            continue

        credit = format_credit(credit_raw)

        # teacher_id and max_students are required by schema
        teacher_id = "T001"
        max_students = 180

        type_code_sql = f"'{sql_escape(type_code)}'" if type_code else "NULL"
        course_values.append(
            "(" +
            f"'{sql_escape(course_id)}', '{sql_escape(course_name)}', {credit}, {max_students}, '{teacher_id}', {type_code_sql}" +
            ")"
        )

    if not course_values:
        raise ValueError("No courses found in courses.json")

    lines.append(",\n".join(course_values) + "\nON DUPLICATE KEY UPDATE course_name=VALUES(course_name), credit=VALUES(credit), max_students=VALUES(max_students), teacher_id=VALUES(teacher_id), type_code=VALUES(type_code);")
    lines.append("")

    # re-assign teacher_id by course_id last digit
    lines.append("-- 4) Re-assign teacher_id based on course_id last digit (stable mapping)")
    lines.append(
        "UPDATE course\n"
        "SET teacher_id = CASE MOD(CAST(RIGHT(course_id, 1) AS UNSIGNED), 6)\n"
        "  WHEN 0 THEN 'T001'\n"
        "  WHEN 1 THEN 'T002'\n"
        "  WHEN 2 THEN 'T003'\n"
        "  WHEN 3 THEN 'T004'\n"
        "  WHEN 4 THEN 'T005'\n"
        "  WHEN 5 THEN 'T006'\n"
        "END;"
    )
    lines.append("")

    lines.append(f"-- Summary: categories={len(cat_values)}, courses_in_json={len(courses)}, courses_inserted={len(course_values)}, courses_skipped={skipped}, synthetic_ids_used={synthetic_counter}")

    out_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Generated: {out_path}")


if __name__ == "__main__":
    main()
