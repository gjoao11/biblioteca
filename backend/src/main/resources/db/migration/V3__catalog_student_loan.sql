CREATE TABLE book_categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE books (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL REFERENCES book_categories (id),
    added_by BIGINT NOT NULL REFERENCES users (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX books_category_id_idx ON books (category_id);

CREATE TABLE copies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES books (id),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'LOANED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE INDEX copies_book_id_idx ON copies (book_id);

CREATE TABLE branches (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE student_categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    max_loans INTEGER NOT NULL CHECK (max_loans > 0)
);

CREATE TABLE students (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    roll_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    branch_id BIGINT NOT NULL REFERENCES branches (id),
    category_id BIGINT NOT NULL REFERENCES student_categories (id),
    year INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX students_email_lower_idx ON students (lower(email));
CREATE INDEX students_branch_id_idx ON students (branch_id);
CREATE INDEX students_category_id_idx ON students (category_id);

CREATE TABLE loans (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    copy_id BIGINT NOT NULL REFERENCES copies (id),
    student_id BIGINT NOT NULL REFERENCES students (id),
    issued_by BIGINT NOT NULL REFERENCES users (id),
    issued_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    returned_at TIMESTAMP WITH TIME ZONE,
    CHECK (due_at > issued_at),
    CHECK (returned_at IS NULL OR returned_at >= issued_at)
);
CREATE UNIQUE INDEX loans_one_open_per_copy_idx ON loans (copy_id) WHERE returned_at IS NULL;
CREATE INDEX loans_open_by_student_idx ON loans (student_id) WHERE returned_at IS NULL;
CREATE INDEX loans_copy_id_idx ON loans (copy_id);
