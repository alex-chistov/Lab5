CREATE EXTENSION IF NOT EXISTS dblink;

CREATE OR REPLACE PROCEDURE sp_create_database(p_dbname VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM dblink_exec(
        'host=localhost dbname=postgres user=' || current_user,
        'CREATE DATABASE ' || quote_ident(p_dbname)
    );
    RAISE NOTICE 'Database "%" created.', p_dbname;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_drop_database(p_dbname VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    PERFORM dblink_exec(
        'host=localhost dbname=postgres user=' || current_user,
        'DO $inner$
         BEGIN
           PERFORM pg_terminate_backend(pid)
           FROM pg_stat_activity
           WHERE datname = ' || quote_literal(p_dbname) || ' AND pid <> pg_backend_pid();
         END $inner$;'
    );
    PERFORM dblink_exec(
        'host=localhost dbname=postgres user=' || current_user,
        'DROP DATABASE IF EXISTS ' || quote_ident(p_dbname)
    );
    RAISE NOTICE 'Database "%" dropped.', p_dbname;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_create_table(p_tablename VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND lower(table_name) = lower(p_tablename)
    ) THEN
         RAISE NOTICE 'Table "%" already exists.', p_tablename;
    ELSE
         EXECUTE format('CREATE TABLE %I (
             id SERIAL PRIMARY KEY,
             title VARCHAR(255),
             author VARCHAR(255),
             publisher VARCHAR(255),
             year INT
         )', p_tablename);
         RAISE NOTICE 'Table "%" created.', p_tablename;
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_clear_table(p_tablename VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    EXECUTE format('TRUNCATE TABLE %I', p_tablename);
    RAISE NOTICE 'Table "%" cleared.', p_tablename;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_add_book(
    p_tablename VARCHAR,
    p_title VARCHAR,
    p_author VARCHAR,
    p_publisher VARCHAR,
    p_year INT
)
LANGUAGE plpgsql AS $$
BEGIN
    EXECUTE format(
      'INSERT INTO %I (title, author, publisher, year) VALUES (%L, %L, %L, %s)',
      p_tablename, p_title, p_author, p_publisher, p_year
    );
    RAISE NOTICE 'Book added: %', p_title;
END;
$$;

CREATE OR REPLACE FUNCTION sp_search_book_by_title(p_tablename VARCHAR, p_title VARCHAR)
RETURNS TABLE(
    id INT,
    title VARCHAR,
    author VARCHAR,
    publisher VARCHAR,
    year INT
)
LANGUAGE plpgsql SECURITY DEFINER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.tables
        WHERE table_schema = 'public'
          AND lower(table_name) = lower(p_tablename)
    ) THEN
        RETURN;
    ELSE
        RETURN QUERY EXECUTE format(
            'SELECT id, title, author, publisher, year FROM %I WHERE title ILIKE %L',
            p_tablename, '%' || p_title || '%'
        );
    END IF;
END;
$$;


CREATE OR REPLACE PROCEDURE sp_update_book(
    p_tablename VARCHAR,
    p_id INT,
    p_title VARCHAR,
    p_author VARCHAR,
    p_publisher VARCHAR,
    p_year INT
)
LANGUAGE plpgsql AS $$
BEGIN
    EXECUTE format(
      'UPDATE %I SET title=%L, author=%L, publisher=%L, year=%s WHERE id=%s',
      p_tablename, p_title, p_author, p_publisher, p_year, p_id
    );
    RAISE NOTICE 'Book updated with id: %', p_id;
END;
$$;

CREATE OR REPLACE PROCEDURE sp_delete_book_by_title(p_tablename VARCHAR, p_title VARCHAR)
LANGUAGE plpgsql AS $$
BEGIN
    EXECUTE format(
      'DELETE FROM %I WHERE title=%L',
      p_tablename, p_title
    );
    RAISE NOTICE 'Book(s) with title "%" deleted.', p_title;
END;
$$;
